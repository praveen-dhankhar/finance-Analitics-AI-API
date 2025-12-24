package com.financeapp.controller;

import com.financeapp.dto.FileUploadResponseDto;
import com.financeapp.dto.UserProfileCreateDto;
import com.financeapp.dto.UserProfileDto;
import com.financeapp.dto.UserProfileUpdateDto;
import com.financeapp.service.FileStorageService;
import com.financeapp.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for User Profile operations
 * Compatible with both H2 and PostgreSQL databases
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Profile Management", description = "APIs for managing user profiles and settings")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final UserProfileService userProfileService;
    private final FileStorageService fileStorageService;
    
    public UserController(UserProfileService userProfileService, FileStorageService fileStorageService) {
        this.userProfileService = userProfileService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create user profile", description = "Create a new user profile with extended information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Profile created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Profile already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> createProfile(
            @Valid @RequestBody UserProfileCreateDto createDto) {
        log.info("Creating user profile");
        
        try {
            UserProfileDto profile = userProfileService.createProfile(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (RuntimeException e) {
            log.error("Error creating profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        log.info("Getting current user profile");
        
        return userProfileService.getProfileByUserId(getCurrentUserId())
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{profileId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get profile by ID", description = "Get a specific user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> getProfileById(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Getting profile by ID: {}", profileId);
        
        return userProfileService.getProfileById(profileId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update current user profile", description = "Update the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateDto updateDto) {
        log.info("Updating current user profile");
        
        try {
            UserProfileDto profile = userProfileService.updateProfileByUserId(getCurrentUserId(), updateDto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error updating profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/{profileId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update profile by ID", description = "Update a specific user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> updateProfile(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Valid @RequestBody UserProfileUpdateDto updateDto) {
        log.info("Updating profile by ID: {}", profileId);
        
        try {
            UserProfileDto profile = userProfileService.updateProfile(profileId, updateDto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error updating profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete current user profile", description = "Delete the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCurrentUserProfile() {
        log.info("Deleting current user profile");
        
        try {
            userProfileService.deleteProfileByUserId(getCurrentUserId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/profile/{profileId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete profile by ID", description = "Delete a specific user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Deleting profile by ID: {}", profileId);
        
        try {
            userProfileService.deleteProfile(profileId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profiles/public")
    @Operation(summary = "Get public profiles", description = "Get all public user profiles with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Public profiles retrieved successfully")
    })
    public ResponseEntity<Page<UserProfileDto>> getPublicProfiles(Pageable pageable) {
        log.info("Getting public profiles with pagination");
        
        Page<UserProfileDto> profiles = userProfileService.getPublicProfiles(pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/profiles/search")
    @Operation(summary = "Search profiles", description = "Search user profiles by location or other criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profiles found successfully")
    })
    public ResponseEntity<List<UserProfileDto>> searchProfiles(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        log.info("Searching profiles with term: {}", searchTerm);
        
        List<UserProfileDto> profiles = userProfileService.searchProfiles(searchTerm);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/profiles/location")
    @Operation(summary = "Get profiles by location", description = "Get user profiles by location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    })
    public ResponseEntity<List<UserProfileDto>> getProfilesByLocation(
            @Parameter(description = "Location") @RequestParam String location) {
        log.info("Getting profiles by location: {}", location);
        
        List<UserProfileDto> profiles = userProfileService.getProfilesByLocation(location);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/profiles/recently-active")
    @Operation(summary = "Get recently active profiles", description = "Get user profiles that were recently active")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    })
    public ResponseEntity<List<UserProfileDto>> getRecentlyActiveProfiles(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "7") int days) {
        log.info("Getting recently active profiles for last {} days", days);
        
        List<UserProfileDto> profiles = userProfileService.getRecentlyActiveProfiles(days);
        return ResponseEntity.ok(profiles);
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload profile picture", description = "Upload a profile picture for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FileUploadResponseDto> uploadProfilePicture(
            @Parameter(description = "Profile picture file") @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile picture for current user");
        
        try {
            FileUploadResponseDto response = fileStorageService.uploadProfilePicture(file, getCurrentUserId());
            if (response.isSuccess()) {
                // Update profile with new picture URL
                userProfileService.updateProfilePicture(getCurrentUserId(), response.fileUrl());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading profile picture: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/profile/picture")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove profile picture", description = "Remove the profile picture of the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture removed successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> removeProfilePicture() {
        log.info("Removing profile picture for current user");
        
        try {
            UserProfileDto profile = userProfileService.removeProfilePicture(getCurrentUserId());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error removing profile picture: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/settings")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update user settings", description = "Update JSON settings for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid settings data"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> updateSettings(
            @Parameter(description = "JSON settings") @RequestBody String settings) {
        log.info("Updating settings for current user");
        
        try {
            UserProfileDto profile = userProfileService.updateSettings(getCurrentUserId(), settings);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error updating settings: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile/settings")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user settings", description = "Get JSON settings for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Settings retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> getSettings() {
        log.info("Getting settings for current user");
        
        try {
            String settings = userProfileService.getSettings(getCurrentUserId());
            return ResponseEntity.ok(settings);
        } catch (RuntimeException e) {
            log.error("Error getting settings: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile/statistics")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get profile statistics", description = "Get statistics about user profiles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileService.ProfileStatisticsDto> getProfileStatistics() {
        log.info("Getting profile statistics");
        
        UserProfileService.ProfileStatisticsDto statistics = userProfileService.getProfileStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/profile/export")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Export user data", description = "Export user data for GDPR compliance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data exported successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> exportUserData() {
        log.info("Exporting user data for GDPR compliance");
        
        try {
            String userData = userProfileService.exportUserData(getCurrentUserId());
            return ResponseEntity.ok(userData);
        } catch (RuntimeException e) {
            log.error("Error exporting user data: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/deactivate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Deactivate profile", description = "Deactivate the current user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> deactivateProfile() {
        log.info("Deactivating current user profile");
        
        try {
            UserProfileDto profile = userProfileService.deactivateProfile(getCurrentUserId());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error deactivating profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/reactivate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Reactivate profile", description = "Reactivate the current user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile reactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileDto> reactivateProfile() {
        log.info("Reactivating current user profile");
        
        try {
            UserProfileDto profile = userProfileService.reactivateProfile(getCurrentUserId());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error reactivating profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/available")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check email availability", description = "Check if an email address is available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email availability checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Boolean> checkEmailAvailability(
            @Parameter(description = "Email address") @RequestParam String email) {
        log.info("Checking email availability: {}", email);
        
        boolean available = userProfileService.isEmailAvailable(email, getCurrentUserId());
        return ResponseEntity.ok(available);
    }

    /**
     * Get current user ID from security context
     * This would typically be extracted from JWT token or security context
     */
    private Long getCurrentUserId() {
        // This is a placeholder implementation
        // In a real application, this would extract the user ID from the security context
        return 1L; // Placeholder user ID
    }
}
