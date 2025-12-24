package com.financeapp.dto.mapper;

import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataResponseDto;
import com.financeapp.dto.FinancialDataUpdateDto;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mapper utility for FinancialData entity and DTOs
 */
@Component
public class FinancialDataMapper {
    
    /**
     * Convert FinancialData entity to FinancialDataDto
     */
    public FinancialDataDto toDto(FinancialData financialData) {
        if (financialData == null) {
            return null;
        }
        
        return new FinancialDataDto(
            financialData.getId(),
            financialData.getUser().getId(),
            financialData.getDate(),
            financialData.getAmount(),
            financialData.getCategory().toString(),
            financialData.getDescription(),
            financialData.getType().toString(),
            financialData.getCreatedAt(),
            financialData.getUpdatedAt()
        );
    }
    
    /**
     * Convert FinancialDataCreateDto to FinancialData entity
     */
    public FinancialData toEntity(FinancialDataCreateDto dto, User user) {
        if (dto == null || user == null) {
            return null;
        }
        
        FinancialData financialData = new FinancialData();
        financialData.setUser(user);
        financialData.setDate(dto.date());
        financialData.setAmount(dto.amount());
        financialData.setCategory(Category.valueOf(dto.category()));
        financialData.setDescription(dto.description());
        financialData.setType(TransactionType.valueOf(dto.type()));
        financialData.setCreatedAt(OffsetDateTime.now());
        financialData.setUpdatedAt(OffsetDateTime.now());
        
        return financialData;
    }
    
    /**
     * Update FinancialData entity with FinancialDataUpdateDto
     */
    public void updateEntity(FinancialData financialData, FinancialDataUpdateDto dto) {
        if (financialData == null || dto == null) {
            return;
        }
        
        if (dto.date() != null) {
            financialData.setDate(dto.date());
        }
        if (dto.amount() != null) {
            financialData.setAmount(dto.amount());
        }
        if (dto.category() != null) {
            financialData.setCategory(Category.valueOf(dto.category()));
        }
        if (dto.description() != null) {
            financialData.setDescription(dto.description());
        }
        if (dto.type() != null) {
            financialData.setType(TransactionType.valueOf(dto.type()));
        }
        financialData.setUpdatedAt(OffsetDateTime.now());
    }
    
    /**
     * Create FinancialDataUpdateDto from FinancialData entity
     */
    public FinancialDataUpdateDto toUpdateDto(FinancialData financialData) {
        if (financialData == null) {
            return null;
        }
        
        return new FinancialDataUpdateDto(
            financialData.getDate(),
            financialData.getAmount(),
            financialData.getCategory().toString(),
            financialData.getDescription(),
            financialData.getType().toString()
        );
    }
    
    /**
     * Convert FinancialData entity to FinancialDataResponseDto
     */
    public FinancialDataResponseDto toResponseDto(FinancialData financialData) {
        if (financialData == null) {
            return null;
        }
        
        return new FinancialDataResponseDto(
            financialData.getId(),
            financialData.getUser().getId(),
            financialData.getDate(),
            financialData.getAmount(),
            financialData.getCategory().toString(),
            financialData.getDescription(),
            financialData.getType().toString(),
            financialData.getCreatedAt(),
            financialData.getUpdatedAt()
        );
    }
    
    /**
     * Convert FinancialDataDto to FinancialData entity
     */
    public FinancialData toEntity(FinancialDataDto dto) {
        if (dto == null) {
            return null;
        }
        
        FinancialData financialData = new FinancialData();
        // ID is set by the database
        financialData.setDate(dto.date());
        financialData.setAmount(dto.amount());
        financialData.setCategory(Category.valueOf(dto.category()));
        financialData.setDescription(dto.description());
        financialData.setType(TransactionType.valueOf(dto.type()));
        financialData.setCreatedAt(dto.createdAt());
        financialData.setUpdatedAt(dto.updatedAt());
        
        return financialData;
    }
    
    /**
     * Update FinancialData entity with FinancialDataDto
     */
    public void updateEntity(FinancialDataDto dto, FinancialData financialData) {
        if (financialData == null || dto == null) {
            return;
        }
        
        if (dto.date() != null) {
            financialData.setDate(dto.date());
        }
        if (dto.amount() != null) {
            financialData.setAmount(dto.amount());
        }
        if (dto.category() != null) {
            financialData.setCategory(Category.valueOf(dto.category()));
        }
        if (dto.description() != null) {
            financialData.setDescription(dto.description());
        }
        if (dto.type() != null) {
            financialData.setType(TransactionType.valueOf(dto.type()));
        }
        financialData.setUpdatedAt(OffsetDateTime.now());
    }
}
