package com.mainstream.activity.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a daily winner in a specific category.
 */
@Entity
@Table(name = "daily_winners",
    uniqueConstraints = @UniqueConstraint(columnNames = {"winner_date", "category"}),
    indexes = {
        @Index(name = "idx_daily_winner_date", columnList = "winner_date,category"),
        @Index(name = "idx_user_daily_wins", columnList = "user_id,winner_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DailyWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date of the win
     */
    @Column(nullable = false)
    private LocalDate winnerDate;

    /**
     * Category of daily winner
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WinnerCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private UserActivity activity;

    /**
     * Achievement value (e.g., time for earliest run, count for most runs, etc.)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal achievementValue;

    /**
     * Description of the achievement
     */
    @Column(length = 500)
    private String achievementDescription;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum WinnerCategory {
        EARLIEST_RUN,           // Frühaufsteher - earliest run of the day
        LATEST_RUN,             // Nachteule - latest run of the day
        LONGEST_STREAK,         // Längste Trainings-Serie
        MOST_RUNS,              // Consistency King - most runs this week
        MOST_ROUTES,            // Explorer - most different routes
        LONGEST_DISTANCE,       // Distance Hero - longest total distance
        FASTEST_TIME            // Schnellster - best time (optional)
    }
}
