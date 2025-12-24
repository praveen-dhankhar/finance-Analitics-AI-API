package com.financeapp.repository;

import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FinancialData entity with comprehensive query methods
 */
@Repository
public interface FinancialDataRepository extends JpaRepository<FinancialData, Long>, JpaSpecificationExecutor<FinancialData> {

    /**
     * Find all financial data for a specific user
     */
    List<FinancialData> findByUser(User user);

    /**
     * Find all financial data for a specific user with pagination
     */
    Page<FinancialData> findByUser(User user, Pageable pageable);

    /**
     * Find financial data by user and date range
     */
    List<FinancialData> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find financial data by user and category
     */
    List<FinancialData> findByUserAndCategory(User user, Category category);

    /**
     * Find financial data by user and transaction type
     */
    List<FinancialData> findByUserAndType(User user, TransactionType type);

    /**
     * Find financial data by user, category, and date range
     */
    List<FinancialData> findByUserAndCategoryAndDateBetween(User user, Category category, 
                                                           LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total amount by user and transaction type
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user = :user AND fd.type = :type")
    BigDecimal sumAmountByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    /**
     * Calculate total amount by user, type, and date range
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user = :user AND fd.type = :type " +
           "AND fd.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(@Param("user") User user, 
                                                 @Param("type") TransactionType type,
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Calculate total amount by user and category
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user = :user AND fd.category = :category")
    BigDecimal sumAmountByUserAndCategory(@Param("user") User user, @Param("category") Category category);

    /**
     * Find recent financial data for a user
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user = :user ORDER BY fd.date DESC, fd.createdAt DESC")
    List<FinancialData> findRecentByUser(@Param("user") User user, Pageable pageable);

    /**
     * Count financial data entries by user
     */
    long countByUser(User user);

    /**
     * Find financial data by user and amount range
     */
    List<FinancialData> findByUserAndAmountBetween(User user, BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find financial data by user ID with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId")
    Page<FinancialData> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find financial data by user ID and date range with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.date BETWEEN :startDate AND :endDate")
    Page<FinancialData> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate, 
                                                  Pageable pageable);
    
    /**
     * Find financial data by user ID and category with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.category = :category")
    Page<FinancialData> findByUserIdAndCategory(@Param("userId") Long userId, 
                                               @Param("category") Category category, 
                                               Pageable pageable);
    
    /**
     * Find financial data by user ID and transaction type with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.type = :type")
    Page<FinancialData> findByUserIdAndType(@Param("userId") Long userId, 
                                           @Param("type") TransactionType type, 
                                           Pageable pageable);
    
    /**
     * Find financial data by user ID, category, and date range with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.category = :category " +
           "AND fd.date BETWEEN :startDate AND :endDate")
    Page<FinancialData> findByUserIdAndCategoryAndDateBetween(@Param("userId") Long userId, 
                                                             @Param("category") Category category,
                                                             @Param("startDate") LocalDate startDate, 
                                                             @Param("endDate") LocalDate endDate, 
                                                             Pageable pageable);
    
    /**
     * Find financial data by user ID and amount range with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.amount BETWEEN :minAmount AND :maxAmount")
    Page<FinancialData> findByUserIdAndAmountBetween(@Param("userId") Long userId, 
                                                    @Param("minAmount") BigDecimal minAmount, 
                                                    @Param("maxAmount") BigDecimal maxAmount, 
                                                    Pageable pageable);
    
    /**
     * Find financial data by description containing text (case-insensitive)
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND " +
           "LOWER(fd.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<FinancialData> findByUserIdAndDescriptionContainingIgnoreCase(@Param("userId") Long userId, 
                                                                      @Param("description") String description, 
                                                                      Pageable pageable);
    
    /**
     * Calculate total amount by user ID and transaction type
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId AND fd.type = :type")
    Optional<BigDecimal> sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
    
    /**
     * Calculate total amount by user ID, type, and date range
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId AND fd.type = :type " +
           "AND fd.date BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumAmountByUserIdAndTypeAndDateRange(@Param("userId") Long userId, 
                                                             @Param("type") TransactionType type,
                                                             @Param("startDate") LocalDate startDate, 
                                                             @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate total amount by user ID and category
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId AND fd.category = :category")
    Optional<BigDecimal> sumAmountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") Category category);
    
    /**
     * Calculate average amount by user ID and transaction type
     */
    @Query("SELECT AVG(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId AND fd.type = :type")
    Optional<BigDecimal> avgAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
    
    /**
     * Calculate average amount by user ID and category
     */
    @Query("SELECT AVG(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId AND fd.category = :category")
    Optional<BigDecimal> avgAmountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") Category category);
    
    /**
     * Find top N financial data entries by amount for a user
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId ORDER BY fd.amount DESC")
    List<FinancialData> findTopByUserIdOrderByAmountDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find financial data statistics by user ID
     * Returns: [totalEntries, totalIncome, totalExpense, avgAmount, minAmount, maxAmount]
     */
    @Query(value = "SELECT COUNT(*), " +
           "SUM(CASE WHEN fd.type = 'INCOME' THEN fd.amount ELSE 0 END), " +
           "SUM(CASE WHEN fd.type = 'EXPENSE' THEN fd.amount ELSE 0 END), " +
           "AVG(fd.amount), " +
           "MIN(fd.amount), " +
           "MAX(fd.amount) " +
           "FROM financial_data fd WHERE fd.user_id = :userId", nativeQuery = true)
    List<Object[]> getFinancialDataStatistics(@Param("userId") Long userId);
    
    /**
     * Find financial data by category with amount aggregation
     * Returns: [category, count, totalAmount, avgAmount]
     */
    @Query("SELECT fd.category, COUNT(fd), SUM(fd.amount), AVG(fd.amount) " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "GROUP BY fd.category ORDER BY SUM(fd.amount) DESC")
    List<Object[]> getFinancialDataByCategory(@Param("userId") Long userId);
    
    /**
     * Find financial data by month for a user
     * Returns: [year, month, count, totalAmount]
     */
    @Query("SELECT YEAR(fd.date), MONTH(fd.date), COUNT(fd), SUM(fd.amount) " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "GROUP BY YEAR(fd.date), MONTH(fd.date) ORDER BY YEAR(fd.date) DESC, MONTH(fd.date) DESC")
    List<Object[]> getFinancialDataByMonth(@Param("userId") Long userId);
    
    /**
     * Find all financial data with pagination and sorting (admin only)
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    Page<FinancialData> findAll(Pageable pageable);
    
    /**
     * Find financial data by multiple criteria with pagination
     */
    @Query("SELECT fd FROM FinancialData fd WHERE " +
           "(:userId IS NULL OR fd.user.id = :userId) AND " +
           "(:category IS NULL OR fd.category = :category) AND " +
           "(:type IS NULL OR fd.type = :type) AND " +
           "(:startDate IS NULL OR fd.date >= :startDate) AND " +
           "(:endDate IS NULL OR fd.date <= :endDate) AND " +
           "(:minAmount IS NULL OR fd.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR fd.amount <= :maxAmount)")
    Page<FinancialData> findByCriteria(@Param("userId") Long userId,
                                      @Param("category") Category category,
                                      @Param("type") TransactionType type,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("minAmount") BigDecimal minAmount,
                                      @Param("maxAmount") BigDecimal maxAmount,
                                      Pageable pageable);

    // Additional methods for FinancialDataService

    /**
     * Find financial data by user ID and filters for export
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND (:category IS NULL OR fd.category = :category) " +
           "AND (:dateFrom IS NULL OR fd.date >= :dateFrom) " +
           "AND (:dateTo IS NULL OR fd.date <= :dateTo) " +
           "ORDER BY fd.date DESC")
    List<FinancialData> findByUserIdAndFilters(@Param("userId") Long userId,
                                              @Param("type") TransactionType type,
                                              @Param("category") Category category,
                                              @Param("dateFrom") LocalDate dateFrom,
                                              @Param("dateTo") LocalDate dateTo);

    /**
     * Find financial data by user ID and date range
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.date BETWEEN :startDate AND :endDate")
    List<FinancialData> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find financial data by user ID and amount range
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.amount BETWEEN :minAmount AND :maxAmount")
    List<FinancialData> findByUserIdAndAmountBetween(@Param("userId") Long userId,
                                                    @Param("minAmount") BigDecimal minAmount,
                                                    @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Find financial data by user ID and category
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.category = :category")
    List<FinancialData> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") Category category);

    /**
     * Find financial data by user ID and type
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.user.id = :userId AND fd.type = :type")
    List<FinancialData> findByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    /**
     * Find financial data by ID and user ID
     */
    @Query("SELECT fd FROM FinancialData fd WHERE fd.id = :id AND fd.user.id = :userId")
    Optional<FinancialData> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Get total amount by type and date range
     */
    @Query("SELECT SUM(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate)")
    BigDecimal getTotalAmountByTypeAndDateRange(@Param("userId") Long userId,
                                              @Param("type") TransactionType type,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Get count by type and date range
     */
    @Query("SELECT COUNT(fd) FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate)")
    Long getCountByTypeAndDateRange(@Param("userId") Long userId,
                                  @Param("type") TransactionType type,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /**
     * Get average amount by date range
     */
    @Query("SELECT AVG(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate)")
    BigDecimal getAverageAmountByDateRange(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Get average amount by category and date range
     */
    @Query("SELECT AVG(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND fd.category = :category " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate)")
    BigDecimal getAverageAmountByCategoryAndDateRange(@Param("userId") Long userId,
                                                    @Param("category") Category category,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Get category aggregations
     */
    @Query("SELECT fd.category, fd.type, SUM(fd.amount), COUNT(fd), AVG(fd.amount) " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate) " +
           "GROUP BY fd.category, fd.type " +
           "ORDER BY SUM(fd.amount) DESC")
    List<Object[]> getCategoryAggregations(@Param("userId") Long userId,
                                         @Param("type") TransactionType type,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Get monthly trends
     */
    @Query("SELECT YEAR(fd.date), MONTH(fd.date), SUM(fd.amount), COUNT(fd), AVG(fd.amount) " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND fd.date >= :startDate AND fd.date <= :endDate " +
           "GROUP BY YEAR(fd.date), MONTH(fd.date) " +
           "ORDER BY YEAR(fd.date) DESC, MONTH(fd.date) DESC")
    List<Object[]> getMonthlyTrends(@Param("userId") Long userId,
                                   @Param("type") TransactionType type,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Get top categories by amount
     */
    @Query("SELECT fd.category, SUM(fd.amount), COUNT(fd) " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "AND (:startDate IS NULL OR fd.date >= :startDate) " +
           "AND (:endDate IS NULL OR fd.date <= :endDate) " +
           "GROUP BY fd.category " +
           "ORDER BY SUM(fd.amount) DESC")
    List<Object[]> getTopCategoriesByAmount(@Param("userId") Long userId,
                                          @Param("type") TransactionType type,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);

    /**
     * Get daily totals for a user between dates (DB-agnostic)
     * Returns: [LocalDate, BigDecimal total]
     */
    @Query("SELECT fd.date, SUM(fd.amount) FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND fd.date BETWEEN :from AND :to GROUP BY fd.date ORDER BY fd.date")
    List<Object[]> getDailyTotals(@Param("userId") Long userId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    /**
     * Get trends by period (daily, weekly, monthly, yearly)
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN :period = 'daily' THEN CAST(fd.date AS string) " +
           "WHEN :period = 'weekly' THEN CONCAT(YEAR(fd.date), '-W', WEEK(fd.date)) " +
           "WHEN :period = 'monthly' THEN CONCAT(YEAR(fd.date), '-', MONTH(fd.date)) " +
           "WHEN :period = 'yearly' THEN CAST(YEAR(fd.date) AS string) " +
           "END as period, " +
           "SUM(fd.amount) as total_amount, " +
           "COUNT(fd.id) as transaction_count " +
           "FROM FinancialData fd WHERE fd.user.id = :userId " +
           "AND (:type IS NULL OR fd.type = :type) " +
           "GROUP BY " +
           "CASE " +
           "WHEN :period = 'daily' THEN CAST(fd.date AS string) " +
           "WHEN :period = 'weekly' THEN CONCAT(YEAR(fd.date), '-W', WEEK(fd.date)) " +
           "WHEN :period = 'monthly' THEN CONCAT(YEAR(fd.date), '-', MONTH(fd.date)) " +
           "WHEN :period = 'yearly' THEN CAST(YEAR(fd.date) AS string) " +
           "END " +
           "ORDER BY period DESC")
    List<Object[]> getTrendsByPeriod(@Param("userId") Long userId,
                                    @Param("period") String period,
                                    @Param("type") TransactionType type,
                                    Pageable pageable);
}
