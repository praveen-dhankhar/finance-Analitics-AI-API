package com.financeapp.repository;

import com.financeapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity with comprehensive query methods
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Count total users
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
    
    /**
     * Find users by partial username match (case-insensitive)
     * @param username partial username to search for
     * @param pageable pagination information
     * @return Page of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<User> findByUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);
    
    /**
     * Find users by partial email match (case-insensitive)
     * @param email partial email to search for
     * @param pageable pagination information
     * @return Page of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> findByEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);
    
    /**
     * Find users created within a date range
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return Page of users created within the date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedAtBetween(@Param("startDate") OffsetDateTime startDate, 
                                     @Param("endDate") OffsetDateTime endDate, 
                                     Pageable pageable);
    
    /**
     * Find users with financial data count greater than specified value
     * @param minDataCount minimum number of financial data entries
     * @param pageable pagination information
     * @return Page of users with sufficient financial data
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.financialData) >= :minDataCount")
    Page<User> findUsersWithMinFinancialDataCount(@Param("minDataCount") int minDataCount, Pageable pageable);
    
    /**
     * Find users with forecast count greater than specified value
     * @param minForecastCount minimum number of forecasts
     * @param pageable pagination information
     * @return Page of users with sufficient forecasts
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.forecasts) >= :minForecastCount")
    Page<User> findUsersWithMinForecastCount(@Param("minForecastCount") int minForecastCount, Pageable pageable);
    
    /**
     * Get user statistics including financial data and forecast counts
     * @param userId the user ID
     * @return Object array containing [userId, financialDataCount, forecastCount, lastActivity]
     */
    @Query("SELECT u.id, SIZE(u.financialData), SIZE(u.forecasts), " +
           "COALESCE(MAX(u.updatedAt), u.createdAt) FROM User u WHERE u.id = :userId")
    List<Object[]> getUserStatistics(@Param("userId") Long userId);
    
    /**
     * Find all users with pagination and sorting
     * @param pageable pagination and sorting information
     * @return Page of users
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    Page<User> findAll(Pageable pageable);
    
    /**
     * Find users by multiple criteria with pagination
     * @param username partial username (optional)
     * @param email partial email (optional)
     * @param pageable pagination information
     * @return Page of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<User> findUsersByCriteria(@Param("username") String username, 
                                  @Param("email") String email, 
                                  Pageable pageable);
}
