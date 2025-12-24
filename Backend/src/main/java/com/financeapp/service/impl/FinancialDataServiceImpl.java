package com.financeapp.service.impl;

import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.dto.FinancialDataResponseDto;
import com.financeapp.dto.mapper.FinancialDataMapper;
import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
import com.financeapp.exception.FinancialDataNotFoundException;
import com.financeapp.exception.ValidationException;
import com.financeapp.repository.FinancialDataRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.FinancialDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialDataServiceImpl implements FinancialDataService {

    private static final Logger logger = LoggerFactory.getLogger(FinancialDataServiceImpl.class);

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialDataMapper financialDataMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialDataResponseDto> getAllFinancialData(Specification<FinancialData> spec, Pageable pageable) {
        logger.info("Fetching financial data with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        // Add user filter to specification
        Long currentUserId = getCurrentUserId();
        Specification<FinancialData> userSpec = spec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), currentUserId));
        
        Page<FinancialData> financialDataPage = financialDataRepository.findAll(userSpec, pageable);
        
        return financialDataPage.map(financialDataMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinancialDataResponseDto> getFinancialDataById(Long id) {
        logger.info("Fetching financial data with ID: {}", id);
        
        Long currentUserId = getCurrentUserId();
        Optional<FinancialData> financialData = financialDataRepository.findByIdAndUserId(id, currentUserId);
        
        return financialData.map(financialDataMapper::toResponseDto);
    }

    @Override
    public FinancialDataResponseDto createFinancialData(FinancialDataDto financialDataDto) {
        logger.info("Creating new financial data - type: {}, category: {}, amount: {}", 
                   financialDataDto.type(), financialDataDto.category(), financialDataDto.amount());
        
        validateFinancialData(financialDataDto);
        
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        FinancialData financialData = new FinancialData();
        financialData.setUser(user);
        financialData.setDate(financialDataDto.date());
        financialData.setAmount(financialDataDto.amount());
        financialData.setCategory(Category.valueOf(financialDataDto.category()));
        financialData.setDescription(financialDataDto.description());
        financialData.setType(TransactionType.valueOf(financialDataDto.type()));
        financialData.setCreatedAt(java.time.OffsetDateTime.now());
        financialData.setUpdatedAt(java.time.OffsetDateTime.now());
        
        FinancialData savedFinancialData = financialDataRepository.save(financialData);
        
        logger.info("Financial data created successfully with ID: {}", savedFinancialData.getId());
        return financialDataMapper.toResponseDto(savedFinancialData);
    }

    @Override
    public FinancialDataResponseDto createFinancialData(FinancialDataCreateDto financialDataCreateDto) {
        logger.info("Creating new financial data (create DTO) - type: {}, category: {}, amount: {}",
                financialDataCreateDto.type(), financialDataCreateDto.category(), financialDataCreateDto.amount());

        if (financialDataCreateDto == null) {
            throw new ValidationException("Financial data cannot be null");
        }
        if (financialDataCreateDto.amount() == null || financialDataCreateDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        if (financialDataCreateDto.type() == null || financialDataCreateDto.type().trim().isEmpty()) {
            throw new ValidationException("Type is required");
        }
        if (financialDataCreateDto.category() == null || financialDataCreateDto.category().trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        if (financialDataCreateDto.date() == null) {
            throw new ValidationException("Date is required");
        }

        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ValidationException("User not found"));

        FinancialData financialData = new FinancialData();
        financialData.setUser(user);
        financialData.setDate(financialDataCreateDto.date());
        financialData.setAmount(financialDataCreateDto.amount());
        financialData.setCategory(Category.valueOf(financialDataCreateDto.category()));
        financialData.setDescription(financialDataCreateDto.description());
        financialData.setType(TransactionType.valueOf(financialDataCreateDto.type()));
        financialData.setCreatedAt(java.time.OffsetDateTime.now());
        financialData.setUpdatedAt(java.time.OffsetDateTime.now());

        FinancialData saved = financialDataRepository.save(financialData);
        return financialDataMapper.toResponseDto(saved);
    }

    @Override
    public Optional<FinancialDataResponseDto> updateFinancialData(Long id, FinancialDataDto financialDataDto) {
        logger.info("Updating financial data with ID: {}", id);
        
        validateFinancialData(financialDataDto);
        
        Long currentUserId = getCurrentUserId();
        Optional<FinancialData> existingFinancialData = financialDataRepository.findByIdAndUserId(id, currentUserId);
        
        if (existingFinancialData.isEmpty()) {
            logger.warn("Financial data not found with ID: {} for user: {}", id, currentUserId);
            return Optional.empty();
        }
        
        FinancialData financialData = existingFinancialData.get();
        if (financialDataDto.date() != null) {
            financialData.setDate(financialDataDto.date());
        }
        if (financialDataDto.amount() != null) {
            financialData.setAmount(financialDataDto.amount());
        }
        if (financialDataDto.category() != null) {
            financialData.setCategory(Category.valueOf(financialDataDto.category()));
        }
        if (financialDataDto.description() != null) {
            financialData.setDescription(financialDataDto.description());
        }
        if (financialDataDto.type() != null) {
            financialData.setType(TransactionType.valueOf(financialDataDto.type()));
        }
        financialData.setUpdatedAt(java.time.OffsetDateTime.now());
        
        FinancialData updatedFinancialData = financialDataRepository.save(financialData);
        
        logger.info("Financial data updated successfully with ID: {}", updatedFinancialData.getId());
        return Optional.of(financialDataMapper.toResponseDto(updatedFinancialData));
    }

    @Override
    public boolean deleteFinancialData(Long id) {
        logger.info("Deleting financial data with ID: {}", id);
        
        Long currentUserId = getCurrentUserId();
        Optional<FinancialData> financialData = financialDataRepository.findByIdAndUserId(id, currentUserId);
        
        if (financialData.isEmpty()) {
            logger.warn("Financial data not found with ID: {} for user: {}", id, currentUserId);
            return false;
        }
        
        financialDataRepository.delete(financialData.get());
        
        logger.info("Financial data deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    @Transactional
    public Map<String, Object> bulkCreateFinancialData(List<FinancialDataDto> financialDataList) {
        logger.info("Bulk creating {} financial data records", financialDataList.size());
        
        if (financialDataList == null || financialDataList.isEmpty()) {
            throw new ValidationException("Financial data list cannot be empty");
        }
        
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        List<FinancialData> validFinancialData = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < financialDataList.size(); i++) {
            try {
                FinancialDataDto dto = financialDataList.get(i);
                validateFinancialData(dto);
                
                FinancialData financialData = new FinancialData();
                financialData.setUser(user);
                financialData.setDate(dto.date());
                financialData.setAmount(dto.amount());
                financialData.setCategory(Category.valueOf(dto.category()));
                financialData.setDescription(dto.description());
                financialData.setType(TransactionType.valueOf(dto.type()));
                financialData.setCreatedAt(java.time.OffsetDateTime.now());
                financialData.setUpdatedAt(java.time.OffsetDateTime.now());
                
                validFinancialData.add(financialData);
            } catch (Exception e) {
                errors.add(String.format("Record %d: %s", i + 1, e.getMessage()));
            }
        }
        
        List<FinancialData> savedFinancialData = financialDataRepository.saveAll(validFinancialData);
        
        Map<String, Object> result = new HashMap<>();
        result.put("createdCount", savedFinancialData.size());
        result.put("failedCount", errors.size());
        result.put("errors", errors);
        result.put("createdIds", savedFinancialData.stream().map(FinancialData::getId).collect(Collectors.toList()));
        
        logger.info("Bulk creation completed - created: {}, failed: {}", 
                   savedFinancialData.size(), errors.size());
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public String exportFinancialData(String format, String type, String category, LocalDate dateFrom, LocalDate dateTo) {
        logger.info("Exporting financial data in {} format", format);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = (type != null && !type.trim().isEmpty()) 
                ? TransactionType.valueOf(type.toUpperCase()) : null;
        Category categoryEnum = (category != null && !category.trim().isEmpty()) 
                ? Category.valueOf(category.toUpperCase()) : null;
        List<FinancialData> financialDataList = financialDataRepository.findByUserIdAndFilters(
                currentUserId, transactionType, categoryEnum, dateFrom, dateTo);
        
        if ("csv".equalsIgnoreCase(format)) {
            return exportToCsv(financialDataList);
        } else if ("json".equalsIgnoreCase(format)) {
            return exportToJson(financialDataList);
        } else {
            throw new ValidationException("Unsupported export format: " + format);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFinancialSummaries(LocalDate dateFrom, LocalDate dateTo) {
        logger.info("Fetching financial summaries - dateFrom: {}, dateTo: {}", dateFrom, dateTo);
        
        Long currentUserId = getCurrentUserId();
        
        // Use database-optimized queries
        BigDecimal totalIncome = financialDataRepository.getTotalAmountByTypeAndDateRange(
                currentUserId, TransactionType.INCOME, dateFrom, dateTo);
        BigDecimal totalExpense = financialDataRepository.getTotalAmountByTypeAndDateRange(
                currentUserId, TransactionType.EXPENSE, dateFrom, dateTo);
        Long totalTransactions = financialDataRepository.getCountByTypeAndDateRange(
                currentUserId, null, dateFrom, dateTo);
        BigDecimal averageAmount = financialDataRepository.getAverageAmountByDateRange(
                currentUserId, dateFrom, dateTo);
        
        Map<String, Object> summaries = new HashMap<>();
        summaries.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summaries.put("totalExpense", totalExpense != null ? totalExpense : BigDecimal.ZERO);
        summaries.put("netAmount", (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO));
        summaries.put("totalTransactions", totalTransactions != null ? totalTransactions : 0L);
        summaries.put("averageAmount", averageAmount != null ? averageAmount : BigDecimal.ZERO);
        summaries.put("dateFrom", dateFrom);
        summaries.put("dateTo", dateTo);
        
        logger.info("Financial summaries retrieved successfully");
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategoryAggregations(String type, LocalDate dateFrom, LocalDate dateTo) {
        logger.info("Fetching category aggregations - type: {}, dateFrom: {}, dateTo: {}", type, dateFrom, dateTo);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = (type != null && !type.trim().isEmpty()) 
                ? TransactionType.valueOf(type.toUpperCase()) : null;
        List<Object[]> results = financialDataRepository.getCategoryAggregations(
                currentUserId, transactionType, dateFrom, dateTo);
        
        List<Map<String, Object>> aggregations = results.stream()
                .map(row -> {
                    Map<String, Object> aggregation = new HashMap<>();
                    aggregation.put("category", row[0]);
                    aggregation.put("type", row[1]);
                    aggregation.put("totalAmount", row[2]);
                    aggregation.put("transactionCount", row[3]);
                    aggregation.put("averageAmount", row[4]);
                    return aggregation;
                })
                .collect(Collectors.toList());
        
        logger.info("Category aggregations retrieved - {} categories", aggregations.size());
        return aggregations;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyTrends(String type, int months) {
        logger.info("Fetching monthly trends - type: {}, months: {}", type, months);
        
        Long currentUserId = getCurrentUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);
        
        TransactionType transactionType = (type != null && !type.trim().isEmpty()) 
                ? TransactionType.valueOf(type.toUpperCase()) : null;
        List<Object[]> results = financialDataRepository.getMonthlyTrends(
                currentUserId, transactionType, startDate, endDate);
        
        List<Map<String, Object>> trends = results.stream()
                .map(row -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("year", row[0]);
                    trend.put("month", row[1]);
                    trend.put("totalAmount", row[2]);
                    trend.put("transactionCount", row[3]);
                    trend.put("averageAmount", row[4]);
                    return trend;
                })
                .collect(Collectors.toList());
        
        logger.info("Monthly trends retrieved - {} months", trends.size());
        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFinancialDataStatistics() {
        logger.info("Fetching financial data statistics");
        
        Long currentUserId = getCurrentUserId();
        List<Object[]> stats = financialDataRepository.getFinancialDataStatistics(currentUserId);
        
        if (stats.isEmpty()) {
            return new HashMap<>();
        }
        
        Object[] stat = stats.get(0);
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", stat[0]);
        statistics.put("totalAmount", stat[1]);
        statistics.put("averageAmount", stat[2]);
        statistics.put("minAmount", stat[3]);
        statistics.put("maxAmount", stat[4]);
        
        logger.info("Financial data statistics retrieved successfully");
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialDataResponseDto> searchFinancialData(Specification<FinancialData> spec, Pageable pageable) {
        return getAllFinancialData(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialDataResponseDto> getFinancialDataByUserId(Long userId, Pageable pageable) {
        logger.info("Fetching financial data for user ID: {}", userId);
        
        Page<FinancialData> financialDataPage = financialDataRepository.findByUserId(userId, pageable);
        return financialDataPage.map(financialDataMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialDataResponseDto> getFinancialDataByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching financial data by date range - startDate: {}, endDate: {}", startDate, endDate);
        
        Long currentUserId = getCurrentUserId();
        List<FinancialData> financialDataList = financialDataRepository.findByUserIdAndDateBetween(
                currentUserId, startDate, endDate);
        
        return financialDataList.stream()
                .map(financialDataMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialDataResponseDto> getFinancialDataByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        logger.info("Fetching financial data by amount range - minAmount: {}, maxAmount: {}", minAmount, maxAmount);
        
        Long currentUserId = getCurrentUserId();
        List<FinancialData> financialDataList = financialDataRepository.findByUserIdAndAmountBetween(
                currentUserId, minAmount, maxAmount);
        
        return financialDataList.stream()
                .map(financialDataMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialDataResponseDto> getFinancialDataByCategory(String category) {
        logger.info("Fetching financial data by category: {}", category);
        
        Long currentUserId = getCurrentUserId();
        Category categoryEnum = Category.valueOf(category.toUpperCase());
        List<FinancialData> financialDataList = financialDataRepository.findByUserIdAndCategory(
                currentUserId, categoryEnum);
        
        return financialDataList.stream()
                .map(financialDataMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialDataResponseDto> getFinancialDataByType(String type) {
        logger.info("Fetching financial data by type: {}", type);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        List<FinancialData> financialDataList = financialDataRepository.findByUserIdAndType(
                currentUserId, transactionType);
        
        return financialDataList.stream()
                .map(financialDataMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByTypeAndDateRange(String type, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching total amount by type and date range - type: {}, startDate: {}, endDate: {}", 
                   type, startDate, endDate);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        BigDecimal total = financialDataRepository.getTotalAmountByTypeAndDateRange(
                currentUserId, transactionType, startDate, endDate);
        
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageAmountByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching average amount by category and date range - category: {}, startDate: {}, endDate: {}", 
                   category, startDate, endDate);
        
        Long currentUserId = getCurrentUserId();
        Category categoryEnum = Category.valueOf(category.toUpperCase());
        BigDecimal average = financialDataRepository.getAverageAmountByCategoryAndDateRange(
                currentUserId, categoryEnum, startDate, endDate);
        
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCountByTypeAndDateRange(String type, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching count by type and date range - type: {}, startDate: {}, endDate: {}", 
                   type, startDate, endDate);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        Long count = financialDataRepository.getCountByTypeAndDateRange(
                currentUserId, transactionType, startDate, endDate);
        
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopCategoriesByAmount(String type, LocalDate startDate, LocalDate endDate, int limit) {
        logger.info("Fetching top categories by amount - type: {}, startDate: {}, endDate: {}, limit: {}", 
                   type, startDate, endDate, limit);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = (type != null && !type.trim().isEmpty()) 
                ? TransactionType.valueOf(type.toUpperCase()) : null;
        List<Object[]> results = financialDataRepository.getTopCategoriesByAmount(
                currentUserId, transactionType, startDate, endDate, PageRequest.of(0, limit));
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> category = new HashMap<>();
                    category.put("category", row[0]);
                    category.put("totalAmount", row[1]);
                    category.put("transactionCount", row[2]);
                    return category;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrendsByPeriod(String period, String type, int limit) {
        logger.info("Fetching trends by period - period: {}, type: {}, limit: {}", period, type, limit);
        
        Long currentUserId = getCurrentUserId();
        TransactionType transactionType = (type != null && !type.trim().isEmpty()) 
                ? TransactionType.valueOf(type.toUpperCase()) : null;
        List<Object[]> results = financialDataRepository.getTrendsByPeriod(
                currentUserId, period, transactionType, PageRequest.of(0, limit));
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("period", row[0]);
                    trend.put("totalAmount", row[1]);
                    trend.put("transactionCount", row[2]);
                    return trend;
                })
                .collect(Collectors.toList());
    }

    // Helper methods

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new ValidationException("User not found"));
    }

    private void validateFinancialData(FinancialDataDto financialDataDto) {
        if (financialDataDto == null) {
            throw new ValidationException("Financial data cannot be null");
        }
        
        if (financialDataDto.amount() == null || financialDataDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        
        if (financialDataDto.type() == null || financialDataDto.type().toString().trim().isEmpty()) {
            throw new ValidationException("Type is required");
        }
        
        if (financialDataDto.category() == null || financialDataDto.category().toString().trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        
        if (financialDataDto.date() == null) {
            throw new ValidationException("Date is required");
        }
    }

    private String exportToCsv(List<FinancialData> financialDataList) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Category,Amount,Date,Description\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (FinancialData data : financialDataList) {
            csv.append(data.getId()).append(",")
               .append(data.getType()).append(",")
               .append(data.getCategory()).append(",")
               .append(data.getAmount()).append(",")
               .append(data.getDate().format(formatter)).append(",")
               .append("\"").append(data.getDescription().replace("\"", "\"\"")).append("\"\n");
        }
        
        return csv.toString();
    }

    private String exportToJson(List<FinancialData> financialDataList) {
        List<Map<String, Object>> jsonData = financialDataList.stream()
                .map(financialDataMapper::toResponseDto)
                .map(dto -> {
                    Map<String, Object> json = new HashMap<>();
                    json.put("id", dto.id());
                    json.put("type", dto.type());
                    json.put("category", dto.category());
                    json.put("amount", dto.amount());
                    json.put("date", dto.date());
                    json.put("description", dto.description());
                    json.put("createdAt", dto.createdAt());
                    json.put("updatedAt", dto.updatedAt());
                    return json;
                })
                .collect(Collectors.toList());
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return mapper.writeValueAsString(jsonData);
        } catch (Exception e) {
            logger.error("Error converting to JSON", e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
