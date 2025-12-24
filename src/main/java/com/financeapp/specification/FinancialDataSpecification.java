package com.financeapp.specification;

import com.financeapp.entity.FinancialData;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA Specifications for FinancialData queries
 * Database-agnostic specifications that work with both H2 and PostgreSQL
 */
public class FinancialDataSpecification {

    public static Specification<FinancialData> buildSpecification(
            String type, String category, LocalDate dateFrom, LocalDate dateTo,
            BigDecimal amountFrom, BigDecimal amountTo, String search) {
        
        return Specification.where(hasType(type))
                .and(hasCategory(category))
                .and(hasDateFrom(dateFrom))
                .and(hasDateTo(dateTo))
                .and(hasAmountFrom(amountFrom))
                .and(hasAmountTo(amountTo))
                .and(hasDescriptionContaining(search));
    }

    public static Specification<FinancialData> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(type)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<FinancialData> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(category)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<FinancialData> hasDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
        };
    }

    public static Specification<FinancialData> hasDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
        };
    }

    public static Specification<FinancialData> hasAmountFrom(BigDecimal amountFrom) {
        return (root, query, criteriaBuilder) -> {
            if (amountFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), amountFrom);
        };
    }

    public static Specification<FinancialData> hasAmountTo(BigDecimal amountTo) {
        return (root, query, criteriaBuilder) -> {
            if (amountTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), amountTo);
        };
    }

    public static Specification<FinancialData> hasDescriptionContaining(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), 
                "%" + search.toLowerCase() + "%"
            );
        };
    }

    public static Specification<FinancialData> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<FinancialData> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return criteriaBuilder.conjunction();
            }
            
            Predicate predicate = criteriaBuilder.conjunction();
            
            if (minAmount != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            
            if (maxAmount != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            
            return predicate;
        };
    }

    public static Specification<FinancialData> hasDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            
            Predicate predicate = criteriaBuilder.conjunction();
            
            if (startDate != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            
            if (endDate != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }
            
            return predicate;
        };
    }

    /**
     * Database-optimized specification for PostgreSQL JSON operations
     * Falls back to standard operations for H2
     */
    public static Specification<FinancialData> hasCustomField(String fieldName, String fieldValue) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(fieldName) || !StringUtils.hasText(fieldValue)) {
                return criteriaBuilder.conjunction();
            }
            
            // For PostgreSQL, we could use JSON operations here
            // For H2, we'll use standard string operations
            // This is a placeholder for future JSON field support
            return criteriaBuilder.conjunction();
        };
    }
}
