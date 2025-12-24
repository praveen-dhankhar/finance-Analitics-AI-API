package com.financeapp.repository;

import com.financeapp.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity with hierarchical support and database-optimized queries
 * Compatible with both H2 and PostgreSQL databases
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find categories by user ID
     * Optimized query with proper indexing
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByUserId(@Param("userId") Long userId);

    /**
     * Find active categories by user ID
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Find root categories (no parent) by user ID
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.parentId IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategoriesByUserId(@Param("userId") Long userId);

    /**
     * Find child categories by parent ID
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Find category by name and user ID
     * Optimized query with proper indexing
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user.id = :userId")
    Optional<Category> findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Find categories by name containing (case-insensitive)
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.user.id = :userId AND c.isActive = true")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Find system categories
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.isSystem = true AND c.isActive = true ORDER BY c.name ASC")
    List<Category> findSystemCategories();

    /**
     * Find categories with high usage count
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.usageCount >= :minUsage AND c.isActive = true ORDER BY c.usageCount DESC")
    List<Category> findFrequentlyUsedCategories(@Param("userId") Long userId, @Param("minUsage") Integer minUsage);

    /**
     * Find recently used categories
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.lastUsedAt >= :date AND c.isActive = true ORDER BY c.lastUsedAt DESC")
    List<Category> findRecentlyUsedCategories(@Param("userId") Long userId, @Param("date") OffsetDateTime date);

    /**
     * Count categories by user ID
     * Optimized count query
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId AND c.isActive = true")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Count child categories by parent ID
     * Optimized count query
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId = :parentId AND c.isActive = true")
    long countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Find categories with no children (leaf categories)
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true AND c.id NOT IN (SELECT DISTINCT c2.parentId FROM Category c2 WHERE c2.parentId IS NOT NULL)")
    List<Category> findLeafCategories(@Param("userId") Long userId);

    /**
     * Find categories by color
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.color = :color AND c.user.id = :userId AND c.isActive = true")
    List<Category> findByColor(@Param("color") String color, @Param("userId") Long userId);

    /**
     * Find categories by icon
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.icon = :icon AND c.user.id = :userId AND c.isActive = true")
    List<Category> findByIcon(@Param("icon") String icon, @Param("userId") Long userId);

    /**
     * Update category usage count and last used time
     * Database-agnostic update query
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.usageCount = c.usageCount + 1, c.lastUsedAt = :usedAt WHERE c.id = :categoryId")
    int incrementUsageCount(@Param("categoryId") Long categoryId, @Param("usedAt") OffsetDateTime usedAt);

    /**
     * Find categories with pagination
     * Optimized for both databases
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    Page<Category> findByUserIdWithPagination(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find categories by hierarchy level
     * Database-agnostic recursive query simulation
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true AND c.parentId IS NULL")
    List<Category> findTopLevelCategories(@Param("userId") Long userId);

    /**
     * Check if category name exists for user
     * Optimized query for name uniqueness validation
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.name = :name AND c.user.id = :userId AND (:categoryId IS NULL OR c.id != :categoryId)")
    long countByNameForUser(@Param("name") String name, @Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * Find categories for bulk operations
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.id IN :categoryIds")
    List<Category> findByIdsAndUserId(@Param("categoryIds") List<Long> categoryIds, @Param("userId") Long userId);

    /**
     * Find categories with metadata
     * Database-agnostic query
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.metadata IS NOT NULL AND c.metadata != '' AND c.isActive = true")
    List<Category> findCategoriesWithMetadata(@Param("userId") Long userId);

    /**
     * PostgreSQL-specific JSON query (commented for H2 compatibility)
     * Uncomment when migrating to PostgreSQL
     */
    // @Query(value = "SELECT * FROM categories WHERE metadata @> :jsonPath", nativeQuery = true)
    // List<Category> findByJsonMetadata(@Param("jsonPath") String jsonPath);

    /**
     * PostgreSQL-specific full-text search (commented for H2 compatibility)
     * Uncomment when migrating to PostgreSQL
     */
    // @Query(value = "SELECT * FROM categories WHERE to_tsvector('english', name || ' ' || COALESCE(description, '')) @@ plainto_tsquery('english', :searchTerm)", nativeQuery = true)
    // List<Category> searchCategoriesFullText(@Param("searchTerm") String searchTerm);

    /**
     * PostgreSQL-specific hierarchical query (commented for H2 compatibility)
     * Uncomment when migrating to PostgreSQL for better performance
     */
    // @Query(value = "WITH RECURSIVE category_tree AS (" +
    //                "SELECT id, name, parent_id, 0 as level FROM categories WHERE parent_id IS NULL AND user_id = :userId" +
    //                "UNION ALL" +
    //                "SELECT c.id, c.name, c.parent_id, ct.level + 1 FROM categories c" +
    //                "JOIN category_tree ct ON c.parent_id = ct.id" +
    //                ") SELECT * FROM category_tree ORDER BY level, name", nativeQuery = true)
    // List<Category> findCategoryTree(@Param("userId") Long userId);
}
