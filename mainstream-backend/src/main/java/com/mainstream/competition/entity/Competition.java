package com.mainstream.competition.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Competition entity representing a challenge that users can participate in.
 */
@Entity
@Table(name = "competitions",
    indexes = {
        @Index(name = "idx_competition_status", columnList = "status"),
        @Index(name = "idx_competition_dates", columnList = "start_date,end_date"),
        @Index(name = "idx_competition_type", columnList = "type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    /**
     * Type of competition
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompetitionType type;

    /**
     * Current status of the competition
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompetitionStatus status;

    /**
     * Difficulty level
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DifficultyLevel difficulty;

    /**
     * Competition start date and time
     */
    @Column(nullable = false, name = "start_date")
    private LocalDateTime startDate;

    /**
     * Competition end date and time
     */
    @Column(nullable = false, name = "end_date")
    private LocalDateTime endDate;

    /**
     * Description of prizes/rewards
     */
    @Column(length = 1000)
    private String prizeDescription;

    /**
     * Competition rules
     */
    @Column(length = 2000)
    private String rules;

    /**
     * Maximum number of participants (null = unlimited)
     */
    @Column
    private Integer maxParticipants;

    /**
     * Icon or badge URL
     */
    @Column(length = 500)
    private String iconUrl;

    /**
     * User who created the competition
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Competition types
     */
    public enum CompetitionType {
        FASTEST_5K,              // Fastest 5km run
        FASTEST_10K,             // Fastest 10km run
        FASTEST_HALF_MARATHON,   // Fastest half marathon
        FASTEST_MARATHON,        // Fastest marathon
        MOST_DISTANCE,           // Most distance covered in period
        MOST_RUNS,               // Most runs in period
        MOST_ELEVATION,          // Most elevation gain
        LONGEST_SINGLE_RUN,      // Longest single run
        CONSISTENCY_CHALLENGE,   // Most consecutive days
        SPECIFIC_ROUTE,          // Complete specific route fastest
        TEAM_CHALLENGE,          // Team-based competition
        CUSTOM                   // Custom competition type
    }

    /**
     * Competition status
     */
    public enum CompetitionStatus {
        DRAFT,      // Not yet published
        UPCOMING,   // Published but not started
        ACTIVE,     // Currently running
        COMPLETED,  // Finished
        CANCELLED   // Cancelled
    }

    /**
     * Difficulty levels
     */
    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        ELITE
    }
}
