package com.financeapp.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public TestDatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        // Delete in FK-safe order; tables may not all exist in H2 for every test run
        String[] tablesInOrder = new String[]{
                "forecast_anomalies",
                "forecast_performances",
                "forecast_results",
                "forecasts",
                "financial_data",
                "categories",
                "user_profiles",
                "accounts",
                "budgets",
                "financial_goals",
                "forecast_jobs",
                "users"
        };
        for (String table : tablesInOrder) {
            try {
                jdbcTemplate.update("DELETE FROM " + table);
            } catch (Exception ignored) {
                // table may not exist in some contexts
            }
        }
    }
}


