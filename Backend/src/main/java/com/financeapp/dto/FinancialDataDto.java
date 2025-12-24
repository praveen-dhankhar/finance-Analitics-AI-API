package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for financial data response
 */
public record FinancialDataDto(
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
) {
    public FinancialDataDto {
        // Validation in constructor
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Financial data ID must be positive");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
    }
}
