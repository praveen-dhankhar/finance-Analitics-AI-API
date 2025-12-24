package com.financeapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI financeForecastOpenAPI() {
        String dbEnv = switch (activeProfile) {
            case "dev", "test" -> "H2 (in-memory)";
            case "integration-test", "staging", "prod" -> "PostgreSQL";
            default -> "H2 (default)";
        };
        String serverUrl = switch (activeProfile) {
            case "dev", "test" -> "http://localhost:8080";
            case "staging" -> "https://staging.yourdomain.com";
            case "prod" -> "https://api.yourdomain.com";
            default -> "http://localhost:8080";
        };
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Forecast API")
                        .description("Comprehensive API documentation. Database environment: " + dbEnv +
                                "\n\nPerformance notes: H2 is optimized for dev/test speed; PostgreSQL for production scale.\n" +
                                "See /actuator/metrics and /actuator/health for environment-specific metrics and health checks.")
                        .version("v1")
                        .contact(new Contact().name("Finance Team").email("support@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Environment & DB usage guide")
                        .url("https://example.com/docs/db-environments"))
                .addServersItem(new Server().url(serverUrl).description("Active profile: " + activeProfile))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addTagsItem(new Tag().name("forecasts").description("Forecasting endpoints (DB-aware)"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}


