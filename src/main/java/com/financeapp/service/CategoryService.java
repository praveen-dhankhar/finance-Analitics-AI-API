package com.financeapp.service;

import com.financeapp.dto.CategoryCreateDto;
import com.financeapp.dto.CategoryDto;
import com.financeapp.dto.CategoryStatisticsDto;
import com.financeapp.dto.CategoryUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Category operations
 * Compatible with both H2 and PostgreSQL databases
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryDto createCategory(CategoryCreateDto createDto);

    /**
     * Get category by ID
     */
    CategoryDto getCategoryById(Long categoryId);

    /**
     * Get all categories for current user
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get active categories for current user
     */
    List<CategoryDto> getActiveCategories();

    /**
     * Get root categories (no parent) for current user
     */
    List<CategoryDto> getRootCategories();

    /**
     * Get child categories by parent ID
     */
    List<CategoryDto> getChildCategories(Long parentId);

    /**
     * Get category tree (hierarchical structure)
     */
    List<CategoryDto> getCategoryTree();

    /**
     * Update category
     */
    CategoryDto updateCategory(Long categoryId, CategoryUpdateDto updateDto);

    /**
     * Delete category
     */
    void deleteCategory(Long categoryId);

    /**
     * Delete category and all its children
     */
    void deleteCategoryWithChildren(Long categoryId);

    /**
     * Search categories by name
     */
    List<CategoryDto> searchCategories(String searchTerm);

    /**
     * Get frequently used categories
     */
    List<CategoryDto> getFrequentlyUsedCategories(Integer minUsage);

    /**
     * Get recently used categories
     */
    List<CategoryDto> getRecentlyUsedCategories(int days);

    /**
     * Get categories by color
     */
    List<CategoryDto> getCategoriesByColor(String color);

    /**
     * Get categories by icon
     */
    List<CategoryDto> getCategoriesByIcon(String icon);

    /**
     * Get leaf categories (no children)
     */
    List<CategoryDto> getLeafCategories();

    /**
     * Get categories with pagination
     */
    Page<CategoryDto> getCategoriesWithPagination(Pageable pageable);

    /**
     * Increment category usage count
     */
    void incrementUsageCount(Long categoryId);

    /**
     * Check if category name exists for user
     */
    boolean isCategoryNameAvailable(String name, Long categoryId);

    /**
     * Get category statistics
     */
    CategoryStatisticsDto getCategoryStatistics(Long categoryId);

    /**
     * Get all category statistics for current user
     */
    List<CategoryStatisticsDto> getAllCategoryStatistics();

    /**
     * Bulk create categories
     */
    List<CategoryDto> bulkCreateCategories(List<CategoryCreateDto> createDtos);

    /**
     * Bulk update categories
     */
    List<CategoryDto> bulkUpdateCategories(List<CategoryBulkUpdateDto> updateDtos);

    /**
     * Bulk delete categories
     */
    void bulkDeleteCategories(List<Long> categoryIds);

    /**
     * Move category to different parent
     */
    CategoryDto moveCategory(Long categoryId, Long newParentId);

    /**
     * Reorder categories
     */
    List<CategoryDto> reorderCategories(List<CategoryReorderDto> reorderDtos);

    /**
     * Get system categories
     */
    List<CategoryDto> getSystemCategories();

    /**
     * Create system category
     */
    CategoryDto createSystemCategory(CategoryCreateDto createDto);

    /**
     * Get categories with metadata
     */
    List<CategoryDto> getCategoriesWithMetadata();

    /**
     * Update category metadata
     */
    CategoryDto updateCategoryMetadata(Long categoryId, String metadata);

    /**
     * Get category path (full hierarchy path)
     */
    String getCategoryPath(Long categoryId);

    /**
     * Get category level (depth in hierarchy)
     */
    int getCategoryLevel(Long categoryId);

    /**
     * Validate category hierarchy (no circular references)
     */
    boolean validateCategoryHierarchy(Long categoryId, Long newParentId);

    /**
     * Bulk update DTO for category operations
     */
    record CategoryBulkUpdateDto(
        Long categoryId,
        CategoryUpdateDto updateDto
    ) {}

    /**
     * Reorder DTO for category operations
     */
    record CategoryReorderDto(
        Long categoryId,
        Integer newSortOrder
    ) {}
}
