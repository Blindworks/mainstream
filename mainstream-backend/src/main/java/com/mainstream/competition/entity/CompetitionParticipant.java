package com.mainstream.competition.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a user's participation in a competition.
 */
@Entity
@Table(name = "competition_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"competition_id", "user_id"}),
    indexes = {
        @Index(name = "idx_participant_competition", columnList = "competition_id"),
        @Index(name = "idx_participant_user", columnList = "user_id"),
        @Index(name = "idx_participant_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CompetitionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date when user joined the competition
     */
    @Column(nullable = false)
    private LocalDateTime joinedDate;

    /**
     * Participant status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParticipantStatus status;

    /**
     * Final score or performance metric
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal finalScore;

    /**
     * Best performance value (e.g., fastest time, longest distance)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal bestPerformance;

    /**
     * Final position/rank in competition (1 = first place)
     */
    @Column
    private Integer position;

    /**
     * Current position in leaderboard (updated during active competition)
     */
    @Column
    private Integer currentPosition;

    /**
     * Total accumulated score (for ongoing competitions)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal currentScore;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Participant status
     */
    public enum ParticipantStatus {
        REGISTERED,     // Registered but competition hasn't started
        ACTIVE,         // Actively participating
        COMPLETED,      // Finished participation
        DISQUALIFIED,   // Disqualified
        WITHDRAWN       // Voluntarily withdrawn
    }
}
