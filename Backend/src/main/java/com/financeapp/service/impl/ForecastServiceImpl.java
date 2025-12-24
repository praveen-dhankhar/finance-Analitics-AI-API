package com.financeapp.service.impl;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.ForecastResult;
import com.financeapp.entity.User;
import com.financeapp.repository.ForecastResultRepository;
import com.financeapp.repository.ForecastConfigRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.FinancialDataRepository;
import com.financeapp.service.ForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class ForecastServiceImpl implements ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastServiceImpl.class);

    private final FinancialDataRepository financialDataRepository;
    private final ForecastResultRepository forecastResultRepository;
    private final UserRepository userRepository;
    private final ForecastConfigRepository forecastConfigRepository;

    public ForecastServiceImpl(FinancialDataRepository financialDataRepository,
                               ForecastResultRepository forecastResultRepository,
                               UserRepository userRepository,
                               ForecastConfigRepository forecastConfigRepository) {
        this.financialDataRepository = financialDataRepository;
        this.forecastResultRepository = forecastResultRepository;
        this.userRepository = userRepository;
        this.forecastConfigRepository = forecastConfigRepository;
    }

    @Override
    @Transactional
    @Async
    @Cacheable(value = "forecasts", key = "#userId + '-' + #config.id + '-' + #startDate + '-' + #horizonDays")
    public java.util.concurrent.CompletableFuture<List<ForecastResult>> generateForecast(Long userId, ForecastConfig config, LocalDate startDate, int horizonDays) {
        log.info("Generating forecast: userId={}, algo={}, horizon={}", userId, config.getAlgorithm(), horizonDays);

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure config is persisted to satisfy non-nullable association
        if (config.getId() == null) {
            config = forecastConfigRepository.save(config);
        }

        // Retrieve daily totals from FinancialData with DB-agnostic queries
        // For H2 and PostgreSQL compatibility, we rely on repository helpers already present
        LocalDate fromDate = startDate.minusDays(180); // lookback window

        List<Object[]> dailyTotals = financialDataRepository.getDailyTotals(userId, fromDate, startDate.minusDays(1));
        List<Double> values = new ArrayList<>();
        for (Object[] row : dailyTotals) {
            BigDecimal amount = (BigDecimal) row[1];
            values.add(amount.doubleValue());
        }

        if (values.isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(Collections.emptyList());
        }

        double[] forecasts;
        switch (config.getAlgorithm()) {
            case SMA -> {
                int w = config.getWindowSize() != null ? config.getWindowSize() : 7;
                forecasts = projectFromHistory(simpleMovingAverage(values, w), horizonDays);
            }
            case EWMA -> {
                double alpha = config.getSmoothingFactor() != null ? config.getSmoothingFactor() : 0.3d;
                forecasts = projectFromHistory(exponentialWeightedMovingAverage(values, alpha), horizonDays);
            }
            case LINEAR_REGRESSION -> forecasts = linearRegressionForecast(values, horizonDays);
            case SEASONAL_DECOMPOSITION -> {
                int season = config.getSeasonLength() != null ? config.getSeasonLength() : 7;
                forecasts = seasonalDecomposition(values, season, horizonDays);
            }
            default -> throw new IllegalArgumentException("Unsupported algorithm");
        }

        List<ForecastResult> results = new ArrayList<>();
        for (int i = 0; i < horizonDays; i++) {
            ForecastResult fr = new ForecastResult();
            fr.setConfig(config);
            fr.setUser(user);
            fr.setTargetDate(startDate.plusDays(i));
            fr.setForecastValue(BigDecimal.valueOf(forecasts[Math.min(i, forecasts.length - 1)]));
            results.add(fr);
        }

        return java.util.concurrent.CompletableFuture.completedFuture(forecastResultRepository.saveAll(results));
    }

    private double[] projectFromHistory(double[] smoothed, int horizon) {
        double last = smoothed[smoothed.length - 1];
        double[] out = new double[horizon];
        for (int i = 0; i < horizon; i++) out[i] = last;
        return out;
    }

    @Override
    public double[] simpleMovingAverage(List<Double> values, int window) {
        if (window <= 0 || values.size() < window) {
            throw new IllegalArgumentException("Invalid window size for SMA");
        }
        double[] out = new double[values.size() - window + 1];
        double sum = 0.0;
        for (int i = 0; i < values.size(); i++) {
            sum += values.get(i);
            if (i >= window) sum -= values.get(i - window);
            if (i >= window - 1) out[i - window + 1] = sum / window;
        }
        return out;
    }

    @Override
    public double[] exponentialWeightedMovingAverage(List<Double> values, double alpha) {
        if (alpha <= 0 || alpha >= 1) {
            throw new IllegalArgumentException("Alpha must be in (0,1)");
        }
        double[] out = new double[values.size()];
        out[0] = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            out[i] = alpha * values.get(i) + (1 - alpha) * out[i - 1];
        }
        return out;
    }

    @Override
    public double[] linearRegressionForecast(List<Double> values, int horizon) {
        int n = values.size();
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = values.get(i);
            sumX += x; sumY += y; sumXX += x * x; sumXY += x * y;
        }
        double denom = n * sumXX - sumX * sumX;
        if (denom == 0) denom = 1e-9;
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        double[] out = new double[horizon];
        for (int i = 0; i < horizon; i++) {
            double x = n + 1 + i;
            out[i] = intercept + slope * x;
        }
        return out;
    }

    @Override
    public double[] seasonalDecomposition(List<Double> values, int seasonLength, int horizon) {
        if (seasonLength <= 1 || values.size() < seasonLength * 2) {
            // Not enough data; fallback to SMA
            return projectFromHistory(simpleMovingAverage(values, Math.min(7, Math.max(2, values.size()))), horizon);
        }
        // Compute seasonal indices (naive average by position in season)
        double[] season = new double[seasonLength];
        int[] counts = new int[seasonLength];
        for (int i = 0; i < values.size(); i++) {
            int idx = i % seasonLength;
            season[idx] += values.get(i);
            counts[idx] += 1;
        }
        for (int i = 0; i < seasonLength; i++) {
            if (counts[i] > 0) season[i] /= counts[i]; else season[i] = 0.0;
        }

        // Trend via linear regression
        double[] trend = linearRegressionForecast(values, horizon + seasonLength);
        double[] out = new double[horizon];
        for (int i = 0; i < horizon; i++) {
            out[i] = Math.max(0.0, trend[i] + season[(values.size() + i) % seasonLength] - seasonLength > 0 ? 0 : 0);
            // simplified: trend plus seasonal component (centered minimalistically)
            out[i] = trend[i] + season[(values.size() + i) % seasonLength];
        }
        return out;
    }

    // Advanced algorithms (lightweight stubs; ready to replace with full impls)
    @Override
    public double[] arimaForecast(List<Double> values, int p, int d, int q, int horizon) {
        // Placeholder: fallback to linear regression projection
        return linearRegressionForecast(values, horizon);
    }

    @Override
    public double[] prophetLikeDecomposition(List<Double> values, int seasonLength, int horizon) {
        return seasonalDecomposition(values, seasonLength, horizon);
    }

    @Override
    public double[] ensembleForecast(List<double[]> memberForecasts) {
        if (memberForecasts == null || memberForecasts.isEmpty()) return new double[0];
        int n = memberForecasts.get(0).length;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0; int cnt = 0;
            for (double[] f : memberForecasts) {
                if (i < f.length) { sum += f[i]; cnt++; }
            }
            out[i] = cnt == 0 ? 0.0 : sum / cnt;
        }
        return out;
    }

    @Override
    public List<Integer> detectAnomalies(List<Double> values, double thresholdSigma) {
        if (values.isEmpty()) return Collections.emptyList();
        double mean = 0.0;
        for (double v : values) mean += v;
        mean /= values.size();
        double var = 0.0;
        for (double v : values) var += (v - mean) * (v - mean);
        var /= Math.max(1, values.size() - 1);
        double std = Math.sqrt(var);
        List<Integer> idxs = new ArrayList<>();
        double thr = Math.max(1e-9, thresholdSigma) * (std <= 1e-9 ? 1.0 : std);
        for (int i = 0; i < values.size(); i++) {
            if (Math.abs(values.get(i) - mean) > thr) idxs.add(i);
        }
        return idxs;
    }

    @Override
    @Transactional
    @Async
    @Cacheable(value = "forecasts", key = "'bt-' + #userId + '-' + #config.id + '-' + #startDate + '-' + #horizonDays + '-' + #lookbackDays")
    public java.util.concurrent.CompletableFuture<List<ForecastResult>> backtestAndStoreAccuracy(Long userId,
                                                                                                 ForecastConfig config,
                                                                                                 LocalDate startDate,
                                                                                                 int horizonDays,
                                                                                                 int lookbackDays) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (config.getId() == null) {
            config = forecastConfigRepository.save(config);
        }

        LocalDate historyFrom = startDate.minusDays(lookbackDays + horizonDays);
        LocalDate historyTo = startDate.minusDays(1);
        List<Object[]> dailyTotals = financialDataRepository.getDailyTotals(userId, historyFrom, historyTo);

        List<Double> values = new ArrayList<>();
        for (Object[] row : dailyTotals) {
            java.math.BigDecimal amount = (java.math.BigDecimal) row[1];
            values.add(amount.doubleValue());
        }
        if (values.size() < Math.max(7, horizonDays)) {
            return java.util.concurrent.CompletableFuture.completedFuture(Collections.emptyList());
        }

        // Split into train/test
        int split = Math.max(1, values.size() - horizonDays);
        List<Double> train = values.subList(0, split);
        List<Double> actual = values.subList(split, values.size());

        double[] forecast;
        switch (config.getAlgorithm()) {
            case SMA -> {
                int w = config.getWindowSize() != null ? config.getWindowSize() : 7;
                forecast = linearRegressionForecast(train, horizonDays); // simple baseline; could use SMA projection
            }
            case EWMA -> {
                double alpha = config.getSmoothingFactor() != null ? config.getSmoothingFactor() : 0.3d;
                double[] ew = exponentialWeightedMovingAverage(train, alpha);
                forecast = projectFromHistory(ew, horizonDays);
            }
            case LINEAR_REGRESSION -> forecast = linearRegressionForecast(train, horizonDays);
            case SEASONAL_DECOMPOSITION -> {
                int season = config.getSeasonLength() != null ? config.getSeasonLength() : 7;
                forecast = seasonalDecomposition(train, season, horizonDays);
            }
            default -> throw new IllegalArgumentException("Unsupported algorithm");
        }

        // Compute MAPE
        double mape = 0.0;
        int n = Math.min(actual.size(), forecast.length);
        for (int i = 0; i < n; i++) {
            double a = Math.max(1e-9, Math.abs(actual.get(i)));
            mape += Math.abs((actual.get(i) - forecast[i]) / a);
        }
        mape = (mape / n) * 100.0;

        List<ForecastResult> results = new ArrayList<>();
        for (int i = 0; i < horizonDays; i++) {
            ForecastResult fr = new ForecastResult();
            fr.setConfig(config);
            fr.setUser(user);
            fr.setTargetDate(startDate.plusDays(i));
            fr.setForecastValue(java.math.BigDecimal.valueOf(forecast[Math.min(i, forecast.length - 1)]));
            fr.setMape(mape);
            results.add(fr);
        }
        return java.util.concurrent.CompletableFuture.completedFuture(forecastResultRepository.saveAll(results));
    }

    @Override
    @Transactional
    @Async
    @Cacheable(value = "forecasts", key = "'batch-' + #userId + '-' + #startDate + '-' + #horizonDays + '-' + #configs.hashCode()")
    public CompletableFuture<Map<Long, List<ForecastResult>>> batchGenerateForecasts(Long userId,
                                                                                      List<ForecastConfig> configs,
                                                                                      LocalDate startDate,
                                                                                      int horizonDays) {
        Map<Long, List<ForecastResult>> out = new LinkedHashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ForecastConfig cfg : configs) {
            CompletableFuture<List<ForecastResult>> f = generateForecast(userId, cfg, startDate, horizonDays)
                .thenApply(results -> {
                    Long key = cfg.getId() != null ? cfg.getId() : -1L;
                    out.put(key, results);
                    return results;
                });
            futures.add(f);
        }
        // Wait for all
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return CompletableFuture.completedFuture(out);
    }
}


