package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fit_file_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FitFileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // File Information
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash", unique = true)
    private String fileHash; // MD5 or SHA256 hash to prevent duplicates

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // FIT File Header Information
    @Column(name = "fit_protocol_version")
    private Integer fitProtocolVersion;

    @Column(name = "fit_profile_version")
    private Integer fitProfileVersion;

    @Column(name = "fit_manufacturer")
    private String fitManufacturer;

    @Column(name = "fit_product")
    private String fitProduct;

    @Column(name = "fit_device_serial")
    private String fitDeviceSerial;

    // Activity Summary Data (from FIT Session Message)
    @Column(name = "activity_start_time")
    private LocalDateTime activityStartTime;

    @Column(name = "activity_end_time")
    private LocalDateTime activityEndTime;

    @Column(name = "total_elapsed_time")
    private Integer totalElapsedTime; // seconds

    @Column(name = "total_timer_time")
    private Integer totalTimerTime; // seconds (active time)

    @Column(name = "total_distance", columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalDistance; // meters

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "total_ascent", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalAscent; // meters

    @Column(name = "total_descent", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalDescent; // meters

    // Running Specific Metrics
    @Column(name = "avg_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal avgSpeed; // m/s

    @Column(name = "max_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal maxSpeed; // m/s

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate; // bpm

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate; // bpm

    @Column(name = "avg_cadence")
    private Integer avgCadence; // steps/min

    @Column(name = "max_cadence")
    private Integer maxCadence; // steps/min

    @Column(name = "avg_running_power")
    private Integer avgRunningPower; // watts

    @Column(name = "max_running_power")
    private Integer maxRunningPower; // watts

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "avg_stride_length", columnDefinition = "DECIMAL(5,3)")
    private BigDecimal avgStrideLength; // meters

    @Column(name = "avg_vertical_oscillation", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgVerticalOscillation; // cm

    @Column(name = "avg_ground_contact_time")
    private Integer avgGroundContactTime; // ms

    @Column(name = "avg_ground_contact_balance", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgGroundContactBalance; // %

    // Training Stress and Load
    @Column(name = "training_stress_score")
    private Integer trainingStressScore;

    @Column(name = "training_load_peak")
    private Integer trainingLoadPeak;

    @Column(name = "recovery_time")
    private Integer recoveryTime; // hours

    // Environmental Data
    @Column(name = "avg_temperature")
    private Integer avgTemperature; // celsius

    @Column(name = "weather_condition")
    private String weatherCondition;

    // GPS and Location Data
    @Column(name = "start_position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal startPositionLat;

    @Column(name = "start_position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal startPositionLong;

    @Column(name = "end_position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal endPositionLat;

    @Column(name = "end_position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal endPositionLong;

    // Heart Rate Zones (5 zones as per FIT spec)
    @Column(name = "hr_zone_1_time")
    private Integer hrZone1Time; // seconds

    @Column(name = "hr_zone_2_time")
    private Integer hrZone2Time; // seconds

    @Column(name = "hr_zone_3_time")
    private Integer hrZone3Time; // seconds

    @Column(name = "hr_zone_4_time")
    private Integer hrZone4Time; // seconds

    @Column(name = "hr_zone_5_time")
    private Integer hrZone5Time; // seconds

    // Pace/Speed Zones (5 zones)
    @Column(name = "speed_zone_1_time")
    private Integer speedZone1Time; // seconds

    @Column(name = "speed_zone_2_time")
    private Integer speedZone2Time; // seconds

    @Column(name = "speed_zone_3_time")
    private Integer speedZone3Time; // seconds

    @Column(name = "speed_zone_4_time")
    private Integer speedZone4Time; // seconds

    @Column(name = "speed_zone_5_time")
    private Integer speedZone5Time; // seconds

    // Power Zones (5 zones) - for running power
    @Column(name = "power_zone_1_time")
    private Integer powerZone1Time; // seconds

    @Column(name = "power_zone_2_time")
    private Integer powerZone2Time; // seconds

    @Column(name = "power_zone_3_time")
    private Integer powerZone3Time; // seconds

    @Column(name = "power_zone_4_time")
    private Integer powerZone4Time; // seconds

    @Column(name = "power_zone_5_time")
    private Integer powerZone5Time; // seconds

    // Additional Session Message Fields (previously missing)
    @Column(name = "sport")
    private String sport;

    @Column(name = "sub_sport")
    private String subSport;

    @Column(name = "total_work")
    private Long totalWork; // joules

    @Column(name = "normalized_power")
    private Integer normalizedPower; // watts

    @Column(name = "intensity_factor", columnDefinition = "DECIMAL(5,3)")
    private BigDecimal intensityFactor;

    @Column(name = "left_right_balance", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal leftRightBalance; // %

    @Column(name = "total_training_effect", columnDefinition = "DECIMAL(3,1)")
    private BigDecimal totalTrainingEffect;

    @Column(name = "total_anaerobic_training_effect", columnDefinition = "DECIMAL(3,1)")
    private BigDecimal totalAnaerobicTrainingEffect;

    @Column(name = "avg_running_cadence")
    private Integer avgRunningCadence; // steps/min

    @Column(name = "max_running_cadence")
    private Integer maxRunningCadence; // steps/min

    @Column(name = "avg_stance_time", columnDefinition = "DECIMAL(5,1)")
    private BigDecimal avgStanceTime; // ms

    @Column(name = "avg_stance_time_percent", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgStanceTimePercent; // %

    @Column(name = "avg_stance_time_balance", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgStanceTimeBalance; // %

    @Column(name = "max_temperature")
    private Integer maxTemperature; // celsius

    @Column(name = "min_temperature")
    private Integer minTemperature; // celsius

    @Column(name = "avg_power", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal avgPower; // watts

    @Column(name = "max_power")
    private Integer maxPower; // watts

    @Column(name = "threshold_power")
    private Integer thresholdPower; // watts (FTP)

    @Column(name = "avg_respiration_rate", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgRespirationRate; // breaths/min

    @Column(name = "max_respiration_rate", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal maxRespirationRate; // breaths/min

    // Environmental and conditions
    @Column(name = "first_lap_index")
    private Integer firstLapIndex;

    @Column(name = "num_laps")
    private Integer numLaps;

    @Column(name = "num_active_lengths")
    private Integer numActiveLengths;

    @Column(name = "pool_length", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal poolLength; // meters

    @Column(name = "pool_length_unit")
    private String poolLengthUnit;

    // Enhanced GPS and positioning
    @Column(name = "gps_accuracy")
    private Integer gpsAccuracy; // meters

    @Column(name = "enhanced_avg_speed", columnDefinition = "DECIMAL(8,5)")
    private BigDecimal enhancedAvgSpeed; // m/s

    @Column(name = "enhanced_max_speed", columnDefinition = "DECIMAL(8,5)")
    private BigDecimal enhancedMaxSpeed; // m/s

    @Column(name = "enhanced_avg_altitude", columnDefinition = "DECIMAL(8,3)")
    private BigDecimal enhancedAvgAltitude; // meters

    @Column(name = "enhanced_min_altitude", columnDefinition = "DECIMAL(8,3)")
    private BigDecimal enhancedMinAltitude; // meters

    @Column(name = "enhanced_max_altitude", columnDefinition = "DECIMAL(8,3)")
    private BigDecimal enhancedMaxAltitude; // meters

    // Training metrics
    @Column(name = "avg_fractional_cadence", columnDefinition = "DECIMAL(4,2)")
    private BigDecimal avgFractionalCadence;

    @Column(name = "max_fractional_cadence", columnDefinition = "DECIMAL(4,2)")
    private BigDecimal maxFractionalCadence;

    @Column(name = "total_fractional_cycles", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalFractionalCycles;

    @Column(name = "avg_total_hemoglobin_conc", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgTotalHemoglobinConc; // g/dL

    @Column(name = "min_total_hemoglobin_conc", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal minTotalHemoglobinConc; // g/dL

    @Column(name = "max_total_hemoglobin_conc", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal maxTotalHemoglobinConc; // g/dL

    @Column(name = "avg_saturated_hemoglobin_percent", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgSaturatedHemoglobinPercent; // %

    @Column(name = "min_saturated_hemoglobin_percent", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal minSaturatedHemoglobinPercent; // %

    @Column(name = "max_saturated_hemoglobin_percent", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal maxSaturatedHemoglobinPercent; // %

    // Relationships
    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitTrackPoint> trackPoints;

    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitLapData> lapData;

    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitDeviceInfo> deviceInfo;

    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitZone> zones;

    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitEvent> events;

    @OneToMany(mappedBy = "fitFileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FitHrv> hrvData;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        DUPLICATE
    }

    // Utility methods
    public Double getDistanceKm() {
        return totalDistance != null ? totalDistance.doubleValue() / 1000.0 : null;
    }

    public Double getAvgPaceMinPerKm() {
        if (avgSpeed != null && avgSpeed.doubleValue() > 0) {
            double speedKmh = avgSpeed.doubleValue() * 3.6;
            return 60.0 / speedKmh; // minutes per km
        }
        return null;
    }

    public Double getAvgSpeedKmh() {
        return avgSpeed != null ? avgSpeed.doubleValue() * 3.6 : null;
    }

    public Double getMaxSpeedKmh() {
        return maxSpeed != null ? maxSpeed.doubleValue() * 3.6 : null;
    }

    public String getFormattedDuration() {
        if (totalTimerTime == null) return "00:00:00";
        
        int hours = totalTimerTime / 3600;
        int minutes = (totalTimerTime % 3600) / 60;
        int seconds = totalTimerTime % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean isProcessed() {
        return processingStatus == ProcessingStatus.COMPLETED;
    }

    public boolean isFailed() {
        return processingStatus == ProcessingStatus.FAILED;
    }
}