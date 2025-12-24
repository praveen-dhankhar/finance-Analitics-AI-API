package com.financeapp.service.impl;

import com.financeapp.dto.UserProfileCreateDto;
import com.financeapp.dto.UserProfileDto;
import com.financeapp.dto.UserProfileUpdateDto;
import com.financeapp.dto.mapper.UserProfileMapper;
import com.financeapp.entity.User;
import com.financeapp.entity.UserProfile;
import com.financeapp.repository.UserProfileRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.UserProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserProfileService
 * Compatible with both H2 and PostgreSQL databases
 */
@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final ObjectMapper objectMapper;
    
    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, UserRepository userRepository, UserProfileMapper userProfileMapper, ObjectMapper objectMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.userProfileMapper = userProfileMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserProfileDto createProfile(UserProfileCreateDto createDto) {
        log.info("Creating user profile for user: {}", getCurrentUserId());
        
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if profile already exists
        if (userProfileRepository.findByUserId(currentUserId).isPresent()) {
            throw new RuntimeException("User profile already exists");
        }

        // Check email uniqueness if provided
        if (createDto.email() != null && !createDto.email().trim().isEmpty()) {
            if (userProfileRepository.countByEmailForOtherUser(createDto.email(), currentUserId) > 0) {
                throw new RuntimeException("Email already exists for another user");
            }
        }

        UserProfile userProfile = userProfileMapper.toEntity(createDto);
        userProfile.setUser(user);
        
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        log.info("Created user profile with ID: {}", savedProfile.getId());
        
        return userProfileMapper.toDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getProfileByUserId(Long userId) {
        log.info("Getting user profile for user ID: {}", userId);
        
        return userProfileRepository.findByUserId(userId)
                .map(userProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getProfileById(Long profileId) {
        log.info("Getting user profile by ID: {}", profileId);
        
        return userProfileRepository.findById(profileId)
                .map(userProfileMapper::toDto);
    }

    @Override
    public UserProfileDto updateProfile(Long profileId, UserProfileUpdateDto updateDto) {
        log.info("Updating user profile with ID: {}", profileId);
        
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Check email uniqueness if being updated
        if (updateDto.email() != null && !updateDto.email().trim().isEmpty()) {
            if (userProfileRepository.countByEmailForOtherUser(updateDto.email(), userProfile.getUser().getId()) > 0) {
                throw new RuntimeException("Email already exists for another user");
            }
        }

        userProfileMapper.updateEntity(userProfile, updateDto);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Updated user profile with ID: {}", updatedProfile.getId());
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    public UserProfileDto updateProfileByUserId(Long userId, UserProfileUpdateDto updateDto) {
        log.info("Updating user profile for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Check email uniqueness if being updated
        if (updateDto.email() != null && !updateDto.email().trim().isEmpty()) {
            if (userProfileRepository.countByEmailForOtherUser(updateDto.email(), userId) > 0) {
                throw new RuntimeException("Email already exists for another user");
            }
        }

        userProfileMapper.updateEntity(userProfile, updateDto);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Updated user profile for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    public void deleteProfile(Long profileId) {
        log.info("Deleting user profile with ID: {}", profileId);
        
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfileRepository.delete(userProfile);
        log.info("Deleted user profile with ID: {}", profileId);
    }

    @Override
    public void deleteProfileByUserId(Long userId) {
        log.info("Deleting user profile for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfileRepository.delete(userProfile);
        log.info("Deleted user profile for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> getPublicProfiles(Pageable pageable) {
        log.info("Getting public profiles with pagination");
        
        return userProfileRepository.findPublicProfiles(pageable)
                .map(userProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesByLocation(String location) {
        log.info("Getting profiles by location: {}", location);
        
        return userProfileRepository.findByLocationContaining(location)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getRecentlyActiveProfiles(int days) {
        log.info("Getting recently active profiles for last {} days", days);
        
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(days);
        return userProfileRepository.findRecentlyActiveProfiles(cutoffDate)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesWithEmailNotifications() {
        log.info("Getting profiles with email notifications enabled");
        
        return userProfileRepository.findProfilesWithEmailNotifications()
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesWithSmsNotifications() {
        log.info("Getting profiles with SMS notifications enabled");
        
        return userProfileRepository.findProfilesWithSmsNotifications()
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesByLanguage(String language) {
        log.info("Getting profiles by language: {}", language);
        
        return userProfileRepository.findByLanguage(language)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesByTimezone(String timezone) {
        log.info("Getting profiles by timezone: {}", timezone);
        
        return userProfileRepository.findByTimezone(timezone)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesWithPictures() {
        log.info("Getting profiles with pictures");
        
        return userProfileRepository.findProfilesWithPictures()
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesWithHighLoginCount(Integer minLoginCount) {
        log.info("Getting profiles with login count >= {}", minLoginCount);
        
        return userProfileRepository.findProfilesWithHighLoginCount(minLoginCount)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        log.info("Updating last login for user ID: {}", userId);
        
        int updated = userProfileRepository.updateLastLogin(userId, OffsetDateTime.now());
        if (updated == 0) {
            log.warn("No profile found for user ID: {}", userId);
        } else {
            log.info("Updated last login for user ID: {}", userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email, Long userId) {
        log.info("Checking email availability: {} for user ID: {}", email, userId);
        
        return userProfileRepository.countByEmailForOtherUser(email, userId) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileStatisticsDto getProfileStatistics() {
        log.info("Getting profile statistics");
        
        long totalProfiles = userProfileRepository.count();
        long activeProfiles = userProfileRepository.countActiveProfiles();
        long publicProfiles = userProfileRepository.countPublicProfiles();
        
        List<UserProfile> profilesWithPictures = userProfileRepository.findProfilesWithPictures();
        List<UserProfile> profilesWithEmailNotifications = userProfileRepository.findProfilesWithEmailNotifications();
        List<UserProfile> profilesWithSmsNotifications = userProfileRepository.findProfilesWithSmsNotifications();
        List<UserProfile> recentlyActiveProfiles = userProfileRepository.findRecentlyActiveProfiles(OffsetDateTime.now().minusDays(7));
        
        double averageLoginCount = userProfileRepository.findAll().stream()
                .mapToInt(UserProfile::getLoginCount)
                .average()
                .orElse(0.0);
        
        return new ProfileStatisticsDto(
            totalProfiles,
            activeProfiles,
            publicProfiles,
            profilesWithPictures.size(),
            profilesWithEmailNotifications.size(),
            profilesWithSmsNotifications.size(),
            averageLoginCount,
            recentlyActiveProfiles.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String exportUserData(Long userId) {
        log.info("Exporting user data for GDPR compliance for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findForGdprExport(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        try {
            return objectMapper.writeValueAsString(userProfileMapper.toDto(userProfile));
        } catch (JsonProcessingException e) {
            log.error("Error exporting user data for user ID: {}", userId, e);
            throw new RuntimeException("Error exporting user data", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> searchProfiles(String searchTerm) {
        log.info("Searching profiles with term: {}", searchTerm);
        
        // Simple search implementation - can be enhanced with PostgreSQL full-text search
        List<UserProfile> profiles = userProfileRepository.findByLocationContaining(searchTerm);
        
        return profiles.stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> getProfilesCreatedAfter(String date) {
        log.info("Getting profiles created after: {}", date);
        
        OffsetDateTime dateTime = OffsetDateTime.parse(date + "T00:00:00Z");
        return userProfileRepository.findProfilesCreatedAfter(dateTime)
                .stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveProfiles() {
        return userProfileRepository.countActiveProfiles();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublicProfiles() {
        return userProfileRepository.countPublicProfiles();
    }

    @Override
    public UserProfileDto updateProfilePicture(Long userId, String pictureUrl) {
        log.info("Updating profile picture for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setProfilePictureUrl(pictureUrl);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Updated profile picture for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    public UserProfileDto removeProfilePicture(Long userId) {
        log.info("Removing profile picture for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setProfilePictureUrl(null);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Removed profile picture for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    public UserProfileDto updateSettings(Long userId, String settings) {
        log.info("Updating settings for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setSettings(settings);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Updated settings for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public String getSettings(Long userId) {
        log.info("Getting settings for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        return userProfile.getSettings();
    }

    @Override
    public void bulkUpdateNotificationPreferences(List<Long> userIds, NotificationPreferencesDto preferences) {
        log.info("Bulk updating notification preferences for {} users", userIds.size());
        
        for (Long userId : userIds) {
            UserProfile userProfile = userProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User profile not found for user ID: " + userId));
            
            if (preferences.emailNotifications() != null) {
                userProfile.setEmailNotifications(preferences.emailNotifications());
            }
            if (preferences.smsNotifications() != null) {
                userProfile.setSmsNotifications(preferences.smsNotifications());
            }
            if (preferences.marketingEmails() != null) {
                userProfile.setMarketingEmails(preferences.marketingEmails());
            }
            
            userProfileRepository.save(userProfile);
        }
        
        log.info("Bulk updated notification preferences for {} users", userIds.size());
    }

    @Override
    public UserProfileDto deactivateProfile(Long userId) {
        log.info("Deactivating profile for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setIsActive(false);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Deactivated profile for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    @Override
    public UserProfileDto reactivateProfile(Long userId) {
        log.info("Reactivating profile for user ID: {}", userId);
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setIsActive(true);
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("Reactivated profile for user ID: {}", userId);
        
        return userProfileMapper.toDto(updatedProfile);
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getId();
    }
}
