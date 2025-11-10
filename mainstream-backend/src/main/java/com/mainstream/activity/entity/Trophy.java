package com.mainstream.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Trophy definition that users can earn.
 */
@Entity
@Table(name = "trophies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Trophy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * Type of trophy
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrophyType type;

    /**
     * Category for organization
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrophyCategory category;

    /**
     * Icon or badge identifier
     */
    @Column(length = 200)
    private String iconUrl;

    /**
     * Criteria value (e.g., 1000 for 1km milestone, 7 for 7-day streak)
     */
    @Column
    private Integer criteriaValue;

    /**
     * Whether this trophy is currently active
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Order for display purposes
     */
    @Column
    private Integer displayOrder;

    /**
     * Location-based trophy fields
     */

    /**
     * Latitude of the trophy location (for LOCATION_BASED trophies)
     */
    @Column
    private Double latitude;

    /**
     * Longitude of the trophy location (for LOCATION_BASED trophies)
     */
    @Column
    private Double longitude;

    /**
     * Collection radius in meters - how close the runner must get to collect
     */
    @Column
    private Integer collectionRadiusMeters;

    /**
     * Start date when this trophy becomes available
     */
    @Column
    private LocalDateTime validFrom;

    /**
     * End date when this trophy expires
     */
    @Column
    private LocalDateTime validUntil;

    /**
     * Image URL for the trophy (can be used for location-based or any trophy)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * JSON configuration for trophy criteria
     * Flexible parameter storage for different trophy types
     * Examples:
     * - DISTANCE_MILESTONE: {"type": "DISTANCE_MILESTONE", "distanceMeters": 10000, "scope": "TOTAL"}
     * - TIME_BASED: {"type": "TIME_BASED", "startHour": 5, "endHour": 7, "requiredCount": 10}
     * - CONSISTENCY: {"type": "CONSISTENCY", "minActivitiesPerWeek": 3, "numberOfWeeks": 4}
     */
    @Column(columnDefinition = "TEXT")
    private String criteriaConfig;

    /**
     * Scope of the trophy check
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CheckScope checkScope;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum TrophyType {
        DISTANCE_MILESTONE,      // 1km, 5km, 10km, 21km, 42km
        STREAK,                  // 7 days, 30 days, etc.
        ROUTE_COMPLETION,        // Completed specific route
        CONSISTENCY,             // Regular training
        TIME_BASED,              // Early morning, late night
        EXPLORER,                // Different routes
        LOCATION_BASED,          // Collect at specific location
        SPECIAL                  // Special achievements
    }

    public enum TrophyCategory {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        ELITE,
        SPECIAL
    }

    public enum CheckScope {
        SINGLE_ACTIVITY,  // Check single activity (e.g., 10km in one run)
        TOTAL,            // Check total sum (e.g., 100km total)
        TIME_RANGE,       // Check within time range (e.g., 7 day streak)
        COUNT             // Count events (e.g., 10x Early Bird)
    }
}
