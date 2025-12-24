package com.financeapp.service;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.dto.UserUpdateDto;
import com.financeapp.dto.mapper.UserMapper;
import com.financeapp.entity.User;
import com.financeapp.exception.InvalidPasswordException;
import com.financeapp.exception.UserAlreadyExistsException;
import com.financeapp.exception.UserNotFoundException;
import com.financeapp.exception.ValidationException;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponseDto testUserResponseDto;
    private UserRegistrationDto testRegistrationDto;
    private UserUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        // ID will be set by the database when saved
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encoded_password");
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());

        testUserResponseDto = new UserResponseDto(
                1L, "testuser", "test@example.com", 
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        testRegistrationDto = new UserRegistrationDto(
                "newuser", "new@example.com", "ValidPass123!"
        );

        testUpdateDto = new UserUpdateDto(
                "updateduser", "updated@example.com", null
        );
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(testRegistrationDto.password());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testRegistrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username already exists")
    void registerUser_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testRegistrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for null registration data")
    void registerUser_NullData() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Registration data is required");
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid username length")
    void registerUser_InvalidUsernameLength() {
        // Given
        UserRegistrationDto invalidDto = new UserRegistrationDto(
                "ab", "test@example.com", "ValidPass123!"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid email format")
    void registerUser_InvalidEmailFormat() {
        // Given
        UserRegistrationDto invalidDto = new UserRegistrationDto(
                "testuser", "invalid-email", "ValidPass123!"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Should throw ValidationException for weak password")
    void registerUser_WeakPassword() {
        // Given
        UserRegistrationDto invalidDto = new UserRegistrationDto(
                "testuser", "test@example.com", "weak"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void authenticateUser_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.authenticateUser("test@example.com", "password");

        // Then
        assertThat(result).isNotNull();
        verify(passwordEncoder).matches("password", testUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found during authentication")
    void authenticateUser_UserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.authenticateUser("nonexistent@example.com", "password"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when password is incorrect")
    void authenticateUser_InvalidPassword() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.authenticateUser("test@example.com", "wrongpassword"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    @DisplayName("Should throw ValidationException when email or password is empty")
    void authenticateUser_EmptyCredentials() {
        // When & Then
        assertThatThrownBy(() -> userService.authenticateUser("", "password"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email and password are required");

        assertThatThrownBy(() -> userService.authenticateUser("test@example.com", ""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email and password are required");
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void getUserById_UserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw ValidationException when email is empty")
    void getUserByEmail_EmptyEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void updateUserProfile_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.updateUserProfile(1L, testUpdateDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when updating to existing email")
    void updateUserProfile_EmailAlreadyExists() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUserProfile(1L, testUpdateDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should update password successfully")
    void updatePassword_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.updatePassword(1L, "currentPass", "NewPass123!");

        // Then
        assertThat(result).isTrue();
        verify(passwordEncoder).encode("NewPass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when current password is wrong")
    void updatePassword_InvalidCurrentPassword() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(1L, "wrongPass", "NewPass123!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid current password");
    }

    @Test
    @DisplayName("Should throw ValidationException when new password is weak")
    void updatePassword_WeakNewPassword() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(1L, "currentPass", "weak"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should check email existence correctly")
    void emailExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When & Then
        assertThat(userService.emailExists("test@example.com")).isTrue();
        assertThat(userService.emailExists("nonexistent@example.com")).isFalse();
        assertThat(userService.emailExists("")).isFalse();
        assertThat(userService.emailExists(null)).isFalse();
    }

    @Test
    @DisplayName("Should check username existence correctly")
    void usernameExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When & Then
        assertThat(userService.usernameExists("testuser")).isTrue();
        assertThat(userService.usernameExists("nonexistent")).isFalse();
        assertThat(userService.usernameExists("")).isFalse();
        assertThat(userService.usernameExists(null)).isFalse();
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void getAllUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should search users by criteria")
    void searchUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findUsersByCriteria(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(testUserResponseDto);

        // When
        Page<UserResponseDto> result = userService.searchUsers("test", "test@example.com", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Success() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(true);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void deleteUser_UserNotFound() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get user statistics successfully")
    void getUserStatistics_Success() {
        // Given
        Object[] stats = {1L, 5, 4, OffsetDateTime.now()};
        List<Object[]> statsList = List.<Object[]>of(stats);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.getUserStatistics(anyLong())).thenReturn(statsList);

        // When
        Object[] result = userService.getUserStatistics(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(stats);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting statistics for non-existent user")
    void getUserStatistics_UserNotFound() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.getUserStatistics(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
