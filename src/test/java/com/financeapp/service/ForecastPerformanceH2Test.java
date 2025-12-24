package com.financeapp.service;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import com.financeapp.repository.FinancialDataRepository;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ForecastPerformanceH2Test {

    @Autowired
    private ForecastService forecastService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Test
    void largeDataset_shouldForecastQuickly() {
        User user = new User();
        user.setUsername("perf-user");
        user.setEmail("perf@example.com");
        user.setPasswordHash("Password@123");
        user = userRepository.save(user);

        LocalDate start = LocalDate.now().minusDays(365);
        for (int i = 0; i < 365; i++) {
            FinancialData fd = new FinancialData();
            fd.setUser(user);
            fd.setAmount(BigDecimal.valueOf(10 + (i % 30)));
            fd.setType(TransactionType.EXPENSE);
            fd.setCategory(Category.FOOD);
            fd.setDate(start.plusDays(i));
            fd.setDescription("Perf " + i);
            financialDataRepository.save(fd);
        }

        ForecastConfig cfg = new ForecastConfig();
        cfg.setUser(user);
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.LINEAR_REGRESSION);

        long t0 = System.currentTimeMillis();
        var results = forecastService.generateForecast(user.getId(), cfg, LocalDate.now().plusDays(1), 14).join();
        long dt = System.currentTimeMillis() - t0;

        assertThat(results).hasSize(14);
        // Basic performance assertion (loose): under 2 seconds on H2
        assertThat(dt).isLessThan(2000);
    }
}


