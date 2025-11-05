package com.mainstream.activity.controller;

import com.mainstream.activity.dto.TrophyDto;
import com.mainstream.activity.dto.UserTrophyDto;
import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserTrophy;
import com.mainstream.activity.service.TrophyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     */
    @GetMapping
    public ResponseEntity<List<TrophyDto>> getAllTrophies() {
        List<Trophy> trophies = trophyService.getAllTrophies();
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
