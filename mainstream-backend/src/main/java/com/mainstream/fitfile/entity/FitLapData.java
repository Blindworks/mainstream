package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fit_lap_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitLapData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    // Lap identification
    @Column(name = "lap_number", nullable = false)
    private Integer lapNumber;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Lap Timing
    @Column(name = "total_elapsed_time")
    private Integer totalElapsedTime; // seconds

    @Column(name = "total_timer_time")
    private Integer totalTimerTime; // seconds (active time)

    @Column(name = "total_moving_time")
    private Integer totalMovingTime; // seconds

    @Column(name = "time_standing")
    private Integer timeStanding; // seconds

    // Distance and Position
    @Column(name = "total_distance", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal totalDistance; // meters

    @Column(name = "start_position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal startPositionLat;

    @Column(name = "start_position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal startPositionLong;

    @Column(name = "end_position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal endPositionLat;

    @Column(name = "end_position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal endPositionLong;

    // Speed Metrics
    @Column(name = "avg_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal avgSpeed; // m/s

    @Column(name = "max_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal maxSpeed; // m/s

    @Column(name = "enhanced_avg_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal enhancedAvgSpeed; // m/s

    @Column(name = "enhanced_max_speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal enhancedMaxSpeed; // m/s

    // Heart Rate Data
    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate; // bpm

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate; // bpm

    @Column(name = "min_heart_rate")
    private Integer minHeartRate; // bpm

    // Running Dynamics
    @Column(name = "avg_cadence")
    private Integer avgCadence; // steps/min

    @Column(name = "max_cadence")
    private Integer maxCadence; // steps/min

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "avg_running_power")
    private Integer avgRunningPower; // watts

    @Column(name = "max_running_power")
    private Integer maxRunningPower; // watts

    @Column(name = "normalized_power")
    private Integer normalizedPower; // watts

    @Column(name = "avg_stride_length", columnDefinition = "DECIMAL(5,3)")
    private BigDecimal avgStrideLength; // meters

    @Column(name = "avg_vertical_oscillation", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgVerticalOscillation; // cm

    @Column(name = "avg_ground_contact_time")
    private Integer avgGroundContactTime; // ms

    @Column(name = "avg_ground_contact_balance", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgGroundContactBalance; // %

    @Column(name = "avg_stance_time", columnDefinition = "DECIMAL(5,1)")
    private BigDecimal avgStanceTime; // ms

    @Column(name = "avg_stance_time_percent", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgStanceTimePercent; // %

    @Column(name = "avg_stance_time_balance", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgStanceTimeBalance; // %

    // Elevation Data
    @Column(name = "total_ascent", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalAscent; // meters

    @Column(name = "total_descent", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalDescent; // meters

    @Column(name = "avg_altitude", columnDefinition = "DECIMAL(7,2)")
    private BigDecimal avgAltitude; // meters

    @Column(name = "max_altitude", columnDefinition = "DECIMAL(7,2)")
    private BigDecimal maxAltitude; // meters

    @Column(name = "min_altitude", columnDefinition = "DECIMAL(7,2)")
    private BigDecimal minAltitude; // meters

    // Energy and Calories
    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "fat_calories")
    private Integer fatCalories;

    // Training Load and Stress
    @Column(name = "intensity_factor", columnDefinition = "DECIMAL(5,3)")
    private BigDecimal intensityFactor;

    @Column(name = "training_stress_score")
    private Integer trainingStressScore;

    @Column(name = "avg_power_to_weight", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal avgPowerToWeight; // watts/kg

    // Environmental
    @Column(name = "avg_temperature")
    private Integer avgTemperature; // celsius

    @Column(name = "max_temperature")
    private Integer maxTemperature; // celsius

    @Column(name = "min_temperature")
    private Integer minTemperature; // celsius

    // Lap Trigger and Type
    @Enumerated(EnumType.STRING)
    @Column(name = "lap_trigger")
    private LapTrigger lapTrigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport")
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_sport")
    private SubSport subSport;

    // Lap Status
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    public enum LapTrigger {
        MANUAL,
        TIME,
        DISTANCE,
        POSITION_START,
        POSITION_LAP,
        POSITION_WAYPOINT,
        POSITION_MARKED,
        SESSION_END,
        FITNESS_EQUIPMENT
    }

    public enum Sport {
        RUNNING,
        CYCLING,
        TRANSITION,
        FITNESS_EQUIPMENT,
        SWIMMING,
        BASKETBALL,
        SOCCER,
        TENNIS,
        AMERICAN_FOOTBALL,
        TRAINING,
        WALKING,
        CROSS_COUNTRY_SKIING,
        ALPINE_SKIING,
        SNOWBOARDING,
        ROWING,
        MOUNTAINEERING,
        HIKING,
        MULTISPORT,
        PADDLING,
        ALL
    }

    public enum SubSport {
        GENERIC,
        TREADMILL,
        STREET,
        TRAIL,
        TRACK,
        SPIN,
        INDOOR_CYCLING,
        ROAD,
        MOUNTAIN,
        DOWNHILL,
        RECUMBENT,
        CYCLOCROSS,
        HAND_CYCLING,
        TRACK_CYCLING,
        INDOOR_ROWING,
        ELLIPTICAL,
        STAIR_CLIMBING,
        LAP_SWIMMING,
        OPEN_WATER,
        ALL
    }

    public enum EventType {
        START,
        STOP,
        CONSECUTIVE_DEPRECIATED,
        MARKER,
        STOP_ALL,
        BEGIN_DEPRECIATED,
        END_DEPRECIATED,
        END_ALL_DEPRECIATED,
        STOP_DISABLE,
        STOP_DISABLE_ALL,
        RECOVERY_HR_AUTO_PAUSE,
        RECOVERY_HR_AUTO_RESUME,
        RECOVERY_HR_DISABLED
    }

    // Utility methods
    public Double getDistanceKm() {
        return totalDistance != null ? totalDistance.doubleValue() / 1000.0 : null;
    }

    public Double getAvgPaceMinPerKm() {
        BigDecimal speed = enhancedAvgSpeed != null ? enhancedAvgSpeed : avgSpeed;
        if (speed != null && speed.doubleValue() > 0) {
            double speedKmh = speed.doubleValue() * 3.6;
            return 60.0 / speedKmh;
        }
        return null;
    }

    public Double getAvgSpeedKmh() {
        BigDecimal speed = enhancedAvgSpeed != null ? enhancedAvgSpeed : avgSpeed;
        return speed != null ? speed.doubleValue() * 3.6 : null;
    }

    public Double getMaxSpeedKmh() {
        BigDecimal speed = enhancedMaxSpeed != null ? enhancedMaxSpeed : maxSpeed;
        return speed != null ? speed.doubleValue() * 3.6 : null;
    }

    public String getFormattedDuration() {
        if (totalTimerTime == null) return "00:00:00";
        
        int hours = totalTimerTime / 3600;
        int minutes = (totalTimerTime % 3600) / 60;
        int seconds = totalTimerTime % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean isRunningLap() {
        return sport == Sport.RUNNING;
    }

    public boolean isAutomaticLap() {
        return lapTrigger == LapTrigger.DISTANCE || lapTrigger == LapTrigger.TIME;
    }

    public boolean isManualLap() {
        return lapTrigger == LapTrigger.MANUAL;
    }
}