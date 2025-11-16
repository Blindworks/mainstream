package com.mainstream.competition.repository;

import com.mainstream.competition.entity.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    /**
     * Find competitions by status
     */
    Page<Competition> findByStatus(Competition.CompetitionStatus status, Pageable pageable);

    /**
     * Find competitions by type
     */
    Page<Competition> findByType(Competition.CompetitionType type, Pageable pageable);

    /**
     * Find competitions by status and type
     */
    Page<Competition> findByStatusAndType(
        Competition.CompetitionStatus status,
        Competition.CompetitionType type,
        Pageable pageable
    );

    /**
     * Find active competitions (currently running)
     */
    @Query("SELECT c FROM Competition c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND c.endDate >= :now")
    List<Competition> findActiveCompetitions(@Param("now") LocalDateTime now);

    /**
     * Find upcoming competitions
     */
    @Query("SELECT c FROM Competition c WHERE c.status = 'UPCOMING' AND c.startDate > :now ORDER BY c.startDate ASC")
    Page<Competition> findUpcomingCompetitions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find competitions by created by user
     */
    Page<Competition> findByCreatedById(Long userId, Pageable pageable);

    /**
     * Search competitions by title or description
     */
    @Query("SELECT c FROM Competition c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Competition> searchCompetitions(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find competitions ending soon
     */
    @Query("SELECT c FROM Competition c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :now AND :endTime ORDER BY c.endDate ASC")
    List<Competition> findEndingSoon(@Param("now") LocalDateTime now, @Param("endTime") LocalDateTime endTime);

    /**
     * Count active competitions in a date range
     */
    @Query("SELECT COUNT(c) FROM Competition c WHERE c.status = 'ACTIVE' AND c.startDate >= :startDate AND c.startDate < :endDate")
    Long countActiveCompetitionsInPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Reassign competitions to a different user (for GDPR deletion).
     * Typically reassigns to a system/admin account.
     */
    @Modifying
    @Query("UPDATE Competition c SET c.createdBy.id = :newUserId WHERE c.createdBy.id = :oldUserId")
    int reassignCompetitions(@Param("oldUserId") Long oldUserId, @Param("newUserId") Long newUserId);

    /**
     * Count competitions created by a user.
     */
    long countByCreatedById(Long userId);
}

