-- Enhanced FIT File Processing Database Migration
-- This migration adds comprehensive support for ALL FIT file data without loss
-- Author: Enhanced FIT File Processing System
-- Date: 2025-08-30

-- Add new fields to existing fit_file_uploads table
ALTER TABLE fit_file_uploads 
ADD COLUMN sport VARCHAR(50),
ADD COLUMN sub_sport VARCHAR(50),
ADD COLUMN total_work BIGINT,
ADD COLUMN normalized_power INTEGER,
ADD COLUMN intensity_factor DECIMAL(5,3),
ADD COLUMN left_right_balance DECIMAL(5,2),
ADD COLUMN total_training_effect DECIMAL(3,1),
ADD COLUMN total_anaerobic_training_effect DECIMAL(3,1),
ADD COLUMN avg_running_cadence INTEGER,
ADD COLUMN max_running_cadence INTEGER,
ADD COLUMN avg_stance_time DECIMAL(5,1),
ADD COLUMN avg_stance_time_percent DECIMAL(5,2),
ADD COLUMN avg_stance_time_balance DECIMAL(5,2),
ADD COLUMN max_temperature INTEGER,
ADD COLUMN min_temperature INTEGER,
ADD COLUMN avg_power DECIMAL(8,2),
ADD COLUMN max_power INTEGER,
ADD COLUMN threshold_power INTEGER,
ADD COLUMN avg_respiration_rate DECIMAL(5,2),
ADD COLUMN max_respiration_rate DECIMAL(5,2),
ADD COLUMN first_lap_index INTEGER,
ADD COLUMN num_laps INTEGER,
ADD COLUMN num_active_lengths INTEGER,
ADD COLUMN pool_length DECIMAL(5,2),
ADD COLUMN pool_length_unit VARCHAR(10),
ADD COLUMN gps_accuracy INTEGER,
ADD COLUMN enhanced_avg_speed DECIMAL(8,5),
ADD COLUMN enhanced_max_speed DECIMAL(8,5),
ADD COLUMN enhanced_avg_altitude DECIMAL(8,3),
ADD COLUMN enhanced_min_altitude DECIMAL(8,3),
ADD COLUMN enhanced_max_altitude DECIMAL(8,3),
ADD COLUMN avg_fractional_cadence DECIMAL(4,2),
ADD COLUMN max_fractional_cadence DECIMAL(4,2),
ADD COLUMN total_fractional_cycles DECIMAL(8,2),
ADD COLUMN avg_total_hemoglobin_conc DECIMAL(5,2),
ADD COLUMN min_total_hemoglobin_conc DECIMAL(5,2),
ADD COLUMN max_total_hemoglobin_conc DECIMAL(5,2),
ADD COLUMN avg_saturated_hemoglobin_percent DECIMAL(5,2),
ADD COLUMN min_saturated_hemoglobin_percent DECIMAL(5,2),
ADD COLUMN max_saturated_hemoglobin_percent DECIMAL(5,2);

-- Add new fields to existing fit_track_points table
ALTER TABLE fit_track_points
ADD COLUMN calories INTEGER,
ADD COLUMN accumulated_power DECIMAL(10,2),
ADD COLUMN grade DECIMAL(5,2),
ADD COLUMN resistance INTEGER,
ADD COLUMN time_from_course INTEGER,
ADD COLUMN cycle_length DECIMAL(5,2),
ADD COLUMN compressed_speed_distance INTEGER,
ADD COLUMN activity_type VARCHAR(50),
ADD COLUMN vertical_speed DECIMAL(6,3),
ADD COLUMN ball_speed DECIMAL(6,3),
ADD COLUMN zone INTEGER,
ADD COLUMN left_power_phase DECIMAL(5,2),
ADD COLUMN left_power_phase_peak DECIMAL(5,2),
ADD COLUMN right_power_phase DECIMAL(5,2),
ADD COLUMN right_power_phase_peak DECIMAL(5,2),
ADD COLUMN left_pedal_smoothness DECIMAL(5,2),
ADD COLUMN right_pedal_smoothness DECIMAL(5,2),
ADD COLUMN left_torque_effectiveness DECIMAL(5,2),
ADD COLUMN right_torque_effectiveness DECIMAL(5,2),
ADD COLUMN respiration_rate DECIMAL(5,2),
ADD COLUMN total_hemoglobin_conc DECIMAL(5,2),
ADD COLUMN saturated_hemoglobin_percent DECIMAL(5,2),
ADD COLUMN motor_revolutions INTEGER,
ADD COLUMN trainer_torque DECIMAL(8,2),
ADD COLUMN trainer_wheel_speed DECIMAL(6,3),
ADD COLUMN absolute_pressure DECIMAL(8,2),
ADD COLUMN depth DECIMAL(5,2),
ADD COLUMN performance_condition INTEGER,
ADD COLUMN device_index INTEGER,
ADD COLUMN cns_load INTEGER,
ADD COLUMN time_in_hr_zone INTEGER,
ADD COLUMN time_in_speed_zone INTEGER,
ADD COLUMN time_in_cadence_zone INTEGER,
ADD COLUMN time_in_power_zone INTEGER,
ADD COLUMN developer_field_4 TEXT,
ADD COLUMN developer_field_5 TEXT;

-- Create fit_device_info table
CREATE TABLE fit_device_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    device_index INTEGER,
    device_type VARCHAR(50),
    manufacturer VARCHAR(100),
    garmin_product VARCHAR(100),
    product_name VARCHAR(200),
    serial_number BIGINT,
    software_version VARCHAR(50),
    hardware_version INTEGER,
    cum_operating_time BIGINT,
    battery_voltage INTEGER,
    battery_status ENUM('NEW', 'GOOD', 'OK', 'LOW', 'CRITICAL', 'CHARGING', 'UNKNOWN'),
    ant_transmission_type INTEGER,
    ant_device_number INTEGER,
    ant_network VARCHAR(50),
    source_type VARCHAR(50),
    timestamp DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    descriptor VARCHAR(255),
    favero_product VARCHAR(100),
    CONSTRAINT fk_device_info_fit_upload 
        FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_device_info_fit_upload (fit_file_upload_id),
    INDEX idx_device_info_device_index (device_index)
);

-- Create fit_zones table
CREATE TABLE fit_zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    zone_type ENUM('HEART_RATE', 'POWER', 'SPEED', 'CADENCE') NOT NULL,
    zone_number INTEGER NOT NULL,
    zone_name VARCHAR(100),
    high_value DECIMAL(10,3),
    low_value DECIMAL(10,3),
    hr_calc_type VARCHAR(50),
    hr_zone_calc_type VARCHAR(50),
    pwr_calc_type VARCHAR(50),
    speed_calc_type VARCHAR(50),
    message_index INTEGER,
    CONSTRAINT fk_zones_fit_upload 
        FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_zones_fit_upload (fit_file_upload_id),
    INDEX idx_zones_type_number (zone_type, zone_number),
    UNIQUE KEY uk_zones_upload_type_number (fit_file_upload_id, zone_type, zone_number)
);

-- Create fit_events table
CREATE TABLE fit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    timestamp DATETIME,
    event ENUM('TIMER', 'WORKOUT', 'WORKOUT_STEP', 'POWER_DOWN', 'POWER_UP', 'OFF_COURSE', 
               'SESSION', 'LAP', 'COURSE_POINT', 'BATTERY', 'VIRTUAL_PARTNER_PACE', 
               'HR_HIGH_ALERT', 'HR_LOW_ALERT', 'SPEED_HIGH_ALERT', 'SPEED_LOW_ALERT', 
               'CAD_HIGH_ALERT', 'CAD_LOW_ALERT', 'POWER_HIGH_ALERT', 'POWER_LOW_ALERT', 
               'RECOVERY_HR', 'BATTERY_LOW', 'TIME_DURATION_ALERT', 'DISTANCE_DURATION_ALERT', 
               'CALORIE_DURATION_ALERT', 'ACTIVITY', 'FITNESS_EQUIPMENT', 'LENGTH', 
               'USER_MARKER', 'SPORT_POINT', 'CALIBRATION', 'FRONT_GEAR_CHANGE', 
               'REAR_GEAR_CHANGE', 'RIDER_POSITION_CHANGE', 'ELEV_HIGH_ALERT', 
               'ELEV_LOW_ALERT', 'COMM_TIMEOUT'),
    event_type ENUM('START', 'STOP', 'CONSECUTIVE_DEPRECIATED', 'MARKER', 'STOP_ALL',
                    'BEGIN_DEPRECIATED', 'END_DEPRECIATED', 'END_ALL_DEPRECIATED',
                    'STOP_DISABLE', 'STOP_DISABLE_ALL'),
    event_group INTEGER,
    timer_time DECIMAL(10,3),
    position_lat DECIMAL(10,8),
    position_long DECIMAL(11,8),
    altitude DECIMAL(7,2),
    distance DECIMAL(10,2),
    data BIGINT,
    score INTEGER,
    opponent_score INTEGER,
    front_gear_num INTEGER,
    front_gear INTEGER,
    rear_gear_num INTEGER,
    rear_gear INTEGER,
    heart_rate INTEGER,
    cadence INTEGER,
    speed DECIMAL(6,3),
    CONSTRAINT fk_events_fit_upload 
        FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_events_fit_upload (fit_file_upload_id),
    INDEX idx_events_timestamp (timestamp),
    INDEX idx_events_type (event, event_type)
);

-- Create fit_hrv table
CREATE TABLE fit_hrv (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fit_file_upload_id BIGINT NOT NULL,
    timestamp DATETIME,
    time DOUBLE,
    rmssd DOUBLE,
    pnn50 DOUBLE,
    stress_score INTEGER,
    hrv_status VARCHAR(50),
    CONSTRAINT fk_hrv_fit_upload 
        FOREIGN KEY (fit_file_upload_id) REFERENCES fit_file_uploads(id) ON DELETE CASCADE,
    INDEX idx_hrv_fit_upload (fit_file_upload_id),
    INDEX idx_hrv_timestamp (timestamp)
);

-- Create fit_hrv_intervals table for storing R-R intervals
CREATE TABLE fit_hrv_intervals (
    fit_hrv_id BIGINT NOT NULL,
    interval_ms INTEGER,
    CONSTRAINT fk_hrv_intervals_hrv 
        FOREIGN KEY (fit_hrv_id) REFERENCES fit_hrv(id) ON DELETE CASCADE,
    INDEX idx_hrv_intervals_hrv_id (fit_hrv_id)
);

-- Add indexes for enhanced performance on existing tables
CREATE INDEX idx_fit_uploads_sport ON fit_file_uploads(sport);
CREATE INDEX idx_fit_uploads_activity_start ON fit_file_uploads(activity_start_time);
CREATE INDEX idx_fit_uploads_processing_status ON fit_file_uploads(processing_status);
CREATE INDEX idx_fit_uploads_user_sport ON fit_file_uploads(user_id, sport);

CREATE INDEX idx_track_points_timestamp ON fit_track_points(timestamp);
CREATE INDEX idx_track_points_sequence ON fit_track_points(fit_file_upload_id, sequence_number);
CREATE INDEX idx_track_points_position ON fit_track_points(position_lat, position_long);

CREATE INDEX idx_lap_data_start_time ON fit_lap_data(start_time);
CREATE INDEX idx_lap_data_lap_number ON fit_lap_data(fit_file_upload_id, lap_number);

-- Add comments for documentation
ALTER TABLE fit_file_uploads COMMENT = 'Enhanced FIT file uploads with comprehensive data capture';
ALTER TABLE fit_device_info COMMENT = 'Device information from FIT files including battery, software, hardware details';
ALTER TABLE fit_zones COMMENT = 'Training zone definitions (HR, Power, Speed, Cadence) from FIT files';
ALTER TABLE fit_events COMMENT = 'Training events like auto-pause, alerts, lap triggers from FIT files';
ALTER TABLE fit_hrv COMMENT = 'Heart Rate Variability data for training load and recovery analysis';
ALTER TABLE fit_hrv_intervals COMMENT = 'Individual R-R intervals for HRV analysis';