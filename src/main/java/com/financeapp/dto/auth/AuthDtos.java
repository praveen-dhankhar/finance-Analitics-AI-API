package com.financeapp.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$";

    public record LoginRequest(
            @JsonProperty("emailOrUsername") @NotBlank(message = "Email or username is required") String emailOrUsername,
            @JsonProperty("password") @NotBlank(message = "Password is required") String password
    ) {}

    public record RegisterRequest(
            @JsonProperty("username") @NotBlank @Size(min = 3, max = 50) String username,
            @JsonProperty("email") @NotBlank @Email @Size(max = 100) String email,
            @JsonProperty("password") @NotBlank @Size(min = 8, max = 100)
            @Pattern(regexp = PASSWORD_REGEX, message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character") String password
    ) {}

    public record RefreshRequest(
            @JsonProperty("refreshToken") @NotBlank String refreshToken
    ) {}

    public record AuthResponse(
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("tokenType") String tokenType,
            @JsonProperty("expiresIn") long expiresIn
    ) {}
}


