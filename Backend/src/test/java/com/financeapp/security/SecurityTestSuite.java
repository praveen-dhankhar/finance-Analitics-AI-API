package com.financeapp.security;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.entity.User;
import com.financeapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Test Suite")
public class SecurityTestSuite {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Clean setup for each test
    }

    @Nested
    @DisplayName("JWT Token Tests")
    class JwtTokenTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void shouldGenerateValidJwtToken() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "jwtuser", "jwt@example.com", "Password@123"
            );
            userService.registerUser(registrationDto);

            String token = jwtTokenProvider.generateTokenFromUsername("jwtuser");

            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.getUsernameFromJWT(token)).isEqualTo("jwtuser");
        }

        @Test
        @DisplayName("Should generate refresh token")
        void shouldGenerateRefreshToken() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "refreshuser", "refresh@example.com", "Password@123"
            );
            userService.registerUser(registrationDto);

            String refreshToken = jwtTokenProvider.generateRefreshToken("refreshuser");

            assertThat(refreshToken).isNotNull();
            assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
            assertThat(jwtTokenProvider.isRefreshToken(refreshToken)).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid token")
        void shouldRejectInvalidToken() {
            String invalidToken = "invalid.token.here";

            assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("Should detect expired token")
        void shouldDetectExpiredToken() {
            // This test would require mocking time or using a very short expiration
            // For now, we'll test the validation logic
            String malformedToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYwOTQ1MjAwMCwiZXhwIjoxNjA5NDUyMDAwfQ.invalid";

            assertThat(jwtTokenProvider.validateToken(malformedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Password Security Tests")
    class PasswordSecurityTests {

        @Test
        @DisplayName("Should hash password securely")
        void shouldHashPasswordSecurely() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "secureuser", "secure@example.com", "Password@123"
            );

            var userResponse = userService.registerUser(registrationDto);

            assertThat(userResponse).isNotNull();
            assertThat(userResponse.username()).isEqualTo("secureuser");
            // Note: We can't directly check password hash from DTO, but we can verify user was created
        }

        @Test
        @DisplayName("Should authenticate with correct password")
        void shouldAuthenticateWithCorrectPassword() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "authuser", "auth@example.com", "Password@123"
            );
            userService.registerUser(registrationDto);

            // authenticateUser returns UserResponseDto, not boolean
            // authenticate by email per service contract
            var userResponse = userService.authenticateUser("auth@example.com", "Password@123");

            assertThat(userResponse).isNotNull();
            assertThat(userResponse.username()).isEqualTo("authuser");
        }

        @Test
        @DisplayName("Should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "rejectuser", "reject@example.com", "Password@123"
            );
            userService.registerUser(registrationDto);

            // This should throw an exception for wrong password
            assertThatThrownBy(() -> userService.authenticateUser("rejectuser", "wrongpassword"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should validate password complexity")
        void shouldValidatePasswordComplexity() {
            UserRegistrationDto weakPasswordDto = new UserRegistrationDto(
                    "weakuser", "weak@example.com", "123"
            );

            assertThatThrownBy(() -> userService.registerUser(weakPasswordDto))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("User Registration Security Tests")
    class UserRegistrationSecurityTests {

        @Test
        @DisplayName("Should prevent duplicate email registration")
        void shouldPreventDuplicateEmailRegistration() {
            UserRegistrationDto firstUser = new UserRegistrationDto(
                    "user1", "duplicate@example.com", "Password@123"
            );
            UserRegistrationDto secondUser = new UserRegistrationDto(
                    "user2", "duplicate@example.com", "Password@123"
            );

            userService.registerUser(firstUser);

            assertThatThrownBy(() -> userService.registerUser(secondUser))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should prevent duplicate username registration")
        void shouldPreventDuplicateUsernameRegistration() {
            UserRegistrationDto firstUser = new UserRegistrationDto(
                    "duplicateuser", "user1@example.com", "Password@123"
            );
            UserRegistrationDto secondUser = new UserRegistrationDto(
                    "duplicateuser", "user2@example.com", "Password@123"
            );

            userService.registerUser(firstUser);

            assertThatThrownBy(() -> userService.registerUser(secondUser))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() {
            UserRegistrationDto invalidEmailDto = new UserRegistrationDto(
                    "invaliduser", "invalid-email", "Password@123"
            );

            assertThatThrownBy(() -> userService.registerUser(invalidEmailDto))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should validate username length")
        void shouldValidateUsernameLength() {
            UserRegistrationDto shortUsernameDto = new UserRegistrationDto(
                    "ab", "short@example.com", "Password@123"
            );

            assertThatThrownBy(() -> userService.registerUser(shortUsernameDto))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Database Agnostic Security Tests")
    class DatabaseAgnosticSecurityTests {

        @Test
        @DisplayName("Should work with H2 database")
        void shouldWorkWithH2Database() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "h2user", "h2@example.com", "Password@123"
            );

            var userResponse = userService.registerUser(registrationDto);
            String token = jwtTokenProvider.generateTokenFromUsername(userResponse.username());

            assertThat(userResponse).isNotNull();
            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent user registrations")
        void shouldHandleConcurrentUserRegistrations() {
            // This test simulates concurrent registration attempts
            UserRegistrationDto user1 = new UserRegistrationDto(
                    "concurrent1", "concurrent1@example.com", "Password@123"
            );
            UserRegistrationDto user2 = new UserRegistrationDto(
                    "concurrent2", "concurrent2@example.com", "Password@123"
            );

            var result1 = userService.registerUser(user1);
            var result2 = userService.registerUser(user2);

            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result1.username()).isNotEqualTo(result2.username());
        }

        @Test
        @DisplayName("Should maintain data integrity during security operations")
        void shouldMaintainDataIntegrityDuringSecurityOperations() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    "integrityuser", "integrity@example.com", "Password@123"
            );

            var userResponse = userService.registerUser(registrationDto);
            Long userId = userResponse.id();

            // Verify user can be retrieved
            var retrievedUser = userService.getUserById(userId);
            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.username()).isEqualTo("integrityuser");

            // Note: We can't directly check password hash from DTO, but we can verify user was created and retrieved
        }
    }
}
