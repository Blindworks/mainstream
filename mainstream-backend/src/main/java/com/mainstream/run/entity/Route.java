package com.mainstream.run.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Creator user ID is required")
    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @NotBlank(message = "Route name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    @Column(name = "distance_meters", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal distanceMeters;

    @DecimalMin(value = "0.0", message = "Elevation gain must be positive")
    @Column(name = "elevation_gain_meters", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal elevationGainMeters;

    @DecimalMin(value = "0.0", message = "Elevation loss must be positive")
    @Column(name = "elevation_loss_meters", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal elevationLossMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RouteType routeType = RouteType.RUNNING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RouteDifficulty difficulty = RouteDifficulty.EASY;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    // Frankfurt-specific fields
    @Column(name = "start_location")
    private String startLocation;

    @Column(name = "end_location")
    private String endLocation;

    @Column(name = "landmarks", columnDefinition = "TEXT")
    private String landmarks; // JSON string with landmark information

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags like "main-river", "city-center", "park"

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoutePoint> routePoints;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RouteType {
        RUNNING, WALKING, CYCLING, MIXED
    }

    public enum RouteDifficulty {
        EASY, MODERATE, HARD, EXPERT
    }

    // Utility methods
    public Double getDistanceKm() {
        return distanceMeters != null ? distanceMeters.doubleValue() / 1000.0 : 0.0;
    }

    public String getFormattedDistance() {
        Double distanceKm = getDistanceKm();
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceMeters.doubleValue());
        } else {
            return String.format("%.2f km", distanceKm);
        }
    }

    public Double getElevationGain() {
        return elevationGainMeters != null ? elevationGainMeters.doubleValue() : 0.0;
    }

    public boolean isFrankfurtRoute() {
        return tags != null && (tags.contains("frankfurt") || tags.contains("main-river"));
    }
}