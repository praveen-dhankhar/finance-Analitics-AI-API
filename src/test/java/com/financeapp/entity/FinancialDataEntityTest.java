package com.financeapp.entity;

import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FinancialData entity validation
 */
class FinancialDataEntityTest {

    private Validator validator;
    private User testUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testUser = new User("testuser", "test@example.com", "hashedpassword123");
    }

    @Test
    void testValidFinancialData() {
        FinancialData data = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertTrue(violations.isEmpty(), "Valid financial data should have no validation violations");
    }

    @Test
    void testFinancialDataWithNullUser() {
        FinancialData data = new FinancialData();
        data.setDate(LocalDate.now());
        data.setAmount(new BigDecimal("100.50"));
        data.setCategory(Category.FOOD);
        data.setType(TransactionType.EXPENSE);
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with null user should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("user")));
    }

    @Test
    void testFinancialDataWithNullDate() {
        FinancialData data = new FinancialData();
        data.setUser(testUser);
        data.setAmount(new BigDecimal("100.50"));
        data.setCategory(Category.FOOD);
        data.setType(TransactionType.EXPENSE);
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with null date should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("date")));
    }

    @Test
    void testFinancialDataWithZeroAmount() {
        FinancialData data = new FinancialData(
            testUser,
            LocalDate.now(),
            BigDecimal.ZERO,
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with zero amount should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void testFinancialDataWithNegativeAmount() {
        FinancialData data = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("-50.00"),
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with negative amount should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void testFinancialDataWithNullCategory() {
        FinancialData data = new FinancialData();
        data.setUser(testUser);
        data.setDate(LocalDate.now());
        data.setAmount(new BigDecimal("100.50"));
        data.setType(TransactionType.EXPENSE);
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with null category should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("category")));
    }

    @Test
    void testFinancialDataWithNullType() {
        FinancialData data = new FinancialData();
        data.setUser(testUser);
        data.setDate(LocalDate.now());
        data.setAmount(new BigDecimal("100.50"));
        data.setCategory(Category.FOOD);
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with null type should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("type")));
    }

    @Test
    void testFinancialDataWithLongDescription() {
        String longDescription = "a".repeat(501);
        FinancialData data = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.FOOD,
            longDescription,
            TransactionType.EXPENSE
        );
        
        Set<ConstraintViolation<FinancialData>> violations = validator.validate(data);
        assertFalse(violations.isEmpty(), "Financial data with long description should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testFinancialDataBusinessLogicMethods() {
        FinancialData incomeData = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.SALARY,
            "Salary",
            TransactionType.INCOME
        );
        
        FinancialData expenseData = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("50.25"),
            Category.FOOD,
            "Groceries",
            TransactionType.EXPENSE
        );
        
        assertTrue(incomeData.isIncome(), "Income transaction should return true for isIncome()");
        assertFalse(incomeData.isExpense(), "Income transaction should return false for isExpense()");
        
        assertTrue(expenseData.isExpense(), "Expense transaction should return true for isExpense()");
        assertFalse(expenseData.isIncome(), "Expense transaction should return false for isIncome()");
    }

    @Test
    void testFinancialDataEqualsAndHashCode() {
        FinancialData data1 = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        
        FinancialData data2 = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        
        assertEquals(data1, data2, "Financial data with same properties should be equal");
        assertEquals(data1.hashCode(), data2.hashCode(), "Equal financial data should have same hash code");
    }
}
