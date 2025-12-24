package com.financeapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * PostgreSQL-specific security configuration.
 * This class contains security settings optimized for PostgreSQL database.
 * 
 * Key differences from H2:
 * - Connection pooling configuration
 * - SSL/TLS settings for production
 * - Database-specific security headers
 * - Performance optimizations for PostgreSQL
 * 
 * See postgresql-optimizations.md for detailed indexing and performance strategies.
 */
@Configuration
@Profile("prod")
@PropertySource("classpath:postgresql-security.properties")
public class PostgreSQLSecurityConfig {

    // PostgreSQL-specific security configurations will be added here
    // when switching from H2 to PostgreSQL in production
    
    // Example configurations that would be added:
    // - SSL connection requirements
    // - Connection pool security settings
    // - Database-specific audit configurations
    // - Performance monitoring for PostgreSQL
    
    // See postgresql-optimizations.md for:
    // - Database indexing strategies for auth tables
    // - Connection pool optimizations
    // - Query performance tuning
    // - Security hardening recommendations
    // - Load testing guidelines
}
