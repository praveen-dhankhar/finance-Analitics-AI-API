package com.financeapp.service;

import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataResponseDto;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.repository.FinancialDataRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.specification.FinancialDataSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.financeapp.testsupport.TestDatabaseCleaner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinancialDataServiceIntegrationTest {

    @Autowired
    private FinancialDataService financialDataService;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseCleaner cleaner;

    private User testUser;
    private FinancialData testFinancialData;

    @BeforeEach
    void setUp() {
        // Clean all tables in FK-safe order to avoid cross-class residue
        cleaner.clean();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("TestPass123!"));
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        testUser = userRepository.save(testUser);

        // Create test financial data
        testFinancialData = new FinancialData();
        testFinancialData.setUser(testUser);
        testFinancialData.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
        testFinancialData.setCategory(com.financeapp.entity.enums.Category.FOOD);
        testFinancialData.setAmount(new BigDecimal("25.50"));
        testFinancialData.setDate(LocalDate.now());
        testFinancialData.setDescription("Lunch at restaurant");
        testFinancialData.setCreatedAt(OffsetDateTime.now());
        testFinancialData.setUpdatedAt(OffsetDateTime.now());
        testFinancialData = financialDataRepository.save(testFinancialData);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get all financial data with pagination")
    void getAllFinancialData_WithPagination_ShouldReturnPagedData() {
        // Create additional test data
        createTestFinancialData(5);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                null, null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 3);

        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by type")
    void getAllFinancialData_WithTypeFilter_ShouldReturnFilteredData() {
        // Create income data
        createTestFinancialData("INCOME", "SALARY", 3);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                "EXPENSE", null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).type()).isEqualTo("EXPENSE");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by category")
    void getAllFinancialData_WithCategoryFilter_ShouldReturnFilteredData() {
        // Create different category data
        createTestFinancialData("EXPENSE", "TRANSPORT", 2);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                null, "FOOD", null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).category()).isEqualTo("FOOD");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by date range")
    void getAllFinancialData_WithDateRangeFilter_ShouldReturnFilteredData() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        // Create data with different dates
        createTestFinancialDataWithDate(yesterday, 2);
        createTestFinancialDataWithDate(tomorrow, 2);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                null, null, today, tomorrow, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3); // 1 existing + 2 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by ID")
    void getFinancialDataById_WithValidId_ShouldReturnFinancialData() {
        Optional<FinancialDataResponseDto> result = financialDataService.getFinancialDataById(testFinancialData.getId());

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(testFinancialData.getId());
        assertThat(result.get().type()).isEqualTo("EXPENSE");
        assertThat(result.get().category()).isEqualTo("FOOD");
        assertThat(result.get().amount()).isEqualTo(new BigDecimal("25.50"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return empty for non-existent financial data")
    void getFinancialDataById_WithInvalidId_ShouldReturnEmpty() {
        Optional<FinancialDataResponseDto> result = financialDataService.getFinancialDataById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should create new financial data")
    void createFinancialData_WithValidData_ShouldCreateFinancialData() {
        FinancialDataDto dto = new FinancialDataDto(
                1L,
                1L,
                LocalDate.now(),
                new BigDecimal("5000.00"),
                "SALARY",
                "Monthly salary",
                "INCOME",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        FinancialDataResponseDto result = financialDataService.createFinancialData(dto);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("INCOME");
        assertThat(result.category()).isEqualTo("SALARY");
        assertThat(result.amount()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(result.description()).isEqualTo("Monthly salary");
        assertThat(result.id()).isNotNull();

        // Verify data was saved
        List<FinancialData> allData = financialDataRepository.findAll();
        assertThat(allData).hasSize(2);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should update existing financial data")
    void updateFinancialData_WithValidData_ShouldUpdateFinancialData() {
        FinancialDataDto dto = new FinancialDataDto(
                1L,
                1L,
                LocalDate.now(),
                new BigDecimal("50.00"),
                "ENTERTAINMENT",
                "Movie tickets",
                "EXPENSE",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        Optional<FinancialDataResponseDto> result = financialDataService.updateFinancialData(testFinancialData.getId(), dto);

        assertThat(result).isPresent();
        assertThat(result.get().category()).isEqualTo("ENTERTAINMENT");
        assertThat(result.get().amount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(result.get().description()).isEqualTo("Movie tickets");

        // Verify data was updated
        FinancialData updatedData = financialDataRepository.findById(testFinancialData.getId()).orElse(null);
        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getCategory().toString()).isEqualTo("ENTERTAINMENT");
        assertThat(updatedData.getAmount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should delete financial data")
    void deleteFinancialData_WithValidId_ShouldDeleteFinancialData() {
        boolean result = financialDataService.deleteFinancialData(testFinancialData.getId());

        assertThat(result).isTrue();

        // Verify data was deleted
        boolean exists = financialDataRepository.existsById(testFinancialData.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should perform bulk create")
    void bulkCreateFinancialData_WithValidData_ShouldCreateMultipleRecords() {
        List<FinancialDataDto> dtoList = List.of(
                new FinancialDataDto(1L, 1L, LocalDate.now(), new BigDecimal("15.00"), "FOOD", "Breakfast", "EXPENSE", OffsetDateTime.now(), OffsetDateTime.now()),
                new FinancialDataDto(2L, 1L, LocalDate.now(), new BigDecimal("5.00"), "TRANSPORT", "Bus fare", "EXPENSE", OffsetDateTime.now(), OffsetDateTime.now()),
                new FinancialDataDto(3L, 1L, LocalDate.now(), new BigDecimal("1000.00"), "BONUS", "Performance bonus", "INCOME", OffsetDateTime.now(), OffsetDateTime.now())
        );

        Map<String, Object> result = financialDataService.bulkCreateFinancialData(dtoList);

        assertThat(result).isNotNull();
        assertThat(result.get("createdCount")).isEqualTo(3);
        assertThat(result.get("failedCount")).isEqualTo(0);
        assertThat(result.get("createdIds")).isInstanceOf(List.class);
        assertThat((List<?>) result.get("createdIds")).hasSize(3);

        // Verify data was created
        List<FinancialData> allData = financialDataRepository.findAll();
        assertThat(allData).hasSize(4); // 1 existing + 3 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should export financial data as CSV")
    void exportFinancialData_AsCSV_ShouldReturnCSVData() {
        // Create additional test data
        createTestFinancialData(3);

        String result = financialDataService.exportFinancialData("csv", null, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result).contains("ID,Type,Category,Amount,Date,Description");
        assertThat(result).contains("EXPENSE");
        assertThat(result).contains("FOOD");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should export financial data as JSON")
    void exportFinancialData_AsJSON_ShouldReturnJSONData() {
        String result = financialDataService.exportFinancialData("json", null, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result).startsWith("[");
        assertThat(result).contains("EXPENSE");
        assertThat(result).contains("FOOD");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial summaries")
    void getFinancialSummaries_ShouldReturnSummaryData() {
        // Create income and expense data
        createTestFinancialData("INCOME", "SALARY", 2);
        createTestFinancialData("EXPENSE", "FOOD", 3);

        Map<String, Object> result = financialDataService.getFinancialSummaries(null, null);

        assertThat(result).isNotNull();
        assertThat(result).containsKey("totalIncome");
        assertThat(result).containsKey("totalExpense");
        assertThat(result).containsKey("netAmount");
        assertThat(result).containsKey("totalTransactions");
        assertThat(result).containsKey("averageAmount");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get category aggregations")
    void getCategoryAggregations_ShouldReturnAggregationData() {
        // Create data with different categories
        createTestFinancialData("EXPENSE", "FOOD", 2);
        createTestFinancialData("EXPENSE", "TRANSPORT", 2);

        List<Map<String, Object>> result = financialDataService.getCategoryAggregations(null, null, null);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("category");
        assertThat(result.get(0)).containsKey("type");
        assertThat(result.get(0)).containsKey("totalAmount");
        assertThat(result.get(0)).containsKey("transactionCount");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get monthly trends")
    void getMonthlyTrends_ShouldReturnTrendData() {
        // Create data for different months
        createTestFinancialDataWithDate(LocalDate.now().minusMonths(1), 2);
        createTestFinancialDataWithDate(LocalDate.now().minusMonths(2), 2);

        List<Map<String, Object>> result = financialDataService.getMonthlyTrends(null, 3);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("year");
        assertThat(result.get(0)).containsKey("month");
        assertThat(result.get(0)).containsKey("totalAmount");
        assertThat(result.get(0)).containsKey("transactionCount");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by date range")
    void getFinancialDataByDateRange_ShouldReturnFilteredData() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Create data outside range
        createTestFinancialDataWithDate(LocalDate.now().minusDays(10), 2);
        // Create data inside range
        createTestFinancialDataWithDate(LocalDate.now(), 2);

        List<FinancialDataResponseDto> result = financialDataService.getFinancialDataByDateRange(startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // 1 existing + 2 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by amount range")
    void getFinancialDataByAmountRange_ShouldReturnFilteredData() {
        BigDecimal minAmount = new BigDecimal("20.00");
        BigDecimal maxAmount = new BigDecimal("30.00");

        // Create data outside range
        createTestFinancialDataWithAmount(new BigDecimal("10.00"), 2);
        // Create data inside range
        createTestFinancialDataWithAmount(new BigDecimal("25.00"), 2);

        List<FinancialDataResponseDto> result = financialDataService.getFinancialDataByAmountRange(minAmount, maxAmount);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // 1 existing + 2 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by category")
    void getFinancialDataByCategory_ShouldReturnFilteredData() {
        // Create data with different categories
        createTestFinancialData("EXPENSE", "TRANSPORT", 2);

        List<FinancialDataResponseDto> result = financialDataService.getFinancialDataByCategory("FOOD");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("FOOD");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by type")
    void getFinancialDataByType_ShouldReturnFilteredData() {
        // Create income data
        createTestFinancialData("INCOME", "SALARY", 2);

        List<FinancialDataResponseDto> result = financialDataService.getFinancialDataByType("EXPENSE");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo("EXPENSE");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get total amount by type and date range")
    void getTotalAmountByTypeAndDateRange_ShouldReturnCorrectAmount() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Create income data
        createTestFinancialData("INCOME", "SALARY", 2);

        BigDecimal result = financialDataService.getTotalAmountByTypeAndDateRange("INCOME", startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get average amount by category and date range")
    void getAverageAmountByCategoryAndDateRange_ShouldReturnCorrectAmount() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Create data with same category
        createTestFinancialData("EXPENSE", "FOOD", 3);

        BigDecimal result = financialDataService.getAverageAmountByCategoryAndDateRange("FOOD", startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get count by type and date range")
    void getCountByTypeAndDateRange_ShouldReturnCorrectCount() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Create expense data
        createTestFinancialData("EXPENSE", "FOOD", 3);

        Long result = financialDataService.getCountByTypeAndDateRange("EXPENSE", startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(4L); // 1 existing + 3 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get top categories by amount")
    void getTopCategoriesByAmount_ShouldReturnTopCategories() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Create data with different categories and amounts
        createTestFinancialDataWithAmount("FOOD", new BigDecimal("100.00"), 2);
        createTestFinancialDataWithAmount("TRANSPORT", new BigDecimal("50.00"), 2);

        List<Map<String, Object>> result = financialDataService.getTopCategoriesByAmount("EXPENSE", startDate, endDate, 5);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("category");
        assertThat(result.get(0)).containsKey("totalAmount");
        assertThat(result.get(0)).containsKey("transactionCount");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get trends by period")
    void getTrendsByPeriod_ShouldReturnTrendData() {
        // Create data for different periods
        createTestFinancialDataWithDate(LocalDate.now().minusDays(1), 2);
        createTestFinancialDataWithDate(LocalDate.now().minusDays(2), 2);

        List<Map<String, Object>> result = financialDataService.getTrendsByPeriod("daily", "EXPENSE", 5);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("period");
        assertThat(result.get(0)).containsKey("totalAmount");
        assertThat(result.get(0)).containsKey("transactionCount");
    }

    // Helper methods

    private void createTestFinancialData(int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
            data.setCategory(com.financeapp.entity.enums.Category.FOOD);
            data.setAmount(new BigDecimal(10 + i * 5));
            data.setDate(LocalDate.now().minusDays(i));
            data.setDescription("Test expense " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }

    private void createTestFinancialData(String type, String category, int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.valueOf(type));
            data.setCategory(com.financeapp.entity.enums.Category.valueOf(category));
            data.setAmount(new BigDecimal(10 + i * 5));
            data.setDate(LocalDate.now().minusDays(i));
            data.setDescription("Test " + type.toLowerCase() + " " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }

    private void createTestFinancialDataWithDate(LocalDate date, int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
            data.setCategory(com.financeapp.entity.enums.Category.FOOD);
            data.setAmount(new BigDecimal(10 + i * 5));
            data.setDate(date);
            data.setDescription("Test expense on " + date);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }

    private void createTestFinancialDataWithAmount(BigDecimal amount, int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
            data.setCategory(com.financeapp.entity.enums.Category.FOOD);
            data.setAmount(amount);
            data.setDate(LocalDate.now());
            data.setDescription("Test expense with amount " + amount);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }

    private void createTestFinancialDataWithAmount(String category, BigDecimal amount, int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
            data.setCategory(com.financeapp.entity.enums.Category.valueOf(category));
            data.setAmount(amount);
            data.setDate(LocalDate.now());
            data.setDescription("Test expense with amount " + amount);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }
}
