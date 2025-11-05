package com.mainstream.run.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gps_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    @NotNull(message = "Run is required")
    private Run run;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(nullable = false, columnDefinition = "DECIMAL(10,8)")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(nullable = false, columnDefinition = "DECIMAL(11,8)")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0", message = "Altitude must be positive")
    @Column(columnDefinition = "DECIMAL(8,2)")
    private BigDecimal altitude;

    @DecimalMin(value = "0.0", message = "Accuracy must be positive")
    @Column(columnDefinition = "DECIMAL(6,2)")
    private BigDecimal accuracy;

    @DecimalMin(value = "0.0", message = "Speed must be positive")
    @Column(name = "speed_kmh", columnDefinition = "DECIMAL(6,2)")
    private BigDecimal speedKmh;

    @DecimalMin(value = "0.0", message = "Distance from start must be positive")
    @Column(name = "distance_from_start_meters", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal distanceFromStartMeters;

    @Column(name = "sequence_number", nullable = false)
    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Utility methods
    public Double getLatitudeDouble() {
        return latitude != null ? latitude.doubleValue() : null;
    }

    public Double getLongitudeDouble() {
        return longitude != null ? longitude.doubleValue() : null;
    }

    public Double getAltitudeDouble() {
        return altitude != null ? altitude.doubleValue() : null;
    }

    public Double getSpeedKmhDouble() {
        return speedKmh != null ? speedKmh.doubleValue() : null;
    }

    public Double getDistanceFromStartKm() {
        return distanceFromStartMeters != null ? distanceFromStartMeters.doubleValue() / 1000.0 : null;
    }

    // Helper method to create a GPS point with basic coordinates
    public static GpsPoint of(Run run, double latitude, double longitude, int sequence, LocalDateTime timestamp) {
        return GpsPoint.builder()
                .run(run)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .sequenceNumber(sequence)
                .timestamp(timestamp)
                .build();
    }

    // Helper method to create a GPS point with full data
    public static GpsPoint of(Run run, double latitude, double longitude, double altitude, 
                             double speed, double distance, int sequence, LocalDateTime timestamp) {
        return GpsPoint.builder()
                .run(run)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .altitude(BigDecimal.valueOf(altitude))
                .speedKmh(BigDecimal.valueOf(speed))
                .distanceFromStartMeters(BigDecimal.valueOf(distance))
                .sequenceNumber(sequence)
                .timestamp(timestamp)
                .build();
    }
}