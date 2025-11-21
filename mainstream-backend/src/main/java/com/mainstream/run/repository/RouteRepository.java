package com.mainstream.run.repository;

import com.mainstream.run.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByCreatorUserIdOrderByCreatedAtDesc(Long creatorUserId);

    Page<Route> findByCreatorUserId(Long creatorUserId, Pageable pageable);

    Page<Route> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Route> findByIsFeaturedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Route r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Route> searchRoutes(@Param("searchTerm") String searchTerm, Pageable pageable);

    long countByCreatorUserId(Long creatorUserId);

    /**
     * Anonymize routes by setting creatorUserId to null (for GDPR deletion).
     * Returns the number of routes anonymized.
     */
    @Modifying
    @Query("UPDATE Route r SET r.creatorUserId = null WHERE r.creatorUserId = :userId")
    int anonymizeByCreatorUserId(@Param("userId") Long userId);

    /**
     * Delete all routes by creator user ID (alternative to anonymization).
     */
    @Modifying
    @Query("DELETE FROM Route r WHERE r.creatorUserId = :userId")
    int deleteByCreatorUserId(@Param("userId") Long userId);
}
