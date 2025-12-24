package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating financial data
 */
public record FinancialDataUpdateDto(
    @JsonProperty("date")
    @PastOrPresent(message = "Date cannot be in the future")
    LocalDate date,
    
    @JsonProperty("amount")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @JsonProperty("category")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    String category,
    
    @JsonProperty("description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @JsonProperty("type")
    @Size(max = 20, message = "Type must not exceed 20 characters")
    String type
) {}