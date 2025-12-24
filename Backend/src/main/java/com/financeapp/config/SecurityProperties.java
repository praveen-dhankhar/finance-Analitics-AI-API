package com.financeapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Database database = new Database();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public static class Jwt {
        private String secret;
        private long expiration = 86400000; // 24 hours
        private long refreshExpiration = 604800000; // 7 days
        private String issuer = "finance-forecast-app";
        private String audience = "finance-forecast-app-users";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }

        public long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
        private List<String> allowedHeaders = List.of("*");
        private boolean allowCredentials = true;
        private long maxAge = 3600;

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class Database {
        private boolean enableAuditing = true;
        private boolean enableSoftDelete = false;
        private int maxConnections = 10;
        private int minConnections = 2;
        private long connectionTimeout = 30000; // 30 seconds
        private long idleTimeout = 600000; // 10 minutes

        public boolean isEnableAuditing() {
            return enableAuditing;
        }

        public void setEnableAuditing(boolean enableAuditing) {
            this.enableAuditing = enableAuditing;
        }

        public boolean isEnableSoftDelete() {
            return enableSoftDelete;
        }

        public void setEnableSoftDelete(boolean enableSoftDelete) {
            this.enableSoftDelete = enableSoftDelete;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public int getMinConnections() {
            return minConnections;
        }

        public void setMinConnections(int minConnections) {
            this.minConnections = minConnections;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }
    }
}
