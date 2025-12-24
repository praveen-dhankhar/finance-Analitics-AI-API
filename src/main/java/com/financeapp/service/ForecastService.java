package com.financeapp.service;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.ForecastResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

public interface ForecastService {

    CompletableFuture<List<ForecastResult>> generateForecast(Long userId, ForecastConfig config, LocalDate startDate, int horizonDays);

    CompletableFuture<List<ForecastResult>> backtestAndStoreAccuracy(Long userId,
                                                                     ForecastConfig config,
                                                                     LocalDate startDate,
                                                                     int horizonDays,
                                                                     int lookbackDays);

    CompletableFuture<Map<Long, List<ForecastResult>>> batchGenerateForecasts(Long userId,
                                                                              List<ForecastConfig> configs,
                                                                              LocalDate startDate,
                                                                              int horizonDays);

    // Advanced algorithms (scaffold)
    double[] arimaForecast(List<Double> values, int p, int d, int q, int horizon);

    double[] prophetLikeDecomposition(List<Double> values, int seasonLength, int horizon);

    double[] ensembleForecast(List<double[]> memberForecasts);

    List<Integer> detectAnomalies(List<Double> values, double thresholdSigma);

    double[] simpleMovingAverage(List<Double> values, int window);

    double[] exponentialWeightedMovingAverage(List<Double> values, double alpha);

    double[] linearRegressionForecast(List<Double> values, int horizon);

    double[] seasonalDecomposition(List<Double> values, int seasonLength, int horizon);
}


