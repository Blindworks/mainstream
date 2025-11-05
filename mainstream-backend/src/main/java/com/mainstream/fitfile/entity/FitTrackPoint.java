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
@Table(name = "fit_track_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitTrackPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    // Time and sequence
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    // GPS Position Data (from Record Message)
    @Column(name = "position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal positionLat; // degrees

    @Column(name = "position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal positionLong; // degrees

    @Column(name = "altitude")
    private Double altitude; // meters

    @Column(name = "enhanced_altitude")
    private Double enhancedAltitude; // meters (higher precision)

    // Distance and Speed
    @Column(name = "distance")
    private Double distance; // meters (cumulative)

    @Column(name = "speed")
    private Double speed; // m/s

    @Column(name = "enhanced_speed")
    private Double enhancedSpeed; // m/s (higher precision)

    // Heart Rate Data
    @Column(name = "heart_rate")
    private Integer heartRate; // bpm

    // Running Dynamics
    @Column(name = "cadence")
    private Integer cadence; // steps/min or rpm

    @Column(name = "running_power")
    private Integer runningPower; // watts

    @Column(name = "vertical_oscillation")
    private Double verticalOscillation; // cm

    @Column(name = "stance_time")
    private Double stanceTime; // ms

    @Column(name = "stance_time_percent")
    private Double stanceTimePercent; // %

    @Column(name = "stance_time_balance")
    private Double stanceTimeBalance; // %

    @Column(name = "step_length")
    private Double stepLength; // meters

    @Column(name = "ground_contact_time")
    private Integer groundContactTime; // ms

    @Column(name = "ground_contact_balance")
    private Double groundContactBalance; // %

    // GPS Quality and Accuracy
    @Column(name = "gps_accuracy")
    private Integer gpsAccuracy; // meters

    @Enumerated(EnumType.STRING)
    @Column(name = "gps_fix_type")
    private GpsFixType gpsFixType;

    @Column(name = "satellites")
    private Integer satellites; // number of satellites

    // Temperature and Environment
    @Column(name = "temperature")
    private Integer temperature; // celsius

    @Column(name = "barometric_pressure")
    private Double barometricPressure; // Pa

    // Training Metrics
    @Column(name = "fractional_cadence")
    private Double fractionalCadence; // for more precise cadence

    @Column(name = "enhanced_respiration_rate")
    private Double enhancedRespirationRate; // breaths/min

    // Power and Performance
    @Column(name = "left_right_balance")
    private Double leftRightBalance; // %

    @Column(name = "power_phase_start_angle")
    private Integer powerPhaseStartAngle; // degrees

    @Column(name = "power_phase_end_angle")
    private Integer powerPhaseEndAngle; // degrees

    // Additional Record Message Fields (previously missing)
    @Column(name = "calories")
    private Integer calories; // cumulative calories

    @Column(name = "accumulated_power")
    private Double accumulatedPower; // watts*seconds

    @Column(name = "grade")
    private Double grade; // % grade

    @Column(name = "resistance")
    private Integer resistance; // trainer resistance

    @Column(name = "time_from_course")
    private Integer timeFromCourse; // seconds

    @Column(name = "cycle_length")
    private Double cycleLength; // meters

    @Column(name = "compressed_speed_distance")
    private Integer compressedSpeedDistance;

    @Column(name = "activity_type")
    private String activityType;

    // Enhanced metrics
    @Column(name = "vertical_speed")
    private Double verticalSpeed; // m/s

    @Column(name = "ball_speed")
    private Double ballSpeed; // m/s (for racket sports)

    @Column(name = "zone")
    private Integer zone; // current training zone

    // Power phase angles (cycling)
    @Column(name = "left_power_phase")
    private Double leftPowerPhase; // degrees

    @Column(name = "left_power_phase_peak")
    private Double leftPowerPhasePeak; // degrees

    @Column(name = "right_power_phase")
    private Double rightPowerPhase; // degrees

    @Column(name = "right_power_phase_peak")
    private Double rightPowerPhasePeak; // degrees

    // Left/Right pedal smoothness and torque effectiveness
    @Column(name = "left_pedal_smoothness")
    private Double leftPedalSmoothness; // %

    @Column(name = "right_pedal_smoothness")
    private Double rightPedalSmoothness; // %

    @Column(name = "left_torque_effectiveness")
    private Double leftTorqueEffectiveness; // %

    @Column(name = "right_torque_effectiveness")
    private Double rightTorqueEffectiveness; // %

    // Respiration and SpO2
    @Column(name = "respiration_rate")
    private Double respirationRate; // breaths/min

    @Column(name = "total_hemoglobin_conc")
    private Double totalHemoglobinConc; // g/dL

    @Column(name = "saturated_hemoglobin_percent")
    private Double saturatedHemoglobinPercent; // %

    @Column(name = "motor_revolutions")
    private Integer motorRevolutions;

    // Trainer/indoor equipment specific
    @Column(name = "trainer_torque")
    private Double trainerTorque; // Nm

    @Column(name = "trainer_wheel_speed")
    private Double trainerWheelSpeed; // m/s

    // Environment
    @Column(name = "absolute_pressure")
    private Double absolutePressure; // Pa

    @Column(name = "depth")
    private Double depth; // m (for swimming)

    // Performance condition
    @Column(name = "performance_condition")
    private Integer performanceCondition;

    // Device index for multi-device setups
    @Column(name = "device_index")
    private Integer deviceIndex;

    // CNS (Central Nervous System) load
    @Column(name = "cns_load")
    private Integer cnsLoad;

    // Additional GPS/Navigation data
    @Column(name = "time_in_hr_zone")
    private Integer timeInHrZone; // seconds

    @Column(name = "time_in_speed_zone")
    private Integer timeInSpeedZone; // seconds

    @Column(name = "time_in_cadence_zone")
    private Integer timeInCadenceZone; // seconds

    @Column(name = "time_in_power_zone")
    private Integer timeInPowerZone; // seconds

    // Developer Fields (for custom metrics like STRYD running power, Wahoo, etc.)
    @Column(name = "developer_field_1")
    private String developerField1;

    @Column(name = "developer_field_2")
    private String developerField2;

    @Column(name = "developer_field_3")
    private String developerField3;

    @Column(name = "developer_field_4")
    private String developerField4;

    @Column(name = "developer_field_5")
    private String developerField5;

    public enum GpsFixType {
        NO_FIX,
        GPS_2D,
        GPS_3D,
        DGPS,
        GPS_GLONASS_2D,
        GPS_GLONASS_3D,
        GPS_GALILEO_2D,
        GPS_GALILEO_3D
    }

    // Utility methods
    public Double getSpeedKmh() {
        Double currentSpeed = enhancedSpeed != null ? enhancedSpeed : speed;
        return currentSpeed != null ? currentSpeed * 3.6 : null;
    }

    public Double getPaceMinPerKm() {
        Double currentSpeed = enhancedSpeed != null ? enhancedSpeed : speed;
        if (currentSpeed != null && currentSpeed > 0) {
            double speedKmh = currentSpeed * 3.6;
            return 60.0 / speedKmh;
        }
        return null;
    }

    public Double getDistanceKm() {
        return distance != null ? distance / 1000.0 : null;
    }

    public Double getAltitudeMeters() {
        Double currentAltitude = enhancedAltitude != null ? enhancedAltitude : altitude;
        return currentAltitude;
    }

    public boolean hasValidGpsPosition() {
        return positionLat != null && positionLong != null 
               && positionLat.compareTo(BigDecimal.ZERO) != 0 
               && positionLong.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean hasRunningDynamics() {
        return verticalOscillation != null || groundContactTime != null 
               || stanceTimeBalance != null || stepLength != null;
    }

    public boolean hasPowerData() {
        return runningPower != null && runningPower > 0;
    }
}