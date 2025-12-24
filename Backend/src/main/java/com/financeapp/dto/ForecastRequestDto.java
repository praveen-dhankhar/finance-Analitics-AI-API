package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financeapp.entity.Forecast.ForecastType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for forecast creation requests
 */
public record ForecastRequestDto(
    @JsonProperty("userId")
    @NotNull(message = "User ID is required")
    Long userId,
    
    @JsonProperty("forecastDate")
    @NotNull(message = "Forecast date is required")
    LocalDate forecastDate,
    
    @JsonProperty("predictedAmount")
    @NotNull(message = "Predicted amount is required")
    @DecimalMin(value = "0.0", message = "Predicted amount must be non-negative")
    BigDecimal predictedAmount,
    
    @JsonProperty("confidenceScore")
    @NotNull(message = "Confidence score is required")
    @DecimalMin(value = "0.0", message = "Confidence score must be at least 0")
    @DecimalMax(value = "100.0", message = "Confidence score must be at most 100")
    BigDecimal confidenceScore,
    
    @JsonProperty("forecastType")
    @NotNull(message = "Forecast type is required")
    ForecastType forecastType,
    
    @JsonProperty("modelName")
    @NotNull(message = "Model name is required")
    @Size(min = 1, max = 100, message = "Model name must be between 1 and 100 characters")
    String modelName,
    
    @JsonProperty("modelVersion")
    @Size(max = 50, message = "Model version must not exceed 50 characters")
    String modelVersion,
    
    @JsonProperty("predictionContext")
    @Size(max = 1000, message = "Prediction context must not exceed 1000 characters")
    String predictionContext
) {
    public ForecastRequestDto {
        if (modelName != null) {
            modelName = modelName.trim();
        }
        if (modelVersion != null) {
            modelVersion = modelVersion.trim();
        }
        if (predictionContext != null) {
            predictionContext = predictionContext.trim();
        }
    }
}
