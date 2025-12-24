package com.financeapp.dto;

import com.financeapp.entity.ForecastConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ForecastDtos {

    public static class BatchGenerateRequest {
        public Long userId;
        public LocalDate startDate; // optional; defaults to tomorrow
        public Integer horizonDays; // optional; defaults to 7
        public List<ForecastConfig> configs;
    }

    public static class AccuracyMetricsDto {
        public Long configId;
        public Double mape;
        public Integer horizonDays;
        public Integer lookbackDays;
    }

    public static class InsightsDto {
        public List<String> topModels;
        public Map<String, Object> aggregates;
        public String notes;
    }
}


