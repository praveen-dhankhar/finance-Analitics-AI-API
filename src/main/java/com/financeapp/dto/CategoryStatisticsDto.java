package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

/**
 * DTO for Category usage statistics
 * Compatible with both H2 and PostgreSQL databases
 */
public record CategoryStatisticsDto(
    Long categoryId,
    String categoryName,
    String categoryPath,
    Integer usageCount,
    Integer financialDataCount,
    Double totalAmount,
    Double averageAmount,
    Double minAmount,
    Double maxAmount,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime firstUsedAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime lastUsedAt,
    
    Integer childCategoriesCount,
    Boolean hasChildren,
    Boolean isLeafCategory
) {
    public boolean isActiveCategory() {
        return usageCount > 0;
    }
    
    public boolean isFrequentlyUsed() {
        return usageCount >= 10;
    }
    
    public boolean isHighValueCategory() {
        return totalAmount != null && totalAmount > 1000.0;
    }
}
