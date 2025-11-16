package com.mainstream.subscription.service;

import com.mainstream.subscription.dto.SubscriptionPlanDto;
import com.mainstream.subscription.entity.SubscriptionPlan;
import com.mainstream.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    public List<SubscriptionPlanDto> getAllActivePlans() {
        log.debug("Fetching all active subscription plans");
        return planRepository.findAllActivePlans().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionPlanDto> getAllPaidPlans() {
        log.debug("Fetching all paid subscription plans");
        return planRepository.findAllPaidPlans().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<SubscriptionPlanDto> getPlanById(Long id) {
        log.debug("Fetching subscription plan with id: {}", id);
        return planRepository.findById(id).map(this::toDto);
    }

    public Optional<SubscriptionPlanDto> getPlanByName(String name) {
        log.debug("Fetching subscription plan with name: {}", name);
        return planRepository.findByName(name).map(this::toDto);
    }

    public Optional<SubscriptionPlanDto> getFreePlan() {
        log.debug("Fetching free subscription plan");
        return planRepository.findFreePlan().map(this::toDto);
    }

    public SubscriptionPlanDto toDto(SubscriptionPlan plan) {
        return SubscriptionPlanDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .intervalType(plan.getIntervalType())
                .intervalCount(plan.getIntervalCount())
                .features(plan.getFeaturesList())
                .isActive(plan.getIsActive())
                .build();
    }
}
