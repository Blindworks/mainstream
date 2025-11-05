package com.mainstream.activity.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a trophy earned by a user.
 */
@Entity
@Table(name = "user_trophies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "trophy_id"}),
    indexes = {
        @Index(name = "idx_user_trophy_earned", columnList = "user_id,earned_at"),
        @Index(name = "idx_trophy_earned", columnList = "trophy_id,earned_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserTrophy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trophy_id", nullable = false)
    private Trophy trophy;

    /**
     * The activity that triggered this trophy (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private UserActivity activity;

    /**
     * When the trophy was earned
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    /**
     * Additional data about how the trophy was earned (JSON format)
     */
    @Column(length = 1000)
    private String metadata;
}
