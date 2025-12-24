package com.financeapp.service;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import com.financeapp.entity.User;
import com.financeapp.repository.ForecastResultRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.FinancialDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ForecastServiceH2IntegrationTest {

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForecastResultRepository forecastResultRepository;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Test
    void generateForecast_SMA_ShouldPersistResults() {
        User user = new User();
        user.setUsername("forecast-user");
        user.setEmail("fuser@example.com");
        user.setPasswordHash("Password@123");
        user = userRepository.save(user);

        // Seed historical data for the user
        for (int i = 10; i >= 1; i--) {
            FinancialData fd = new FinancialData();
            fd.setUser(user);
            fd.setAmount(java.math.BigDecimal.valueOf(10 * i));
            fd.setType(TransactionType.EXPENSE);
            fd.setCategory(Category.FOOD);
            fd.setDate(LocalDate.now().minusDays(i));
            fd.setDescription("Seed " + i);
            financialDataRepository.save(fd);
        }

        ForecastConfig cfg = new ForecastConfig();
        cfg.setUser(user);
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.SMA);
        cfg.setWindowSize(3);

        var results = forecastService.generateForecast(user.getId(), cfg, LocalDate.now(), 5).join();
        assertThat(results).hasSize(5);
        assertThat(forecastResultRepository.findByUser(user, org.springframework.data.domain.PageRequest.of(0, 10))).isNotNull();
    }
}


