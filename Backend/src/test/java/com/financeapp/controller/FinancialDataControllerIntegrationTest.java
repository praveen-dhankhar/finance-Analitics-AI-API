package com.financeapp.controller;

import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.repository.FinancialDataRepository;
import com.financeapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class FinancialDataControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private FinancialData testFinancialData;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up database
        financialDataRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$test");
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
    void getAllFinancialData_WithPagination_ShouldReturnPagedData() throws Exception {
        // Create additional test data
        createTestFinancialData(5);

        mockMvc.perform(get("/api/v1/financial-data")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by type")
    void getAllFinancialData_WithTypeFilter_ShouldReturnFilteredData() throws Exception {
        // Create income data
        createTestFinancialData("INCOME", "SALARY", 3);

        mockMvc.perform(get("/api/v1/financial-data")
                .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by category")
    void getAllFinancialData_WithCategoryFilter_ShouldReturnFilteredData() throws Exception {
        // Create different category data
        createTestFinancialData("EXPENSE", "TRANSPORT", 2);

        mockMvc.perform(get("/api/v1/financial-data")
                .param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].category").value("FOOD"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should filter financial data by date range")
    void getAllFinancialData_WithDateRangeFilter_ShouldReturnFilteredData() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        // Create data with different dates
        createTestFinancialDataWithDate(yesterday, 2);
        createTestFinancialDataWithDate(tomorrow, 2);

        mockMvc.perform(get("/api/v1/financial-data")
                .param("dateFrom", today.toString())
                .param("dateTo", tomorrow.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3)); // 1 existing + 2 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should search financial data by description")
    void getAllFinancialData_WithSearchFilter_ShouldReturnFilteredData() throws Exception {
        // Create data with specific descriptions
        createTestFinancialDataWithDescription("coffee shop", 2);
        createTestFinancialDataWithDescription("grocery store", 2);

        mockMvc.perform(get("/api/v1/financial-data")
                .param("search", "coffee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].description").value(containsString("coffee")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial data by ID")
    void getFinancialDataById_WithValidId_ShouldReturnFinancialData() throws Exception {
        mockMvc.perform(get("/api/v1/financial-data/{id}", testFinancialData.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFinancialData.getId()))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.description").value("Lunch at restaurant"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return 404 for non-existent financial data")
    void getFinancialDataById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/financial-data/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should create new financial data")
    void createFinancialData_WithValidData_ShouldCreateFinancialData() throws Exception {
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

        mockMvc.perform(post("/api/v1/financial-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("SALARY"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.description").value("Monthly salary"))
                .andExpect(jsonPath("$.id").exists());

        // Verify data was saved
        List<FinancialData> allData = financialDataRepository.findAll();
        assertThat(allData).hasSize(2);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return 400 for invalid financial data")
    void createFinancialData_WithInvalidData_ShouldReturn400() throws Exception {
        FinancialDataCreateDto dto = new FinancialDataCreateDto(
                LocalDate.now(),
                new BigDecimal("-10.00"), // Invalid amount
                "FOOD",
                "Invalid data",
                "" // Invalid type
        );

        mockMvc.perform(post("/api/v1/financial-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should update existing financial data")
    void updateFinancialData_WithValidData_ShouldUpdateFinancialData() throws Exception {
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

        mockMvc.perform(put("/api/v1/financial-data/{id}", testFinancialData.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("ENTERTAINMENT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.description").value("Movie tickets"));

        // Verify data was updated
        FinancialData updatedData = financialDataRepository.findById(testFinancialData.getId()).orElse(null);
        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getCategory().toString()).isEqualTo("ENTERTAINMENT");
        assertThat(updatedData.getAmount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should delete financial data")
    void deleteFinancialData_WithValidId_ShouldDeleteFinancialData() throws Exception {
        mockMvc.perform(delete("/api/v1/financial-data/{id}", testFinancialData.getId()))
                .andExpect(status().isNoContent());

        // Verify data was deleted
        boolean exists = financialDataRepository.existsById(testFinancialData.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should perform advanced search")
    void searchFinancialData_WithAdvancedFilters_ShouldReturnFilteredData() throws Exception {
        // Create test data
        createTestFinancialData("INCOME", "SALARY", 3);
        createTestFinancialData("EXPENSE", "FOOD", 2);

        mockMvc.perform(get("/api/v1/financial-data/search")
                .param("type", "EXPENSE")
                .param("category", "FOOD")
                .param("sortBy", "amount")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3)); // 1 existing + 2 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should perform bulk create")
    void bulkCreateFinancialData_WithValidData_ShouldCreateMultipleRecords() throws Exception {
        List<FinancialDataDto> dtoList = List.of(
                new FinancialDataDto(1L, 1L, LocalDate.now(), new BigDecimal("15.00"), "FOOD", "Breakfast", "EXPENSE", OffsetDateTime.now(), OffsetDateTime.now()),
                new FinancialDataDto(2L, 1L, LocalDate.now(), new BigDecimal("5.00"), "TRANSPORT", "Bus fare", "EXPENSE", OffsetDateTime.now(), OffsetDateTime.now()),
                new FinancialDataDto(3L, 1L, LocalDate.now(), new BigDecimal("1000.00"), "BONUS", "Performance bonus", "INCOME", OffsetDateTime.now(), OffsetDateTime.now())
        );

        mockMvc.perform(post("/api/v1/financial-data/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdCount").value(3))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.createdIds").isArray())
                .andExpect(jsonPath("$.createdIds.length()").value(3));

        // Verify data was created
        List<FinancialData> allData = financialDataRepository.findAll();
        assertThat(allData).hasSize(4); // 1 existing + 3 new
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should export financial data as CSV")
    void exportFinancialData_AsCSV_ShouldReturnCSVData() throws Exception {
        // Create additional test data
        createTestFinancialData(3);

        mockMvc.perform(get("/api/v1/financial-data/export")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(containsString("ID,Type,Category,Amount,Date,Description")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should export financial data as JSON")
    void exportFinancialData_AsJSON_ShouldReturnJSONData() throws Exception {
        mockMvc.perform(get("/api/v1/financial-data/export")
                .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(containsString("[")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get financial summaries")
    void getFinancialSummaries_ShouldReturnSummaryData() throws Exception {
        // Create income and expense data
        createTestFinancialData("INCOME", "SALARY", 2);
        createTestFinancialData("EXPENSE", "FOOD", 3);

        mockMvc.perform(get("/api/v1/financial-data/summaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpense").exists())
                .andExpect(jsonPath("$.netAmount").exists())
                .andExpect(jsonPath("$.totalTransactions").exists())
                .andExpect(jsonPath("$.averageAmount").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get category aggregations")
    void getCategoryAggregations_ShouldReturnAggregationData() throws Exception {
        // Create data with different categories
        createTestFinancialData("EXPENSE", "FOOD", 2);
        createTestFinancialData("EXPENSE", "TRANSPORT", 2);

        mockMvc.perform(get("/api/v1/financial-data/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].totalAmount").exists())
                .andExpect(jsonPath("$[0].transactionCount").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should get monthly trends")
    void getMonthlyTrends_ShouldReturnTrendData() throws Exception {
        // Create data for different months
        createTestFinancialDataWithDate(LocalDate.now().minusMonths(1), 2);
        createTestFinancialDataWithDate(LocalDate.now().minusMonths(2), 2);

        mockMvc.perform(get("/api/v1/financial-data/trends/monthly")
                .param("months", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].year").exists())
                .andExpect(jsonPath("$[0].month").exists())
                .andExpect(jsonPath("$[0].totalAmount").exists())
                .andExpect(jsonPath("$[0].transactionCount").exists());
    }

    @Test
    @DisplayName("Should require authentication for all endpoints")
    void allEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/financial-data"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/financial-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
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

    private void createTestFinancialDataWithDescription(String description, int count) {
        for (int i = 0; i < count; i++) {
            FinancialData data = new FinancialData();
            data.setUser(testUser);
            data.setType(com.financeapp.entity.enums.TransactionType.EXPENSE);
            data.setCategory(com.financeapp.entity.enums.Category.FOOD);
            data.setAmount(new BigDecimal(10 + i * 5));
            data.setDate(LocalDate.now());
            data.setDescription(description + " " + i);
            data.setCreatedAt(OffsetDateTime.now());
            data.setUpdatedAt(OffsetDateTime.now());
            financialDataRepository.save(data);
        }
    }
}
