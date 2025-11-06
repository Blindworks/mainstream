package com.mainstream.run.repository;

import com.mainstream.run.entity.Run;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {

    // Find runs by user
    Page<Run> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
    
    List<Run> findByUserIdOrderByStartTimeDesc(Long userId);
    
    Optional<Run> findByIdAndUserId(Long id, Long userId);

    // Find run by Strava activity ID
    Optional<Run> findByStravaActivityIdAndUserId(Long stravaActivityId, Long userId);

    // Find run by Nike activity ID
    Optional<Run> findByNikeActivityIdAndUserId(String nikeActivityId, Long userId);

    // Find runs in date range
    List<Run> findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
        Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find runs by status
    List<Run> findByUserIdAndStatusOrderByStartTimeDesc(Long userId, Run.RunStatus status);

    // Find public runs
    @Query("SELECT r FROM Run r WHERE r.isPublic = true ORDER BY r.startTime DESC")
    Page<Run> findPublicRuns(Pageable pageable);

    // Find runs by type
    List<Run> findByUserIdAndRunTypeOrderByStartTimeDesc(Long userId, Run.RunType runType);

    // Statistics queries
    @Query("SELECT COUNT(r) FROM Run r WHERE r.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(r.distanceMeters) FROM Run r WHERE r.userId = :userId")
    Double sumDistanceByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(r.durationSeconds) FROM Run r WHERE r.userId = :userId")
    Long sumDurationByUserId(@Param("userId") Long userId);

    // Recent runs
    @Query("SELECT r FROM Run r WHERE r.userId = :userId ORDER BY r.startTime DESC")
    List<Run> findRecentRunsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find runs by distance range
    @Query("SELECT r FROM Run r WHERE r.userId = :userId AND r.distanceMeters BETWEEN :minDistance AND :maxDistance ORDER BY r.startTime DESC")
    List<Run> findByUserIdAndDistanceRange(
        @Param("userId") Long userId,
        @Param("minDistance") Double minDistance,
        @Param("maxDistance") Double maxDistance
    );

    // Count distinct users who have runs for today
    @Query("SELECT COUNT(DISTINCT r.userId) FROM Run r WHERE r.startTime >= :startOfDay AND r.startTime < :endOfDay AND r.status = 'COMPLETED'")
    Long countDistinctUsersWithRunsToday(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
}