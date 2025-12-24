package com.financeapp.security;

import com.financeapp.config.TestContainersConfig;
import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test-postgres")
@Transactional
@Disabled("Enable with -Dspring.profiles.active=test-postgres to run with TestContainers")
public class PostgreSQLSecurityTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        // This will be handled by @Transactional
    }

    @Test
    void testUserRegistrationWithPostgreSQL() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "postgresuser", "postgres@example.com", "Password@123"
        );

        var userResponse = userService.registerUser(registrationDto);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.username()).isEqualTo("postgresuser");
        assertThat(userResponse.email()).isEqualTo("postgres@example.com");
        // Note: We can't directly check password hash from DTO
    }

    @Test
    void testJwtTokenGenerationWithPostgreSQL() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "jwtuser", "jwt@example.com", "Password@123"
        );

        var userResponse = userService.registerUser(registrationDto);
        String token = jwtTokenProvider.generateTokenFromUsername(userResponse.username());

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromJWT(token)).isEqualTo("jwtuser");
    }

    @Test
    void testPasswordEncryptionWithPostgreSQL() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "encryptuser", "encrypt@example.com", "Password@123"
        );

        var userResponse = userService.registerUser(registrationDto);

        // Verify user was created
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.username()).isEqualTo("encryptuser");

        // Verify password can be authenticated
        var authResponse = userService.authenticateUser("encryptuser", "Password@123");
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.username()).isEqualTo("encryptuser");
    }

    @Test
    void testUserAuthenticationWithPostgreSQL() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "authuser", "auth@example.com", "Password@123"
        );

        userService.registerUser(registrationDto);

        // Test successful authentication
        var authResponse = userService.authenticateUser("authuser", "Password@123");
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.username()).isEqualTo("authuser");

        // Test failed authentication - this should throw an exception
        try {
            userService.authenticateUser("authuser", "wrongpassword");
            // If we get here, the test should fail
            assertThat(false).isTrue();
        } catch (Exception ex) {
            // Expected exception
            assertThat(ex).isNotNull();
        }
    }

    @Test
    void testRefreshTokenWithPostgreSQL() {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "refreshuser", "refresh@example.com", "Password@123"
        );

        var userResponse = userService.registerUser(registrationDto);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userResponse.username());

        assertThat(refreshToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromJWT(refreshToken)).isEqualTo("refreshuser");
    }
}
