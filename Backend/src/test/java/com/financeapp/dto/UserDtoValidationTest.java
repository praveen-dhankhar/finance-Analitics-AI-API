package com.financeapp.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User DTO validation
 */
class UserDtoValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void userRegistrationDto_validData_shouldPassValidation() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto(
            "john_doe",
            "john@example.com",
            "password123"
        );
        
        // When
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void userRegistrationDto_invalidData_shouldFailValidation() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto(
            "", // Invalid: empty username (triggers both @NotBlank and @Size)
            "invalid-email", // Invalid: not a valid email
            "123" // Invalid: password too short
        );
        
        // When
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(4); // Empty string triggers both @NotBlank and @Size
        assertThat(violations).extracting("message")
            .contains(
                "Username is required",
                "Username must be between 3 and 50 characters",
                "Email must be valid",
                "Password must be between 8 and 100 characters"
            );
    }
    
    @Test
    void userRegistrationDto_usernameTooLong_shouldFailValidation() {
        // Given
        String longUsername = "a".repeat(51);
        UserRegistrationDto dto = new UserRegistrationDto(
            longUsername,
            "john@example.com",
            "password123"
        );
        
        // When
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username must be between 3 and 50 characters");
    }
    
    @Test
    void userUpdateDto_validData_shouldPassValidation() {
        // Given
        UserUpdateDto dto = new UserUpdateDto(
            "john_doe_updated",
            "john.updated@example.com",
            "newpassword123"
        );
        
        // When
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void userUpdateDto_partialData_shouldPassValidation() {
        // Given
        UserUpdateDto dto = new UserUpdateDto(
            "john_doe_updated",
            null, // Optional field
            null  // Optional field
        );
        
        // When
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void userUpdateDto_invalidEmail_shouldFailValidation() {
        // Given
        UserUpdateDto dto = new UserUpdateDto(
            "john_doe",
            "invalid-email",
            "password123"
        );
        
        // When
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email must be valid");
    }
    
    @Test
    void userUpdateDto_hasUpdates_shouldReturnTrue() {
        // Given
        UserUpdateDto dto = new UserUpdateDto(
            "john_doe_updated",
            "john@example.com",
            "newpassword123"
        );
        
        // When
        boolean hasUpdates = dto.hasUpdates();
        
        // Then
        assertThat(hasUpdates).isTrue();
    }
    
    @Test
    void userUpdateDto_noUpdates_shouldReturnFalse() {
        // Given
        UserUpdateDto dto = new UserUpdateDto(null, null, null);
        
        // When
        boolean hasUpdates = dto.hasUpdates();
        
        // Then
        assertThat(hasUpdates).isFalse();
    }
}
