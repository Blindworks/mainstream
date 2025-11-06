package com.mainstream.run.repository;

import com.mainstream.run.entity.GpsPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsPointRepository extends JpaRepository<GpsPoint, Long> {

    /**
     * Find all GPS points for a run, ordered by sequence number
     */
    List<GpsPoint> findByRunIdOrderBySequenceNumberAsc(Long runId);

    /**
     * Find GPS points for a run with valid coordinates (not null)
     */
    @Query("SELECT g FROM GpsPoint g WHERE g.run.id = :runId AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL ORDER BY g.sequenceNumber ASC")
    List<GpsPoint> findByRunIdWithValidCoordinates(@Param("runId") Long runId);

    /**
     * Count GPS points for a run
     */
    Long countByRunId(Long runId);

    /**
     * Delete all GPS points for a run
     */
    void deleteByRunId(Long runId);

    /**
     * Find GPS points in a specific sequence range
     */
    List<GpsPoint> findByRunIdAndSequenceNumberBetweenOrderBySequenceNumberAsc(
        Long runId, Integer startSeq, Integer endSeq);
}
