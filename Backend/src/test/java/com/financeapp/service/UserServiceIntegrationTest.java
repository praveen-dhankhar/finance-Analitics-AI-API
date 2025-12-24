package com.financeapp.service;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.dto.UserUpdateDto;
import com.financeapp.entity.User;
import com.financeapp.exception.InvalidPasswordException;
import com.financeapp.exception.UserAlreadyExistsException;
import com.financeapp.exception.UserNotFoundException;
import com.financeapp.exception.ValidationException;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import com.financeapp.testsupport.TestDatabaseCleaner;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for UserService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseCleaner cleaner;

    private User testUser;
    private UserRegistrationDto testRegistrationDto;
    private UserUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        // Clean all tables in FK-safe order to avoid cross-class residue
        cleaner.clean();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("TestPass123!"));
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        testUser = userRepository.save(testUser);

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
        // When
        UserResponseDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("newuser");
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.id()).isNotNull();

        // Verify user was saved to database
        User savedUser = userRepository.findById(result.id()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(passwordEncoder.matches("ValidPass123!", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void registerUser_EmailAlreadyExists() {
        // Given
        UserRegistrationDto duplicateEmailDto = new UserRegistrationDto(
                "differentuser", "test@example.com", "ValidPass123!"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(duplicateEmailDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username already exists")
    void registerUser_UsernameAlreadyExists() {
        // Given
        UserRegistrationDto duplicateUsernameDto = new UserRegistrationDto(
                "testuser", "different@example.com", "ValidPass123!"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(duplicateUsernameDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
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
        // When
        UserResponseDto result = userService.authenticateUser("test@example.com", "TestPass123!");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found during authentication")
    void authenticateUser_UserNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.authenticateUser("nonexistent@example.com", "password"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when password is incorrect")
    void authenticateUser_InvalidPassword() {
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
        // When
        UserResponseDto result = userService.getUserById(testUser.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testUser.getId());
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void getUserById_UserNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmail_Success() {
        // When
        UserResponseDto result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.username()).isEqualTo("testuser");
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
    @DisplayName("Should get user by username successfully")
    void getUserByUsername_Success() {
        // When
        UserResponseDto result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void updateUserProfile_Success() {
        // When
        UserResponseDto result = userService.updateUserProfile(testUser.getId(), testUpdateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("updateduser");
        assertThat(result.email()).isEqualTo("updated@example.com");

        // Verify user was updated in database
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("updateduser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when updating to existing email")
    void updateUserProfile_EmailAlreadyExists() {
        // Given - Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash(passwordEncoder.encode("AnotherPass123!"));
        anotherUser.setCreatedAt(OffsetDateTime.now());
        anotherUser.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(anotherUser);

        UserUpdateDto duplicateEmailDto = new UserUpdateDto(
                "updateduser", "another@example.com", null
        );

        // When & Then
        assertThatThrownBy(() -> userService.updateUserProfile(testUser.getId(), duplicateEmailDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should update password successfully")
    void updatePassword_Success() {
        // When
        boolean result = userService.updatePassword(testUser.getId(), "TestPass123!", "NewPass123!");

        // Then
        assertThat(result).isTrue();

        // Verify password was updated in database
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("NewPass123!", updatedUser.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("TestPass123!", updatedUser.getPasswordHash())).isFalse();
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when current password is wrong")
    void updatePassword_InvalidCurrentPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), "wrongpass", "NewPass123!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid current password");
    }

    @Test
    @DisplayName("Should throw ValidationException when new password is weak")
    void updatePassword_WeakNewPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), "TestPass123!", "weak"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    @DisplayName("Should check email existence correctly")
    void emailExists() {
        // When & Then
        assertThat(userService.emailExists("test@example.com")).isTrue();
        assertThat(userService.emailExists("nonexistent@example.com")).isFalse();
        assertThat(userService.emailExists("")).isFalse();
        assertThat(userService.emailExists(null)).isFalse();
    }

    @Test
    @DisplayName("Should check username existence correctly")
    void usernameExists() {
        // When & Then
        assertThat(userService.usernameExists("testuser")).isTrue();
        assertThat(userService.usernameExists("nonexistent")).isFalse();
        assertThat(userService.usernameExists("")).isFalse();
        assertThat(userService.usernameExists(null)).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all users with pagination")
    void getAllUsers() {
        // Given - Create additional users
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash(passwordEncoder.encode("Pass123!"));
        user2.setCreatedAt(OffsetDateTime.now());
        user2.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should search users by criteria")
    void searchUsers() {
        // Given - Create additional users
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash(passwordEncoder.encode("Pass123!"));
        user2.setCreatedAt(OffsetDateTime.now());
        user2.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponseDto> result = userService.searchUsers("user", null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(user -> user.username().contains("user"));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Success() {
        // When
        boolean result = userService.deleteUser(testUser.getId());

        // Then
        assertThat(result).isTrue();

        // Verify user was deleted from database
        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void deleteUser_UserNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should get user statistics successfully")
    void getUserStatistics_Success() {
        // When
        Object[] stats = userService.getUserStatistics(testUser.getId());

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.length).isEqualTo(4);
        assertThat(stats[0]).isEqualTo(testUser.getId()); // userId
        assertThat(stats[1]).isEqualTo(0); // financialDataCount
        assertThat(stats[2]).isEqualTo(0); // forecastCount
        assertThat(stats[3]).isNotNull(); // lastActivity
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting statistics for non-existent user")
    void getUserStatistics_UserNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserStatistics(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle multiple user registrations and retrievals")
    void multipleUserOperations() {
        // Given - Register multiple users
        UserRegistrationDto user1Dto = new UserRegistrationDto("user1", "user1@example.com", "Pass123!");
        UserRegistrationDto user2Dto = new UserRegistrationDto("user2", "user2@example.com", "ValidPass456!");
        UserRegistrationDto user3Dto = new UserRegistrationDto("user3", "user3@example.com", "ValidPass789!");

        // When
        UserResponseDto user1 = userService.registerUser(user1Dto);
        UserResponseDto user2 = userService.registerUser(user2Dto);
        UserResponseDto user3 = userService.registerUser(user3Dto);

        // Then
        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(user3).isNotNull();
        assertThat(user1.username()).isEqualTo("user1");
        assertThat(user2.username()).isEqualTo("user2");
        assertThat(user3.username()).isEqualTo("user3");

        // Verify all users can be retrieved
        assertThat(userService.getUserById(user1.id())).isNotNull();
        assertThat(userService.getUserById(user2.id())).isNotNull();
        assertThat(userService.getUserById(user3.id())).isNotNull();

        // Verify pagination works
        Pageable pageable = PageRequest.of(0, 5);
        Page<UserResponseDto> allUsers = userService.getAllUsers(pageable);
        assertThat(allUsers.getTotalElements()).isEqualTo(4); // Including the test user from setUp
    }

    @Test
    @DisplayName("Should handle password complexity requirements")
    void passwordComplexityRequirements() {
        // Test various password scenarios
        List<String> validPasswords = List.of(
                "ValidPass123!", "Complex@456", "Strong@789", "Secure$012"
        );

        List<String> invalidPasswords = List.of(
                "weak", "12345678", "abcdefgh", "ABCDEFGH", "!@#$%^&*"
        );

        // Valid passwords should work
        for (String password : validPasswords) {
            UserRegistrationDto validDto = new UserRegistrationDto("user" + password.hashCode(), 
                    "user" + password.hashCode() + "@example.com", password);
            UserResponseDto result = userService.registerUser(validDto);
            assertThat(result).isNotNull();
        }

        // Invalid passwords should fail
        for (String password : invalidPasswords) {
            UserRegistrationDto invalidDto = new UserRegistrationDto("user" + password.hashCode(), 
                    "user" + password.hashCode() + "@example.com", password);
            assertThatThrownBy(() -> userService.registerUser(invalidDto))
                    .isInstanceOf(ValidationException.class);
        }
    }
}
