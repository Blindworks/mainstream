package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to store event messages from FIT files.
 * Captures training events like auto-pause, lap triggers, recoveries, etc.
 */
@Entity
@Table(name = "fit_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    // Event identification
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "event_group")
    private Integer eventGroup;

    // Timer information
    @Column(name = "timer_time", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal timerTime; // seconds

    // Position data
    @Column(name = "position_lat", columnDefinition = "DECIMAL(10,8)")
    private BigDecimal positionLat;

    @Column(name = "position_long", columnDefinition = "DECIMAL(11,8)")
    private BigDecimal positionLong;

    @Column(name = "altitude", columnDefinition = "DECIMAL(7,2)")
    private BigDecimal altitude;

    @Column(name = "distance", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal distance;

    // Event-specific data
    @Column(name = "data")
    private Long data; // Generic data field - meaning depends on event type

    @Column(name = "score")
    private Integer score;

    @Column(name = "opponent_score")
    private Integer opponentScore;

    // Front/rear gear for cycling
    @Column(name = "front_gear_num")
    private Integer frontGearNum;

    @Column(name = "front_gear")
    private Integer frontGear;

    @Column(name = "rear_gear_num")
    private Integer rearGearNum;

    @Column(name = "rear_gear")
    private Integer rearGear;

    // Heart rate and cadence at event
    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "cadence")
    private Integer cadence;

    // Speed at event
    @Column(name = "speed", columnDefinition = "DECIMAL(6,3)")
    private BigDecimal speed;

    public enum Event {
        TIMER,
        WORKOUT,
        WORKOUT_STEP,
        POWER_DOWN,
        POWER_UP,
        OFF_COURSE,
        SESSION,
        LAP,
        COURSE_POINT,
        BATTERY,
        VIRTUAL_PARTNER_PACE,
        HR_HIGH_ALERT,
        HR_LOW_ALERT,
        SPEED_HIGH_ALERT,
        SPEED_LOW_ALERT,
        CAD_HIGH_ALERT,
        CAD_LOW_ALERT,
        POWER_HIGH_ALERT,
        POWER_LOW_ALERT,
        RECOVERY_HR,
        BATTERY_LOW,
        TIME_DURATION_ALERT,
        DISTANCE_DURATION_ALERT,
        CALORIE_DURATION_ALERT,
        ACTIVITY,
        FITNESS_EQUIPMENT,
        LENGTH,
        USER_MARKER,
        SPORT_POINT,
        CALIBRATION,
        FRONT_GEAR_CHANGE,
        REAR_GEAR_CHANGE,
        RIDER_POSITION_CHANGE,
        ELEV_HIGH_ALERT,
        ELEV_LOW_ALERT,
        COMM_TIMEOUT
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
        STOP_DISABLE_ALL
    }

    // Utility methods
    public boolean isTimerEvent() {
        return event == Event.TIMER;
    }

    public boolean isAutoStopEvent() {
        return event == Event.TIMER && eventType == EventType.STOP;
    }

    public boolean isAutoStartEvent() {
        return event == Event.TIMER && eventType == EventType.START;
    }

    public boolean isLapEvent() {
        return event == Event.LAP;
    }

    public boolean isAlertEvent() {
        return event != null && event.name().contains("ALERT");
    }

    public String getEventDescription() {
        if (event == null) return "Unknown Event";
        
        switch (event) {
            case TIMER:
                return eventType == EventType.START ? "Auto Resume" : "Auto Pause";
            case LAP:
                return "Lap " + (eventType == EventType.START ? "Start" : "End");
            case HR_HIGH_ALERT:
                return "Heart Rate High Alert";
            case HR_LOW_ALERT:
                return "Heart Rate Low Alert";
            case SPEED_HIGH_ALERT:
                return "Speed High Alert";
            case SPEED_LOW_ALERT:
                return "Speed Low Alert";
            case BATTERY_LOW:
                return "Battery Low";
            case USER_MARKER:
                return "User Marker";
            default:
                return event.name().replace("_", " ");
        }
    }

    public boolean hasPositionData() {
        return positionLat != null && positionLong != null 
               && positionLat.compareTo(BigDecimal.ZERO) != 0 
               && positionLong.compareTo(BigDecimal.ZERO) != 0;
    }
}