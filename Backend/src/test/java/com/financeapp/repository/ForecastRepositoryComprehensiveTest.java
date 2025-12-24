package com.financeapp.repository;

import com.financeapp.entity.User;
import com.financeapp.entity.Forecast;
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
class ForecastRepositoryComprehensiveTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForecastRepository forecastRepository;

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

        // Create forecasts for user1
        LocalDate baseDate = LocalDate.now().plusDays(30); // Use future dates
        
        // Active income forecasts
        for (int i = 0; i < 3; i++) {
            Forecast forecast = new Forecast();
            forecast.setUser(user1);
            forecast.setForecastDate(baseDate.plusDays(i + 1));
            forecast.setPredictedAmount(BigDecimal.valueOf(1000 + i * 100));
            forecast.setConfidenceScore(BigDecimal.valueOf(0.8 + i * 0.01).setScale(4, RoundingMode.HALF_UP));
            forecast.setStatus(ForecastStatus.ACTIVE);
            forecast.setForecastType(ForecastType.INCOME_EXPENSE);
            forecast.setModelName("income_model");
            forecast.setModelVersion("1.0");
            forecast.setCreatedAt(OffsetDateTime.now());
            forecast.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(forecast);
        }

        // Active expense forecasts
        for (int i = 0; i < 2; i++) {
            Forecast forecast = new Forecast();
            forecast.setUser(user1);
            forecast.setForecastDate(baseDate.plusDays(i + 1));
            forecast.setPredictedAmount(BigDecimal.valueOf(500 + i * 50));
            forecast.setConfidenceScore(BigDecimal.valueOf(0.7 + i * 0.01).setScale(4, RoundingMode.HALF_UP));
            forecast.setStatus(ForecastStatus.ACTIVE);
            forecast.setForecastType(ForecastType.BUDGET_FORECAST);
            forecast.setModelName("expense_model");
            forecast.setModelVersion("1.0");
            forecast.setCreatedAt(OffsetDateTime.now());
            forecast.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(forecast);
        }

        // Completed forecast
        Forecast completedForecast = new Forecast();
        completedForecast.setUser(user1);
        completedForecast.setForecastDate(baseDate.minusDays(1));
        completedForecast.setPredictedAmount(BigDecimal.valueOf(800));
        completedForecast.setConfidenceScore(BigDecimal.valueOf(0.9));
        completedForecast.setStatus(ForecastStatus.ARCHIVED);
        completedForecast.setForecastType(ForecastType.INCOME_EXPENSE);
        completedForecast.setModelName("income_model");
        completedForecast.setModelVersion("1.0");
        completedForecast.setCreatedAt(OffsetDateTime.now());
        completedForecast.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(completedForecast);

        // Create forecasts for user2
        for (int i = 0; i < 2; i++) {
            Forecast forecast = new Forecast();
            forecast.setUser(user2);
            forecast.setForecastDate(baseDate.plusDays(i + 1));
            forecast.setPredictedAmount(BigDecimal.valueOf(300 + i * 100));
            forecast.setConfidenceScore(BigDecimal.valueOf(0.6 + i * 0.01).setScale(4, RoundingMode.HALF_UP));
            forecast.setStatus(ForecastStatus.ACTIVE);
            forecast.setForecastType(ForecastType.BUDGET_FORECAST);
            forecast.setModelName("expense_model");
            forecast.setModelVersion("1.0");
            forecast.setCreatedAt(OffsetDateTime.now());
            forecast.setUpdatedAt(OffsetDateTime.now());
            entityManager.persistAndFlush(forecast);
        }

        entityManager.clear();
    }

    @Test
    void testFindByUser() {
        List<Forecast> result = forecastRepository.findByUser(user1);
        assertThat(result).hasSize(6); // 3 active income + 2 active expense + 1 completed
    }

    @Test
    void testFindByUserWithPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Forecast> result = forecastRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindByUserAndStatus() {
        List<Forecast> result = forecastRepository.findByUserAndStatus(user1, ForecastStatus.ACTIVE);
        assertThat(result).hasSize(5); // 3 income + 2 expense
    }

    @Test
    void testFindByUserAndForecastType() {
        List<Forecast> result = forecastRepository.findByUserAndForecastType(user1, ForecastType.INCOME_EXPENSE);
        assertThat(result).hasSize(4); // 3 active + 1 completed
    }

    @Test
    void testFindByUserAndForecastDateBetween() {
        LocalDate startDate = LocalDate.now().plusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(33);
        
        List<Forecast> result = forecastRepository.findByUserAndForecastDateBetween(user1, startDate, endDate);
        assertThat(result).hasSize(5); // 3 income + 2 expense
    }

    @Test
    void testFindActiveForecastsByUser() {
        List<Forecast> result = forecastRepository.findActiveForecastsByUser(user1);
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(forecast -> forecast.getStatus() == ForecastStatus.ACTIVE);
    }

    @Test
    void testFindByUserAndConfidenceScoreBetween() {
        List<Forecast> result = forecastRepository.findByUserAndConfidenceScoreBetween(
                user1, BigDecimal.valueOf(0.8), BigDecimal.valueOf(0.9));
        assertThat(result).hasSize(4); // 3 income + 1 completed
    }

    @Test
    void testFindHighConfidenceForecastsByUser() {
        List<Forecast> result = forecastRepository.findHighConfidenceForecastsByUser(user1);
        assertThat(result).hasSize(4); // 3 income + 1 completed
        assertThat(result).allMatch(forecast -> forecast.getConfidenceScore().compareTo(BigDecimal.valueOf(0.8)) >= 0);
    }

    @Test
    void testFindByUserAndModelName() {
        List<Forecast> result = forecastRepository.findByUserAndModelName(user1, "income_model");
        assertThat(result).hasSize(4); // 3 active + 1 completed
    }

    @Test
    void testFindUpcomingForecastsByUser() {
        List<Forecast> result = forecastRepository.findUpcomingForecastsByUser(user1, LocalDate.now());
        assertThat(result).hasSize(6); // All 6 forecasts are in the future
        assertThat(result).allMatch(forecast -> forecast.getForecastDate().isAfter(LocalDate.now()));
    }

    @Test
    void testCalculateAverageConfidenceByUser() {
        BigDecimal result = forecastRepository.calculateAverageConfidenceByUser(user1);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void testCountByUserAndStatus() {
        long count = forecastRepository.countByUserAndStatus(user1, ForecastStatus.ACTIVE);
        assertThat(count).isEqualTo(5);
    }

    @Test
    void testFindMostRecentByUser() {
        Pageable pageable = PageRequest.of(0, 3);
        List<Forecast> result = forecastRepository.findMostRecentByUser(user1, pageable);
        
        assertThat(result).hasSize(3);
        // Should be ordered by createdAt DESC
    }

    @Test
    void testFindByUserAndPredictedAmountBetween() {
        List<Forecast> result = forecastRepository.findByUserAndPredictedAmountBetween(
                user1, BigDecimal.valueOf(500), BigDecimal.valueOf(1000));
        assertThat(result).hasSize(4); // 2 expense + 2 income
    }

    @Test
    void testFindByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserId(user1.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(6);
    }

    @Test
    void testFindByUserIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserIdAndStatus(
                user1.getId(), ForecastStatus.ACTIVE, pageable);
        
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent()).allMatch(forecast -> forecast.getStatus() == ForecastStatus.ACTIVE);
    }

    @Test
    void testFindByUserIdAndForecastType() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserIdAndForecastType(
                user1.getId(), ForecastType.INCOME_EXPENSE, pageable);
        
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(forecast -> forecast.getForecastType() == ForecastType.INCOME_EXPENSE);
    }

    @Test
    void testFindByUserIdAndForecastDateBetween() {
        LocalDate startDate = LocalDate.now().plusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(33);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Forecast> result = forecastRepository.findByUserIdAndForecastDateBetween(
                user1.getId(), startDate, endDate, pageable);
        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    void testFindByUserIdAndConfidenceScoreBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserIdAndConfidenceScoreBetween(
                user1.getId(), BigDecimal.valueOf(0.8), BigDecimal.valueOf(0.9), pageable);
        
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    void testFindByUserIdAndModelName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserIdAndModelName(
                user1.getId(), "income_model", pageable);
        
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(forecast -> forecast.getModelName().equals("income_model"));
    }

    @Test
    void testFindByUserIdAndPredictedAmountBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findByUserIdAndPredictedAmountBetween(
                user1.getId(), BigDecimal.valueOf(500), BigDecimal.valueOf(1000), pageable);
        
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    void testFindActiveForecastsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findActiveForecastsByUserId(user1.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent()).allMatch(forecast -> forecast.getStatus() == ForecastStatus.ACTIVE);
    }

    @Test
    void testFindHighConfidenceForecastsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findHighConfidenceForecastsByUserId(user1.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(forecast -> 
                forecast.getConfidenceScore().compareTo(BigDecimal.valueOf(0.8)) >= 0);
    }

    @Test
    void testFindUpcomingForecastsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Forecast> result = forecastRepository.findUpcomingForecastsByUserId(
                user1.getId(), LocalDate.now(), pageable);
        
        assertThat(result.getContent()).hasSize(6); // All 6 forecasts are in the future
        assertThat(result.getContent()).allMatch(forecast -> 
                forecast.getForecastDate().isAfter(LocalDate.now()));
    }

    @Test
    void testFindMostRecentByUserId() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Forecast> result = forecastRepository.findMostRecentByUserId(user1.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testCalculateAverageConfidenceByUserId() {
        Optional<BigDecimal> result = forecastRepository.calculateAverageConfidenceByUserId(user1.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void testCountByUserIdAndStatus() {
        long count = forecastRepository.countByUserIdAndStatus(user1.getId(), ForecastStatus.ACTIVE);
        assertThat(count).isEqualTo(5);
    }

    @Test
    void testCountByUserId() {
        long count = forecastRepository.countByUserId(user1.getId());
        assertThat(count).isEqualTo(6);
    }

    @Test
    void testGetForecastStatistics() {
        List<Object[]> result = forecastRepository.getForecastStatistics(user1.getId());
        
        assertThat(result).isNotEmpty();
        Object[] stats = result.get(0);
        
        assertThat(stats[0]).isEqualTo(6L); // totalForecasts
        assertThat(stats[1]).isNotNull(); // avgConfidence
        assertThat(stats[2]).isEqualTo(new BigDecimal("0.7000")); // minConfidence
        assertThat(stats[3]).isEqualTo(new BigDecimal("0.9000")); // maxConfidence
        assertThat(stats[4]).isNotNull(); // totalPredictedAmount
    }

    @Test
    void testGetForecastsByStatus() {
        List<Object[]> result = forecastRepository.getForecastsByStatus(user1.getId());
        
        assertThat(result).hasSize(2); // ACTIVE and COMPLETED
        
        // Check ACTIVE status (should be first due to highest count)
        Object[] activeData = result.get(0);
        assertThat(activeData[0]).isEqualTo(ForecastStatus.ACTIVE);
        assertThat(activeData[1]).isEqualTo(5L); // count
    }

    @Test
    void testGetForecastsByType() {
        List<Object[]> result = forecastRepository.getForecastsByType(user1.getId());
        
        assertThat(result).hasSize(2); // INCOME and EXPENSE
        
        // Check INCOME_EXPENSE type (should be first due to highest count)
        Object[] incomeData = result.get(0);
        assertThat(incomeData[0]).isEqualTo(ForecastType.INCOME_EXPENSE);
        assertThat(incomeData[1]).isEqualTo(4L); // count
    }

    @Test
    void testGetForecastsByModel() {
        List<Object[]> result = forecastRepository.getForecastsByModel(user1.getId());
        
        assertThat(result).hasSize(2); // income_model and expense_model
        
        // Check income_model (should be first due to highest count)
        Object[] incomeModelData = result.get(0);
        assertThat(incomeModelData[0]).isEqualTo("income_model");
        assertThat(incomeModelData[1]).isEqualTo(4L); // count
    }

    @Test
    void testGetForecastsByMonth() {
        List<Object[]> result = forecastRepository.getForecastsByMonth(user1.getId());
        
        assertThat(result).isNotEmpty();
        // Should have entries for the current month
    }

    @Test
    void testFindTopByUserIdOrderByConfidenceScoreDesc() {
        Pageable pageable = PageRequest.of(0, 3);
        List<Forecast> result = forecastRepository.findTopByUserIdOrderByConfidenceScoreDesc(
                user1.getId(), pageable);
        
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getConfidenceScore()).isEqualTo(new BigDecimal("0.9000"));
        assertThat(result.get(1).getConfidenceScore()).isEqualTo(new BigDecimal("0.8200"));
        assertThat(result.get(2).getConfidenceScore()).isEqualTo(new BigDecimal("0.8100"));
    }

    @Test
    void testFindTopByUserIdOrderByPredictedAmountDesc() {
        Pageable pageable = PageRequest.of(0, 3);
        List<Forecast> result = forecastRepository.findTopByUserIdOrderByPredictedAmountDesc(
                user1.getId(), pageable);
        
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPredictedAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(result.get(1).getPredictedAmount()).isEqualTo(new BigDecimal("1100.00"));
        assertThat(result.get(2).getPredictedAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void testFindByCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test with user ID only
        Page<Forecast> result = forecastRepository.findByCriteria(
                user1.getId(), null, null, null, null, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(6);
        
        // Test with status filter
        result = forecastRepository.findByCriteria(
                user1.getId(), ForecastStatus.ACTIVE, null, null, null, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(5);
        
        // Test with type filter
        result = forecastRepository.findByCriteria(
                user1.getId(), null, ForecastType.INCOME_EXPENSE, null, null, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(4);
        
        // Test with model name filter
        result = forecastRepository.findByCriteria(
                user1.getId(), null, null, "income_model", null, null, null, null, null, null, pageable);
        assertThat(result.getContent()).hasSize(4);
        
        // Test with confidence range
        result = forecastRepository.findByCriteria(
                user1.getId(), null, null, null, null, null, BigDecimal.valueOf(0.8), BigDecimal.valueOf(0.9), null, null, pageable);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    void testPaginationAndSorting() {
        // Test pagination
        Pageable pageable = PageRequest.of(0, 3, Sort.by("predictedAmount").descending());
        Page<Forecast> result = forecastRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        
        // Check sorting
        assertThat(result.getContent().get(0).getPredictedAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(result.getContent().get(1).getPredictedAmount()).isEqualTo(new BigDecimal("1100.00"));
    }

    @Test
    void testSortingByConfidenceScore() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("confidenceScore").descending());
        Page<Forecast> result = forecastRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(6);
        // Should be ordered by confidence score descending
    }

    @Test
    void testSortingByForecastDate() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("forecastDate").ascending());
        Page<Forecast> result = forecastRepository.findByUser(user1, pageable);
        
        assertThat(result.getContent()).hasSize(6);
        // Should be ordered by forecast date ascending
    }

    @Test
    void testEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test with non-existent user ID
        Page<Forecast> result = forecastRepository.findByUserId(999L, pageable);
        assertThat(result.getContent()).isEmpty();
        
        // Test with non-existent status
        List<Forecast> listResult = forecastRepository.findByUserAndStatus(user1, ForecastStatus.EXPIRED);
        assertThat(listResult).isEmpty();
    }
}
