package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financeapp.entity.Forecast.ForecastStatus;
import com.financeapp.entity.Forecast.ForecastType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for forecast response
 */
public record ForecastDto(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("userId")
    Long userId,
    
    @JsonProperty("forecastDate")
    LocalDate forecastDate,
    
    @JsonProperty("predictedAmount")
    BigDecimal predictedAmount,
    
    @JsonProperty("confidenceScore")
    BigDecimal confidenceScore,
    
    @JsonProperty("forecastType")
    ForecastType forecastType,
    
    @JsonProperty("status")
    ForecastStatus status,
    
    @JsonProperty("modelName")
    String modelName,
    
    @JsonProperty("modelVersion")
    String modelVersion,
    
    @JsonProperty("predictionContext")
    String predictionContext,
    
    @JsonProperty("createdAt")
    OffsetDateTime createdAt,
    
    @JsonProperty("updatedAt")
    OffsetDateTime updatedAt
) {
    public ForecastDto {
        // Validation in constructor
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Forecast ID must be positive");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (forecastDate == null) {
            throw new IllegalArgumentException("Forecast date cannot be null");
        }
        if (predictedAmount == null || predictedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Predicted amount must be non-negative");
        }
        if (confidenceScore == null || 
            confidenceScore.compareTo(BigDecimal.ZERO) < 0 || 
            confidenceScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Confidence score must be between 0 and 100");
        }
        if (forecastType == null) {
            throw new IllegalArgumentException("Forecast type cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }
    }
}
