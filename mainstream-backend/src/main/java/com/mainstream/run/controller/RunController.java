package com.mainstream.run.controller;

import com.mainstream.run.dto.RunDto;
import com.mainstream.run.dto.RunStatsDto;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.run.service.RunService;
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
        sb.append("Total runs: ").append(runs.size()).append("\n");
        
        for (Run run : runs.subList(0, Math.min(5, runs.size()))) {
            sb.append("Run ID: ").append(run.getId())
              .append(", Title: ").append(run.getTitle())
              .append(", Distance: ").append(run.getDistanceMeters())
              .append(", Duration: ").append(run.getDurationSeconds())  
              .append(", StoredPace: ").append(run.getAveragePaceSecondsPerKm())
              .append("\n");
        }
        
        return ResponseEntity.ok(sb.toString());
    }
}