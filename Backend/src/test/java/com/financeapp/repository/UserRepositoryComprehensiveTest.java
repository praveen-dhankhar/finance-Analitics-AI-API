package com.financeapp.repository;

import com.financeapp.entity.User;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.Forecast;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import com.financeapp.entity.Forecast.ForecastStatus;
import com.financeapp.entity.Forecast.ForecastType;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryComprehensiveTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private ForecastRepository forecastRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User();
        user1.setUsername("john_doe");
        user1.setEmail("john@example.com");
        user1.setPasswordHash("hashed_password_1");
        user1.setCreatedAt(OffsetDateTime.now());
        user1.setUpdatedAt(OffsetDateTime.now());
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setUsername("jane_smith");
        user2.setEmail("jane@example.com");
        user2.setPasswordHash("hashed_password_2");
        user2.setCreatedAt(OffsetDateTime.now());
        user2.setUpdatedAt(OffsetDateTime.now());
        user2 = entityManager.persistAndFlush(user2);

        user3 = new User();
        user3.setUsername("bob_wilson");
        user3.setEmail("bob@example.com");
        user3.setPasswordHash("hashed_password_3");
        user3.setCreatedAt(OffsetDateTime.now());
        user3.setUpdatedAt(OffsetDateTime.now());
        user3 = entityManager.persistAndFlush(user3);

        // Create financial data for user1
        for (int i = 0; i < 5; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user1);
            data.setDate(LocalDate.now().minusDays(i));
            data.setAmount(BigDecimal.valueOf(100 + i * 10));
            data.setCategory(Category.FOOD);
            data.setType(TransactionType.EXPENSE);
            data.setDescription("Test expense " + i);
            entityManager.persistAndFlush(data);
        }

        // Create financial data for user2
        for (int i = 0; i < 3; i++) {
            FinancialData data = new FinancialData();
            data.setUser(user2);
            data.setDate(LocalDate.now().minusDays(i));
            data.setAmount(BigDecimal.valueOf(200 + i * 20));
            data.setCategory(Category.SALARY);
            data.setType(TransactionType.INCOME);
            data.setDescription("Test income " + i);
            entityManager.persistAndFlush(data);
        }

        // Create forecasts for user1
        LocalDate baseDate = LocalDate.now().plusDays(30); // Use future dates
        for (int i = 0; i < 4; i++) {
            Forecast forecast = new Forecast();
            forecast.setUser(user1);
            forecast.setForecastDate(baseDate.plusDays(i + 1));
            forecast.setPredictedAmount(BigDecimal.valueOf(500 + i * 100));
            forecast.setConfidenceScore(BigDecimal.valueOf(0.8 + i * 0.01).setScale(4, RoundingMode.HALF_UP));
            forecast.setStatus(ForecastStatus.ACTIVE);
            forecast.setForecastType(ForecastType.INCOME_EXPENSE);
            forecast.setModelName("test_model");
            forecast.setModelVersion("1.0");
            forecast.setCreatedAt(OffsetDateTime.now());
            forecast.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(forecast);
        }

        // Create forecasts for user2
        for (int i = 0; i < 2; i++) {
            Forecast forecast = new Forecast();
            forecast.setUser(user2);
            forecast.setForecastDate(LocalDate.now().plusDays(i + 1));
            forecast.setPredictedAmount(BigDecimal.valueOf(300 + i * 50));
            forecast.setConfidenceScore(BigDecimal.valueOf(0.7 + i * 0.01).setScale(4, RoundingMode.HALF_UP));
            forecast.setStatus(ForecastStatus.ACTIVE);
            forecast.setForecastType(ForecastType.BUDGET_FORECAST);
            forecast.setModelName("test_model");
            forecast.setModelVersion("1.0");
            forecast.setCreatedAt(OffsetDateTime.now());
            forecast.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(forecast);
        }

        entityManager.clear();
    }

    @Test
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("jane@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_smith");
    }

    @Test
    void testExistsByUsername() {
        assertThat(userRepository.existsByUsername("john_doe")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void testExistsByEmail() {
        assertThat(userRepository.existsByEmail("jane@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void testFindByUsernameOrEmail() {
        Optional<User> found = userRepository.findByUsernameOrEmail("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");

        found = userRepository.findByUsernameOrEmail("bob@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob_wilson");
    }

    @Test
    void testCountAllUsers() {
        long count = userRepository.countAllUsers();
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testFindByUsernameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findByUsernameContainingIgnoreCase("john", pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testFindByEmailContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findByEmailContainingIgnoreCase("example", pageable);
        
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testFindByCreatedAtBetween() {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> result = userRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testFindUsersWithMinFinancialDataCount() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findUsersWithMinFinancialDataCount(3, pageable);
        
        assertThat(result.getContent()).hasSize(2); // user1 has 5, user2 has 3
        assertThat(result.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "jane_smith");
    }

    @Test
    void testFindUsersWithMinForecastCount() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findUsersWithMinForecastCount(2, pageable);
        
        assertThat(result.getContent()).hasSize(2); // user1 has 4, user2 has 2
        assertThat(result.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "jane_smith");
    }

    @Test
    void testGetUserStatistics() {
        List<Object[]> result = userRepository.getUserStatistics(user1.getId());
        assertThat(result).isNotEmpty();
        
        Object[] userStats = result.get(0);
        assertThat(userStats[0]).isEqualTo(user1.getId()); // userId
        assertThat(userStats[1]).isEqualTo(5); // financialDataCount
        assertThat(userStats[2]).isEqualTo(4); // forecastCount
    }

    @Test
    void testFindUsersByCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Search by username
        Page<User> result = userRepository.findUsersByCriteria("john", null, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
        
        // Search by email
        result = userRepository.findUsersByCriteria(null, "jane", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane@example.com");
        
        // Search by both
        result = userRepository.findUsersByCriteria("bob", "bob", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("bob_wilson");
    }

    @Test
    void testPaginationAndSorting() {
        // Test pagination
        Pageable pageable = PageRequest.of(0, 2, Sort.by("username").ascending());
        Page<User> result = userRepository.findAll(pageable);
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("bob_wilson");
        assertThat(result.getContent().get(1).getUsername()).isEqualTo("jane_smith");
        
        // Test second page
        pageable = PageRequest.of(1, 2, Sort.by("username").ascending());
        result = userRepository.findAll(pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testSortingByEmail() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").descending());
        Page<User> result = userRepository.findAll(pageable);
        
        assertThat(result.getContent()).hasSize(3);
        // Check that all expected emails are present, regardless of exact order
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("jane@example.com", "john@example.com", "bob@example.com");
    }

    @Test
    void testCaseInsensitiveSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test case insensitive username search
        Page<User> result = userRepository.findByUsernameContainingIgnoreCase("JOHN", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
        
        // Test case insensitive email search
        result = userRepository.findByEmailContainingIgnoreCase("EXAMPLE", pageable);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test with non-existent username
        Page<User> result = userRepository.findByUsernameContainingIgnoreCase("nonexistent", pageable);
        assertThat(result.getContent()).isEmpty();
        
        // Test with non-existent email
        result = userRepository.findByEmailContainingIgnoreCase("nonexistent", pageable);
        assertThat(result.getContent()).isEmpty();
    }
}
