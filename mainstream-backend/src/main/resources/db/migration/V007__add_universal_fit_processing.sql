-- =========================================================================
-- Migration V007: Add Universal FIT Processing Support
-- =========================================================================
-- Description: Adds support for generic FIT message storage and
-- unknown message tracking for zero data loss
-- Author: Claude
-- Date: 2025-01-05
-- =========================================================================

-- =========================================================================
-- Table: fit_messages
-- Description: Generic storage for all FIT messages using JSONB
-- =========================================================================
CREATE TABLE fit_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    message_type VARCHAR(100) NOT NULL COMMENT 'FIT message type name (e.g., file_id, session, record)',
    message_number INT COMMENT 'Global message number from FIT protocol',
    message_index INT COMMENT 'Message index for ordered messages (laps, lengths)',
    message_timestamp DATETIME COMMENT 'Message timestamp if present',
    message_data JSON NOT NULL COMMENT 'All message fields as JSON',
    developer_fields JSON COMMENT 'Developer fields (ConnectIQ apps, etc.)',
    sequence_number INT COMMENT 'Order of message in file',
    fully_parsed BOOLEAN DEFAULT TRUE COMMENT 'Was message fully parsed?',
    parsing_notes TEXT COMMENT 'Any warnings or notes about parsing',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_fit_msg_upload_type (fit_file_upload_id, message_type),
    INDEX idx_fit_msg_timestamp (message_timestamp),
    INDEX idx_fit_msg_type (message_type),
    INDEX idx_fit_msg_sequence (fit_file_upload_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Generic storage for all FIT message types';

-- =========================================================================
-- Table: fit_unknown_messages
-- Description: Storage for unknown/unsupported FIT messages
-- =========================================================================
CREATE TABLE fit_unknown_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    global_message_number INT NOT NULL COMMENT 'Global message number',
    local_message_type INT COMMENT 'Local message type (0-15)',
    sequence_number INT COMMENT 'Order in file',
    raw_data JSON COMMENT 'Raw message data',
    field_definitions JSON COMMENT 'Field definitions if available',
    metadata TEXT COMMENT 'Additional metadata',
    completely_unknown BOOLEAN DEFAULT FALSE COMMENT 'Completely unknown vs. known but not implemented',
    unknown_reason TEXT COMMENT 'Why was this stored as unknown',
    reprocess_status ENUM('PENDING', 'SKIPPED', 'PROCESSED', 'FAILED') DEFAULT 'PENDING',
    last_reprocess_attempt DATETIME COMMENT 'Last reprocessing attempt',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_fit_unknown_upload (fit_file_upload_id),
    INDEX idx_fit_unknown_msg_num (global_message_number),
    INDEX idx_fit_unknown_reprocess (reprocess_status, last_reprocess_attempt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Storage for unknown/unsupported FIT messages';

-- =========================================================================
-- Add missing indexes to existing tables for better performance
-- =========================================================================

-- Index for track point queries by timestamp
ALTER TABLE fit_track_points
ADD INDEX IF NOT EXISTS idx_track_point_timestamp (fit_file_upload_id, timestamp);

-- Index for lap queries
ALTER TABLE fit_lap_data
ADD INDEX IF NOT EXISTS idx_lap_start_time (fit_file_upload_id, start_time);

-- Index for device info queries
ALTER TABLE fit_device_info
ADD INDEX IF NOT EXISTS idx_device_timestamp (fit_file_upload_id, timestamp);

-- Index for event queries
ALTER TABLE fit_events
ADD INDEX IF NOT EXISTS idx_event_timestamp (fit_file_upload_id, timestamp);

-- Index for HRV queries
ALTER TABLE fit_hrv
ADD INDEX IF NOT EXISTS idx_hrv_upload (fit_file_upload_id);

-- Index for zone queries
ALTER TABLE fit_zones
ADD INDEX IF NOT EXISTS idx_zone_type (fit_file_upload_id, zone_type);

-- =========================================================================
-- Views for easier querying
-- =========================================================================

-- View: Message statistics per upload
CREATE OR REPLACE VIEW v_fit_message_stats AS
SELECT
    fit_file_upload_id,
    message_type,
    COUNT(*) as message_count,
    MIN(message_timestamp) as first_timestamp,
    MAX(message_timestamp) as last_timestamp,
    SUM(CASE WHEN fully_parsed = FALSE THEN 1 ELSE 0 END) as partially_parsed_count,
    SUM(CASE WHEN developer_fields IS NOT NULL THEN 1 ELSE 0 END) as developer_field_count
FROM fit_messages
GROUP BY fit_file_upload_id, message_type;

-- View: Unknown message statistics
CREATE OR REPLACE VIEW v_fit_unknown_stats AS
SELECT
    global_message_number,
    COUNT(DISTINCT fit_file_upload_id) as affected_uploads,
    COUNT(*) as total_occurrences,
    SUM(CASE WHEN completely_unknown = TRUE THEN 1 ELSE 0 END) as completely_unknown_count,
    MAX(created_at) as last_seen
FROM fit_unknown_messages
GROUP BY global_message_number
ORDER BY total_occurrences DESC;

-- View: Upload processing summary
CREATE OR REPLACE VIEW v_fit_upload_summary AS
SELECT
    u.id,
    u.user_id,
    u.original_filename,
    u.processing_status,
    u.sport,
    u.sub_sport,
    u.activity_start_time,
    u.total_distance,
    u.total_timer_time,
    COUNT(DISTINCT m.message_type) as distinct_message_types,
    COUNT(m.id) as total_messages,
    COUNT(um.id) as unknown_message_count,
    SUM(CASE WHEN m.fully_parsed = FALSE THEN 1 ELSE 0 END) as partially_parsed_count
FROM fit_file_uploads u
LEFT JOIN fit_messages m ON m.fit_file_upload_id = u.id
LEFT JOIN fit_unknown_messages um ON um.fit_file_upload_id = u.id
GROUP BY u.id;

-- =========================================================================
-- Stored Procedures for common operations
-- =========================================================================

DELIMITER $$

-- Get all messages for an upload with pagination
CREATE PROCEDURE sp_get_upload_messages(
    IN p_upload_id BIGINT,
    IN p_message_type VARCHAR(100),
    IN p_offset INT,
    IN p_limit INT
)
BEGIN
    IF p_message_type IS NULL OR p_message_type = '' THEN
        SELECT * FROM fit_messages
        WHERE fit_file_upload_id = p_upload_id
        ORDER BY sequence_number
        LIMIT p_offset, p_limit;
    ELSE
        SELECT * FROM fit_messages
        WHERE fit_file_upload_id = p_upload_id
        AND message_type = p_message_type
        ORDER BY sequence_number
        LIMIT p_offset, p_limit;
    END IF;
END$$

-- Get processing statistics for an upload
CREATE PROCEDURE sp_get_upload_processing_stats(
    IN p_upload_id BIGINT
)
BEGIN
    SELECT
        'Total Messages' as metric,
        COUNT(*) as value
    FROM fit_messages
    WHERE fit_file_upload_id = p_upload_id

    UNION ALL

    SELECT
        'Unknown Messages' as metric,
        COUNT(*) as value
    FROM fit_unknown_messages
    WHERE fit_file_upload_id = p_upload_id

    UNION ALL

    SELECT
        'Distinct Message Types' as metric,
        COUNT(DISTINCT message_type) as value
    FROM fit_messages
    WHERE fit_file_upload_id = p_upload_id

    UNION ALL

    SELECT
        'Messages with Developer Fields' as metric,
        COUNT(*) as value
    FROM fit_messages
    WHERE fit_file_upload_id = p_upload_id
    AND developer_fields IS NOT NULL;
END$$

DELIMITER ;

-- =========================================================================
-- Cleanup/maintenance procedures
-- =========================================================================

DELIMITER $$

-- Archive old unknown messages that have been processed
CREATE PROCEDURE sp_archive_processed_unknown_messages(
    IN p_days_old INT
)
BEGIN
    DELETE FROM fit_unknown_messages
    WHERE reprocess_status = 'PROCESSED'
    AND created_at < DATE_SUB(NOW(), INTERVAL p_days_old DAY);

    SELECT ROW_COUNT() as deleted_count;
END$$

DELIMITER ;

-- =========================================================================
-- Add comments to existing tables for documentation
-- =========================================================================

ALTER TABLE fit_file_uploads
COMMENT='Main FIT file upload records with session summary data';

ALTER TABLE fit_track_points
COMMENT='Time-series data points from FIT record messages';

ALTER TABLE fit_lap_data
COMMENT='Lap summary data from FIT lap messages';

ALTER TABLE fit_device_info
COMMENT='Device metadata from FIT device_info messages';

ALTER TABLE fit_events
COMMENT='Training events from FIT event messages';

ALTER TABLE fit_hrv
COMMENT='Heart Rate Variability data from FIT HRV messages';

ALTER TABLE fit_zones
COMMENT='Training zone definitions from FIT zone messages';

-- =========================================================================
-- Migration complete
-- =========================================================================

-- Log migration completion
INSERT INTO schema_version (version_number, description, migration_date)
VALUES ('V007', 'Add universal FIT processing support', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);
