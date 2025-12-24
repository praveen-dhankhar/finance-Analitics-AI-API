package com.financeapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void allCoreTablesExist() {
		String[] tables = {
				"USERS",
				"FINANCIAL_DATA",
				"FORECASTS",
				"ACCOUNTS",
				"TRANSACTIONS",
				"BUDGETS",
				"FINANCIAL_GOALS"
		};
		for (String table : tables) {
			try {
				// Try to query the table directly - if it exists, this won't throw an exception
				jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
				// If we get here, the table exists
			} catch (Exception e) {
				// Table doesn't exist or can't be queried
				throw new AssertionError("Table " + table + " does not exist or cannot be queried", e);
			}
		}
		// If we get here, all tables exist
		assertThat(true).isTrue(); // All tables exist
	}
}


