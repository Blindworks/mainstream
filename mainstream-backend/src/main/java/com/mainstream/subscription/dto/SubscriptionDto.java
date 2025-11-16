package com.mainstream.subscription.dto;

import com.mainstream.subscription.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private Long id;
    private Long userId;
    private SubscriptionPlanDto plan;
    private Subscription.SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean cancelAtPeriodEnd;
    private LocalDateTime canceledAt;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
}
