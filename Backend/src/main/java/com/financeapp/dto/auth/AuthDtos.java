package com.financeapp.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public final class AuthDtos {

        public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$";

        @Schema(description = "Request body for user login")
        public record LoginRequest(
                        @Schema(description = "Email address or unique username", example = "john.doe@example.com") @JsonProperty("emailOrUsername") @NotBlank(message = "Email or username is required") String emailOrUsername,

                        @Schema(description = "User password", example = "Password123!") @JsonProperty("password") @NotBlank(message = "Password is required") String password) {
        }

        @Schema(description = "Request body for new user registration")
        public record RegisterRequest(
                        @Schema(description = "Unique username for the account", minLength = 3, maxLength = 50, example = "johndoe") @JsonProperty("username") @NotBlank @Size(min = 3, max = 50) String username,

                        @Schema(description = "Valid email address", maxLength = 100, example = "john.doe@example.com") @JsonProperty("email") @NotBlank @Email @Size(max = 100) String email,

                        @Schema(description = "Secure password containing uppercase, lowercase, number and special char", minLength = 8, example = "SecurePass123!") @JsonProperty("password") @NotBlank @Size(min = 8, max = 100) @Pattern(regexp = PASSWORD_REGEX, message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character") String password) {
        }

        @Schema(description = "Request body to refresh an expired access token")
        public record RefreshRequest(
                        @Schema(description = "The refresh token provided during login or previous refresh") @JsonProperty("refreshToken") @NotBlank String refreshToken) {
        }

        @Schema(description = "Response containing JWT tokens and expiration info")
        public record AuthResponse(
                        @Schema(description = "JWT access token for authenticating subsequent requests") @JsonProperty("accessToken") String accessToken,

                        @Schema(description = "Token used to obtain a new access token without re-logging in") @JsonProperty("refreshToken") String refreshToken,

                        @Schema(description = "Authentication scheme", example = "Bearer") @JsonProperty("tokenType") String tokenType,

                        @Schema(description = "Access token expiration time in milliseconds") @JsonProperty("expiresIn") long expiresIn) {
        }
}
