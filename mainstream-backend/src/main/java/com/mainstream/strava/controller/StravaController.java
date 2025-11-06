package com.mainstream.strava.controller;

import com.mainstream.run.entity.Run;
import com.mainstream.strava.service.StravaApiService;
import com.mainstream.strava.service.StravaSyncService;
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
@RequestMapping("/api/strava")
@RequiredArgsConstructor
@Slf4j
public class StravaController {

    private final StravaApiService stravaApiService;
    private final StravaSyncService stravaSyncService;

    /**
     * Get Strava authorization URL
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String authUrl = stravaApiService.getAuthorizationUrl();
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Connect Strava account (OAuth callback)
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connectStrava(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("code") String authorizationCode) {

        log.info("Connecting Strava for user ID: {}", userId);

        try {
            User user = stravaSyncService.connectStrava(userId, authorizationCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully connected to Strava");
            response.put("stravaUserId", user.getStravaUserId());
            response.put("connectedAt", user.getStravaConnectedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to connect Strava for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to connect to Strava: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Disconnect Strava account
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnectStrava(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Disconnecting Strava for user ID: {}", userId);

        try {
            stravaSyncService.disconnectStrava(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully disconnected from Strava");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to disconnect Strava for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to disconnect from Strava: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Sync activities from Strava
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncActivities(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        log.info("Syncing Strava activities for user ID: {}, since: {}", userId, since);

        try {
            // Default to 30 days ago if no date specified
            if (since == null) {
                since = LocalDateTime.now().minusDays(30);
            }

            List<Run> syncedRuns = stravaSyncService.syncActivities(userId, since);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully synced activities from Strava");
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
     * Get Strava connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStravaStatus(
            @RequestHeader("X-User-Id") Long userId) {

        log.debug("Getting Strava status for user ID: {}", userId);

        // This would require UserService to fetch user details
        // For now, return a simple response
        Map<String, Object> response = new HashMap<>();
        response.put("connected", false); // Will be updated with actual user data

        return ResponseEntity.ok(response);
    }
}
