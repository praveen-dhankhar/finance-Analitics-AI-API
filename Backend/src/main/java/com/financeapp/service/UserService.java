package com.financeapp.service;

import com.financeapp.dto.UserRegistrationDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.dto.UserUpdateDto;
import com.financeapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations
 */
public interface UserService {

    /**
     * Register a new user with encrypted password
     * @param registrationDto user registration data
     * @return created user response
     * @throws com.financeapp.exception.UserAlreadyExistsException if email/username already exists
     * @throws com.financeapp.exception.ValidationException if validation fails
     */
    UserResponseDto registerUser(UserRegistrationDto registrationDto);

    /**
     * Authenticate user with email and password
     * @param email user email
     * @param password plain text password
     * @return authenticated user response
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     * @throws com.financeapp.exception.InvalidPasswordException if password is incorrect
     */
    UserResponseDto authenticateUser(String email, String password);

    /**
     * Get user by ID
     * @param userId user ID
     * @return user response
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     */
    UserResponseDto getUserById(Long userId);

    /**
     * Get user by email
     * @param email user email
     * @return user response
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     */
    UserResponseDto getUserByEmail(String email);

    /**
     * Get user by username
     * @param username username
     * @return user response
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     */
    UserResponseDto getUserByUsername(String username);

    /**
     * Update user profile
     * @param userId user ID
     * @param updateDto update data
     * @return updated user response
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     * @throws com.financeapp.exception.ValidationException if validation fails
     */
    UserResponseDto updateUserProfile(Long userId, UserUpdateDto updateDto);

    /**
     * Update user password
     * @param userId user ID
     * @param currentPassword current password
     * @param newPassword new password
     * @return success status
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     * @throws com.financeapp.exception.InvalidPasswordException if current password is incorrect
     * @throws com.financeapp.exception.ValidationException if new password validation fails
     */
    boolean updatePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Check if email exists
     * @param email email to check
     * @return true if email exists
     */
    boolean emailExists(String email);

    /**
     * Check if username exists
     * @param username username to check
     * @return true if username exists
     */
    boolean usernameExists(String username);

    /**
     * Get all users with pagination
     * @param pageable pagination information
     * @return page of users
     */
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    /**
     * Search users by criteria
     * @param username partial username (optional)
     * @param email partial email (optional)
     * @param pageable pagination information
     * @return page of matching users
     */
    Page<UserResponseDto> searchUsers(String username, String email, Pageable pageable);

    /**
     * Delete user by ID
     * @param userId user ID
     * @return success status
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     */
    boolean deleteUser(Long userId);

    /**
     * Get user statistics
     * @param userId user ID
     * @return user statistics
     * @throws com.financeapp.exception.UserNotFoundException if user not found
     */
    Object[] getUserStatistics(Long userId);
}
