package com.financeapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Basic application health
        health.put("status", "UP");
        health.put("timestamp", OffsetDateTime.now());
        health.put("application", "Finance Forecast App");
        health.put("version", "1.0.0");
        
        // Database health check
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put("status", "UP");
            database.put("type", getDatabaseType());
            database.put("url", connection.getMetaData().getURL());
            database.put("driver", connection.getMetaData().getDriverName());
        } catch (SQLException e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        health.put("database", database);
        
        // Active profiles
        health.put("profiles", environment.getActiveProfiles());
        
        return ResponseEntity.ok(health);
    }

    private String getDatabaseType() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            String profile = activeProfiles[0];
            switch (profile) {
                case "test":
                    return "H2 (Test)";
                case "dev":
                    return "H2 (Development)";
                case "prod":
                    return "PostgreSQL (Production)";
                default:
                    return "Unknown";
            }
        }
        return "H2 (Default)";
    }
}
