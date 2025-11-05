package com.mainstream.activity.controller;

import com.mainstream.activity.dto.CreateTrophyRequest;
import com.mainstream.activity.dto.TrophyDto;
import com.mainstream.activity.dto.UpdateTrophyRequest;
import com.mainstream.activity.dto.UserTrophyDto;
import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserTrophy;
import com.mainstream.activity.service.TrophyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
