package com.financeapp.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financeapp.entity.Forecast.ForecastType;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JSON serialization/deserialization of DTOs
 */
class JsonSerializationTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    void userRegistrationDto_serialization_shouldWork() throws JsonProcessingException {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto(
            "john_doe",
            "john@example.com",
            "password123"
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        UserRegistrationDto deserialized = objectMapper.readValue(json, UserRegistrationDto.class);
        
        // Then
        assertThat(deserialized).isEqualTo(dto);
        assertThat(json).contains("username", "email", "password");
    }
    
    @Test
    void userResponseDto_serialization_shouldWork() throws JsonProcessingException {
        // Given
        OffsetDateTime fixedTime = OffsetDateTime.of(2025, 9, 5, 10, 0, 0, 0, ZoneOffset.ofHours(5));
        UserResponseDto dto = new UserResponseDto(
            1L,
            "john_doe",
            "john@example.com",
            fixedTime,
            fixedTime
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        UserResponseDto deserialized = objectMapper.readValue(json, UserResponseDto.class);
        
        // Then
        assertThat(deserialized.id()).isEqualTo(dto.id());
        assertThat(deserialized.username()).isEqualTo(dto.username());
        assertThat(deserialized.email()).isEqualTo(dto.email());
        assertThat(deserialized.createdAt()).isNotNull();
        assertThat(deserialized.updatedAt()).isNotNull();
        assertThat(json).contains("id", "username", "email", "createdAt", "updatedAt");
    }
    
    @Test
    void financialDataCreateDto_serialization_shouldWork() throws JsonProcessingException {
        // Given
        FinancialDataCreateDto dto = new FinancialDataCreateDto(
            LocalDate.now(),
            new BigDecimal("100.50"),
            "SALARY",
            "Monthly salary",
            "INCOME"
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        FinancialDataCreateDto deserialized = objectMapper.readValue(json, FinancialDataCreateDto.class);
        
        // Then
        assertThat(deserialized).isEqualTo(dto);
        assertThat(json).contains("date", "amount", "category", "description", "type");
    }
    
    @Test
    void financialDataDto_serialization_shouldWork() throws JsonProcessingException {
        // Given
        OffsetDateTime fixedTime = OffsetDateTime.of(2025, 9, 5, 10, 0, 0, 0, ZoneOffset.ofHours(5));
        FinancialDataDto dto = new FinancialDataDto(
            1L,
            1L,
            LocalDate.now(),
            new BigDecimal("100.50"),
            "SALARY",
            "Monthly salary",
            "INCOME",
            fixedTime,
            fixedTime
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        FinancialDataDto deserialized = objectMapper.readValue(json, FinancialDataDto.class);
        
        // Then
        assertThat(deserialized.id()).isEqualTo(dto.id());
        assertThat(deserialized.userId()).isEqualTo(dto.userId());
        assertThat(deserialized.date()).isEqualTo(dto.date());
        assertThat(deserialized.amount()).isEqualTo(dto.amount());
        assertThat(deserialized.category()).isEqualTo(dto.category());
        assertThat(deserialized.description()).isEqualTo(dto.description());
        assertThat(deserialized.type()).isEqualTo(dto.type());
        assertThat(deserialized.createdAt()).isNotNull();
        assertThat(deserialized.updatedAt()).isNotNull();
        assertThat(json).contains("id", "userId", "date", "amount", "category", "type");
    }
    
    @Test
    void forecastRequestDto_serialization_shouldWork() throws JsonProcessingException {
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
        String json = objectMapper.writeValueAsString(dto);
        ForecastRequestDto deserialized = objectMapper.readValue(json, ForecastRequestDto.class);
        
        // Then
        assertThat(deserialized.userId()).isEqualTo(dto.userId());
        assertThat(deserialized.forecastDate()).isEqualTo(dto.forecastDate());
        assertThat(deserialized.predictedAmount()).isEqualTo(dto.predictedAmount());
        assertThat(deserialized.confidenceScore()).isEqualTo(dto.confidenceScore());
        assertThat(deserialized.forecastType()).isEqualTo(dto.forecastType());
        assertThat(deserialized.modelName()).isEqualTo(dto.modelName());
        assertThat(deserialized.modelVersion()).isEqualTo(dto.modelVersion());
        assertThat(deserialized.predictionContext()).isEqualTo(dto.predictionContext());
        assertThat(json).contains("userId", "forecastDate", "predictedAmount", "confidenceScore", "forecastType");
    }
    
    @Test
    void forecastResponseDto_serialization_shouldWork() throws JsonProcessingException {
        // Given
        OffsetDateTime fixedTime = OffsetDateTime.of(2025, 9, 5, 10, 0, 0, 0, ZoneOffset.ofHours(5));
        ForecastResponseDto dto = new ForecastResponseDto(
            1L,
            1L,
            LocalDate.now().plusDays(30),
            new BigDecimal("5000.00"),
            new BigDecimal("85.5"),
            ForecastType.INCOME_EXPENSE,
            com.financeapp.entity.Forecast.ForecastStatus.ACTIVE,
            "ML Model v1.0",
            "1.0.0",
            "Based on historical data analysis",
            fixedTime,
            fixedTime,
            true,
            false
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        ForecastResponseDto deserialized = objectMapper.readValue(json, ForecastResponseDto.class);
        
        // Then
        assertThat(deserialized.id()).isEqualTo(dto.id());
        assertThat(deserialized.userId()).isEqualTo(dto.userId());
        assertThat(deserialized.forecastDate()).isEqualTo(dto.forecastDate());
        assertThat(deserialized.predictedAmount()).isEqualTo(dto.predictedAmount());
        assertThat(deserialized.confidenceScore()).isEqualTo(dto.confidenceScore());
        assertThat(deserialized.forecastType()).isEqualTo(dto.forecastType());
        assertThat(deserialized.status()).isEqualTo(dto.status());
        assertThat(deserialized.modelName()).isEqualTo(dto.modelName());
        assertThat(deserialized.modelVersion()).isEqualTo(dto.modelVersion());
        assertThat(deserialized.predictionContext()).isEqualTo(dto.predictionContext());
        assertThat(deserialized.isActive()).isEqualTo(dto.isActive());
        assertThat(deserialized.isExpired()).isEqualTo(dto.isExpired());
        assertThat(deserialized.createdAt()).isNotNull();
        assertThat(deserialized.updatedAt()).isNotNull();
        assertThat(json).contains("id", "userId", "forecastDate", "predictedAmount", "isActive", "isExpired");
    }
    
    @Test
    void userUpdateDto_partialData_serialization_shouldWork() throws JsonProcessingException {
        // Given
        UserUpdateDto dto = new UserUpdateDto(
            "john_doe_updated",
            null, // Optional field
            null  // Optional field
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        UserUpdateDto deserialized = objectMapper.readValue(json, UserUpdateDto.class);
        
        // Then
        assertThat(deserialized).isEqualTo(dto);
        assertThat(json).contains("username");
        assertThat(json).contains("null"); // null values should be included
    }
    
    @Test
    void financialDataUpdateDto_partialData_serialization_shouldWork() throws JsonProcessingException {
        // Given
        FinancialDataUpdateDto dto = new FinancialDataUpdateDto(
            null, // Optional field
            new BigDecimal("200.00"),
            null, // Optional field
            null, // Optional field
            null  // Optional field
        );
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        FinancialDataUpdateDto deserialized = objectMapper.readValue(json, FinancialDataUpdateDto.class);
        
        // Then
        assertThat(deserialized).isEqualTo(dto);
        assertThat(json).contains("amount");
        assertThat(json).contains("null"); // null values should be included
    }
}
