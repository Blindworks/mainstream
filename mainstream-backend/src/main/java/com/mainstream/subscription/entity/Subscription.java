package com.mainstream.subscription.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @NotNull(message = "Plan is required")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "cancel_at_period_end", nullable = false)
    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELED,
        EXPIRED,
        PENDING,
        SUSPENDED
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    public boolean isPremium() {
        return isActive() && plan != null && !plan.isFree();
    }
}
