package com.mainstream.subscription.controller;

import com.mainstream.subscription.dto.SubscriptionPlanDto;
import com.mainstream.subscription.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanDto>> getAllActivePlans() {
        log.debug("Getting all active subscription plans");
        List<SubscriptionPlanDto> plans = planService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/paid")
    public ResponseEntity<List<SubscriptionPlanDto>> getAllPaidPlans() {
        log.debug("Getting all paid subscription plans");
        List<SubscriptionPlanDto> plans = planService.getAllPaidPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanDto> getPlanById(@PathVariable Long id) {
        log.debug("Getting subscription plan with id: {}", id);
        return planService.getPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/free")
    public ResponseEntity<SubscriptionPlanDto> getFreePlan() {
        log.debug("Getting free subscription plan");
        return planService.getFreePlan()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
