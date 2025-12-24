package com.financeapp.service;

import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.dto.FinancialDataResponseDto;
import com.financeapp.entity.FinancialData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for FinancialData operations
 * Provides database-agnostic financial data management
 */
public interface FinancialDataService {

    /**
     * Get all financial data with pagination and filtering
     */
    Page<FinancialDataResponseDto> getAllFinancialData(Specification<FinancialData> spec, Pageable pageable);

    /**
     * Get financial data by ID
     */
    Optional<FinancialDataResponseDto> getFinancialDataById(Long id);

    /**
     * Create new financial data
     */
    FinancialDataResponseDto createFinancialData(FinancialDataDto financialDataDto);

    FinancialDataResponseDto createFinancialData(FinancialDataCreateDto financialDataCreateDto);

    /**
     * Update financial data
     */
    Optional<FinancialDataResponseDto> updateFinancialData(Long id, FinancialDataDto financialDataDto);

    /**
     * Delete financial data
     */
    boolean deleteFinancialData(Long id);

    /**
     * Bulk create financial data (optimized for both H2 and PostgreSQL)
     */
    Map<String, Object> bulkCreateFinancialData(List<FinancialDataDto> financialDataList);

    /**
     * Export financial data in specified format
     */
    String exportFinancialData(String format, String type, String category, LocalDate dateFrom, LocalDate dateTo);

    /**
     * Get financial summaries with database-optimized queries
     */
    Map<String, Object> getFinancialSummaries(LocalDate dateFrom, LocalDate dateTo);

    /**
     * Get category aggregations using database-agnostic GROUP BY operations
     */
    List<Map<String, Object>> getCategoryAggregations(String type, LocalDate dateFrom, LocalDate dateTo);

    /**
     * Get monthly trends with database-specific optimizations
     */
    List<Map<String, Object>> getMonthlyTrends(String type, int months);

    /**
     * Get financial data statistics
     */
    Map<String, Object> getFinancialDataStatistics();

    /**
     * Search financial data with advanced filters
     */
    Page<FinancialDataResponseDto> searchFinancialData(Specification<FinancialData> spec, Pageable pageable);

    /**
     * Get financial data by user ID with pagination
     */
    Page<FinancialDataResponseDto> getFinancialDataByUserId(Long userId, Pageable pageable);

    /**
     * Get financial data by date range
     */
    List<FinancialDataResponseDto> getFinancialDataByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get financial data by amount range
     */
    List<FinancialDataResponseDto> getFinancialDataByAmountRange(java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount);

    /**
     * Get financial data by type
     */
    List<FinancialDataResponseDto> getFinancialDataByType(String type);

    /**
     * Get financial data by category
     */
    List<FinancialDataResponseDto> getFinancialDataByCategory(String category);

    /**
     * Get total amount by type and date range
     */
    java.math.BigDecimal getTotalAmountByTypeAndDateRange(String type, LocalDate startDate, LocalDate endDate);

    /**
     * Get average amount by category and date range
     */
    java.math.BigDecimal getAverageAmountByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate);

    /**
     * Get count by type and date range
     */
    Long getCountByTypeAndDateRange(String type, LocalDate startDate, LocalDate endDate);

    /**
     * Get top categories by amount
     */
    List<Map<String, Object>> getTopCategoriesByAmount(String type, LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Get financial data trends by period
     */
    List<Map<String, Object>> getTrendsByPeriod(String period, String type, int limit);
}
