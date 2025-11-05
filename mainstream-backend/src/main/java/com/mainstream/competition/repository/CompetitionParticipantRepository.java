package com.mainstream.competition.repository;

import com.mainstream.competition.entity.CompetitionParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionParticipantRepository extends JpaRepository<CompetitionParticipant, Long> {

    /**
     * Find participant by competition and user
     */
    Optional<CompetitionParticipant> findByCompetitionIdAndUserId(Long competitionId, Long userId);

    /**
     * Check if user is participating in competition
     */
    boolean existsByCompetitionIdAndUserId(Long competitionId, Long userId);

    /**
     * Find all participants for a competition
     */
    List<CompetitionParticipant> findByCompetitionIdOrderByCurrentPositionAsc(Long competitionId);

    /**
     * Find all participants for a competition with pagination
     */
    Page<CompetitionParticipant> findByCompetitionId(Long competitionId, Pageable pageable);

    /**
     * Find all competitions for a user
     */
    List<CompetitionParticipant> findByUserIdOrderByJoinedDateDesc(Long userId);

    /**
     * Find active competitions for a user
     */
    @Query("SELECT cp FROM CompetitionParticipant cp WHERE cp.user.id = :userId AND cp.status = 'ACTIVE' ORDER BY cp.joinedDate DESC")
    List<CompetitionParticipant> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Count participants in a competition
     */
    long countByCompetitionId(Long competitionId);

    /**
     * Count active participants in a competition
     */
    long countByCompetitionIdAndStatus(Long competitionId, CompetitionParticipant.ParticipantStatus status);

    /**
     * Get leaderboard for a competition
     */
    @Query("SELECT cp FROM CompetitionParticipant cp WHERE cp.competition.id = :competitionId AND cp.status IN ('REGISTERED', 'ACTIVE', 'COMPLETED') ORDER BY cp.currentPosition ASC NULLS LAST, cp.currentScore DESC")
    List<CompetitionParticipant> getLeaderboard(@Param("competitionId") Long competitionId);

    /**
     * Get top N participants for a competition
     */
    @Query("SELECT cp FROM CompetitionParticipant cp WHERE cp.competition.id = :competitionId AND cp.status IN ('REGISTERED', 'ACTIVE', 'COMPLETED') ORDER BY cp.currentPosition ASC NULLS LAST, cp.currentScore DESC")
    List<CompetitionParticipant> getTopParticipants(@Param("competitionId") Long competitionId, Pageable pageable);
}
