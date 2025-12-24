package com.financeapp.dto.mapper;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.dto.UserUpdateDto;
import com.financeapp.entity.User;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mapper utility for User entity and DTOs
 */
@Component
public class UserMapper {
    
    /**
     * Convert User entity to UserResponseDto
     */
    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    /**
     * Convert UserRegistrationDto to User entity
     */
    public User toEntity(UserRegistrationDto dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(dto.password()); // Note: This should be hashed in service layer
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        
        return user;
    }
    
    /**
     * Update User entity with UserUpdateDto
     */
    public void updateEntity(User user, UserUpdateDto dto) {
        if (user == null || dto == null) {
            return;
        }
        
        if (dto.username() != null) {
            user.setUsername(dto.username());
        }
        if (dto.email() != null) {
            user.setEmail(dto.email());
        }
        if (dto.password() != null) {
            user.setPasswordHash(dto.password()); // Note: This should be hashed in service layer
        }
        user.setUpdatedAt(OffsetDateTime.now());
    }
    
    /**
     * Create UserUpdateDto from User entity
     */
    public UserUpdateDto toUpdateDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserUpdateDto(
            user.getUsername(),
            user.getEmail(),
            null // Never include password in update DTO
        );
    }
}
