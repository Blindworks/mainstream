package com.mainstream.run.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    @Column(name = "distance_meters", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal distanceMeters;

    @DecimalMin(value = "0.0", message = "Average pace must be positive")
    @Column(name = "average_pace_seconds_per_km", columnDefinition = "DECIMAL(6,2)")
    private Double averagePaceSecondsPerKm;

    @DecimalMin(value = "0.0", message = "Max speed must be positive")
    @Column(name = "max_speed_kmh", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal maxSpeedKmh;

    @DecimalMin(value = "0.0", message = "Average speed must be positive")
    @Column(name = "average_speed_kmh", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal averageSpeedKmh;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @DecimalMin(value = "0.0", message = "Elevation gain must be positive")
    @Column(name = "elevation_gain_meters", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal elevationGainMeters;

    @DecimalMin(value = "0.0", message = "Elevation loss must be positive")
    @Column(name = "elevation_loss_meters", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal elevationLossMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RunType runType = RunType.OUTDOOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RunStatus status = RunStatus.DRAFT;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "temperature_celsius")
    private Integer temperatureCelsius;

    @Column(name = "humidity_percentage")
    private Integer humidityPercentage;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "strava_activity_id")
    private Long stravaActivityId;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GpsPoint> gpsPoints;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RunType {
        OUTDOOR, TREADMILL, TRACK, TRAIL
    }

    public enum RunStatus {
        DRAFT, ACTIVE, COMPLETED, PAUSED, CANCELLED
    }

    public boolean isCompleted() {
        return status == RunStatus.COMPLETED;
    }

    public boolean isActive() {
        return status == RunStatus.ACTIVE;
    }

    public Double getDistanceKm() {
        return distanceMeters != null ? distanceMeters.doubleValue() / 1000.0 : 0.0;
    }

    public String getFormattedDuration() {
        if (durationSeconds == null) return "00:00:00";
        
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}