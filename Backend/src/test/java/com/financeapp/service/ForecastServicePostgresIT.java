package com.financeapp.service;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.User;
import com.financeapp.repository.ForecastResultRepository;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-postgres")
@EnabledIfSystemProperty(named = "it.pg", matches = "true")
@Disabled("Enable with -Dit.pg=true and test-postgres profile")
public class ForecastServicePostgresIT {

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForecastResultRepository forecastResultRepository;

    @Test
    void generateForecast_LR_ShouldPersistResults_Postgres() {
        User user = new User();
        user.setUsername("forecast-pg");
        user.setEmail("fpg@example.com");
        user.setPasswordHash("Password@123");
        user = userRepository.save(user);

        ForecastConfig cfg = new ForecastConfig();
        cfg.setUser(user);
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.LINEAR_REGRESSION);

        var results = forecastService.generateForecast(user.getId(), cfg, LocalDate.now(), 7).join();
        assertThat(results).hasSize(7);
        assertThat(forecastResultRepository.findByUser(user, org.springframework.data.domain.PageRequest.of(0, 10))).isNotNull();
    }
}


