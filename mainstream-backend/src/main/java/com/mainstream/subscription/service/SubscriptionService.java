package com.mainstream.subscription.service;

import com.mainstream.subscription.dto.SubscriptionDto;
import com.mainstream.subscription.entity.Subscription;
import com.mainstream.subscription.entity.SubscriptionPlan;
import com.mainstream.subscription.repository.SubscriptionPlanRepository;
import com.mainstream.subscription.repository.SubscriptionRepository;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanService planService;

    public Optional<SubscriptionDto> getActiveSubscriptionByUserId(Long userId) {
        log.debug("Fetching active subscription for user: {}", userId);
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .map(this::toDto);
    }

    public List<SubscriptionDto> getAllSubscriptionsByUserId(Long userId) {
        log.debug("Fetching all subscriptions for user: {}", userId);
        return subscriptionRepository.findAllByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.hasActiveSubscription(userId);
    }

    public boolean hasPremiumSubscription(Long userId) {
        return subscriptionRepository.hasPremiumSubscription(userId);
    }

    @Transactional
    public SubscriptionDto createSubscription(Long userId, Long planId) {
        log.info("Creating subscription for user {} with plan {}", userId, planId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + planId));

        // Cancel any existing active subscription
        subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .ifPresent(existingSubscription -> {
                    log.info("Canceling existing subscription {} for user {}", existingSubscription.getId(), userId);
                    existingSubscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
                    existingSubscription.setCanceledAt(LocalDateTime.now());
                    subscriptionRepository.save(existingSubscription);
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(now, plan);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .cancelAtPeriodEnd(false)
                .autoRenew(true)
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Created subscription {} for user {}", subscription.getId(), userId);

        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto cancelSubscription(Long subscriptionId, boolean cancelImmediately) {
        log.info("Canceling subscription {} (immediately: {})", subscriptionId, cancelImmediately);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + subscriptionId));

        if (cancelImmediately) {
            subscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(LocalDateTime.now());
        } else {
            subscription.setCancelAtPeriodEnd(true);
            subscription.setCanceledAt(LocalDateTime.now());
        }

        subscription.setAutoRenew(false);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription {} canceled", subscriptionId);
        return toDto(subscription);
    }

    @Transactional
    public void expireSubscriptions() {
        log.info("Checking for expired subscriptions");
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());

        for (Subscription subscription : expiredSubscriptions) {
            log.info("Expiring subscription {} for user {}", subscription.getId(), subscription.getUser().getId());
            subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
        }

        log.info("Expired {} subscriptions", expiredSubscriptions.size());
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubscriptionPlan plan) {
        String intervalType = plan.getIntervalType().toLowerCase();
        int intervalCount = plan.getIntervalCount();

        return switch (intervalType) {
            case "day" -> startDate.plusDays(intervalCount);
            case "week" -> startDate.plusWeeks(intervalCount);
            case "month" -> startDate.plusMonths(intervalCount);
            case "year" -> startDate.plusYears(intervalCount);
            default -> startDate.plusMonths(intervalCount);
        };
    }

    public SubscriptionDto toDto(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .plan(planService.toDto(subscription.getPlan()))
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .canceledAt(subscription.getCanceledAt())
                .autoRenew(subscription.getAutoRenew())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
