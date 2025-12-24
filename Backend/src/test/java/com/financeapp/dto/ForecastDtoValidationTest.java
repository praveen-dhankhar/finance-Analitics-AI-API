package com.financeapp.dto;

import com.financeapp.entity.Forecast.ForecastType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Forecast DTO validation
 */
class ForecastDtoValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void forecastRequestDto_validData_shouldPassValidation() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(30),
            new BigDecimal("5000.00"),
            new BigDecimal("85.5"),
            ForecastType.INCOME_EXPENSE,
            "ML Model v1.0",
            "1.0.0",
            "Based on historical data analysis"
        );
        
        // When
        Set<ConstraintViolation<ForecastRequestDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void forecastRequestDto_invalidData_shouldFailValidation() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            null, // Invalid: null user ID
            LocalDate.now().plusDays(30),
            new BigDecimal("-100.00"), // Invalid: negative amount
            new BigDecimal("150.00"), // Invalid: confidence score > 100
            null, // Invalid: null forecast type
            "", // Invalid: empty model name
            "A".repeat(51), // Invalid: model version too long
            "A".repeat(1001) // Invalid: prediction context too long
        );
        
        // When
        Set<ConstraintViolation<ForecastRequestDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(7);
        assertThat(violations).extracting("message")
            .contains(
                "User ID is required",
                "Predicted amount must be non-negative",
                "Confidence score must be at most 100",
                "Forecast type is required",
                "Model name must be between 1 and 100 characters",
                "Model version must not exceed 50 characters",
                "Prediction context must not exceed 1000 characters"
            );
    }
    
    @Test
    void forecastRequestDto_confidenceScoreTooLow_shouldFailValidation() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(30),
            new BigDecimal("5000.00"),
            new BigDecimal("-10.0"), // Invalid: negative confidence score
            ForecastType.INCOME_EXPENSE,
            "ML Model v1.0",
            "1.0.0",
            "Based on historical data analysis"
        );
        
        // When
        Set<ConstraintViolation<ForecastRequestDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Confidence score must be at least 0");
    }
    
    @Test
    void forecastRequestDto_modelNameTooLong_shouldFailValidation() {
        // Given
        String longModelName = "A".repeat(101);
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(30),
            new BigDecimal("5000.00"),
            new BigDecimal("85.5"),
            ForecastType.INCOME_EXPENSE,
            longModelName, // Invalid: model name too long
            "1.0.0",
            "Based on historical data analysis"
        );
        
        // When
        Set<ConstraintViolation<ForecastRequestDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Model name must be between 1 and 100 characters");
    }
    
    @Test
    void forecastRequestDto_edgeCaseValues_shouldPassValidation() {
        // Given
        ForecastRequestDto dto = new ForecastRequestDto(
            1L,
            LocalDate.now().plusDays(30),
            BigDecimal.ZERO, // Valid: zero amount
            new BigDecimal("100.0"), // Valid: maximum confidence score
            ForecastType.INCOME_EXPENSE,
            "A", // Valid: minimum model name length
            "1.0.0",
            "A".repeat(1000) // Valid: maximum prediction context length
        );
        
        // When
        Set<ConstraintViolation<ForecastRequestDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).isEmpty();
    }
}
