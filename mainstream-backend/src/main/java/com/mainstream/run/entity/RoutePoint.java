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
@Table(name = "route_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @NotNull(message = "Route is required")
    private Route route;

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

    @Column(name = "sequence_number", nullable = false)
    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;

    @DecimalMin(value = "0.0", message = "Distance from start must be positive")
    @Column(name = "distance_from_start_meters", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal distanceFromStartMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type")
    @Builder.Default
    private PointType pointType = PointType.WAYPOINT;

    @Column(name = "point_name")
    private String pointName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum PointType {
        START, END, WAYPOINT, LANDMARK, CHECKPOINT, WATER_STATION, TURN
    }

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

    public Double getDistanceFromStartKm() {
        return distanceFromStartMeters != null ? distanceFromStartMeters.doubleValue() / 1000.0 : null;
    }

    // Helper method to create a route point
    public static RoutePoint of(Route route, double latitude, double longitude, int sequence, PointType type) {
        return RoutePoint.builder()
                .route(route)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .sequenceNumber(sequence)
                .pointType(type)
                .build();
    }

    // Helper method to create a named route point (for landmarks)
    public static RoutePoint of(Route route, double latitude, double longitude, int sequence, 
                               PointType type, String name, String description) {
        return RoutePoint.builder()
                .route(route)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .sequenceNumber(sequence)
                .pointType(type)
                .pointName(name)
                .description(description)
                .build();
    }
}