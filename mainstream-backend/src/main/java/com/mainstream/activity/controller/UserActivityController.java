package com.mainstream.activity.controller;

import com.mainstream.activity.dto.UserActivityDto;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for user activities.
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class UserActivityController {

    private final UserActivityService userActivityService;

    /**
     * Get all activities for the current user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<UserActivityDto>> getMyActivities(
            @RequestHeader("X-User-Id") Long userId) {

        List<UserActivity> activities = userActivityService.getUserActivities(userId);
        List<UserActivityDto> dtos = activities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific activity by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserActivityDto> getActivityById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        UserActivity activity = userActivityService.getActivityById(id);

        if (activity == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if user owns this activity
        if (!activity.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(toDto(activity));
    }

    /**
     * Convert entity to DTO.
     */
    private UserActivityDto toDto(UserActivity activity) {
        return UserActivityDto.builder()
                .id(activity.getId())
                .userId(activity.getUser().getId())
                .userName(activity.getUser().getEmail())
                .fitFileUploadId(activity.getFitFileUpload().getId())
                .matchedRouteId(activity.getMatchedRoute() != null ? activity.getMatchedRoute().getId() : null)
                .matchedRouteName(activity.getMatchedRoute() != null ? activity.getMatchedRoute().getName() : null)
                .direction(activity.getDirection())
                .activityStartTime(activity.getActivityStartTime())
                .activityEndTime(activity.getActivityEndTime())
                .durationSeconds(activity.getDurationSeconds())
                .distanceMeters(activity.getDistanceMeters())
                .matchedDistanceMeters(activity.getMatchedDistanceMeters())
                .routeCompletionPercentage(activity.getRouteCompletionPercentage())
                .averageMatchingAccuracyMeters(activity.getAverageMatchingAccuracyMeters())
                .isCompleteRoute(activity.getIsCompleteRoute())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
