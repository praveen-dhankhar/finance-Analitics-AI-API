package com.financeapp.dto.mapper;

import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataUpdateDto;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

/**
 * Unit tests for FinancialDataMapper
 */
class FinancialDataMapperTest {
    
    private FinancialDataMapper financialDataMapper;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        financialDataMapper = new FinancialDataMapper();
        
        testUser = new User();
        setId(testUser, 1L);
        testUser.setUsername("test_user");
        testUser.setEmail("test@example.com");
    }
    
    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID for testing", e);
        }
    }
    
    @Test
    void toDto_validFinancialData_shouldReturnDto() {
        // Given
        FinancialData financialData = new FinancialData();
        setId(financialData, 1L);
        financialData.setUser(testUser);
        financialData.setDate(LocalDate.now());
        financialData.setAmount(new BigDecimal("100.50"));
        financialData.setCategory(Category.SALARY);
        financialData.setDescription("Monthly salary");
        financialData.setType(TransactionType.INCOME);
        financialData.setCreatedAt(OffsetDateTime.now());
        financialData.setUpdatedAt(OffsetDateTime.now());
        
        // When
        FinancialDataDto result = financialDataMapper.toDto(financialData);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.date()).isEqualTo(financialData.getDate());
        assertThat(result.amount()).isEqualTo(financialData.getAmount());
        assertThat(result.category()).isEqualTo(financialData.getCategory().toString());
        assertThat(result.description()).isEqualTo(financialData.getDescription());
        assertThat(result.type()).isEqualTo(financialData.getType().toString());
    }
    
    @Test
    void toDto_nullFinancialData_shouldReturnNull() {
        // When
        FinancialDataDto result = financialDataMapper.toDto(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void toEntity_validCreateDto_shouldReturnFinancialData() {
        // Given
        FinancialDataCreateDto dto = new FinancialDataCreateDto(
            LocalDate.now(),
            new BigDecimal("100.50"),
            "SALARY",
            "Monthly salary",
            "INCOME"
        );
        
        // When
        FinancialData result = financialDataMapper.toEntity(dto, testUser);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getDate()).isEqualTo(dto.date());
        assertThat(result.getAmount()).isEqualTo(dto.amount());
        assertThat(result.getCategory().toString()).isEqualTo(dto.category());
        assertThat(result.getDescription()).isEqualTo(dto.description());
        assertThat(result.getType().toString()).isEqualTo(dto.type());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void toEntity_nullDtoOrUser_shouldReturnNull() {
        // Given
        FinancialDataCreateDto dto = new FinancialDataCreateDto(
            LocalDate.now(), new BigDecimal("100.50"), 
            "SALARY", "Test", "INCOME"
        );
        
        // When
        FinancialData result1 = financialDataMapper.toEntity(null, testUser);
        FinancialData result2 = financialDataMapper.toEntity(dto, null);
        
        // Then
        assertThat(result1).isNull();
        assertThat(result2).isNull();
    }
    
    @Test
    void updateEntity_validUpdateDto_shouldUpdateFinancialData() {
        // Given
        FinancialData financialData = new FinancialData();
        setId(financialData, 1L);
        financialData.setUser(testUser);
        financialData.setDate(LocalDate.now().minusDays(1));
        financialData.setAmount(new BigDecimal("100.50"));
        financialData.setCategory(Category.SALARY);
        financialData.setDescription("Old description");
        financialData.setType(TransactionType.INCOME);
        financialData.setCreatedAt(OffsetDateTime.now().minusDays(2));
        financialData.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        FinancialDataUpdateDto dto = new FinancialDataUpdateDto(
            LocalDate.now(),
            new BigDecimal("200.00"),
            "ENTERTAINMENT",
            "New description",
            "EXPENSE"
        );
        
        // When
        financialDataMapper.updateEntity(financialData, dto);
        
        // Then
        assertThat(financialData.getDate()).isEqualTo(dto.date());
        assertThat(financialData.getAmount()).isEqualTo(dto.amount());
        assertThat(financialData.getCategory().toString()).isEqualTo(dto.category());
        assertThat(financialData.getDescription()).isEqualTo(dto.description());
        assertThat(financialData.getType().toString()).isEqualTo(dto.type());
        assertThat(financialData.getUpdatedAt()).isAfter(financialData.getCreatedAt());
    }
    
    @Test
    void updateEntity_partialUpdateDto_shouldUpdateOnlyProvidedFields() {
        // Given
        FinancialData financialData = new FinancialData();
        setId(financialData, 1L);
        financialData.setUser(testUser);
        financialData.setDate(LocalDate.now().minusDays(1));
        financialData.setAmount(new BigDecimal("100.50"));
        financialData.setCategory(Category.SALARY);
        financialData.setDescription("Old description");
        financialData.setType(TransactionType.INCOME);
        financialData.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        FinancialDataUpdateDto dto = new FinancialDataUpdateDto(
            null, // Not updating date
            new BigDecimal("200.00"),
            null, // Not updating category
            null, // Not updating description
            null  // Not updating type
        );
        
        // When
        financialDataMapper.updateEntity(financialData, dto);
        
        // Then
        assertThat(financialData.getDate()).isEqualTo(LocalDate.now().minusDays(1)); // Unchanged
        assertThat(financialData.getAmount()).isEqualTo(dto.amount()); // Updated
        assertThat(financialData.getCategory()).isEqualTo(Category.SALARY); // Unchanged
        assertThat(financialData.getDescription()).isEqualTo("Old description"); // Unchanged
        assertThat(financialData.getType()).isEqualTo(TransactionType.INCOME); // Unchanged
    }
    
    @Test
    void updateEntity_nullFinancialDataOrDto_shouldNotThrowException() {
        // Given
        FinancialData financialData = new FinancialData();
        FinancialDataUpdateDto dto = new FinancialDataUpdateDto(
            LocalDate.now(), new BigDecimal("100.00"), "SALARY", "Test", "INCOME"
        );
        
        // When & Then
        financialDataMapper.updateEntity(null, dto);
        financialDataMapper.updateEntity(financialData, null);
        // Should not throw exceptions
    }
    
    @Test
    void toUpdateDto_validFinancialData_shouldReturnUpdateDto() {
        // Given
        FinancialData financialData = new FinancialData();
        setId(financialData, 1L);
        financialData.setUser(testUser);
        financialData.setDate(LocalDate.now());
        financialData.setAmount(new BigDecimal("100.50"));
        financialData.setCategory(Category.SALARY);
        financialData.setDescription("Monthly salary");
        financialData.setType(TransactionType.INCOME);
        
        // When
        FinancialDataUpdateDto result = financialDataMapper.toUpdateDto(financialData);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.date()).isEqualTo(financialData.getDate());
        assertThat(result.amount()).isEqualTo(financialData.getAmount());
        assertThat(result.category()).isEqualTo(financialData.getCategory().toString());
        assertThat(result.description()).isEqualTo(financialData.getDescription());
        assertThat(result.type()).isEqualTo(financialData.getType().toString());
    }
    
    @Test
    void toUpdateDto_nullFinancialData_shouldReturnNull() {
        // When
        FinancialDataUpdateDto result = financialDataMapper.toUpdateDto(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void toDto_invalidFinancialData_shouldThrowException() {
        // Given
        FinancialData financialData = new FinancialData();
        setId(financialData, null); // Invalid: null ID
        financialData.setUser(testUser);
        financialData.setDate(LocalDate.now());
        financialData.setAmount(new BigDecimal("100.50"));
        financialData.setCategory(Category.SALARY);
        financialData.setDescription("Test");
        financialData.setType(TransactionType.INCOME);
        
        // When & Then
        assertThatThrownBy(() -> financialDataMapper.toDto(financialData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Financial data ID must be positive");
    }
}
