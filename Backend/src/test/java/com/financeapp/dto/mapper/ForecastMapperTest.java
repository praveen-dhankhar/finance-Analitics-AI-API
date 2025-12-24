package com.financeapp.dto.mapper;

import com.financeapp.dto.ForecastDto;
import com.financeapp.dto.ForecastRequestDto;
import com.financeapp.dto.ForecastResponseDto;
import com.financeapp.entity.Forecast;
import com.financeapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

/**
 * Unit tests for ForecastMapper
 */
class ForecastMapperTest {
    
    private ForecastMapper forecastMapper;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        forecastMapper = new ForecastMapper();
        
        testUser = new User();
        setId(testUser, 1L);
        testUser.setUsername("test_user");
        testUser.setEmail("test@example.com");
    }
    
    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID for testing", e);
        }
    }
    
    @Test
    void toDto_validForecast_shouldReturnDto() {
        // Given
        Forecast forecast = createTestForecast();
        
        // When
        ForecastDto result = forecastMapper.toDto(forecast);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(forecast.getId());
        assertThat(result.userId()).isEqualTo(forecast.getUser().getId());
        assertThat(result.forecastDate()).isEqualTo(forecast.getForecastDate());
        assertThat(result.predictedAmount()).isEqualTo(forecast.getPredictedAmount());
        assertThat(result.confidenceScore()).isEqualTo(forecast.getConfidenceScore());
        assertThat(result.forecastType()).isEqualTo(forecast.getForecastType());
        assertThat(result.status()).isEqualTo(forecast.getStatus());
        assertThat(result.modelName()).isEqualTo(forecast.getModelName());
        assertThat(result.modelVersion()).isEqualTo(forecast.getModelVersion());
        assertThat(result.predictionContext()).isEqualTo(forecast.getPredictionContext());
    }
    
    @Test
    void toResponseDto_validForecast_shouldReturnResponseDto() {
        // Given
        Forecast forecast = createTestForecast();
        
        // When
        ForecastResponseDto result = forecastMapper.toResponseDto(forecast);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(forecast.getId());
        assertThat(result.userId()).isEqualTo(forecast.getUser().getId());
        assertThat(result.forecastDate()).isEqualTo(forecast.getForecastDate());
        assertThat(result.predictedAmount()).isEqualTo(forecast.getPredictedAmount());
        assertThat(result.confidenceScore()).isEqualTo(forecast.getConfidenceScore());
        assertThat(result.forecastType()).isEqualTo(forecast.getForecastType());
        assertThat(result.status()).isEqualTo(forecast.getStatus());
        assertThat(result.modelName()).isEqualTo(forecast.getModelName());
        assertThat(result.modelVersion()).isEqualTo(forecast.getModelVersion());
        assertThat(result.predictionContext()).isEqualTo(forecast.getPredictionContext());
        assertThat(result.isActive()).isTrue();
        assertThat(result.isExpired()).isFalse();
    }
    
    @Test
    void toResponseDto_expiredForecast_shouldReturnCorrectExpiredStatus() {
        // Given
        Forecast forecast = createTestForecast();
        forecast.setStatus(Forecast.ForecastStatus.EXPIRED);
        forecast.setForecastDate(LocalDate.now().minusDays(1));
        
        // When
        ForecastResponseDto result = forecastMapper.toResponseDto(forecast);
        
        // Then
        assertThat(result.isActive()).isFalse();
        assertThat(result.isExpired()).isTrue();
    }
    
    @Test
    void toEntity_validRequestDto_shouldReturnForecast() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(30),
            new BigDecimal("5000.00"),
            new BigDecimal("85.5"),
            Forecast.ForecastType.INCOME_EXPENSE,
            "ML Model v1.0",
            "1.0.0",
            "Based on historical data analysis"
        );
        
        // When
        Forecast result = forecastMapper.toEntity(dto, testUser);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getForecastDate()).isEqualTo(dto.forecastDate());
        assertThat(result.getPredictedAmount()).isEqualTo(dto.predictedAmount());
        assertThat(result.getConfidenceScore()).isEqualTo(dto.confidenceScore());
        assertThat(result.getForecastType()).isEqualTo(dto.forecastType());
        assertThat(result.getStatus()).isEqualTo(Forecast.ForecastStatus.ACTIVE);
        assertThat(result.getModelName()).isEqualTo(dto.modelName());
        assertThat(result.getModelVersion()).isEqualTo(dto.modelVersion());
        assertThat(result.getPredictionContext()).isEqualTo(dto.predictionContext());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void toEntity_nullDtoOrUser_shouldReturnNull() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            1L, LocalDate.now().plusDays(30), new BigDecimal("5000.00"),
            new BigDecimal("85.5"), Forecast.ForecastType.INCOME_EXPENSE,
            "ML Model v1.0", "1.0.0", "Test context"
        );
        
        // When
        Forecast result1 = forecastMapper.toEntity(null, testUser);
        Forecast result2 = forecastMapper.toEntity(dto, null);
        
        // Then
        assertThat(result1).isNull();
        assertThat(result2).isNull();
    }
    
    @Test
    void updateEntity_validRequestDto_shouldUpdateForecast() {
        // Given
        Forecast forecast = createTestForecast();
        forecast.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(60),
            new BigDecimal("6000.00"),
            new BigDecimal("90.0"),
            Forecast.ForecastType.INVESTMENT_RETURN,
            "ML Model v2.0",
            "2.0.0",
            "Updated prediction context"
        );
        
        // When
        forecastMapper.updateEntity(forecast, dto);
        
        // Then
        assertThat(forecast.getForecastDate()).isEqualTo(dto.forecastDate());
        assertThat(forecast.getPredictedAmount()).isEqualTo(dto.predictedAmount());
        assertThat(forecast.getConfidenceScore()).isEqualTo(dto.confidenceScore());
        assertThat(forecast.getForecastType()).isEqualTo(dto.forecastType());
        assertThat(forecast.getModelName()).isEqualTo(dto.modelName());
        assertThat(forecast.getModelVersion()).isEqualTo(dto.modelVersion());
        assertThat(forecast.getPredictionContext()).isEqualTo(dto.predictionContext());
        assertThat(forecast.getUpdatedAt()).isAfter(forecast.getCreatedAt());
    }
    
    @Test
    void updateEntity_partialRequestDto_shouldUpdateOnlyProvidedFields() {
        // Given
        Forecast forecast = createTestForecast();
        forecast.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            null, // Not updating forecast date
            new BigDecimal("6000.00"),
            null, // Not updating confidence score
            null, // Not updating forecast type
            "ML Model v2.0",
            null, // Not updating model version
            null  // Not updating prediction context
        );
        
        // When
        forecastMapper.updateEntity(forecast, dto);
        
        // Then
        assertThat(forecast.getForecastDate()).isEqualTo(LocalDate.now().plusDays(30)); // Unchanged
        assertThat(forecast.getPredictedAmount()).isEqualTo(dto.predictedAmount()); // Updated
        assertThat(forecast.getConfidenceScore()).isEqualTo(new BigDecimal("85.5")); // Unchanged
        assertThat(forecast.getForecastType()).isEqualTo(Forecast.ForecastType.INCOME_EXPENSE); // Unchanged
        assertThat(forecast.getModelName()).isEqualTo(dto.modelName()); // Updated
        assertThat(forecast.getModelVersion()).isEqualTo("1.0.0"); // Unchanged
        assertThat(forecast.getPredictionContext()).isEqualTo("Test context"); // Unchanged
    }
    
    @Test
    void updateEntity_nullForecastOrDto_shouldNotThrowException() {
        // Given
        Forecast forecast = createTestForecast();
        ForecastRequestDto dto = new ForecastRequestDto(
            1L, LocalDate.now().plusDays(30), new BigDecimal("5000.00"),
            new BigDecimal("85.5"), Forecast.ForecastType.INCOME_EXPENSE,
            "ML Model v1.0", "1.0.0", "Test context"
        );
        
        // When & Then
        forecastMapper.updateEntity(null, dto);
        forecastMapper.updateEntity(forecast, null);
        // Should not throw exceptions
    }
    
    @Test
    void toDto_nullForecast_shouldReturnNull() {
        // When
        ForecastDto result = forecastMapper.toDto(null);
        ForecastResponseDto responseResult = forecastMapper.toResponseDto(null);
        
        // Then
        assertThat(result).isNull();
        assertThat(responseResult).isNull();
    }
    
    @Test
    void toDto_invalidForecast_shouldThrowException() {
        // Given
        Forecast forecast = new Forecast();
        setId(forecast, null); // Invalid: null ID
        forecast.setUser(testUser);
        forecast.setForecastDate(LocalDate.now().plusDays(30));
        forecast.setPredictedAmount(new BigDecimal("5000.00"));
        forecast.setConfidenceScore(new BigDecimal("85.5"));
        forecast.setForecastType(Forecast.ForecastType.INCOME_EXPENSE);
        forecast.setStatus(Forecast.ForecastStatus.ACTIVE);
        forecast.setModelName("ML Model v1.0");
        
        // When & Then
        assertThatThrownBy(() -> forecastMapper.toDto(forecast))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Forecast ID must be positive");
    }
    
    private Forecast createTestForecast() {
        Forecast forecast = new Forecast();
        setId(forecast, 1L);
        forecast.setUser(testUser);
        forecast.setForecastDate(LocalDate.now().plusDays(30));
        forecast.setPredictedAmount(new BigDecimal("5000.00"));
        forecast.setConfidenceScore(new BigDecimal("85.5"));
        forecast.setForecastType(Forecast.ForecastType.INCOME_EXPENSE);
        forecast.setStatus(Forecast.ForecastStatus.ACTIVE);
        forecast.setModelName("ML Model v1.0");
        forecast.setModelVersion("1.0.0");
        forecast.setPredictionContext("Test context");
        forecast.setCreatedAt(OffsetDateTime.now());
        forecast.setUpdatedAt(OffsetDateTime.now());
        return forecast;
    }
}
