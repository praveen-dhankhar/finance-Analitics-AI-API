package com.financeapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for Category operations
 * Compatible with both H2 and PostgreSQL databases
 */
public record CategoryDto(
    Long id,
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Size(max = 50, message = "Color must not exceed 50 characters")
    String color,
    
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    String icon,
    
    Long parentId,
    
    String parentName,
    
    Long userId,
    
    String userName,
    
    Integer sortOrder,
    
    Boolean isActive,
    
    Boolean isSystem,
    
    Integer usageCount,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime lastUsedAt,
    
    String metadata,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime updatedAt,
    
    List<CategoryDto> children
) {
    public boolean isRootCategory() {
        return parentId == null;
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    public boolean isLeafCategory() {
        return !hasChildren();
    }
    
    public String getFullPath() {
        StringBuilder path = new StringBuilder(name);
        if (parentName != null && !parentName.isEmpty()) {
            path.insert(0, parentName + " > ");
        }
        return path.toString();
    }
}
