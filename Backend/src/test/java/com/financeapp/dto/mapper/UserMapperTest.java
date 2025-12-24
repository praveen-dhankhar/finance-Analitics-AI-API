package com.financeapp.dto.mapper;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.dto.UserUpdateDto;
import com.financeapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

/**
 * Unit tests for UserMapper
 */
class UserMapperTest {
    
    private UserMapper userMapper;
    
    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }
    
    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID for testing", e);
        }
    }
    
    @Test
    void toResponseDto_validUser_shouldReturnResponseDto() {
        // Given
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        
        setId(user, 1L);
        
        // When
        UserResponseDto result = userMapper.toResponseDto(user);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("john_doe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(user.getUpdatedAt());
    }
    
    @Test
    void toResponseDto_nullUser_shouldReturnNull() {
        // When
        UserResponseDto result = userMapper.toResponseDto(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void toEntity_validRegistrationDto_shouldReturnUser() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto(
            "john_doe",
            "john@example.com",
            "password123"
        );
        
        // When
        User result = userMapper.toEntity(dto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("password123");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void toEntity_nullDto_shouldReturnNull() {
        // When
        User result = userMapper.toEntity(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void updateEntity_validUpdateDto_shouldUpdateUser() {
        // Given
        User user = new User();
        setId(user, 1L);
        user.setUsername("old_username");
        user.setEmail("old@example.com");
        user.setPasswordHash("old_password");
        user.setCreatedAt(OffsetDateTime.now().minusDays(2));
        user.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        UserUpdateDto dto = new UserUpdateDto(
            "new_username",
            "new@example.com",
            "new_password"
        );
        
        // When
        userMapper.updateEntity(user, dto);
        
        // Then
        assertThat(user.getUsername()).isEqualTo("new_username");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("new_password");
        assertThat(user.getUpdatedAt()).isAfter(user.getCreatedAt());
    }
    
    @Test
    void updateEntity_partialUpdateDto_shouldUpdateOnlyProvidedFields() {
        // Given
        User user = new User();
        setId(user, 1L);
        user.setUsername("old_username");
        user.setEmail("old@example.com");
        user.setPasswordHash("old_password");
        user.setCreatedAt(OffsetDateTime.now().minusDays(2));
        user.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        
        UserUpdateDto dto = new UserUpdateDto(
            "new_username",
            null, // Not updating email
            null  // Not updating password
        );
        
        // When
        userMapper.updateEntity(user, dto);
        
        // Then
        assertThat(user.getUsername()).isEqualTo("new_username");
        assertThat(user.getEmail()).isEqualTo("old@example.com"); // Unchanged
        assertThat(user.getPasswordHash()).isEqualTo("old_password"); // Unchanged
        assertThat(user.getUpdatedAt()).isAfter(user.getCreatedAt());
    }
    
    @Test
    void updateEntity_nullUserOrDto_shouldNotThrowException() {
        // Given
        User user = new User();
        UserUpdateDto dto = new UserUpdateDto("username", "email@example.com", "password");
        
        // When & Then
        userMapper.updateEntity(null, dto);
        userMapper.updateEntity(user, null);
        // Should not throw exceptions
    }
    
    @Test
    void toUpdateDto_validUser_shouldReturnUpdateDto() {
        // Given
        User user = new User();
        setId(user, 1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("password123");
        
        // When
        UserUpdateDto result = userMapper.toUpdateDto(user);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("john_doe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.password()).isNull(); // Password should not be included
    }
    
    @Test
    void toUpdateDto_nullUser_shouldReturnNull() {
        // When
        UserUpdateDto result = userMapper.toUpdateDto(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void toResponseDto_invalidUser_shouldThrowException() {
        // Given
        User user = new User();
        setId(user, null); // Invalid: null ID
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        
        // When & Then
        assertThatThrownBy(() -> userMapper.toResponseDto(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID must be positive");
    }
}
