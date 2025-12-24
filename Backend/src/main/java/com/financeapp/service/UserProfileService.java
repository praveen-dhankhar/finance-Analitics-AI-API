package com.financeapp.service;

import com.financeapp.dto.UserProfileCreateDto;
import com.financeapp.dto.UserProfileDto;
import com.financeapp.dto.UserProfileUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for UserProfile operations
 * Compatible with both H2 and PostgreSQL databases
 */
public interface UserProfileService {

    /**
     * Create a new user profile
     */
    UserProfileDto createProfile(UserProfileCreateDto createDto);

    /**
     * Get user profile by user ID
     */
    Optional<UserProfileDto> getProfileByUserId(Long userId);

    /**
     * Get user profile by profile ID
     */
    Optional<UserProfileDto> getProfileById(Long profileId);

    /**
     * Update user profile
     */
    UserProfileDto updateProfile(Long profileId, UserProfileUpdateDto updateDto);

    /**
     * Update user profile by user ID
     */
    UserProfileDto updateProfileByUserId(Long userId, UserProfileUpdateDto updateDto);

    /**
     * Delete user profile
     */
    void deleteProfile(Long profileId);

    /**
     * Delete user profile by user ID
     */
    void deleteProfileByUserId(Long userId);

    /**
     * Get all public profiles with pagination
     */
    Page<UserProfileDto> getPublicProfiles(Pageable pageable);

    /**
     * Get profiles by location
     */
    List<UserProfileDto> getProfilesByLocation(String location);

    /**
     * Get recently active profiles
     */
    List<UserProfileDto> getRecentlyActiveProfiles(int days);

    /**
     * Get profiles with email notifications enabled
     */
    List<UserProfileDto> getProfilesWithEmailNotifications();

    /**
     * Get profiles with SMS notifications enabled
     */
    List<UserProfileDto> getProfilesWithSmsNotifications();

    /**
     * Get profiles by language
     */
    List<UserProfileDto> getProfilesByLanguage(String language);

    /**
     * Get profiles by timezone
     */
    List<UserProfileDto> getProfilesByTimezone(String timezone);

    /**
     * Get profiles with profile pictures
     */
    List<UserProfileDto> getProfilesWithPictures();

    /**
     * Get profiles with high login count
     */
    List<UserProfileDto> getProfilesWithHighLoginCount(Integer minLoginCount);

    /**
     * Update last login time and increment login count
     */
    void updateLastLogin(Long userId);

    /**
     * Check if email exists for other users
     */
    boolean isEmailAvailable(String email, Long userId);

    /**
     * Get profile statistics
     */
    ProfileStatisticsDto getProfileStatistics();

    /**
     * Export user data for GDPR compliance
     */
    String exportUserData(Long userId);

    /**
     * Search profiles by name or bio
     */
    List<UserProfileDto> searchProfiles(String searchTerm);

    /**
     * Get profiles created after a specific date
     */
    List<UserProfileDto> getProfilesCreatedAfter(String date);

    /**
     * Count active profiles
     */
    long countActiveProfiles();

    /**
     * Count public profiles
     */
    long countPublicProfiles();

    /**
     * Update profile picture URL
     */
    UserProfileDto updateProfilePicture(Long userId, String pictureUrl);

    /**
     * Remove profile picture
     */
    UserProfileDto removeProfilePicture(Long userId);

    /**
     * Update user settings (JSON)
     */
    UserProfileDto updateSettings(Long userId, String settings);

    /**
     * Get user settings (JSON)
     */
    String getSettings(Long userId);

    /**
     * Bulk update notification preferences
     */
    void bulkUpdateNotificationPreferences(List<Long> userIds, NotificationPreferencesDto preferences);

    /**
     * Deactivate user profile
     */
    UserProfileDto deactivateProfile(Long userId);

    /**
     * Reactivate user profile
     */
    UserProfileDto reactivateProfile(Long userId);

    /**
     * Profile statistics DTO
     */
    record ProfileStatisticsDto(
        long totalProfiles,
        long activeProfiles,
        long publicProfiles,
        long profilesWithPictures,
        long profilesWithEmailNotifications,
        long profilesWithSmsNotifications,
        double averageLoginCount,
        long recentlyActiveProfiles
    ) {}

    /**
     * Notification preferences DTO
     */
    record NotificationPreferencesDto(
        Boolean emailNotifications,
        Boolean smsNotifications,
        Boolean marketingEmails
    ) {}
}
