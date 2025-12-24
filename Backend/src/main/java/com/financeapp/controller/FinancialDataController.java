package com.financeapp.controller;

import com.financeapp.dto.FinancialDataDto;
import com.financeapp.dto.FinancialDataCreateDto;
import com.financeapp.dto.FinancialDataResponseDto;
import com.financeapp.dto.mapper.FinancialDataMapper;
import com.financeapp.entity.FinancialData;
import com.financeapp.service.FinancialDataService;
import com.financeapp.specification.FinancialDataSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/financial-data")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Financial Data", description = "Financial data management API")
public class FinancialDataController {

    private static final Logger logger = LoggerFactory.getLogger(FinancialDataController.class);

    @Autowired
    private FinancialDataService financialDataService;

    @Autowired
    private FinancialDataMapper financialDataMapper;

    @Operation(summary = "Get all financial data with pagination and filtering")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<FinancialDataResponseDto>> getAllFinancialData(
            @Parameter(description = "Filter by type (INCOME, EXPENSE)") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by date from (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Filter by amount from") @RequestParam(required = false) BigDecimal amountFrom,
            @Parameter(description = "Filter by amount to") @RequestParam(required = false) BigDecimal amountTo,
            @Parameter(description = "Search in description") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        
        logger.info("Fetching financial data with filters - type: {}, category: {}, dateFrom: {}, dateTo: {}, search: {}", 
                   type, category, dateFrom, dateTo, search);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                type, category, dateFrom, dateTo, amountFrom, amountTo, search);
        
        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, pageable);
        
        logger.info("Retrieved {} financial data records", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get financial data by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FinancialDataResponseDto> getFinancialDataById(@PathVariable Long id) {
        logger.info("Fetching financial data with ID: {}", id);
        
        Optional<FinancialDataResponseDto> result = financialDataService.getFinancialDataById(id);
        
        return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new financial data")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FinancialDataResponseDto> createFinancialData(@Valid @RequestBody FinancialDataCreateDto dto) {
        logger.info("Creating new financial data - type: {}, category: {}, amount: {}", dto.type(), dto.category(), dto.amount());
        FinancialDataResponseDto result = financialDataService.createFinancialData(dto);
        logger.info("Financial data created successfully with ID: {}", result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Update financial data")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FinancialDataResponseDto> updateFinancialData(
            @PathVariable Long id, 
            @Valid @RequestBody FinancialDataDto financialDataDto) {
        
        logger.info("Updating financial data with ID: {}", id);
        
        Optional<FinancialDataResponseDto> result = financialDataService.updateFinancialData(id, financialDataDto);
        
        return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete financial data")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteFinancialData(@PathVariable Long id) {
        logger.info("Deleting financial data with ID: {}", id);
        
        boolean deleted = financialDataService.deleteFinancialData(id);
        
        if (deleted) {
            logger.info("Financial data deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search financial data with advanced filters")
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<FinancialDataResponseDto>> searchFinancialData(
            @Parameter(description = "Filter by type (INCOME, EXPENSE)") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by date from (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Filter by amount from") @RequestParam(required = false) BigDecimal amountFrom,
            @Parameter(description = "Filter by amount to") @RequestParam(required = false) BigDecimal amountTo,
            @Parameter(description = "Search in description") @RequestParam(required = false) String search,
            @Parameter(description = "Sort by field") @RequestParam(required = false, defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(required = false, defaultValue = "DESC") String sortDirection,
            @PageableDefault(size = 50) Pageable pageable) {
        
        logger.info("Advanced search for financial data - type: {}, category: {}, dateFrom: {}, dateTo: {}, search: {}", 
                   type, category, dateFrom, dateTo, search);

        // Create custom sort
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<FinancialData> spec = FinancialDataSpecification.buildSpecification(
                type, category, dateFrom, dateTo, amountFrom, amountTo, search);
        
        Page<FinancialDataResponseDto> result = financialDataService.getAllFinancialData(spec, customPageable);
        
        logger.info("Search returned {} financial data records", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Bulk create financial data")
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> bulkCreateFinancialData(@Valid @RequestBody List<FinancialDataDto> financialDataList) {
        logger.info("Bulk creating {} financial data records", financialDataList.size());
        
        Map<String, Object> result = financialDataService.bulkCreateFinancialData(financialDataList);
        
        logger.info("Bulk creation completed - created: {}, failed: {}", 
                   result.get("createdCount"), result.get("failedCount"));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Export financial data")
    @GetMapping("/export")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> exportFinancialData(
            @Parameter(description = "Export format (csv, json)") @RequestParam(defaultValue = "csv") String format,
            @Parameter(description = "Filter by type") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        logger.info("Exporting financial data in {} format", format);
        
        String exportData = financialDataService.exportFinancialData(format, type, category, dateFrom, dateTo);
        
        HttpHeaders headers = new HttpHeaders();
        if ("csv".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "financial-data.csv");
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "financial-data.json");
        }
        
        logger.info("Financial data exported successfully");
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }

    @Operation(summary = "Get financial summaries")
    @GetMapping("/summaries")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getFinancialSummaries(
            @Parameter(description = "Filter by date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        logger.info("Fetching financial summaries - dateFrom: {}, dateTo: {}", dateFrom, dateTo);
        
        Map<String, Object> summaries = financialDataService.getFinancialSummaries(dateFrom, dateTo);
        
        logger.info("Financial summaries retrieved successfully");
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "Get category aggregations")
    @GetMapping("/categories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Map<String, Object>>> getCategoryAggregations(
            @Parameter(description = "Filter by type") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        logger.info("Fetching category aggregations - type: {}, dateFrom: {}, dateTo: {}", type, dateFrom, dateTo);
        
        List<Map<String, Object>> aggregations = financialDataService.getCategoryAggregations(type, dateFrom, dateTo);
        
        logger.info("Category aggregations retrieved - {} categories", aggregations.size());
        return ResponseEntity.ok(aggregations);
    }

    @Operation(summary = "Get monthly trends")
    @GetMapping("/trends/monthly")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends(
            @Parameter(description = "Filter by type") @RequestParam(required = false) String type,
            @Parameter(description = "Number of months to include") @RequestParam(defaultValue = "12") int months) {
        
        logger.info("Fetching monthly trends - type: {}, months: {}", type, months);
        
        List<Map<String, Object>> trends = financialDataService.getMonthlyTrends(type, months);
        
        logger.info("Monthly trends retrieved - {} months", trends.size());
        return ResponseEntity.ok(trends);
    }
}
