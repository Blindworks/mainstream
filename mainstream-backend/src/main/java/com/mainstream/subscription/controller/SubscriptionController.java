package com.mainstream.subscription.controller;

import com.mainstream.subscription.dto.SubscriptionDto;
import com.mainstream.subscription.service.SubscriptionService;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @GetMapping("/current")
    public ResponseEntity<SubscriptionDto> getCurrentSubscription(@RequestHeader("X-User-Email") String email) {
        log.debug("Getting current subscription for user: {}", email);

        Long userId = getUserIdByEmail(email);
        return subscriptionService.getActiveSubscriptionByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionHistory(@RequestHeader("X-User-Email") String email) {
        log.debug("Getting subscription history for user: {}", email);

        Long userId = getUserIdByEmail(email);
        List<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSubscriptionStatus(@RequestHeader("X-User-Email") String email) {
        log.debug("Getting subscription status for user: {}", email);

        Long userId = getUserIdByEmail(email);
        boolean hasActive = subscriptionService.hasActiveSubscription(userId);
        boolean hasPremium = subscriptionService.hasPremiumSubscription(userId);

        Map<String, Object> status = Map.of(
                "hasActiveSubscription", hasActive,
                "hasPremiumSubscription", hasPremium
        );

        return ResponseEntity.ok(status);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionDto> cancelSubscription(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediately) {
        log.info("Canceling subscription {} for user: {} (immediately: {})", subscriptionId, email, immediately);

        Long userId = getUserIdByEmail(email);

        // Verify subscription belongs to user
        SubscriptionDto subscription = subscriptionService.getActiveSubscriptionByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active subscription found for user"));

        if (!subscription.getId().equals(subscriptionId)) {
            throw new IllegalArgumentException("Subscription does not belong to user");
        }

        SubscriptionDto canceledSubscription = subscriptionService.cancelSubscription(subscriptionId, immediately);
        return ResponseEntity.ok(canceledSubscription);
    }

    private Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email))
                .getId();
    }
}
