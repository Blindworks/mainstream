package com.mainstream.activity.controller;

import com.mainstream.activity.dto.CreateTrophyRequest;
import com.mainstream.activity.dto.TrophyDto;
import com.mainstream.activity.dto.TrophyProgressDto;
import com.mainstream.activity.dto.UpdateTrophyRequest;
import com.mainstream.activity.dto.UserTrophyDto;
import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserTrophy;
import com.mainstream.activity.repository.UserTrophyRepository;
import com.mainstream.activity.service.TrophyService;
import com.mainstream.activity.service.trophy.TrophyProgress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for trophies and achievements.
 */
@RestController
@RequestMapping("/api/trophies")
@RequiredArgsConstructor
@Slf4j
public class TrophyController {

    private final TrophyService trophyService;
    private final UserTrophyRepository userTrophyRepository;

    /**
     * Get all available trophies.
     * For admin users: returns all trophies (including inactive)
     * For regular users: returns only active trophies
     */
    @GetMapping
    public ResponseEntity<List<TrophyDto>> getAllTrophies(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        List<Trophy> trophies;
        if ("ADMIN".equals(userRole)) {
            trophies = trophyService.getAllTrophiesForAdmin();
        } else {
            trophies = trophyService.getAllTrophies();
        }

        List<TrophyDto> dtos = trophies.stream()
                .map(this::toTrophyDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get trophies earned by the current user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<UserTrophyDto>> getMyTrophies(
            @RequestHeader("X-User-Id") Long userId) {

        List<UserTrophy> userTrophies = trophyService.getUserTrophies(userId);
        List<UserTrophyDto> dtos = userTrophies.stream()
                .map(this::toUserTrophyDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get progress for all trophies for the current user.
     * Returns progress for both earned and unearned trophies.
     */
    @GetMapping("/progress")
    public ResponseEntity<List<TrophyProgressDto>> getTrophyProgress(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching trophy progress for user: {}", userId);

        // Calculate progress for all trophies
        Map<Long, TrophyProgress> progressMap = trophyService.calculateAllTrophyProgress(userId);

        // Check which trophies are already earned
        List<UserTrophy> earnedTrophies = trophyService.getUserTrophies(userId);
        Map<Long, Boolean> earnedMap = earnedTrophies.stream()
                .collect(Collectors.toMap(
                        ut -> ut.getTrophy().getId(),
                        ut -> true
                ));

        // Convert to DTOs
        List<TrophyProgressDto> progressDtos = progressMap.entrySet().stream()
                .map(entry -> {
                    TrophyProgress progress = entry.getValue();
                    return TrophyProgressDto.builder()
                            .trophyId(entry.getKey())
                            .currentValue(progress.getCurrentValue())
                            .targetValue(progress.getTargetValue())
                            .percentage(progress.getPercentage())
                            .isComplete(progress.isComplete())
                            .isEarned(earnedMap.getOrDefault(entry.getKey(), false))
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDtos);
    }

    /**
     * Get trophies earned for a specific activity/run.
     */
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<UserTrophyDto>> getTrophiesForActivity(
            @PathVariable Long activityId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching trophies for activity {} and user: {}", activityId, userId);

        List<UserTrophy> userTrophies = trophyService.getTrophiesForActivity(activityId, userId);
        List<UserTrophyDto> dtos = userTrophies.stream()
                .map(this::toUserTrophyDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Initialize default trophies (admin endpoint).
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initializeTrophies() {
        try {
            trophyService.initializeDefaultTrophies();
            return ResponseEntity.ok("Default trophies initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing trophies", e);
            return ResponseEntity.status(500).body("Error initializing trophies: " + e.getMessage());
        }
    }

    /**
     * Update existing trophies with criteriaConfig (admin endpoint).
     * This migrates old trophies to the new config-based system.
     */
    @PostMapping("/update-configs")
    @PreAuthorize("hasRole('ADMIN')")//
    public ResponseEntity<String> updateTrophyConfigs() {
        try {
            trophyService.updateExistingTrophiesWithCriteriaConfig();
            return ResponseEntity.ok("Trophy configurations updated successfully");
        } catch (Exception e) {
            log.error("Error updating trophy configs", e);
            return ResponseEntity.status(500).body("Error updating trophy configs: " + e.getMessage());
        }
    }

    /**
     * Get trophy by ID (admin endpoint).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrophyDto> getTrophyById(@PathVariable Long id) {
        return trophyService.getTrophyById(id)
                .map(trophy -> ResponseEntity.ok(toTrophyDto(trophy)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new trophy (admin endpoint).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrophyDto> createTrophy(@Valid @RequestBody CreateTrophyRequest request) {
        try {
            Trophy trophy = trophyService.createTrophy(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toTrophyDto(trophy));
        } catch (IllegalArgumentException e) {
            log.error("Error creating trophy: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating trophy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing trophy (admin endpoint).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrophyDto> updateTrophy(@PathVariable Long id, @Valid @RequestBody UpdateTrophyRequest request) {
        try {
            Trophy trophy = trophyService.updateTrophy(id, request);
            return ResponseEntity.ok(toTrophyDto(trophy));
        } catch (IllegalArgumentException e) {
            log.error("Error updating trophy: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error updating trophy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a trophy (admin endpoint).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrophy(@PathVariable Long id) {
        try {
            trophyService.deleteTrophy(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting trophy: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Error deleting trophy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Unexpected error deleting trophy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activate a trophy (admin endpoint).
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrophyDto> activateTrophy(@PathVariable Long id) {
        try {
            Trophy trophy = trophyService.activateTrophy(id);
            return ResponseEntity.ok(toTrophyDto(trophy));
        } catch (IllegalArgumentException e) {
            log.error("Error activating trophy: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error activating trophy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deactivate a trophy (admin endpoint).
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrophyDto> deactivateTrophy(@PathVariable Long id) {
        try {
            Trophy trophy = trophyService.deactivateTrophy(id);
            return ResponseEntity.ok(toTrophyDto(trophy));
        } catch (IllegalArgumentException e) {
            log.error("Error deactivating trophy: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error deactivating trophy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get today's trophy - a trophy that is only available on this specific day.
     * Returns the trophy that has validFrom/validUntil dates matching today.
     */
    @GetMapping("/daily/today")
    public ResponseEntity<TrophyDto> getTodaysTrophy() {
        log.info("Fetching today's trophy");

        return trophyService.getTodaysTrophy()
                .map(trophy -> ResponseEntity.ok(toTrophyDto(trophy)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get users who have earned today's trophy.
     * Returns a list of users who achieved today's trophy along with when they earned it.
     */
    @GetMapping("/daily/today/winners")
    public ResponseEntity<List<UserTrophyDto>> getTodaysTrophyWinners() {
        log.info("Fetching winners of today's trophy");

        return trophyService.getTodaysTrophy()
                .map(trophy -> {
                    List<UserTrophy> winners = trophyService.getTodaysTrophyWinners(trophy.getId());
                    List<UserTrophyDto> dtos = winners.stream()
                            .map(this::toUserTrophyDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(dtos);
                })
                .orElse(ResponseEntity.ok(List.of())); // Return empty list if no daily trophy
    }

    /**
     * Convert Trophy entity to DTO.
     */
    private TrophyDto toTrophyDto(Trophy trophy) {
        return TrophyDto.builder()
                .id(trophy.getId())
                .code(trophy.getCode())
                .name(trophy.getName())
                .description(trophy.getDescription())
                .type(trophy.getType())
                .category(trophy.getCategory())
                .iconUrl(trophy.getIconUrl())
                .criteriaValue(trophy.getCriteriaValue())
                .isActive(trophy.getIsActive())
                .displayOrder(trophy.getDisplayOrder())
                // Location-based fields
                .latitude(trophy.getLatitude())
                .longitude(trophy.getLongitude())
                .collectionRadiusMeters(trophy.getCollectionRadiusMeters())
                .validFrom(trophy.getValidFrom())
                .validUntil(trophy.getValidUntil())
                .imageUrl(trophy.getImageUrl())
                // Generic configurable trophy criteria
                .criteriaConfig(trophy.getCriteriaConfig())
                .checkScope(trophy.getCheckScope())
                .createdAt(trophy.getCreatedAt())
                .updatedAt(trophy.getUpdatedAt())
                .build();
    }

    /**
     * Convert UserTrophy entity to DTO.
     */
    private UserTrophyDto toUserTrophyDto(UserTrophy userTrophy) {
        return UserTrophyDto.builder()
                .id(userTrophy.getId())
                .userId(userTrophy.getUser().getId())
                .userName(userTrophy.getUser().getEmail())
                .trophy(toTrophyDto(userTrophy.getTrophy()))
                .activityId(userTrophy.getActivity() != null ? userTrophy.getActivity().getId() : null)
                .earnedAt(userTrophy.getEarnedAt())
                .metadata(userTrophy.getMetadata())
                .build();
    }
}
