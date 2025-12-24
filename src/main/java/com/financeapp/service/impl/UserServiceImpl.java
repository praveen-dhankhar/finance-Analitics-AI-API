package com.financeapp.service.impl;

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
import com.financeapp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

/**
 * Implementation of UserService with comprehensive business logic
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // Validate input data first
        validateRegistrationData(registrationDto);
        
        logger.info("Registering new user with email: {}", registrationDto.email());
        
        // Check if user already exists
        if (emailExists(registrationDto.email())) {
            logger.warn("Registration failed: Email already exists: {}", registrationDto.email());
            throw new UserAlreadyExistsException("Email already exists: " + registrationDto.email());
        }
        
        if (usernameExists(registrationDto.username())) {
            logger.warn("Registration failed: Username already exists: {}", registrationDto.username());
            throw new UserAlreadyExistsException("Username already exists: " + registrationDto.username());
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registrationDto.username());
        user.setEmail(registrationDto.email());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.password()));
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        
        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public UserResponseDto authenticateUser(String email, String password) {
        logger.debug("Authenticating user with email: {}", email);
        
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            logger.warn("Authentication failed: Invalid email or password");
            throw new ValidationException("Email and password are required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: Invalid password for user: {}", email);
            throw new InvalidPasswordException("Invalid password");
        }
        
        logger.info("User authenticated successfully: {}", email);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        logger.debug("Getting user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email is required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
        
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        
        if (!StringUtils.hasText(username)) {
            throw new ValidationException("Username is required");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });
        
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateUserProfile(Long userId, UserUpdateDto updateDto) {
        logger.info("Updating user profile for ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        
        // Validate update data
        validateUpdateData(updateDto);
        
        // Check for conflicts if email/username is being updated
        if (StringUtils.hasText(updateDto.email()) && !updateDto.email().equals(user.getEmail())) {
            if (emailExists(updateDto.email())) {
                logger.warn("Profile update failed: Email already exists: {}", updateDto.email());
                throw new UserAlreadyExistsException("Email already exists: " + updateDto.email());
            }
        }
        
        if (StringUtils.hasText(updateDto.username()) && !updateDto.username().equals(user.getUsername())) {
            if (usernameExists(updateDto.username())) {
                logger.warn("Profile update failed: Username already exists: {}", updateDto.username());
                throw new UserAlreadyExistsException("Username already exists: " + updateDto.username());
            }
        }
        
        // Update user fields
        userMapper.updateEntity(user, updateDto);
        user.setUpdatedAt(OffsetDateTime.now());
        
        User updatedUser = userRepository.save(user);
        logger.info("User profile updated successfully for ID: {}", userId);
        
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Updating password for user ID: {}", userId);
        
        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            throw new ValidationException("Current password and new password are required");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            logger.warn("Password update failed: Invalid current password for user ID: {}", userId);
            throw new InvalidPasswordException("Invalid current password");
        }
        
        // Validate new password
        validatePassword(newPassword);
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        
        logger.info("Password updated successfully for user ID: {}", userId);
        return true;
    }

    @Override
    public boolean emailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean usernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        logger.debug("Getting all users with pagination: {}", pageable);
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> searchUsers(String username, String email, Pageable pageable) {
        logger.debug("Searching users with criteria - username: {}, email: {}", username, email);
        
        Page<User> users = userRepository.findUsersByCriteria(username, email, pageable);
        return users.map(userMapper::toResponseDto);
    }

    @Override
    public boolean deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            logger.warn("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        logger.info("User deleted successfully with ID: {}", userId);
        return true;
    }

    @Override
    public Object[] getUserStatistics(Long userId) {
        logger.debug("Getting user statistics for ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            logger.warn("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        return userRepository.getUserStatistics(userId).get(0);
    }

    // Private validation methods

    private void validateRegistrationData(UserRegistrationDto dto) {
        if (dto == null) {
            throw new ValidationException("Registration data is required");
        }
        
        if (!StringUtils.hasText(dto.username())) {
            throw new ValidationException("Username is required");
        }
        
        if (dto.username().length() < 3 || dto.username().length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }
        
        if (!StringUtils.hasText(dto.email())) {
            throw new ValidationException("Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(dto.email()).matches()) {
            throw new ValidationException("Invalid email format");
        }
        
        if (!StringUtils.hasText(dto.password())) {
            throw new ValidationException("Password is required");
        }
        
        validatePassword(dto.password());
    }

    private void validateUpdateData(UserUpdateDto dto) {
        if (dto == null) {
            throw new ValidationException("Update data is required");
        }
        
        if (StringUtils.hasText(dto.username()) && (dto.username().length() < 3 || dto.username().length() > 50)) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }
        
        if (StringUtils.hasText(dto.email()) && !EMAIL_PATTERN.matcher(dto.email()).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Password is required");
        }
        
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character");
        }
    }
}
