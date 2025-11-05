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
 * Generic FIT Message Entity for storing any FIT message type
 * Uses JSONB for flexible schema-less storage of message fields
 * This allows forward compatibility with new FIT message types
 */
@Entity
@Table(name = "fit_messages", indexes = {
    @Index(name = "idx_fit_msg_upload_type", columnList = "fit_file_upload_id,message_type"),
    @Index(name = "idx_fit_msg_timestamp", columnList = "message_timestamp"),
    @Index(name = "idx_fit_msg_type", columnList = "message_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FitMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    /**
     * FIT Message Type Name (e.g., "file_id", "session", "record", "device_info", "event", etc.)
     * Based on FIT Profile.xlsx message definitions
     */
    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    /**
     * FIT Message Number (Global Message Number from FIT protocol)
     * Used for message identification and future reference
     */
    @Column(name = "message_number")
    private Integer messageNumber;

    /**
     * Message Index (for messages that have a message_index field)
     * Used to order related messages (e.g., laps, lengths)
     */
    @Column(name = "message_index")
    private Integer messageIndex;

    /**
     * Message timestamp (if message contains timestamp field)
     * Stored separately for efficient querying and indexing
     */
    @Column(name = "message_timestamp")
    private LocalDateTime messageTimestamp;

    /**
     * All message fields stored as JSON
     * This provides maximum flexibility and forward compatibility
     * Field names match FIT Profile.xlsx field definitions
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_data", columnDefinition = "JSON")
    private Map<String, Object> messageData;

    /**
     * Developer fields (custom fields from ConnectIQ apps, etc.)
     * Stored separately from standard fields
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "developer_fields", columnDefinition = "JSON")
    private Map<String, Object> developerFields;

    /**
     * Sequence number within the file (order of message appearance)
     * Useful for reconstructing exact file structure
     */
    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    /**
     * Flag indicating if this message was fully parsed
     * False if there were unknown fields or parsing issues
     */
    @Column(name = "fully_parsed")
    @Builder.Default
    private Boolean fullyParsed = true;

    /**
     * Any warnings or notes about message parsing
     */
    @Column(name = "parsing_notes", columnDefinition = "TEXT")
    private String parsingNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Helper method to get a field value by name
     */
    public Object getField(String fieldName) {
        return messageData != null ? messageData.get(fieldName) : null;
    }

    /**
     * Helper method to get a developer field value by name
     */
    public Object getDeveloperField(String fieldName) {
        return developerFields != null ? developerFields.get(fieldName) : null;
    }

    /**
     * Check if message has a specific field
     */
    public boolean hasField(String fieldName) {
        return messageData != null && messageData.containsKey(fieldName);
    }

    /**
     * Get total number of fields
     */
    public int getFieldCount() {
        return messageData != null ? messageData.size() : 0;
    }

    /**
     * Get total number of developer fields
     */
    public int getDeveloperFieldCount() {
        return developerFields != null ? developerFields.size() : 0;
    }
}
