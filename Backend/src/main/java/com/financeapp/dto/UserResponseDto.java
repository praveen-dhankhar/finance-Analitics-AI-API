package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * DTO for user response data
 */
public record UserResponseDto(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("username")
    String username,
    
    @JsonProperty("email")
    String email,
    
    @JsonProperty("createdAt")
    OffsetDateTime createdAt,
    
    @JsonProperty("updatedAt")
    OffsetDateTime updatedAt
) {
    public UserResponseDto {
        // Validation in constructor
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }
}
