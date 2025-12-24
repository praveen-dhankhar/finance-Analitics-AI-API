package com.financeapp.service;

import com.financeapp.entity.ForecastConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledForecastJobs {

    private static final Logger log = LoggerFactory.getLogger(ScheduledForecastJobs.class);
    private final ForecastService forecastService;

    public ScheduledForecastJobs(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    // Daily batch forecast placeholder; in production load configs per user
    @Scheduled(cron = "0 15 2 * * *")
    public void runNightlyForecasts() {
        log.info("Running nightly batch forecasts (placeholder)");
        ForecastConfig cfg = new ForecastConfig();
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.LINEAR_REGRESSION);
        forecastService.batchGenerateForecasts(1L, List.of(cfg), LocalDate.now().plusDays(1), 7);
    }
}


