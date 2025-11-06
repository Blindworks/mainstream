package com.mainstream.run.controller;

import com.mainstream.activity.dto.UserActivityDto;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.service.UserActivityService;
import com.mainstream.fitfile.dto.LapDto;
import com.mainstream.run.dto.RunDto;
import com.mainstream.run.dto.RunStatsDto;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.GpsPointRepository;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.run.service.RunService;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;
    private final RunRepository runRepository;
    private final UserActivityService userActivityService;
    private final UserRepository userRepository;
    private final GpsPointRepository gpsPointRepository;

    @GetMapping
    public ResponseEntity<Page<RunDto>> getAllRuns(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
        
        log.info("Fetching runs for user: {}", userId);
        
        Page<RunDto> runs = runService.getRunsForUser(userId, pageable);
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/{runId}")
    public ResponseEntity<RunDto> getRunById(
            @PathVariable Long runId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Fetching run {} for user: {}", runId, userId);
        
        Optional<RunDto> run = runService.getRunById(runId, userId);
        return run.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RunDto> createRun(
            @Valid @RequestBody Run run,
            @RequestHeader("X-User-Id") Long userId) {
        
        run.setUserId(userId);
        
        log.info("Creating manual run for user: {}", userId);
        
        RunDto createdRun = runService.createRun(run);
        return ResponseEntity.status(201).body(createdRun);
    }

    @PutMapping("/{runId}")
    public ResponseEntity<RunDto> updateRun(
            @PathVariable Long runId,
            @Valid @RequestBody Run runUpdates,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Updating run {} for user: {}", runId, userId);
        
        Optional<RunDto> updatedRun = runService.updateRun(runId, userId, runUpdates);
        return updatedRun.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{runId}")
    public ResponseEntity<Void> deleteRun(
            @PathVariable Long runId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Deleting run {} for user: {}", runId, userId);
        
        boolean deleted = runService.deleteRun(runId, userId);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<RunDto>> getRunsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching runs for user {} between {} and {}", userId, startDate, endDate);
        
        List<RunDto> runs = runService.getRunsInDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/stats")
    public ResponseEntity<RunStatsDto> getRunningStats(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Calculating running stats for user: {}", userId);
        
        RunStatsDto stats = runService.getRunningStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RunDto>> getRecentRuns(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching {} recent runs for user: {}", limit, userId);
        
        // Use the date range method to get recent runs
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(3); // Last 3 months
        
        List<RunDto> recentRuns = runService.getRunsInDateRange(userId, startDate, endDate);
        
        // Limit the results
        List<RunDto> limitedRuns = recentRuns.stream()
                .limit(limit)
                .toList();
        
        return ResponseEntity.ok(limitedRuns);
    }

    @GetMapping("/activity-summary")
    public ResponseEntity<List<RunDto>> getActivitySummary(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestHeader("X-User-Id") Long userId) {
        
        LocalDateTime startDate;
        LocalDateTime endDate;
        
        if (month != null) {
            // Monthly summary
            startDate = LocalDateTime.of(year, month, 1, 0, 0);
            endDate = startDate.plusMonths(1).minusSeconds(1);
            log.info("Fetching monthly summary for user {} for {}/{}", userId, month, year);
        } else {
            // Yearly summary
            startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            log.info("Fetching yearly summary for user {} for {}", userId, year);
        }
        
        List<RunDto> runs = runService.getRunsInDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debugRuns() {
        List<Run> runs = runRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Total runs: ").append(runs.size()).append("\n\n");

        for (Run run : runs.subList(0, Math.min(5, runs.size()))) {
            long gpsCount = gpsPointRepository.countByRunId(run.getId());
            sb.append("Run ID: ").append(run.getId())
              .append(", User ID: ").append(run.getUserId())
              .append(", Title: ").append(run.getTitle())
              .append(", Distance: ").append(run.getDistanceMeters())
              .append(", Duration: ").append(run.getDurationSeconds())
              .append(", GPS Points: ").append(gpsCount)
              .append("\n");
        }

        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping("/{runId}/laps")
    public ResponseEntity<List<LapDto>> getRunLaps(
            @PathVariable Long runId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching laps for run {} and user: {}", runId, userId);

        List<LapDto> laps = runService.getRunLaps(runId, userId);
        return ResponseEntity.ok(laps);
    }

    /**
     * Match a run against predefined routes and create a user activity if matched.
     */
    @PostMapping("/{runId}/match-route")
    public ResponseEntity<?> matchRunToRoute(
            @PathVariable Long runId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Attempting to match run {} to routes for user {}", runId, userId);

        // Check if run exists at all
        Optional<Run> anyRunOpt = runRepository.findById(runId);
        if (anyRunOpt.isEmpty()) {
            log.warn("Run {} does not exist in database", runId);
            return ResponseEntity.status(404)
                    .body(new RouteMatchResponse(false, "Run nicht gefunden", null));
        }

        // Find the run for this specific user
        Optional<Run> runOpt = runRepository.findByIdAndUserId(runId, userId);
        if (runOpt.isEmpty()) {
            log.warn("Run {} exists but does not belong to user {} (belongs to user {})",
                    runId, userId, anyRunOpt.get().getUserId());
            return ResponseEntity.status(403)
                    .body(new RouteMatchResponse(false, "Keine Berechtigung für diesen Run", null));
        }

        Run run = runOpt.get();
        log.info("Found run: id={}, title={}, user={}", run.getId(), run.getTitle(), run.getUserId());

        // Find the user
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.error("User {} not found", userId);
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();

        // Check if run has GPS points
        long gpsPointCount = gpsPointRepository.countByRunId(runId);
        if (gpsPointCount == 0) {
            log.warn("Run {} has no GPS points - cannot match to route", runId);
            return ResponseEntity.ok()
                    .body(new RouteMatchResponse(false, "Run hat keine GPS-Daten", null));
        }

        log.info("Run {} has {} GPS points", runId, gpsPointCount);

        // Attempt to match the run to a route
        UserActivity activity = userActivityService.processAndCreateActivityFromRun(user, run);

        if (activity == null) {
            log.info("Run {} did not match any predefined route", runId);
            return ResponseEntity.ok()
                    .body(new RouteMatchResponse(false, "Keine passende Strecke gefunden", null));
        }

        log.info("Run {} matched to route: {}", runId, activity.getMatchedRoute().getName());

        UserActivityDto activityDto = convertToDto(activity);
        return ResponseEntity.ok()
                .body(new RouteMatchResponse(true, "Route matched successfully", activityDto));
    }

    /**
     * Response wrapper for route matching
     */
    public static class RouteMatchResponse {
        public boolean matched;
        public String message;
        public UserActivityDto activity;

        public RouteMatchResponse(boolean matched, String message, UserActivityDto activity) {
            this.matched = matched;
            this.message = message;
            this.activity = activity;
        }
    }

    /**
     * Convert UserActivity to DTO
     */
    private UserActivityDto convertToDto(UserActivity activity) {
        UserActivityDto dto = new UserActivityDto();
        dto.setId(activity.getId());
        dto.setUserId(activity.getUser().getId());

        if (activity.getRun() != null) {
            dto.setRunId(activity.getRun().getId());
        }
        if (activity.getFitFileUpload() != null) {
            dto.setFitFileUploadId(activity.getFitFileUpload().getId());
        }
        if (activity.getMatchedRoute() != null) {
            dto.setMatchedRouteId(activity.getMatchedRoute().getId());
            dto.setMatchedRouteName(activity.getMatchedRoute().getName());
        }

        dto.setDirection(activity.getDirection());
        dto.setActivityStartTime(activity.getActivityStartTime());
        dto.setActivityEndTime(activity.getActivityEndTime());
        dto.setDurationSeconds(activity.getDurationSeconds());
        dto.setDistanceMeters(activity.getDistanceMeters());
        dto.setMatchedDistanceMeters(activity.getMatchedDistanceMeters());
        dto.setRouteCompletionPercentage(activity.getRouteCompletionPercentage());
        dto.setAverageMatchingAccuracyMeters(activity.getAverageMatchingAccuracyMeters());
        dto.setIsCompleteRoute(activity.getIsCompleteRoute());
        dto.setCreatedAt(activity.getCreatedAt());

        return dto;
    }
}