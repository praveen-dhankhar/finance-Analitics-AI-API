package com.financeapp.dto.mapper;

import com.financeapp.dto.ForecastDto;
import com.financeapp.dto.ForecastRequestDto;
import com.financeapp.dto.ForecastResponseDto;
import com.financeapp.entity.Forecast;
import com.financeapp.entity.User;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mapper utility for Forecast entity and DTOs
 */
@Component
public class ForecastMapper {
    
    /**
     * Convert Forecast entity to ForecastDto
     */
    public ForecastDto toDto(Forecast forecast) {
        if (forecast == null) {
            return null;
        }
        
        return new ForecastDto(
            forecast.getId(),
            forecast.getUser().getId(),
            forecast.getForecastDate(),
            forecast.getPredictedAmount(),
            forecast.getConfidenceScore(),
            forecast.getForecastType(),
            forecast.getStatus(),
            forecast.getModelName(),
            forecast.getModelVersion(),
            forecast.getPredictionContext(),
            forecast.getCreatedAt(),
            forecast.getUpdatedAt()
        );
    }
    
    /**
     * Convert Forecast entity to ForecastResponseDto
     */
    public ForecastResponseDto toResponseDto(Forecast forecast) {
        if (forecast == null) {
            return null;
        }
        
        return new ForecastResponseDto(
            forecast.getId(),
            forecast.getUser().getId(),
            forecast.getForecastDate(),
            forecast.getPredictedAmount(),
            forecast.getConfidenceScore(),
            forecast.getForecastType(),
            forecast.getStatus(),
            forecast.getModelName(),
            forecast.getModelVersion(),
            forecast.getPredictionContext(),
            forecast.getCreatedAt(),
            forecast.getUpdatedAt(),
            forecast.getStatus() == Forecast.ForecastStatus.ACTIVE,
            forecast.getStatus() == Forecast.ForecastStatus.EXPIRED || 
            (forecast.getForecastDate() != null && forecast.getForecastDate().isBefore(java.time.LocalDate.now()))
        );
    }
    
    /**
     * Convert ForecastRequestDto to Forecast entity
     */
    public Forecast toEntity(ForecastRequestDto dto, User user) {
        if (dto == null || user == null) {
            return null;
        }
        
        Forecast forecast = new Forecast();
        forecast.setUser(user);
        forecast.setForecastDate(dto.forecastDate());
        forecast.setPredictedAmount(dto.predictedAmount());
        forecast.setConfidenceScore(dto.confidenceScore());
        forecast.setForecastType(dto.forecastType());
        forecast.setStatus(Forecast.ForecastStatus.ACTIVE); // Default status
        forecast.setModelName(dto.modelName());
        forecast.setModelVersion(dto.modelVersion());
        forecast.setPredictionContext(dto.predictionContext());
        forecast.setCreatedAt(OffsetDateTime.now());
        forecast.setUpdatedAt(OffsetDateTime.now());
        
        return forecast;
    }
    
    /**
     * Update Forecast entity with ForecastRequestDto
     */
    public void updateEntity(Forecast forecast, ForecastRequestDto dto) {
        if (forecast == null || dto == null) {
            return;
        }
        
        if (dto.forecastDate() != null) {
            forecast.setForecastDate(dto.forecastDate());
        }
        if (dto.predictedAmount() != null) {
            forecast.setPredictedAmount(dto.predictedAmount());
        }
        if (dto.confidenceScore() != null) {
            forecast.setConfidenceScore(dto.confidenceScore());
        }
        if (dto.forecastType() != null) {
            forecast.setForecastType(dto.forecastType());
        }
        if (dto.modelName() != null) {
            forecast.setModelName(dto.modelName());
        }
        if (dto.modelVersion() != null) {
            forecast.setModelVersion(dto.modelVersion());
        }
        if (dto.predictionContext() != null) {
            forecast.setPredictionContext(dto.predictionContext());
        }
        forecast.setUpdatedAt(OffsetDateTime.now());
    }
}
