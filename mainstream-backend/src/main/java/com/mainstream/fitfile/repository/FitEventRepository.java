package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FitEventRepository extends JpaRepository<FitEvent, Long> {

    List<FitEvent> findByFitFileUploadId(Long fitFileUploadId);

    List<FitEvent> findByFitFileUploadIdOrderByTimestampAsc(Long fitFileUploadId);

    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event = :event ORDER BY e.timestamp ASC")
    List<FitEvent> findByFitFileUploadIdAndEvent(@Param("fitFileUploadId") Long fitFileUploadId, 
                                                @Param("event") FitEvent.Event event);

    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event = :event AND e.eventType = :eventType ORDER BY e.timestamp ASC")
    List<FitEvent> findByFitFileUploadIdAndEventAndEventType(
        @Param("fitFileUploadId") Long fitFileUploadId,
        @Param("event") FitEvent.Event event,
        @Param("eventType") FitEvent.EventType eventType);

    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.timestamp BETWEEN :startTime AND :endTime ORDER BY e.timestamp ASC")
    List<FitEvent> findByFitFileUploadIdAndTimestampBetween(
        @Param("fitFileUploadId") Long fitFileUploadId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    // Find auto-pause events
    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event = 'TIMER' AND e.eventType = 'STOP' ORDER BY e.timestamp ASC")
    List<FitEvent> findAutoPauseEvents(@Param("fitFileUploadId") Long fitFileUploadId);

    // Find auto-resume events
    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event = 'TIMER' AND e.eventType = 'START' ORDER BY e.timestamp ASC")
    List<FitEvent> findAutoResumeEvents(@Param("fitFileUploadId") Long fitFileUploadId);

    // Find lap events
    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event = 'LAP' ORDER BY e.timestamp ASC")
    List<FitEvent> findLapEvents(@Param("fitFileUploadId") Long fitFileUploadId);

    // Find alert events
    @Query("SELECT e FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId AND e.event LIKE '%ALERT%' ORDER BY e.timestamp ASC")
    List<FitEvent> findAlertEvents(@Param("fitFileUploadId") Long fitFileUploadId);

    @Modifying
    @Query("DELETE FROM FitEvent e WHERE e.fitFileUpload.id = :fitFileUploadId")
    void deleteByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);
}