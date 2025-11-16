package com.mainstream.garmin.controller;

import com.mainstream.garmin.service.GarminApiService;
import com.mainstream.garmin.service.GarminSyncService;
import com.mainstream.run.entity.Run;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/garmin")
@RequiredArgsConstructor
@Slf4j
public class GarminController {

    private final GarminApiService garminApiService;
    private final GarminSyncService garminSyncService;

    /**
     * Get Garmin authorization URL
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String authUrl = garminApiService.getAuthorizationUrl();
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Connect Garmin account (OAuth callback)
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connectGarmin(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("code") String authorizationCode) {

        log.info("Connecting Garmin for user ID: {}", userId);

        try {
            User user = garminSyncService.connectGarmin(userId, authorizationCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully connected to Garmin");
            response.put("garminUserId", user.getGarminUserId());
            response.put("connectedAt", user.getGarminConnectedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to connect Garmin for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to connect to Garmin: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Disconnect Garmin account
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnectGarmin(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Disconnecting Garmin for user ID: {}", userId);

        try {
            garminSyncService.disconnectGarmin(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully disconnected from Garmin");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to disconnect Garmin for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to disconnect from Garmin: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Sync activities from Garmin
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncActivities(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        log.info("Syncing Garmin activities for user ID: {}, since: {}", userId, since);

        try {
            // Default to 30 days ago if no date specified
            if (since == null) {
                since = LocalDateTime.now().minusDays(30);
            }

            List<Run> syncedRuns = garminSyncService.syncActivities(userId, since);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully synced activities from Garmin");
            response.put("syncedCount", syncedRuns.size());
            response.put("runs", syncedRuns);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to sync activities for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to sync activities: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get Garmin connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGarminStatus(
            @RequestHeader("X-User-Id") Long userId) {

        log.debug("Getting Garmin status for user ID: {}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("connected", false);

        return ResponseEntity.ok(response);
    }

    /**
     * Backfill GPS points for a specific Garmin run
     */
    @PostMapping("/runs/{runId}/backfill-gps")
    public ResponseEntity<Map<String, Object>> backfillGpsForRun(
            @PathVariable Long runId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Backfilling GPS points for run ID: {} and user ID: {}", runId, userId);

        try {
            int gpsPointCount = garminSyncService.backfillGpsPointsForRun(userId, runId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully backfilled GPS points");
            response.put("runId", runId);
            response.put("gpsPointCount", gpsPointCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to backfill GPS points for run ID: {} and user ID: {}", runId, userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to backfill GPS points: " + e.getMessage());
            response.put("runId", runId);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Backfill GPS points for all Garmin runs that don't have GPS data
     */
    @PostMapping("/backfill-all-gps")
    public ResponseEntity<Map<String, Object>> backfillAllMissingGpsPoints(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Backfilling GPS points for all Garmin runs without GPS data for user ID: {}", userId);

        try {
            Map<String, Object> result = garminSyncService.backfillAllMissingGpsPoints(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "GPS backfill complete");
            response.putAll(result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to backfill GPS points for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to backfill GPS points: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
