package com.financeapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void flywayMigrationsApplyAndTablesExist() {
		Integer users = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'USERS'", Integer.class);
		Integer fin = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'FINANCIAL_DATA'", Integer.class);
		Integer forecasts = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'FORECASTS'", Integer.class);
		assertThat(users).isNotNull();
		assertThat(fin).isNotNull();
		assertThat(forecasts).isNotNull();
		assertThat(users).isGreaterThanOrEqualTo(1);
		assertThat(fin).isGreaterThanOrEqualTo(1);
		assertThat(forecasts).isGreaterThanOrEqualTo(1);
	}
}


