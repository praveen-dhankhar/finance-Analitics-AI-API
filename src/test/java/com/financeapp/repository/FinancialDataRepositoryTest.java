package com.financeapp.repository;

import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for FinancialData entity using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
class FinancialDataRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private FinancialData testData;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword123");
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(testUser);

        testData = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("100.50"),
            Category.FOOD,
            "Test transaction",
            TransactionType.EXPENSE
        );
        testData.setCreatedAt(OffsetDateTime.now());
        testData.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(testData);
    }

    @Test
    void testFindByUser() {
        List<FinancialData> found = financialDataRepository.findByUser(testUser);
        
        assertFalse(found.isEmpty(), "Should find financial data for user");
        assertEquals(1, found.size());
        assertEquals(testData.getId(), found.get(0).getId());
    }

    @Test
    void testFindByUserWithPagination() {
        // Add more test data
        FinancialData data2 = new FinancialData(
            testUser,
            LocalDate.now().minusDays(1),
            new BigDecimal("200.00"),
            Category.SALARY,
            "Salary",
            TransactionType.INCOME
        );
        entityManager.persistAndFlush(data2);

        Page<FinancialData> page = financialDataRepository.findByUser(testUser, PageRequest.of(0, 1));
        
        assertEquals(2, page.getTotalElements(), "Should have 2 total elements");
        assertEquals(1, page.getContent().size(), "Should have 1 element in this page");
    }

    @Test
    void testFindByUserAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        
        List<FinancialData> found = financialDataRepository.findByUserAndDateBetween(testUser, startDate, endDate);
        
        assertFalse(found.isEmpty(), "Should find data within date range");
        assertEquals(testData.getId(), found.get(0).getId());
    }

    @Test
    void testFindByUserAndCategory() {
        List<FinancialData> found = financialDataRepository.findByUserAndCategory(testUser, Category.FOOD);
        
        assertFalse(found.isEmpty(), "Should find data by category");
        assertEquals(Category.FOOD, found.get(0).getCategory());
    }

    @Test
    void testFindByUserAndType() {
        List<FinancialData> found = financialDataRepository.findByUserAndType(testUser, TransactionType.EXPENSE);
        
        assertFalse(found.isEmpty(), "Should find data by transaction type");
        assertEquals(TransactionType.EXPENSE, found.get(0).getType());
    }

    @Test
    void testSumAmountByUserAndType() {
        // Add income data
        FinancialData incomeData = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("500.00"),
            Category.SALARY,
            "Salary",
            TransactionType.INCOME
        );
        entityManager.persistAndFlush(incomeData);

        BigDecimal expenseSum = financialDataRepository.sumAmountByUserAndType(testUser, TransactionType.EXPENSE);
        BigDecimal incomeSum = financialDataRepository.sumAmountByUserAndType(testUser, TransactionType.INCOME);

        assertEquals(new BigDecimal("100.50"), expenseSum, "Should sum expense amounts correctly");
        assertEquals(new BigDecimal("500.00"), incomeSum, "Should sum income amounts correctly");
    }

    @Test
    void testSumAmountByUserAndCategory() {
        BigDecimal sum = financialDataRepository.sumAmountByUserAndCategory(testUser, Category.FOOD);
        
        assertEquals(new BigDecimal("100.50"), sum, "Should sum amounts by category correctly");
    }

    @Test
    void testCountByUser() {
        long count = financialDataRepository.countByUser(testUser);
        assertEquals(1, count, "Should count financial data for user");

        // Add another data entry
        FinancialData data2 = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("50.00"),
            Category.TRANSPORTATION,
            "Bus fare",
            TransactionType.EXPENSE
        );
        entityManager.persistAndFlush(data2);

        count = financialDataRepository.countByUser(testUser);
        assertEquals(2, count, "Should count all financial data for user");
    }

    @Test
    void testFindByUserAndAmountBetween() {
        List<FinancialData> found = financialDataRepository.findByUserAndAmountBetween(
            testUser, 
            new BigDecimal("50.00"), 
            new BigDecimal("150.00")
        );
        
        assertFalse(found.isEmpty(), "Should find data within amount range");
        assertEquals(testData.getId(), found.get(0).getId());
    }

    @Test
    void testSaveFinancialData() {
        FinancialData newData = new FinancialData(
            testUser,
            LocalDate.now(),
            new BigDecimal("75.25"),
            Category.ENTERTAINMENT,
            "Movie ticket",
            TransactionType.EXPENSE
        );
        
        FinancialData saved = financialDataRepository.save(newData);
        
        assertNotNull(saved.getId(), "Saved financial data should have an ID");
        assertEquals(new BigDecimal("75.25"), saved.getAmount());
        assertEquals(Category.ENTERTAINMENT, saved.getCategory());
    }
}
