-- =========================================================================
-- Migration V008: Add Run Support to User Activities
-- =========================================================================
-- Description: Extends user_activities to support both FIT file uploads
-- and manual runs with GPS points. Enables route matching for all run types.
-- Author: Claude
-- Date: 2025-01-06
-- =========================================================================

-- =========================================================================
-- Modify user_activities table
-- Description: Add run_id column and make fit_file_upload_id nullable
-- =========================================================================

-- Make fit_file_upload_id nullable (activities can be from either FIT files OR runs)
ALTER TABLE user_activities
    MODIFY COLUMN fit_file_upload_id BIGINT NULL;

-- Add run_id column for linking to manual runs
ALTER TABLE user_activities
    ADD COLUMN run_id BIGINT NULL
    COMMENT 'Reference to manual run (if activity is from a manual run)';

-- Add foreign key constraint
ALTER TABLE user_activities
    ADD CONSTRAINT fk_user_activity_run
    FOREIGN KEY (run_id) REFERENCES runs(id) ON DELETE CASCADE;

-- Add index for run_id lookups
CREATE INDEX idx_user_activity_run ON user_activities(run_id);

-- Add constraint to ensure activity has either FIT file OR run (not both, not neither)
ALTER TABLE user_activities
    ADD CONSTRAINT chk_activity_source
    CHECK (
        (fit_file_upload_id IS NOT NULL AND run_id IS NULL) OR
        (fit_file_upload_id IS NULL AND run_id IS NOT NULL)
    );

-- =========================================================================
-- Comments
-- =========================================================================
ALTER TABLE user_activities COMMENT =
'User activities matched to predefined routes. Activities can originate from either FIT file uploads or manual runs with GPS data.';
