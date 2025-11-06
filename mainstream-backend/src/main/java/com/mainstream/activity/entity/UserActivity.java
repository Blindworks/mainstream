package com.mainstream.activity.entity;

import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.run.entity.Run;
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
 * Represents a user's running activity that has been matched to a predefined route.
 * Can be created from either a FIT file upload or a manual run entry.
 */
@Entity
@Table(name = "user_activities", indexes = {
    @Index(name = "idx_user_activity_date", columnList = "user_id,activity_start_time"),
    @Index(name = "idx_route_activity_date", columnList = "matched_route_id,activity_start_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id")
    private FitFileUpload fitFileUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private Run run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_route_id")
    private PredefinedRoute matchedRoute;

    /**
     * Running direction on the matched route
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RunDirection direction;

    /**
     * Start time of the activity
     */
    @Column(nullable = false)
    private LocalDateTime activityStartTime;

    /**
     * End time of the activity
     */
    @Column
    private LocalDateTime activityEndTime;

    /**
     * Duration in seconds
     */
    @Column
    private Integer durationSeconds;

    /**
     * Distance covered in meters
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal distanceMeters;

    /**
     * Distance matched to the predefined route in meters
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal matchedDistanceMeters;

    /**
     * Percentage of the route completed (0-100)
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal routeCompletionPercentage;

    /**
     * Average matching accuracy in meters (how close the GPS track was to the route)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal averageMatchingAccuracyMeters;

    /**
     * Whether this is a complete route run (vs partial)
     */
    @Column(nullable = false)
    private Boolean isCompleteRoute = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum RunDirection {
        CLOCKWISE,
        COUNTER_CLOCKWISE,
        UNKNOWN
    }
}
