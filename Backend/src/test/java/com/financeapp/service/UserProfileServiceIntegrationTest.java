package com.financeapp.service;

import com.financeapp.dto.UserProfileCreateDto;
import com.financeapp.dto.UserProfileDto;
import com.financeapp.dto.UserProfileUpdateDto;
import com.financeapp.entity.User;
import com.financeapp.entity.UserProfile;
import com.financeapp.repository.UserProfileRepository;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.financeapp.testsupport.TestDatabaseCleaner;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserProfileService
 * Compatible with both H2 and PostgreSQL databases
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserProfileServiceIntegrationTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseCleaner cleaner;

    private User testUser;

    @BeforeEach
    void setUp() {
        cleaner.clean();
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("TestPass123!"));
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createProfile_WithValidData_ShouldCreateProfile() {
        // Given
        UserProfileCreateDto createDto = new UserProfileCreateDto(
            "John",
            "Doe",
            "john.doe@example.com",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "New York",
            "Software Developer",
            "America/New_York",
            "en",
            true,
            true,
            false,
            true,
            "{\"theme\": \"dark\", \"notifications\": true}"
        );

        // When
        UserProfileDto profile = userProfileService.createProfile(createDto);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.firstName()).isEqualTo("John");
        assertThat(profile.lastName()).isEqualTo("Doe");
        assertThat(profile.email()).isEqualTo("john.doe@example.com");
        assertThat(profile.phoneNumber()).isEqualTo("+1234567890");
        assertThat(profile.location()).isEqualTo("New York");
        assertThat(profile.bio()).isEqualTo("Software Developer");
        assertThat(profile.timezone()).isEqualTo("America/New_York");
        assertThat(profile.language()).isEqualTo("en");
        assertThat(profile.isPublic()).isTrue();
        assertThat(profile.emailNotifications()).isTrue();
        assertThat(profile.smsNotifications()).isFalse();
        assertThat(profile.marketingEmails()).isTrue();
        assertThat(profile.settings()).isEqualTo("{\"theme\": \"dark\", \"notifications\": true}");
        assertThat(profile.isActive()).isTrue();
        assertThat(profile.loginCount()).isEqualTo(0);

        // Verify in database
        Optional<UserProfile> savedProfile = userProfileRepository.findByUserId(testUser.getId());
        assertThat(savedProfile).isPresent();
        assertThat(savedProfile.get().getFirstName()).isEqualTo("John");
        assertThat(savedProfile.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProfileByUserId_WithExistingProfile_ShouldReturnProfile() {
        // Given
        UserProfile userProfile = new UserProfile(testUser, "Jane", "Smith");
        userProfile.setEmail("jane.smith@example.com");
        userProfile.setLocation("London");
        userProfileRepository.save(userProfile);

        // When
        Optional<UserProfileDto> profile = userProfileService.getProfileByUserId(testUser.getId());

        // Then
        assertThat(profile).isPresent();
        assertThat(profile.get().firstName()).isEqualTo("Jane");
        assertThat(profile.get().lastName()).isEqualTo("Smith");
        assertThat(profile.get().email()).isEqualTo("jane.smith@example.com");
        assertThat(profile.get().location()).isEqualTo("London");
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_WithValidData_ShouldUpdateProfile() {
        // Given
        UserProfile userProfile = new UserProfile(testUser, "Bob", "Johnson");
        userProfile.setEmail("bob.johnson@example.com");
        userProfile = userProfileRepository.save(userProfile);

        UserProfileUpdateDto updateDto = new UserProfileUpdateDto(
            "Robert", // Updated first name
            null, // Keep last name
            "robert.johnson@example.com", // Updated email
            "+9876543210", // Updated phone
            LocalDate.of(1985, 5, 15), // Updated date of birth
            "San Francisco", // Updated location
            "Senior Developer", // Updated bio
            "America/Los_Angeles", // Updated timezone
            "es", // Updated language
            false, // Updated isPublic
            false, // Updated emailNotifications
            true, // Updated smsNotifications
            false, // Updated marketingEmails
            "{\"theme\": \"light\"}" // Updated settings
        );

        // When
        UserProfileDto updatedProfile = userProfileService.updateProfile(userProfile.getId(), updateDto);

        // Then
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.firstName()).isEqualTo("Robert");
        assertThat(updatedProfile.lastName()).isEqualTo("Johnson"); // Should remain unchanged
        assertThat(updatedProfile.email()).isEqualTo("robert.johnson@example.com");
        assertThat(updatedProfile.phoneNumber()).isEqualTo("+9876543210");
        assertThat(updatedProfile.location()).isEqualTo("San Francisco");
        assertThat(updatedProfile.bio()).isEqualTo("Senior Developer");
        assertThat(updatedProfile.timezone()).isEqualTo("America/Los_Angeles");
        assertThat(updatedProfile.language()).isEqualTo("es");
        assertThat(updatedProfile.isPublic()).isFalse();
        assertThat(updatedProfile.emailNotifications()).isFalse();
        assertThat(updatedProfile.smsNotifications()).isTrue();
        assertThat(updatedProfile.marketingEmails()).isFalse();
        assertThat(updatedProfile.settings()).isEqualTo("{\"theme\": \"light\"}");
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteProfile_WithExistingProfile_ShouldDeleteProfile() {
        // Given
        UserProfile userProfile = new UserProfile(testUser, "Alice", "Brown");
        userProfile = userProfileRepository.save(userProfile);

        // When
        userProfileService.deleteProfile(userProfile.getId());

        // Then
        Optional<UserProfile> deletedProfile = userProfileRepository.findById(userProfile.getId());
        assertThat(deletedProfile).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateLastLogin_WithExistingProfile_ShouldUpdateLoginInfo() {
        // Given
        UserProfile userProfile = new UserProfile(testUser, "Charlie", "Wilson");
        userProfile.setLoginCount(5);
        userProfileRepository.save(userProfile);

        // When
        userProfileService.updateLastLogin(testUser.getId());

        // Then
        Optional<UserProfile> updatedProfile = userProfileRepository.findByUserId(testUser.getId());
        assertThat(updatedProfile).isPresent();
        assertThat(updatedProfile.get().getLoginCount()).isEqualTo(6);
        assertThat(updatedProfile.get().getLastLoginAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "testuser")
    void isEmailAvailable_WithUniqueEmail_ShouldReturnTrue() {
        // Given
        String email = "unique@example.com";

        // When
        boolean available = userProfileService.isEmailAvailable(email, testUser.getId());

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @WithMockUser(username = "testuser")
    void isEmailAvailable_WithExistingEmail_ShouldReturnFalse() {
        // Given
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("password123");
        anotherUser = userRepository.save(anotherUser);

        UserProfile userProfile = new UserProfile(anotherUser, "David", "Lee");
        userProfile.setEmail("existing@example.com");
        userProfileRepository.save(userProfile);

        // When
        boolean available = userProfileService.isEmailAvailable("existing@example.com", testUser.getId());

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProfileStatistics_ShouldReturnStatistics() {
        // Given
        UserProfile userProfile1 = new UserProfile(testUser, "User1", "Test1");
        userProfile1.setIsPublic(true);
        userProfile1.setEmailNotifications(true);
        userProfile1.setLoginCount(10);
        userProfileRepository.save(userProfile1);

        // Create a different user for the second profile
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("password123");
        anotherUser = userRepository.save(anotherUser);

        UserProfile userProfile2 = new UserProfile(anotherUser, "User2", "Test2");
        userProfile2.setIsPublic(false);
        userProfile2.setEmailNotifications(false);
        userProfile2.setLoginCount(5);
        userProfileRepository.save(userProfile2);

        // When
        UserProfileService.ProfileStatisticsDto statistics = userProfileService.getProfileStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.totalProfiles()).isGreaterThanOrEqualTo(2);
        assertThat(statistics.activeProfiles()).isGreaterThanOrEqualTo(2);
        assertThat(statistics.publicProfiles()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.profilesWithEmailNotifications()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.averageLoginCount()).isGreaterThan(0);
    }
}
