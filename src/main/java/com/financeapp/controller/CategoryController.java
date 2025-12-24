package com.financeapp.controller;

import com.financeapp.dto.CategoryCreateDto;
import com.financeapp.dto.CategoryDto;
import com.financeapp.dto.CategoryStatisticsDto;
import com.financeapp.dto.CategoryUpdateDto;
import com.financeapp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category operations
 * Compatible with both H2 and PostgreSQL databases
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "APIs for managing hierarchical categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create category", description = "Create a new category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Category name already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryCreateDto createDto) {
        log.info("Creating category: {}", createDto.name());
        
        try {
            CategoryDto category = categoryService.createCategory(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (RuntimeException e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get category by ID", description = "Get a specific category by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Getting category by ID: {}", categoryId);
        
        try {
            CategoryDto category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            log.error("Error getting category: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all categories", description = "Get all categories for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.info("Getting all categories for current user");
        
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get active categories", description = "Get all active categories for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        log.info("Getting active categories for current user");
        
        List<CategoryDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get root categories", description = "Get root categories (no parent) for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        log.info("Getting root categories for current user");
        
        List<CategoryDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get category tree", description = "Get hierarchical category tree for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getCategoryTree() {
        log.info("Getting category tree for current user");
        
        List<CategoryDto> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get child categories", description = "Get child categories of a specific parent category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child categories retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parent category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getChildCategories(
            @Parameter(description = "Parent category ID") @PathVariable Long parentId) {
        log.info("Getting child categories for parent ID: {}", parentId);
        
        try {
            List<CategoryDto> categories = categoryService.getChildCategories(parentId);
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            log.error("Error getting child categories: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update category", description = "Update a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateDto updateDto) {
        log.info("Updating category with ID: {}", categoryId);
        
        try {
            CategoryDto category = categoryService.updateCategory(categoryId, updateDto);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            log.error("Error updating category: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete category", description = "Delete a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category has children"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting category: {}", e.getMessage());
            if (e.getMessage().contains("children")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{categoryId}/with-children")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete category with children", description = "Delete a category and all its children")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category and children deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCategoryWithChildren(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Deleting category with children for ID: {}", categoryId);
        
        try {
            categoryService.deleteCategoryWithChildren(categoryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting category with children: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search categories", description = "Search categories by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories found successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> searchCategories(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        log.info("Searching categories with term: {}", searchTerm);
        
        List<CategoryDto> categories = categoryService.searchCategories(searchTerm);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/frequently-used")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get frequently used categories", description = "Get categories with high usage count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Frequently used categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getFrequentlyUsedCategories(
            @Parameter(description = "Minimum usage count") @RequestParam(defaultValue = "10") Integer minUsage) {
        log.info("Getting frequently used categories with min usage: {}", minUsage);
        
        List<CategoryDto> categories = categoryService.getFrequentlyUsedCategories(minUsage);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/recently-used")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get recently used categories", description = "Get categories that were recently used")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recently used categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getRecentlyUsedCategories(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("Getting recently used categories for last {} days", days);
        
        List<CategoryDto> categories = categoryService.getRecentlyUsedCategories(days);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/by-color")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get categories by color", description = "Get categories filtered by color")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getCategoriesByColor(
            @Parameter(description = "Color") @RequestParam String color) {
        log.info("Getting categories by color: {}", color);
        
        List<CategoryDto> categories = categoryService.getCategoriesByColor(color);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/by-icon")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get categories by icon", description = "Get categories filtered by icon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getCategoriesByIcon(
            @Parameter(description = "Icon") @RequestParam String icon) {
        log.info("Getting categories by icon: {}", icon);
        
        List<CategoryDto> categories = categoryService.getCategoriesByIcon(icon);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/leaf")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get leaf categories", description = "Get categories with no children")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leaf categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getLeafCategories() {
        log.info("Getting leaf categories for current user");
        
        List<CategoryDto> categories = categoryService.getLeafCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get categories with pagination", description = "Get categories with pagination support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<CategoryDto>> getCategoriesWithPagination(Pageable pageable) {
        log.info("Getting categories with pagination");
        
        Page<CategoryDto> categories = categoryService.getCategoriesWithPagination(pageable);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/{categoryId}/increment-usage")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Increment category usage", description = "Increment the usage count of a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usage count incremented successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> incrementUsageCount(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Incrementing usage count for category ID: {}", categoryId);
        
        try {
            categoryService.incrementUsageCount(categoryId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error incrementing usage count: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{categoryId}/statistics")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get category statistics", description = "Get usage statistics for a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryStatisticsDto> getCategoryStatistics(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Getting statistics for category ID: {}", categoryId);
        
        try {
            CategoryStatisticsDto statistics = categoryService.getCategoryStatistics(categoryId);
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            log.error("Error getting category statistics: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all category statistics", description = "Get usage statistics for all categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryStatisticsDto>> getAllCategoryStatistics() {
        log.info("Getting statistics for all categories");
        
        List<CategoryStatisticsDto> statistics = categoryService.getAllCategoryStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bulk create categories", description = "Create multiple categories at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categories created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> bulkCreateCategories(
            @Valid @RequestBody List<CategoryCreateDto> createDtos) {
        log.info("Bulk creating {} categories", createDtos.size());
        
        List<CategoryDto> categories = categoryService.bulkCreateCategories(createDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(categories);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bulk update categories", description = "Update multiple categories at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> bulkUpdateCategories(
            @Valid @RequestBody List<CategoryService.CategoryBulkUpdateDto> updateDtos) {
        log.info("Bulk updating {} categories", updateDtos.size());
        
        List<CategoryDto> categories = categoryService.bulkUpdateCategories(updateDtos);
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bulk delete categories", description = "Delete multiple categories at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categories deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> bulkDeleteCategories(
            @RequestBody List<Long> categoryIds) {
        log.info("Bulk deleting {} categories", categoryIds.size());
        
        categoryService.bulkDeleteCategories(categoryIds);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Move category", description = "Move a category to a different parent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category moved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid hierarchy"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryDto> moveCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "New parent ID") @RequestParam Long newParentId) {
        log.info("Moving category ID: {} to parent ID: {}", categoryId, newParentId);
        
        try {
            CategoryDto category = categoryService.moveCategory(categoryId, newParentId);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            log.error("Error moving category: {}", e.getMessage());
            if (e.getMessage().contains("hierarchy")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Reorder categories", description = "Reorder categories by sort order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> reorderCategories(
            @Valid @RequestBody List<CategoryService.CategoryReorderDto> reorderDtos) {
        log.info("Reordering {} categories", reorderDtos.size());
        
        List<CategoryDto> categories = categoryService.reorderCategories(reorderDtos);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/system")
    @Operation(summary = "Get system categories", description = "Get system-defined categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System categories retrieved successfully")
    })
    public ResponseEntity<List<CategoryDto>> getSystemCategories() {
        log.info("Getting system categories");
        
        List<CategoryDto> categories = categoryService.getSystemCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/with-metadata")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get categories with metadata", description = "Get categories that have metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDto>> getCategoriesWithMetadata() {
        log.info("Getting categories with metadata");
        
        List<CategoryDto> categories = categoryService.getCategoriesWithMetadata();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}/metadata")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update category metadata", description = "Update JSON metadata for a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metadata updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CategoryDto> updateCategoryMetadata(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "JSON metadata") @RequestBody String metadata) {
        log.info("Updating metadata for category ID: {}", categoryId);
        
        try {
            CategoryDto category = categoryService.updateCategoryMetadata(categoryId, metadata);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            log.error("Error updating category metadata: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{categoryId}/path")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get category path", description = "Get the full hierarchical path of a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category path retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> getCategoryPath(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Getting category path for ID: {}", categoryId);
        
        try {
            String path = categoryService.getCategoryPath(categoryId);
            return ResponseEntity.ok(path);
        } catch (RuntimeException e) {
            log.error("Error getting category path: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{categoryId}/level")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get category level", description = "Get the hierarchical level of a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category level retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Integer> getCategoryLevel(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Getting category level for ID: {}", categoryId);
        
        try {
            int level = categoryService.getCategoryLevel(categoryId);
            return ResponseEntity.ok(level);
        } catch (RuntimeException e) {
            log.error("Error getting category level: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/available")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check category name availability", description = "Check if a category name is available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Name availability checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Boolean> checkCategoryNameAvailability(
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category ID (for updates)") @RequestParam(required = false) Long categoryId) {
        log.info("Checking category name availability: {} for category ID: {}", name, categoryId);
        
        boolean available = categoryService.isCategoryNameAvailable(name, categoryId);
        return ResponseEntity.ok(available);
    }
}
