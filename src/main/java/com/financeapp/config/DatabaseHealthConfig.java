package com.financeapp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseHealthConfig {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                Map<String, Object> details = new HashMap<>();
                details.put("profile", activeProfile);
                details.put("dbProduct", meta.getDatabaseProductName());
                details.put("dbVersion", meta.getDatabaseProductVersion());
                details.put("url", meta.getURL());
                if (dataSource instanceof HikariDataSource hikari) {
                    details.put("poolActive", hikari.getHikariPoolMXBean() != null ? hikari.getHikariPoolMXBean().getActiveConnections() : -1);
                    details.put("poolIdle", hikari.getHikariPoolMXBean() != null ? hikari.getHikariPoolMXBean().getIdleConnections() : -1);
                    details.put("poolTotal", hikari.getHikariPoolMXBean() != null ? hikari.getHikariPoolMXBean().getTotalConnections() : -1);
                }
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down(e).withDetail("profile", activeProfile).build();
            }
        };
    }

    @Bean
    public InfoContributor databaseInfoContributor(DataSource dataSource) {
        return (Info.Builder builder) -> {
            Map<String, Object> db = new HashMap<>();
            db.put("profile", activeProfile);
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                db.put("product", meta.getDatabaseProductName());
                db.put("version", meta.getDatabaseProductVersion());
                db.put("url", meta.getURL());
            } catch (Exception ignored) { }
            builder.withDetail("database", db);
        };
    }
}


