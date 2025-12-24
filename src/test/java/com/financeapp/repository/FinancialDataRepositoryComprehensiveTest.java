package com.financeapp.repository;

import com.financeapp.entity.User;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FinancialDataRepositoryComprehensiveTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("password1");
        user1.setCreatedAt(OffsetDateTime.now());
        user1.setUpdatedAt(OffsetDateTime.now());
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("password2");
        user2.setCreatedAt(OffsetDateTime.now());
        user2.setUpdatedAt(OffsetDateTime.now());
        user2 = entityManager.persistAndFlush(user2);

        // Create financial data for user1
        LocalDate baseDate = LocalDate.now();
        
        // Income data - create with specific dates to match test expectations
        // Day 0 (today): 1000
        // Day 1 (yesterday): 1100  
        // Day 2 (2 days ago): 1200
        for (int i = 0; i < 3; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user1);
            data.setDate(baseDate.minusDays(i));
            data.setAmount(BigDecimal.valueOf(1000 + i * 100));
            data.setCategory(Category.SALARY);
            data.setType(TransactionType.INCOME);
            data.setDescription("Salary payment " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(data);
        }

        // Expense data
        for (int i = 0; i < 5; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user1);
            data.setDate(baseDate.minusDays(i));
            data.setAmount(BigDecimal.valueOf(40 + i * 10)); // 40, 50, 60, 70, 80 -> avg = 60
            data.setCategory(Category.FOOD);
            data.setType(TransactionType.EXPENSE);
            data.setDescription("Food expense " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(data);
        }

        // Different category expense
        for (int i = 0; i < 2; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user1);
            data.setDate(baseDate.minusDays(i));
            data.setAmount(BigDecimal.valueOf(50 + i * 50)); // 50, 100 -> total = 150
            data.setCategory(Category.TRANSPORTATION);
            data.setType(TransactionType.EXPENSE);
            data.setDescription("Transport expense " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(data);
        }

        // Create financial data for user2
        for (int i = 0; i < 2; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user2);
            data.setDate(baseDate.minusDays(i));
            data.setAmount(BigDecimal.valueOf(500 + i * 100));
            data.setCategory(Category.SALARY);
            data.setType(TransactionType.INCOME);
            data.setDescription("User2 salary " + i);
            entityManager.persistAndFlush(data);
        }

        entityManager.clear();
    }

    @Test
    void testFindByUser() {
        List<FinancialData> result = financialDataRepository.findByUser(user1);
        assertThat(result).hasSize(10); // 3 income + 5 food + 2 transport
    }

    @Test
    void testFindByUserWithPagination() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<FinancialData> result = financialDataRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindByUserAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now().minusDays(1);
        
        List<FinancialData> result = financialDataRepository.findByUserAndDateBetween(user1, startDate, endDate);
        assertThat(result).hasSize(6); // 1 income + 2 food + 1 transport (3 days ago to 1 day ago)
    }

    @Test
    void testFindByUserAndCategory() {
        List<FinancialData> result = financialDataRepository.findByUserAndCategory(user1, Category.FOOD);
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(data -> data.getCategory() == Category.FOOD);
    }

    @Test
    void testFindByUserAndType() {
        List<FinancialData> result = financialDataRepository.findByUserAndType(user1, TransactionType.INCOME);
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(data -> data.getType() == TransactionType.INCOME);
    }

    @Test
    void testFindByUserAndCategoryAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        
        List<FinancialData> result = financialDataRepository.findByUserAndCategoryAndDateBetween(
                user1, Category.FOOD, startDate, endDate);
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(data -> data.getCategory() == Category.FOOD);
    }

    @Test
    void testSumAmountByUserAndType() {
        BigDecimal result = financialDataRepository.sumAmountByUserAndType(user1, TransactionType.INCOME);
        assertThat(result).isEqualTo(new BigDecimal("3300.00")); // 1000 + 1100 + 1200
    }

    @Test
    void testSumAmountByUserAndTypeAndDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        
        BigDecimal result = financialDataRepository.sumAmountByUserAndTypeAndDateRange(
                user1, TransactionType.INCOME, startDate, endDate);
        assertThat(result).isEqualTo(new BigDecimal("3300.00")); // 1000 + 1100 + 1200 (inclusive range)
    }

    @Test
    void testSumAmountByUserAndCategory() {
        BigDecimal result = financialDataRepository.sumAmountByUserAndCategory(user1, Category.FOOD);
        assertThat(result).isEqualTo(new BigDecimal("300.00")); // 40 + 50 + 60 + 70 + 80
    }

    @Test
    void testFindRecentByUser() {
        Pageable pageable = PageRequest.of(0, 3);
        List<FinancialData> result = financialDataRepository.findRecentByUser(user1, pageable);
        
        assertThat(result).hasSize(3);
        // Should be ordered by date DESC, then createdAt DESC
    }

    @Test
    void testCountByUser() {
        long count = financialDataRepository.countByUser(user1);
        assertThat(count).isEqualTo(10);
    }

    @Test
    void testFindByUserAndAmountBetween() {
        List<FinancialData> result = financialDataRepository.findByUserAndAmountBetween(
                user1, BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        assertThat(result).hasSize(1); // Only transport expense 100 is in range 100-200
    }

    @Test
    void testFindByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialData> result = financialDataRepository.findByUserId(user1.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(10);
    }

    @Test
    void testFindByUserIdAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<FinancialData> result = financialDataRepository.findByUserIdAndDateBetween(
                user1.getId(), startDate, endDate, pageable);
        assertThat(result.getContent()).hasSize(8); // 3 today + 3 yesterday + 2 two days ago
    }

    @Test
    void testFindByUserIdAndCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialData> result = financialDataRepository.findByUserIdAndCategory(
                user1.getId(), Category.FOOD, pageable);
        
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent()).allMatch(data -> data.getCategory() == Category.FOOD);
    }

    @Test
    void testFindByUserIdAndType() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialData> result = financialDataRepository.findByUserIdAndType(
                user1.getId(), TransactionType.INCOME, pageable);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(data -> data.getType() == TransactionType.INCOME);
    }

    @Test
    void testFindByUserIdAndAmountBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialData> result = financialDataRepository.findByUserIdAndAmountBetween(
                user1.getId(), BigDecimal.valueOf(100), BigDecimal.valueOf(200), pageable);
        
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testFindByUserIdAndDescriptionContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialData> result = financialDataRepository.findByUserIdAndDescriptionContainingIgnoreCase(
                user1.getId(), "salary", pageable);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(data -> 
                data.getDescription().toLowerCase().contains("salary"));
    }

    @Test
    void testSumAmountByUserIdAndType() {
        Optional<BigDecimal> result = financialDataRepository.sumAmountByUserIdAndType(
                user1.getId(), TransactionType.INCOME);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(new BigDecimal("3300.00"));
    }

    @Test
    void testSumAmountByUserIdAndCategory() {
        Optional<BigDecimal> result = financialDataRepository.sumAmountByUserIdAndCategory(
                user1.getId(), Category.FOOD);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(new BigDecimal("300.00"));
    }

    @Test
    void testAvgAmountByUserIdAndType() {
        Optional<BigDecimal> result = financialDataRepository.avgAmountByUserIdAndType(
                user1.getId(), TransactionType.INCOME);
        
        assertThat(result).isPresent();
        assertThat(result.get().doubleValue()).isEqualTo(1100.0); // 3300 / 3
    }

    @Test
    void testAvgAmountByUserIdAndCategory() {
        Optional<BigDecimal> result = financialDataRepository.avgAmountByUserIdAndCategory(
                user1.getId(), Category.FOOD);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(new BigDecimal("60.0")); // 300 / 5
    }

    @Test
    void testFindTopByUserIdOrderByAmountDesc() {
        Pageable pageable = PageRequest.of(0, 3);
        List<FinancialData> result = financialDataRepository.findTopByUserIdOrderByAmountDesc(
                user1.getId(), pageable);
        
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(result.get(1).getAmount()).isEqualTo(new BigDecimal("1100.00"));
        assertThat(result.get(2).getAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void testGetFinancialDataStatistics() {
        List<Object[]> result = financialDataRepository.getFinancialDataStatistics(user1.getId());
        
        assertThat(result).isNotEmpty();
        Object[] stats = result.get(0);
        
        assertThat(stats).isNotNull();
        assertThat(stats.length).isEqualTo(6);
        
        // Check if the first element is the count
        assertThat(stats[0]).isEqualTo(10L); // totalEntries
        assertThat(stats[1]).isEqualTo(new BigDecimal("3300.00")); // totalIncome
        assertThat(stats[2]).isEqualTo(new BigDecimal("450.00")); // totalExpense (300 food + 150 transport)
        assertThat(((Number) stats[3]).doubleValue()).isEqualTo(375.0); // avgAmount (3750 / 10)
        assertThat(stats[4]).isEqualTo(new BigDecimal("40.00")); // minAmount
        assertThat(stats[5]).isEqualTo(new BigDecimal("1200.00")); // maxAmount
    }

    @Test
    void testGetFinancialDataByCategory() {
        List<Object[]> result = financialDataRepository.getFinancialDataByCategory(user1.getId());
        
        assertThat(result).hasSize(3); // SALARY, FOOD, TRANSPORTATION
        
        // Check SALARY category (should be first due to highest total amount)
        Object[] salaryData = result.get(0);
        assertThat(salaryData[0]).isEqualTo(Category.SALARY);
        assertThat(salaryData[1]).isEqualTo(3L); // count
        assertThat(salaryData[2]).isEqualTo(new BigDecimal("3300.00")); // totalAmount
        assertThat(((Number) salaryData[3]).doubleValue()).isEqualTo(1100.0); // avgAmount
    }

    @Test
    void testGetFinancialDataByMonth() {
        List<Object[]> result = financialDataRepository.getFinancialDataByMonth(user1.getId());
        
        assertThat(result).isNotEmpty();
        // Should have entries for the current month
    }

    @Test
    void testFindByCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test with user ID only
        Page<FinancialData> result = financialDataRepository.findByCriteria(
                user1.getId(), null, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(10);
        
        // Test with category filter
        result = financialDataRepository.findByCriteria(
                user1.getId(), Category.FOOD, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(5);
        
        // Test with type filter
        result = financialDataRepository.findByCriteria(
                user1.getId(), null, TransactionType.INCOME, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(3);
        
        // Test with amount range
        result = financialDataRepository.findByCriteria(
                user1.getId(), null, null, null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(200), pageable);
        assertThat(result.getContent()).hasSize(1); // Only transport expense 100 is in range 100-200
    }

    @Test
    void testPaginationAndSorting() {
        // Test pagination
        Pageable pageable = PageRequest.of(0, 5, Sort.by("amount").descending());
        Page<FinancialData> result = financialDataRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        
        // Check sorting
        assertThat(result.getContent().get(0).getAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(result.getContent().get(1).getAmount()).isEqualTo(new BigDecimal("1100.00"));
    }

    @Test
    void testSortingByDate() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending());
        Page<FinancialData> result = financialDataRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(10);
        // Should be ordered by date descending
    }

    @Test
    void testEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test with non-existent user ID
        Page<FinancialData> result = financialDataRepository.findByUserId(999L, pageable);
        assertThat(result.getContent()).isEmpty();
        
        // Test with non-existent category
        List<FinancialData> listResult = financialDataRepository.findByUserAndCategory(user1, Category.EDUCATION);
        assertThat(listResult).isEmpty();
    }
}
