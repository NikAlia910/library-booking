package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.enumeration.ResourceType;
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
    @Query(
        "SELECT r FROM Resource r WHERE " +
        "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
        "(:author IS NULL OR LOWER(r.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
        "(:keywords IS NULL OR LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keywords, '%'))) AND " +
        "(:resourceType IS NULL OR r.resourceType = :resourceType)"
    )
    Page<Resource> findBySearchCriteria(
        @Param("title") String title,
        @Param("author") String author,
        @Param("keywords") String keywords,
        @Param("resourceType") ResourceType resourceType,
        Pageable pageable
    );
}
