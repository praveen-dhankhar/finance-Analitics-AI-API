package com.financeapp.service.impl;

import com.financeapp.dto.CategoryCreateDto;
import com.financeapp.dto.CategoryDto;
import com.financeapp.dto.CategoryStatisticsDto;
import com.financeapp.dto.CategoryUpdateDto;
import com.financeapp.dto.mapper.CategoryMapper;
import com.financeapp.entity.Category;
import com.financeapp.entity.User;
import com.financeapp.repository.CategoryRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService
 * Compatible with both H2 and PostgreSQL databases
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    
    public CategoryServiceImpl(CategoryRepository categoryRepository, UserRepository userRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDto createCategory(CategoryCreateDto createDto) {
        log.info("Creating category: {}", createDto.name());
        
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check name uniqueness
        if (categoryRepository.countByNameForUser(createDto.name(), currentUserId, null) > 0) {
            throw new RuntimeException("Category name already exists");
        }

        // Validate parent category if provided
        Category parentCategory = null;
        if (createDto.parentId() != null) {
            parentCategory = categoryRepository.findById(createDto.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            
            if (!parentCategory.getUser().getId().equals(currentUserId)) {
                throw new RuntimeException("Parent category does not belong to current user");
            }
            
            if (!validateCategoryHierarchy(null, createDto.parentId())) {
                throw new RuntimeException("Invalid category hierarchy");
            }
        }

        Category category = categoryMapper.toEntity(createDto);
        category.setUser(user);
        category.setParent(parentCategory);
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with ID: {}", savedCategory.getId());
        
        // Fetch the saved category to get populated relationships
        Category fetchedCategory = categoryRepository.findById(savedCategory.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch created category"));
        
        return categoryMapper.toDto(fetchedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Getting category by ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        log.info("Getting all categories for current user");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findByUserId(currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        log.info("Getting active categories for current user");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findActiveByUserId(currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        log.info("Getting root categories for current user");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findRootCategoriesByUserId(currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(Long parentId) {
        log.info("Getting child categories for parent ID: {}", parentId);
        
        // Verify parent category belongs to current user
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent category not found"));
        
        if (!parentCategory.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Parent category does not belong to current user");
        }
        
        return categoryRepository.findChildrenByParentId(parentId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryTree() {
        log.info("Getting category tree for current user");
        
        Long currentUserId = getCurrentUserId();
        List<Category> rootCategories = categoryRepository.findRootCategoriesByUserId(currentUserId);
        
        return rootCategories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryUpdateDto updateDto) {
        log.info("Updating category with ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        // Check name uniqueness if being updated
        if (updateDto.name() != null) {
            if (categoryRepository.countByNameForUser(updateDto.name(), getCurrentUserId(), categoryId) > 0) {
                throw new RuntimeException("Category name already exists");
            }
        }
        
        // Validate parent category if being updated
        if (updateDto.parentId() != null) {
            if (!validateCategoryHierarchy(categoryId, updateDto.parentId())) {
                throw new RuntimeException("Invalid category hierarchy");
            }
        }
        
        categoryMapper.updateEntity(category, updateDto);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category with ID: {}", updatedCategory.getId());
        
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        // Check if category has children
        if (categoryRepository.countChildrenByParentId(categoryId) > 0) {
            throw new RuntimeException("Cannot delete category with children. Use deleteCategoryWithChildren instead.");
        }
        
        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", categoryId);
    }

    @Override
    public void deleteCategoryWithChildren(Long categoryId) {
        log.info("Deleting category with children for ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        // Recursively delete children first
        List<Category> children = categoryRepository.findChildrenByParentId(categoryId);
        for (Category child : children) {
            deleteCategoryWithChildren(child.getId());
        }
        
        categoryRepository.delete(category);
        log.info("Deleted category with children for ID: {}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String searchTerm) {
        log.info("Searching categories with term: {}", searchTerm);
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findByNameContainingIgnoreCase(searchTerm, currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getFrequentlyUsedCategories(Integer minUsage) {
        log.info("Getting frequently used categories with min usage: {}", minUsage);
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findFrequentlyUsedCategories(currentUserId, minUsage)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getRecentlyUsedCategories(int days) {
        log.info("Getting recently used categories for last {} days", days);
        
        Long currentUserId = getCurrentUserId();
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(days);
        return categoryRepository.findRecentlyUsedCategories(currentUserId, cutoffDate)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByColor(String color) {
        log.info("Getting categories by color: {}", color);
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findByColor(color, currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByIcon(String icon) {
        log.info("Getting categories by icon: {}", icon);
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findByIcon(icon, currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getLeafCategories() {
        log.info("Getting leaf categories for current user");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findLeafCategories(currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategoriesWithPagination(Pageable pageable) {
        log.info("Getting categories with pagination");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findByUserIdWithPagination(currentUserId, pageable)
                .map(categoryMapper::toDtoWithoutChildren);
    }

    @Override
    @Transactional
    public void incrementUsageCount(Long categoryId) {
        log.info("Incrementing usage count for category ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        int updated = categoryRepository.incrementUsageCount(categoryId, OffsetDateTime.now());
        if (updated == 0) {
            log.warn("No category found for ID: {}", categoryId);
        } else {
            log.info("Incremented usage count for category ID: {}", categoryId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name, Long categoryId) {
        log.info("Checking category name availability: {} for category ID: {}", name, categoryId);
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.countByNameForUser(name, currentUserId, categoryId) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatisticsDto getCategoryStatistics(Long categoryId) {
        log.info("Getting statistics for category ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        long childCount = categoryRepository.countChildrenByParentId(categoryId);
        
        return new CategoryStatisticsDto(
            category.getId(),
            category.getName(),
            category.getFullPath(),
            category.getUsageCount(),
            0, // financialDataCount - would need to join with financial_data table
            0.0, // totalAmount - would need to join with financial_data table
            0.0, // averageAmount - would need to join with financial_data table
            0.0, // minAmount - would need to join with financial_data table
            0.0, // maxAmount - would need to join with financial_data table
            category.getCreatedAt(), // firstUsedAt
            category.getLastUsedAt(),
            (int) childCount,
            childCount > 0,
            childCount == 0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatisticsDto> getAllCategoryStatistics() {
        log.info("Getting statistics for all categories");
        
        Long currentUserId = getCurrentUserId();
        List<Category> categories = categoryRepository.findActiveByUserId(currentUserId);
        
        return categories.stream()
                .map(category -> getCategoryStatistics(category.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> bulkCreateCategories(List<CategoryCreateDto> createDtos) {
        log.info("Bulk creating {} categories", createDtos.size());
        
        List<CategoryDto> createdCategories = new ArrayList<>();
        
        for (CategoryCreateDto createDto : createDtos) {
            try {
                CategoryDto createdCategory = createCategory(createDto);
                createdCategories.add(createdCategory);
            } catch (Exception e) {
                log.error("Error creating category: {}", createDto.name(), e);
                // Continue with other categories
            }
        }
        
        log.info("Bulk created {} categories", createdCategories.size());
        return createdCategories;
    }

    @Override
    public List<CategoryDto> bulkUpdateCategories(List<CategoryBulkUpdateDto> updateDtos) {
        log.info("Bulk updating {} categories", updateDtos.size());
        
        List<CategoryDto> updatedCategories = new ArrayList<>();
        
        for (CategoryBulkUpdateDto updateDto : updateDtos) {
            try {
                CategoryDto updatedCategory = updateCategory(updateDto.categoryId(), updateDto.updateDto());
                updatedCategories.add(updatedCategory);
            } catch (Exception e) {
                log.error("Error updating category ID: {}", updateDto.categoryId(), e);
                // Continue with other categories
            }
        }
        
        log.info("Bulk updated {} categories", updatedCategories.size());
        return updatedCategories;
    }

    @Override
    public void bulkDeleteCategories(List<Long> categoryIds) {
        log.info("Bulk deleting {} categories", categoryIds.size());
        
        Long currentUserId = getCurrentUserId();
        List<Category> categories = categoryRepository.findByIdsAndUserId(categoryIds, currentUserId);
        
        for (Category category : categories) {
            try {
                deleteCategoryWithChildren(category.getId());
            } catch (Exception e) {
                log.error("Error deleting category ID: {}", category.getId(), e);
                // Continue with other categories
            }
        }
        
        log.info("Bulk deleted categories");
    }

    @Override
    public CategoryDto moveCategory(Long categoryId, Long newParentId) {
        log.info("Moving category ID: {} to parent ID: {}", categoryId, newParentId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        // Validate new parent
        if (newParentId != null) {
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("New parent category not found"));
            
            if (!newParent.getUser().getId().equals(getCurrentUserId())) {
                throw new RuntimeException("New parent category does not belong to current user");
            }
            
            if (!validateCategoryHierarchy(categoryId, newParentId)) {
                throw new RuntimeException("Invalid category hierarchy");
            }
        }
        
        category.setParentId(newParentId);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Moved category ID: {} to parent ID: {}", categoryId, newParentId);
        
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> reorderCategories(List<CategoryReorderDto> reorderDtos) {
        log.info("Reordering {} categories", reorderDtos.size());
        
        List<CategoryDto> reorderedCategories = new ArrayList<>();
        
        for (CategoryReorderDto reorderDto : reorderDtos) {
            try {
                Category category = categoryRepository.findById(reorderDto.categoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                
                // Check if category belongs to current user
                if (!category.getUser().getId().equals(getCurrentUserId())) {
                    throw new RuntimeException("Category does not belong to current user");
                }
                
                category.setSortOrder(reorderDto.newSortOrder());
                Category updatedCategory = categoryRepository.save(category);
                reorderedCategories.add(categoryMapper.toDto(updatedCategory));
            } catch (Exception e) {
                log.error("Error reordering category ID: {}", reorderDto.categoryId(), e);
                // Continue with other categories
            }
        }
        
        log.info("Reordered {} categories", reorderedCategories.size());
        return reorderedCategories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getSystemCategories() {
        log.info("Getting system categories");
        
        return categoryRepository.findSystemCategories()
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto createSystemCategory(CategoryCreateDto createDto) {
        log.info("Creating system category: {}", createDto.name());
        
        // Only allow system categories to be created by admin users
        // This would need proper role-based authorization
        
        Category category = categoryMapper.toEntity(createDto);
        category.setIsSystem(true);
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Created system category with ID: {}", savedCategory.getId());
        
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesWithMetadata() {
        log.info("Getting categories with metadata");
        
        Long currentUserId = getCurrentUserId();
        return categoryRepository.findCategoriesWithMetadata(currentUserId)
                .stream()
                .map(categoryMapper::toDtoWithoutChildren)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategoryMetadata(Long categoryId, String metadata) {
        log.info("Updating metadata for category ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        category.setMetadata(metadata);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated metadata for category ID: {}", categoryId);
        
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public String getCategoryPath(Long categoryId) {
        log.info("Getting category path for ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        return category.getFullPath();
    }

    @Override
    @Transactional(readOnly = true)
    public int getCategoryLevel(Long categoryId) {
        log.info("Getting category level for ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if category belongs to current user
        if (!category.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Category does not belong to current user");
        }
        
        return category.getLevel();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCategoryHierarchy(Long categoryId, Long newParentId) {
        log.info("Validating category hierarchy: {} -> {}", categoryId, newParentId);
        
        // Check for circular references
        if (categoryId != null && categoryId.equals(newParentId)) {
            return false;
        }
        
        // Check if newParentId would create a circular reference
        if (newParentId != null) {
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("New parent category not found"));
            
            // Traverse up the hierarchy to check for circular reference
            Category current = newParent;
            while (current.getParentId() != null) {
                if (current.getParentId().equals(categoryId)) {
                    return false; // Circular reference detected
                }
                current = categoryRepository.findById(current.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent category not found"));
            }
        }
        
        return true;
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getId();
    }
}
