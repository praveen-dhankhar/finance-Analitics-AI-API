package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for user update requests
 */
public record UserUpdateDto(
    @JsonProperty("username")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @JsonProperty("email")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,
    
    @JsonProperty("password")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password
) {
    public UserUpdateDto {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
    
    /**
     * Check if any field is being updated
     */
    public boolean hasUpdates() {
        return username != null || email != null || password != null;
    }
}
