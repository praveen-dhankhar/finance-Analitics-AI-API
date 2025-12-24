package com.financeapp.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class PoolMetricsBinder {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void bind() {
        if (dataSource instanceof HikariDataSource hikari) {
            // Enable Micrometer metrics for HikariCP
            hikari.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(meterRegistry));
        }
    }
}


