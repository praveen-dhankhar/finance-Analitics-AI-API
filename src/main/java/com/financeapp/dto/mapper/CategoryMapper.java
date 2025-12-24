package com.financeapp.dto.mapper;

import com.financeapp.dto.CategoryCreateDto;
import com.financeapp.dto.CategoryDto;
import com.financeapp.dto.CategoryUpdateDto;
import com.financeapp.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Category entity and DTOs
 * Compatible with both H2 and PostgreSQL databases
 */
@Component
public class CategoryMapper {

    /**
     * Convert Category entity to CategoryDto
     */
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getColor(),
            category.getIcon(),
            category.getParentId(),
            category.getParent() != null ? category.getParent().getName() : null,
            category.getUser() != null ? category.getUser().getId() : null,
            category.getUser() != null ? category.getUser().getUsername() : null,
            category.getSortOrder(),
            category.getIsActive(),
            category.getIsSystem(),
            category.getUsageCount(),
            category.getLastUsedAt(),
            category.getMetadata(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            mapChildrenToDto(category.getChildren())
        );
    }

    /**
     * Convert CategoryCreateDto to Category entity
     */
    public Category toEntity(CategoryCreateDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setColor(dto.color());
        category.setIcon(dto.icon());
        // parentId will be set automatically when parent is set
        category.setSortOrder(dto.sortOrder() != null ? dto.sortOrder() : 0);
        category.setIsActive(dto.isActive() != null ? dto.isActive() : true);
        category.setIsSystem(false);
        category.setUsageCount(0);
        category.setMetadata(dto.metadata());

        return category;
    }

    /**
     * Update Category entity with CategoryUpdateDto
     */
    public void updateEntity(Category category, CategoryUpdateDto dto) {
        if (category == null || dto == null) {
            return;
        }

        if (dto.name() != null) {
            category.setName(dto.name());
        }
        if (dto.description() != null) {
            category.setDescription(dto.description());
        }
        if (dto.color() != null) {
            category.setColor(dto.color());
        }
        if (dto.icon() != null) {
            category.setIcon(dto.icon());
        }
        if (dto.parentId() != null) {
            category.setParentId(dto.parentId());
        }
        if (dto.sortOrder() != null) {
            category.setSortOrder(dto.sortOrder());
        }
        if (dto.isActive() != null) {
            category.setIsActive(dto.isActive());
        }
        if (dto.metadata() != null) {
            category.setMetadata(dto.metadata());
        }
    }

    /**
     * Convert CategoryUpdateDto to Category entity (for partial updates)
     */
    public Category toEntity(CategoryUpdateDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setColor(dto.color());
        category.setIcon(dto.icon());
        category.setParentId(dto.parentId());
        category.setSortOrder(dto.sortOrder());
        category.setIsActive(dto.isActive());
        category.setMetadata(dto.metadata());

        return category;
    }

    /**
     * Map children categories to DTOs recursively
     */
    private List<CategoryDto> mapChildrenToDto(List<Category> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }

        return children.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Category entities to list of CategoryDto
     */
    public List<CategoryDto> toDtoList(List<Category> categories) {
        if (categories == null) {
            return null;
        }

        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert Category entity to CategoryDto without children (for performance)
     */
    public CategoryDto toDtoWithoutChildren(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getColor(),
            category.getIcon(),
            category.getParentId(),
            category.getParent() != null ? category.getParent().getName() : null,
            category.getUser() != null ? category.getUser().getId() : null,
            category.getUser() != null ? category.getUser().getUsername() : null,
            category.getSortOrder(),
            category.getIsActive(),
            category.getIsSystem(),
            category.getUsageCount(),
            category.getLastUsedAt(),
            category.getMetadata(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            null // No children for performance
        );
    }
}
