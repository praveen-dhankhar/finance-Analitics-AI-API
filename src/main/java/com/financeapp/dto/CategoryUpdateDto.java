package com.financeapp.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating Category
 * Compatible with both H2 and PostgreSQL databases
 */
public record CategoryUpdateDto(
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Size(max = 50, message = "Color must not exceed 50 characters")
    String color,
    
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    String icon,
    
    Long parentId,
    
    Integer sortOrder,
    
    Boolean isActive,
    
    String metadata
) {
    public boolean hasAnyField() {
        return name != null || description != null || color != null || 
               icon != null || parentId != null || sortOrder != null || 
               isActive != null || metadata != null;
    }
}
