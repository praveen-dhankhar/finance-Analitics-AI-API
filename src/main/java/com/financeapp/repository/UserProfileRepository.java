package com.financeapp.repository;

import com.financeapp.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserProfile entity with database-optimized queries
 * Compatible with both H2 and PostgreSQL databases
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find user profile by user ID
     * Optimized query with proper indexing
     */
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") Long userId);

    /**
     * Find user profile by email
     * Optimized query with proper indexing
     */
    @Query("SELECT up FROM UserProfile up WHERE up.email = :email")
    Optional<UserProfile> findByEmail(@Param("email") String email);

    /**
     * Find active user profiles
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true")
    List<UserProfile> findActiveProfiles();

    /**
     * Find public user profiles with pagination
     * Optimized for both databases
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isPublic = true AND up.isActive = true")
    Page<UserProfile> findPublicProfiles(Pageable pageable);

    /**
     * Find user profiles by location
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.location LIKE %:location% AND up.isActive = true")
    List<UserProfile> findByLocationContaining(@Param("location") String location);

    /**
     * Find user profiles created after a specific date
     * Optimized query with proper indexing
     */
    @Query("SELECT up FROM UserProfile up WHERE up.createdAt >= :date AND up.isActive = true")
    List<UserProfile> findProfilesCreatedAfter(@Param("date") OffsetDateTime date);

    /**
     * Find user profiles with recent login activity
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.lastLoginAt >= :date AND up.isActive = true ORDER BY up.lastLoginAt DESC")
    List<UserProfile> findRecentlyActiveProfiles(@Param("date") OffsetDateTime date);

    /**
     * Count active user profiles
     * Optimized count query
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isActive = true")
    long countActiveProfiles();

    /**
     * Count public user profiles
     * Optimized count query
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isPublic = true AND up.isActive = true")
    long countPublicProfiles();

    /**
     * Find user profiles with email notifications enabled
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.emailNotifications = true AND up.isActive = true")
    List<UserProfile> findProfilesWithEmailNotifications();

    /**
     * Find user profiles with SMS notifications enabled
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.smsNotifications = true AND up.isActive = true")
    List<UserProfile> findProfilesWithSmsNotifications();

    /**
     * Find user profiles by language
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.language = :language AND up.isActive = true")
    List<UserProfile> findByLanguage(@Param("language") String language);

    /**
     * Find user profiles by timezone
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.timezone = :timezone AND up.isActive = true")
    List<UserProfile> findByTimezone(@Param("timezone") String timezone);

    /**
     * Find user profiles with profile pictures
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.profilePictureUrl IS NOT NULL AND up.profilePictureUrl != '' AND up.isActive = true")
    List<UserProfile> findProfilesWithPictures();

    /**
     * Find user profiles with high login count
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.loginCount >= :minLoginCount AND up.isActive = true ORDER BY up.loginCount DESC")
    List<UserProfile> findProfilesWithHighLoginCount(@Param("minLoginCount") Integer minLoginCount);

    /**
     * Update last login time and increment login count
     * Database-agnostic update query
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserProfile up SET up.lastLoginAt = :loginTime, up.loginCount = up.loginCount + 1 WHERE up.user.id = :userId")
    int updateLastLogin(@Param("userId") Long userId, @Param("loginTime") OffsetDateTime loginTime);

    /**
     * Find user profiles for GDPR export
     * Database-agnostic query
     */
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    Optional<UserProfile> findForGdprExport(@Param("userId") Long userId);

    /**
     * Check if email exists for other users
     * Optimized query for email uniqueness validation
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.email = :email AND up.user.id != :userId")
    long countByEmailForOtherUser(@Param("email") String email, @Param("userId") Long userId);

    /**
     * PostgreSQL-specific JSON query (commented for H2 compatibility)
     * Uncomment when migrating to PostgreSQL
     */
    // @Query(value = "SELECT * FROM user_profiles WHERE settings @> :jsonPath", nativeQuery = true)
    // List<UserProfile> findByJsonSetting(@Param("jsonPath") String jsonPath);

    /**
     * PostgreSQL-specific full-text search (commented for H2 compatibility)
     * Uncomment when migrating to PostgreSQL
     */
    // @Query(value = "SELECT * FROM user_profiles WHERE to_tsvector('english', first_name || ' ' || last_name || ' ' || COALESCE(bio, '')) @@ plainto_tsquery('english', :searchTerm)", nativeQuery = true)
    // List<UserProfile> searchProfilesFullText(@Param("searchTerm") String searchTerm);
}
