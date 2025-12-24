package com.financeapp.service;

import com.financeapp.dto.CategoryCreateDto;
import com.financeapp.dto.CategoryDto;
import com.financeapp.dto.CategoryStatisticsDto;
import com.financeapp.dto.CategoryUpdateDto;
import com.financeapp.entity.Category;
import com.financeapp.entity.User;
import com.financeapp.repository.CategoryRepository;
import com.financeapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.financeapp.testsupport.TestDataUtil;
import com.financeapp.testsupport.TestDatabaseCleaner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CategoryService
 * Compatible with both H2 and PostgreSQL databases
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    @Autowired
    private TestDatabaseCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner.clean();
        // Create a fixed test user matching @WithMockUser
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password123");
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createCategory_WithValidData_ShouldCreateCategory() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto(
            "Food & Dining",
            "Restaurants, groceries, and food expenses",
            "#FF6B6B",
            "food",
            null, // No parent
            1,
            true,
            "{\"icon\": \"food\", \"color\": \"#FF6B6B\"}"
        );

        // When
        CategoryDto category = categoryService.createCategory(createDto);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.name()).isEqualTo("Food & Dining");
        assertThat(category.description()).isEqualTo("Restaurants, groceries, and food expenses");
        assertThat(category.color()).isEqualTo("#FF6B6B");
        assertThat(category.icon()).isEqualTo("food");
        assertThat(category.parentId()).isNull();
        assertThat(category.userId()).isEqualTo(testUser.getId());
        assertThat(category.sortOrder()).isEqualTo(1);
        assertThat(category.isActive()).isTrue();
        assertThat(category.isSystem()).isFalse();
        assertThat(category.usageCount()).isEqualTo(0);
        assertThat(category.metadata()).isEqualTo("{\"icon\": \"food\", \"color\": \"#FF6B6B\"}");

        // Verify in database
        Optional<Category> savedCategory = categoryRepository.findById(category.id());
        assertThat(savedCategory).isPresent();
        assertThat(savedCategory.get().getName()).isEqualTo("Food & Dining");
        assertThat(savedCategory.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createCategory_WithParent_ShouldCreateHierarchicalCategory() {
        // Given - Create parent category first
        CategoryCreateDto parentDto = new CategoryCreateDto(
            "Transportation",
            "Transport related expenses",
            "#4ECDC4",
            "car",
            null,
            1,
            true,
            null
        );
        CategoryDto parentCategory = categoryService.createCategory(parentDto);

        CategoryCreateDto childDto = new CategoryCreateDto(
            "Gas",
            "Gasoline expenses",
            "#4ECDC4",
            "gas",
            parentCategory.id(),
            1,
            true,
            null
        );

        // When
        CategoryDto childCategory = categoryService.createCategory(childDto);

        // Then
        assertThat(childCategory).isNotNull();
        assertThat(childCategory.name()).isEqualTo("Gas");
        assertThat(childCategory.parentId()).isEqualTo(parentCategory.id());
        assertThat(childCategory.parentName()).isEqualTo("Transportation");

        // Verify hierarchy
        List<CategoryDto> children = categoryService.getChildCategories(parentCategory.id());
        assertThat(children).hasSize(1);
        assertThat(children.get(0).name()).isEqualTo("Gas");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllCategories_WithMultipleCategories_ShouldReturnAllCategories() {
        // Given
        CategoryCreateDto dto1 = new CategoryCreateDto("Category 1", "Description 1", null, null, null, 1, true, null);
        CategoryCreateDto dto2 = new CategoryCreateDto("Category 2", "Description 2", null, null, null, 2, true, null);
        CategoryCreateDto dto3 = new CategoryCreateDto("Category 3", "Description 3", null, null, null, 3, false, null);

        categoryService.createCategory(dto1);
        categoryService.createCategory(dto2);
        categoryService.createCategory(dto3);

        // When
        List<CategoryDto> categories = categoryService.getAllCategories();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(CategoryDto::name)
                .containsExactlyInAnyOrder("Category 1", "Category 2", "Category 3");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getActiveCategories_WithMixedCategories_ShouldReturnOnlyActiveCategories() {
        // Given
        CategoryCreateDto activeDto = new CategoryCreateDto("Active Category", "Description", null, null, null, 1, true, null);
        CategoryCreateDto inactiveDto = new CategoryCreateDto("Inactive Category", "Description", null, null, null, 2, false, null);

        categoryService.createCategory(activeDto);
        categoryService.createCategory(inactiveDto);

        // When
        List<CategoryDto> activeCategories = categoryService.getActiveCategories();

        // Then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories.get(0).name()).isEqualTo("Active Category");
        assertThat(activeCategories.get(0).isActive()).isTrue();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getRootCategories_WithHierarchicalCategories_ShouldReturnOnlyRootCategories() {
        // Given
        CategoryCreateDto rootDto = new CategoryCreateDto("Root Category", "Description", null, null, null, 1, true, null);
        CategoryDto rootCategory = categoryService.createCategory(rootDto);

        CategoryCreateDto childDto = new CategoryCreateDto("Child Category", "Description", null, null, rootCategory.id(), 1, true, null);
        categoryService.createCategory(childDto);

        // When
        List<CategoryDto> rootCategories = categoryService.getRootCategories();

        // Then
        assertThat(rootCategories).hasSize(1);
        assertThat(rootCategories.get(0).name()).isEqualTo("Root Category");
        assertThat(rootCategories.get(0).parentId()).isNull();
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto(
            "Original Name",
            "Original Description",
            "#FF0000",
            "original-icon",
            null,
            1,
            true,
            null
        );
        CategoryDto originalCategory = categoryService.createCategory(createDto);

        CategoryUpdateDto updateDto = new CategoryUpdateDto(
            "Updated Name",
            "Updated Description",
            "#00FF00",
            "updated-icon",
            null,
            2,
            false,
            "{\"updated\": true}"
        );

        // When
        CategoryDto updatedCategory = categoryService.updateCategory(originalCategory.id(), updateDto);

        // Then
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.name()).isEqualTo("Updated Name");
        assertThat(updatedCategory.description()).isEqualTo("Updated Description");
        assertThat(updatedCategory.color()).isEqualTo("#00FF00");
        assertThat(updatedCategory.icon()).isEqualTo("updated-icon");
        assertThat(updatedCategory.sortOrder()).isEqualTo(2);
        assertThat(updatedCategory.isActive()).isFalse();
        assertThat(updatedCategory.metadata()).isEqualTo("{\"updated\": true}");
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteCategory_WithExistingCategory_ShouldDeleteCategory() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto("To Delete", "Description", null, null, null, 1, true, null);
        CategoryDto category = categoryService.createCategory(createDto);

        // When
        categoryService.deleteCategory(category.id());

        // Then
        Optional<Category> deletedCategory = categoryRepository.findById(category.id());
        assertThat(deletedCategory).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchCategories_WithMatchingName_ShouldReturnMatchingCategories() {
        // Given
        CategoryCreateDto dto1 = new CategoryCreateDto("Food & Dining", "Description", null, null, null, 1, true, null);
        CategoryCreateDto dto2 = new CategoryCreateDto("Food Delivery", "Description", null, null, null, 2, true, null);
        CategoryCreateDto dto3 = new CategoryCreateDto("Transportation", "Description", null, null, null, 3, true, null);

        categoryService.createCategory(dto1);
        categoryService.createCategory(dto2);
        categoryService.createCategory(dto3);

        // When
        List<CategoryDto> searchResults = categoryService.searchCategories("Food");

        // Then
        assertThat(searchResults).hasSize(2);
        assertThat(searchResults).extracting(CategoryDto::name)
                .containsExactlyInAnyOrder("Food & Dining", "Food Delivery");
    }

    @Test
    @WithMockUser(username = "testuser")
    void incrementUsageCount_WithExistingCategory_ShouldIncrementUsageCount() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto("Test Category", "Description", null, null, null, 1, true, null);
        CategoryDto category = categoryService.createCategory(createDto);

        // When
        categoryService.incrementUsageCount(category.id());

        // Then
        CategoryDto updatedCategory = categoryService.getCategoryById(category.id());
        assertThat(updatedCategory.usageCount()).isEqualTo(1);
        assertThat(updatedCategory.lastUsedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCategoryStatistics_WithExistingCategory_ShouldReturnStatistics() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto("Statistics Category", "Description", null, null, null, 1, true, null);
        CategoryDto category = categoryService.createCategory(createDto);

        // When
        CategoryStatisticsDto statistics = categoryService.getCategoryStatistics(category.id());

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.categoryId()).isEqualTo(category.id());
        assertThat(statistics.categoryName()).isEqualTo("Statistics Category");
        assertThat(statistics.categoryPath()).isEqualTo("Statistics Category");
        assertThat(statistics.usageCount()).isEqualTo(0);
        assertThat(statistics.childCategoriesCount()).isEqualTo(0);
        assertThat(statistics.hasChildren()).isFalse();
        assertThat(statistics.isLeafCategory()).isTrue();
    }

    @Test
    @WithMockUser(username = "testuser")
    void isCategoryNameAvailable_WithUniqueName_ShouldReturnTrue() {
        // Given
        String name = "Unique Category Name";

        // When
        boolean available = categoryService.isCategoryNameAvailable(name, null);

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @WithMockUser(username = "testuser")
    void isCategoryNameAvailable_WithExistingName_ShouldReturnFalse() {
        // Given
        CategoryCreateDto createDto = new CategoryCreateDto("Existing Name", "Description", null, null, null, 1, true, null);
        CategoryDto category = categoryService.createCategory(createDto);

        // When
        boolean available = categoryService.isCategoryNameAvailable("Existing Name", null);

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCategoryPath_WithHierarchicalCategory_ShouldReturnFullPath() {
        // Given
        CategoryCreateDto parentDto = new CategoryCreateDto("Parent", "Description", null, null, null, 1, true, null);
        CategoryDto parentCategory = categoryService.createCategory(parentDto);

        CategoryCreateDto childDto = new CategoryCreateDto("Child", "Description", null, null, parentCategory.id(), 1, true, null);
        CategoryDto childCategory = categoryService.createCategory(childDto);

        // When
        String path = categoryService.getCategoryPath(childCategory.id());

        // Then
        assertThat(path).isEqualTo("Parent > Child");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCategoryLevel_WithHierarchicalCategory_ShouldReturnCorrectLevel() {
        // Given
        CategoryCreateDto parentDto = new CategoryCreateDto("Parent", "Description", null, null, null, 1, true, null);
        CategoryDto parentCategory = categoryService.createCategory(parentDto);

        CategoryCreateDto childDto = new CategoryCreateDto("Child", "Description", null, null, parentCategory.id(), 1, true, null);
        CategoryDto childCategory = categoryService.createCategory(childDto);

        // When
        int parentLevel = categoryService.getCategoryLevel(parentCategory.id());
        int childLevel = categoryService.getCategoryLevel(childCategory.id());

        // Then
        assertThat(parentLevel).isEqualTo(0); // Root level
        assertThat(childLevel).isEqualTo(1); // Child level
    }
}
