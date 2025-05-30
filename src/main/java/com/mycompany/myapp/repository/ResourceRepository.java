package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.enumeration.ResourceType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Resource entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    /**
     * Find resources by title containing the given text (case insensitive)
     */
    @Query("SELECT r FROM Resource r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Resource> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Find resources by author containing the given text (case insensitive)
     */
    @Query("SELECT r FROM Resource r WHERE LOWER(r.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Resource> findByAuthorContainingIgnoreCase(@Param("author") String author, Pageable pageable);

    /**
     * Find resources by keywords containing the given text (case insensitive)
     */
    @Query("SELECT r FROM Resource r WHERE LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keywords, '%'))")
    Page<Resource> findByKeywordsContainingIgnoreCase(@Param("keywords") String keywords, Pageable pageable);

    /**
     * Find resources by resource type
     */
    Page<Resource> findByResourceType(ResourceType resourceType, Pageable pageable);

    /**
     * Advanced search combining multiple criteria
     */
    @Query(
        "SELECT r FROM Resource r WHERE " +
        "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
        "(:author IS NULL OR LOWER(r.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
        "(:keywords IS NULL OR LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keywords, '%'))) AND " +
        "(:resourceType IS NULL OR r.resourceType = :resourceType)"
    )
    Page<Resource> findByCriteria(
        @Param("title") String title,
        @Param("author") String author,
        @Param("keywords") String keywords,
        @Param("resourceType") ResourceType resourceType,
        Pageable pageable
    );
}
