package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FitMessageRepository extends JpaRepository<FitMessage, Long> {

    /**
     * Find all messages for a specific FIT file upload
     */
    List<FitMessage> findByFitFileUploadIdOrderBySequenceNumber(Long fitFileUploadId);

    /**
     * Find messages by type for a specific upload
     */
    List<FitMessage> findByFitFileUploadIdAndMessageTypeOrderBySequenceNumber(
        Long fitFileUploadId, String messageType);

    /**
     * Find messages within a timestamp range
     */
    List<FitMessage> findByFitFileUploadIdAndMessageTimestampBetween(
        Long fitFileUploadId, LocalDateTime start, LocalDateTime end);

    /**
     * Count messages by type for an upload
     */
    long countByFitFileUploadIdAndMessageType(Long fitFileUploadId, String messageType);

    /**
     * Get all distinct message types for an upload
     */
    @Query("SELECT DISTINCT m.messageType FROM FitMessage m WHERE m.fitFileUpload.id = :uploadId")
    List<String> findDistinctMessageTypesByUploadId(@Param("uploadId") Long uploadId);

    /**
     * Find messages that were not fully parsed
     */
    List<FitMessage> findByFitFileUploadIdAndFullyParsedFalse(Long fitFileUploadId);

    /**
     * Delete all messages for a specific upload
     */
    void deleteByFitFileUploadId(Long fitFileUploadId);

    /**
     * Find messages with developer fields
     */
    @Query("SELECT m FROM FitMessage m WHERE m.fitFileUpload.id = :uploadId " +
           "AND m.developerFields IS NOT NULL AND SIZE(m.developerFields) > 0")
    List<FitMessage> findMessagesWithDeveloperFields(@Param("uploadId") Long uploadId);

    /**
     * Get message statistics for an upload
     */
    @Query("SELECT m.messageType, COUNT(m) FROM FitMessage m " +
           "WHERE m.fitFileUpload.id = :uploadId GROUP BY m.messageType")
    List<Object[]> getMessageStatistics(@Param("uploadId") Long uploadId);
}
