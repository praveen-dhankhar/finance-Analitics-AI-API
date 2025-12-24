package com.financeapp.repository;

import com.financeapp.entity.Forecast;
import com.financeapp.entity.User;
import com.financeapp.entity.Forecast.ForecastStatus;
import com.financeapp.entity.Forecast.ForecastType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Forecast entity with comprehensive query methods
 */
@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    /**
     * Find all forecasts for a specific user
     */
    List<Forecast> findByUser(User user);

    /**
     * Find all forecasts for a specific user with pagination
     */
    Page<Forecast> findByUser(User user, Pageable pageable);

    /**
     * Find forecasts by user and status
     */
    List<Forecast> findByUserAndStatus(User user, ForecastStatus status);

    /**
     * Find forecasts by user and type
     */
    List<Forecast> findByUserAndForecastType(User user, ForecastType forecastType);

    /**
     * Find forecasts by user and date range
     */
    List<Forecast> findByUserAndForecastDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find active forecasts for a user
     */
    @Query("SELECT f FROM Forecast f WHERE f.user = :user AND f.status = 'ACTIVE' ORDER BY f.forecastDate ASC")
    List<Forecast> findActiveForecastsByUser(@Param("user") User user);

    /**
     * Find forecasts by confidence score range
     */
    List<Forecast> findByUserAndConfidenceScoreBetween(User user, BigDecimal minConfidence, BigDecimal maxConfidence);

    /**
     * Find high confidence forecasts (>= 0.8)
     */
    @Query("SELECT f FROM Forecast f WHERE f.user = :user AND f.confidenceScore >= 0.8 ORDER BY f.confidenceScore DESC")
    List<Forecast> findHighConfidenceForecastsByUser(@Param("user") User user);

    /**
     * Find forecasts by model name
     */
    List<Forecast> findByUserAndModelName(User user, String modelName);

    /**
     * Find upcoming forecasts (future dates)
     */
    @Query("SELECT f FROM Forecast f WHERE f.user = :user AND f.forecastDate > :currentDate ORDER BY f.forecastDate ASC")
    List<Forecast> findUpcomingForecastsByUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate);

    /**
     * Calculate average confidence score by user
     */
    @Query("SELECT AVG(f.confidenceScore) FROM Forecast f WHERE f.user = :user")
    BigDecimal calculateAverageConfidenceByUser(@Param("user") User user);

    /**
     * Count forecasts by user and status
     */
    long countByUserAndStatus(User user, ForecastStatus status);

    /**
     * Find most recent forecast for a user
     */
    @Query("SELECT f FROM Forecast f WHERE f.user = :user ORDER BY f.createdAt DESC")
    List<Forecast> findMostRecentByUser(@Param("user") User user, Pageable pageable);

    /**
     * Find forecasts by predicted amount range
     */
    List<Forecast> findByUserAndPredictedAmountBetween(User user, BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find forecasts by user ID with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId")
    Page<Forecast> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find forecasts by user ID and status with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.status = :status")
    Page<Forecast> findByUserIdAndStatus(@Param("userId") Long userId, 
                                        @Param("status") ForecastStatus status, 
                                        Pageable pageable);
    
    /**
     * Find forecasts by user ID and type with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.forecastType = :forecastType")
    Page<Forecast> findByUserIdAndForecastType(@Param("userId") Long userId, 
                                              @Param("forecastType") ForecastType forecastType, 
                                              Pageable pageable);
    
    /**
     * Find forecasts by user ID and date range with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.forecastDate BETWEEN :startDate AND :endDate")
    Page<Forecast> findByUserIdAndForecastDateBetween(@Param("userId") Long userId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate, 
                                                     Pageable pageable);
    
    /**
     * Find forecasts by user ID and confidence score range with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.confidenceScore BETWEEN :minConfidence AND :maxConfidence")
    Page<Forecast> findByUserIdAndConfidenceScoreBetween(@Param("userId") Long userId, 
                                                        @Param("minConfidence") BigDecimal minConfidence, 
                                                        @Param("maxConfidence") BigDecimal maxConfidence, 
                                                        Pageable pageable);
    
    /**
     * Find forecasts by user ID and model name with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.modelName = :modelName")
    Page<Forecast> findByUserIdAndModelName(@Param("userId") Long userId, 
                                           @Param("modelName") String modelName, 
                                           Pageable pageable);
    
    /**
     * Find forecasts by user ID and predicted amount range with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.predictedAmount BETWEEN :minAmount AND :maxAmount")
    Page<Forecast> findByUserIdAndPredictedAmountBetween(@Param("userId") Long userId, 
                                                        @Param("minAmount") BigDecimal minAmount, 
                                                        @Param("maxAmount") BigDecimal maxAmount, 
                                                        Pageable pageable);
    
    /**
     * Find active forecasts by user ID with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.status = 'ACTIVE' ORDER BY f.forecastDate ASC")
    Page<Forecast> findActiveForecastsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find high confidence forecasts by user ID (>= 0.8) with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.confidenceScore >= 0.8 ORDER BY f.confidenceScore DESC")
    Page<Forecast> findHighConfidenceForecastsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find upcoming forecasts by user ID (future dates) with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId AND f.forecastDate > :currentDate ORDER BY f.forecastDate ASC")
    Page<Forecast> findUpcomingForecastsByUserId(@Param("userId") Long userId, 
                                                @Param("currentDate") LocalDate currentDate, 
                                                Pageable pageable);
    
    /**
     * Find most recent forecasts by user ID with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    Page<Forecast> findMostRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Calculate average confidence score by user ID
     */
    @Query("SELECT AVG(f.confidenceScore) FROM Forecast f WHERE f.user.id = :userId")
    Optional<BigDecimal> calculateAverageConfidenceByUserId(@Param("userId") Long userId);
    
    /**
     * Count forecasts by user ID and status
     */
    @Query("SELECT COUNT(f) FROM Forecast f WHERE f.user.id = :userId AND f.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ForecastStatus status);
    
    /**
     * Count forecasts by user ID
     */
    @Query("SELECT COUNT(f) FROM Forecast f WHERE f.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * Find forecast statistics by user ID
     * Returns: [totalForecasts, avgConfidence, minConfidence, maxConfidence, totalPredictedAmount]
     */
    @Query("SELECT COUNT(f), AVG(f.confidenceScore), MIN(f.confidenceScore), MAX(f.confidenceScore), SUM(f.predictedAmount) " +
           "FROM Forecast f WHERE f.user.id = :userId")
    List<Object[]> getForecastStatistics(@Param("userId") Long userId);
    
    /**
     * Find forecasts by status with amount aggregation
     * Returns: [status, count, avgConfidence, totalPredictedAmount]
     */
    @Query("SELECT f.status, COUNT(f), AVG(f.confidenceScore), SUM(f.predictedAmount) " +
           "FROM Forecast f WHERE f.user.id = :userId " +
           "GROUP BY f.status ORDER BY COUNT(f) DESC")
    List<Object[]> getForecastsByStatus(@Param("userId") Long userId);
    
    /**
     * Find forecasts by type with amount aggregation
     * Returns: [forecastType, count, avgConfidence, totalPredictedAmount]
     */
    @Query("SELECT f.forecastType, COUNT(f), AVG(f.confidenceScore), SUM(f.predictedAmount) " +
           "FROM Forecast f WHERE f.user.id = :userId " +
           "GROUP BY f.forecastType ORDER BY COUNT(f) DESC")
    List<Object[]> getForecastsByType(@Param("userId") Long userId);
    
    /**
     * Find forecasts by model with amount aggregation
     * Returns: [modelName, count, avgConfidence, totalPredictedAmount]
     */
    @Query("SELECT f.modelName, COUNT(f), AVG(f.confidenceScore), SUM(f.predictedAmount) " +
           "FROM Forecast f WHERE f.user.id = :userId " +
           "GROUP BY f.modelName ORDER BY COUNT(f) DESC")
    List<Object[]> getForecastsByModel(@Param("userId") Long userId);
    
    /**
     * Find forecasts by month for a user
     * Returns: [year, month, count, avgConfidence, totalPredictedAmount]
     */
    @Query("SELECT YEAR(f.forecastDate), MONTH(f.forecastDate), COUNT(f), AVG(f.confidenceScore), SUM(f.predictedAmount) " +
           "FROM Forecast f WHERE f.user.id = :userId " +
           "GROUP BY YEAR(f.forecastDate), MONTH(f.forecastDate) ORDER BY YEAR(f.forecastDate) DESC, MONTH(f.forecastDate) DESC")
    List<Object[]> getForecastsByMonth(@Param("userId") Long userId);
    
    /**
     * Find top N forecasts by confidence score for a user
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId ORDER BY f.confidenceScore DESC")
    List<Forecast> findTopByUserIdOrderByConfidenceScoreDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find top N forecasts by predicted amount for a user
     */
    @Query("SELECT f FROM Forecast f WHERE f.user.id = :userId ORDER BY f.predictedAmount DESC")
    List<Forecast> findTopByUserIdOrderByPredictedAmountDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all forecasts with pagination and sorting (admin only)
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    Page<Forecast> findAll(Pageable pageable);
    
    /**
     * Find forecasts by multiple criteria with pagination
     */
    @Query("SELECT f FROM Forecast f WHERE " +
           "(:userId IS NULL OR f.user.id = :userId) AND " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:forecastType IS NULL OR f.forecastType = :forecastType) AND " +
           "(:modelName IS NULL OR f.modelName = :modelName) AND " +
           "(:startDate IS NULL OR f.forecastDate >= :startDate) AND " +
           "(:endDate IS NULL OR f.forecastDate <= :endDate) AND " +
           "(:minConfidence IS NULL OR f.confidenceScore >= :minConfidence) AND " +
           "(:maxConfidence IS NULL OR f.confidenceScore <= :maxConfidence) AND " +
           "(:minAmount IS NULL OR f.predictedAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR f.predictedAmount <= :maxAmount)")
    Page<Forecast> findByCriteria(@Param("userId") Long userId,
                                 @Param("status") ForecastStatus status,
                                 @Param("forecastType") ForecastType forecastType,
                                 @Param("modelName") String modelName,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("minConfidence") BigDecimal minConfidence,
                                 @Param("maxConfidence") BigDecimal maxConfidence,
                                 @Param("minAmount") BigDecimal minAmount,
                                 @Param("maxAmount") BigDecimal maxAmount,
                                 Pageable pageable);
}
