package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitUnknownMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitUnknownMessageRepository extends JpaRepository<FitUnknownMessage, Long> {

    /**
     * Find all unknown messages for a specific upload
     */
    List<FitUnknownMessage> findByFitFileUploadIdOrderBySequenceNumber(Long fitFileUploadId);

    /**
     * Find unknown messages by global message number
     */
    List<FitUnknownMessage> findByGlobalMessageNumber(Integer globalMessageNumber);

    /**
     * Find unknown messages that are pending reprocessing
     */
    List<FitUnknownMessage> findByReprocessStatus(FitUnknownMessage.ReprocessStatus status);

    /**
     * Count unknown messages for an upload
     */
    long countByFitFileUploadId(Long fitFileUploadId);

    /**
     * Get statistics on unknown message types across all uploads
     */
    @Query("SELECT u.globalMessageNumber, COUNT(u), u.completelyUnknown " +
           "FROM FitUnknownMessage u GROUP BY u.globalMessageNumber, u.completelyUnknown " +
           "ORDER BY COUNT(u) DESC")
    List<Object[]> getUnknownMessageStatistics();

    /**
     * Find all uploads that have unknown messages
     */
    @Query("SELECT DISTINCT u.fitFileUpload.id FROM FitUnknownMessage u")
    List<Long> findUploadsWithUnknownMessages();

    /**
     * Delete all unknown messages for a specific upload
     */
    void deleteByFitFileUploadId(Long fitFileUploadId);

    /**
     * Find unknown messages that should be reprocessed
     */
    @Query("SELECT u FROM FitUnknownMessage u WHERE u.reprocessStatus = :status " +
           "AND (u.lastReprocessAttempt IS NULL OR u.lastReprocessAttempt < :beforeDate)")
    List<FitUnknownMessage> findMessagesForReprocessing(
        @Param("status") FitUnknownMessage.ReprocessStatus status,
        @Param("beforeDate") java.time.LocalDateTime beforeDate
    );
}
