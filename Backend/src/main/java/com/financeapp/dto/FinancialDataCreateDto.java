package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating financial data
 */
public record FinancialDataCreateDto(
    @JsonProperty("date")
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    LocalDate date,
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @JsonProperty("category")
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    String category,
    
    @JsonProperty("description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @JsonProperty("type")
    @NotBlank(message = "Type is required")
    @Size(max = 20, message = "Type must not exceed 20 characters")
    String type
) {}