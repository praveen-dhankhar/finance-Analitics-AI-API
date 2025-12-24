package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for financial data response
 */
public record FinancialDataResponseDto(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("userId")
    Long userId,
    
    @JsonProperty("date")
    LocalDate date,
    
    @JsonProperty("amount")
    BigDecimal amount,
    
    @JsonProperty("category")
    String category,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("type")
    String type,
    
    @JsonProperty("createdAt")
    OffsetDateTime createdAt,
    
    @JsonProperty("updatedAt")
    OffsetDateTime updatedAt
) {}
