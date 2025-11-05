package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for storing unknown/unsupported FIT messages
 * This ensures zero data loss even when new message types are introduced
 * These messages can be reprocessed when support is added
 */
@Entity
@Table(name = "fit_unknown_messages", indexes = {
    @Index(name = "idx_fit_unknown_upload", columnList = "fit_file_upload_id"),
    @Index(name = "idx_fit_unknown_msg_num", columnList = "global_message_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FitUnknownMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    /**
     * Global Message Number from FIT protocol
     * This identifies the message type
     */
    @Column(name = "global_message_number", nullable = false)
    private Integer globalMessageNumber;

    /**
     * Local Message Type (0-15) used in compressed timestamp headers
     */
    @Column(name = "local_message_type")
    private Integer localMessageType;

    /**
     * Sequence number within the file
     */
    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    /**
     * Raw message data as JSON
     * Contains all fields that were present in the message
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "JSON")
    private Map<String, Object> rawData;

    /**
     * Field definitions if available from FIT Decode
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_definitions", columnDefinition = "JSON")
    private Map<String, Object> fieldDefinitions;

    /**
     * Additional metadata about the message
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Flag indicating if this message type was completely unknown
     * vs. known but not yet implemented
     */
    @Column(name = "completely_unknown")
    @Builder.Default
    private Boolean completelyUnknown = false;

    /**
     * Reason why this message was stored as unknown
     */
    @Column(name = "unknown_reason", columnDefinition = "TEXT")
    private String unknownReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Status for reprocessing
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reprocess_status")
    @Builder.Default
    private ReprocessStatus reprocessStatus = ReprocessStatus.PENDING;

    public enum ReprocessStatus {
        PENDING,        // Not yet reprocessed
        SKIPPED,        // Intentionally skipped
        PROCESSED,      // Successfully reprocessed
        FAILED          // Reprocessing failed
    }

    /**
     * When this message was last attempted to be reprocessed
     */
    @Column(name = "last_reprocess_attempt")
    private LocalDateTime lastReprocessAttempt;
}
