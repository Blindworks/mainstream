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
        SPECIAL                  // Special achievements
    }

    public enum TrophyCategory {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        ELITE,
        SPECIAL
    }
}
