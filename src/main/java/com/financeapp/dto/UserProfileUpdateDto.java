package com.financeapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * DTO for updating UserProfile
 * Compatible with both H2 and PostgreSQL databases
 */
public record UserProfileUpdateDto(
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName,
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName,
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phoneNumber,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth,
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    String location,
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,
    
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    String timezone,
    
    @Size(max = 10, message = "Language must not exceed 10 characters")
    String language,
    
    Boolean isPublic,
    
    Boolean emailNotifications,
    
    Boolean smsNotifications,
    
    Boolean marketingEmails,
    
    String settings
) {
    public boolean hasAnyField() {
        return firstName != null || lastName != null || email != null || 
               phoneNumber != null || dateOfBirth != null || location != null || 
               bio != null || timezone != null || language != null || 
               isPublic != null || emailNotifications != null || 
               smsNotifications != null || marketingEmails != null || 
               settings != null;
    }
}
