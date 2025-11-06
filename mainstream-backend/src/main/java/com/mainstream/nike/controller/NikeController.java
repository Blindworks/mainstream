package com.mainstream.nike.controller;

import com.mainstream.nike.service.NikeSyncService;
import com.mainstream.run.entity.Run;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nike")
@RequiredArgsConstructor
@Slf4j
public class NikeController {

    private final NikeSyncService nikeSyncService;
    private final UserRepository userRepository;

    /**
     * Connect Nike account with manually provided access token
     * Since Nike doesn't provide public OAuth, users must extract their bearer token manually
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connectNike(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {

        log.info("Connecting Nike for user ID: {}", userId);

        String accessToken = request.get("accessToken");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access token is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = nikeSyncService.connectNike(userId, accessToken.trim());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully connected to Nike Run Club");
            response.put("nikeUserId", user.getNikeUserId());
            response.put("connectedAt", user.getNikeConnectedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to connect Nike for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to connect to Nike: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Disconnect Nike account
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnectNike(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Disconnecting Nike for user ID: {}", userId);

        try {
            nikeSyncService.disconnectNike(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully disconnected from Nike Run Club");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to disconnect Nike for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to disconnect from Nike: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Sync all activities from Nike Run Club
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncActivities(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Syncing Nike activities for user ID: {}", userId);

        try {
            List<Run> syncedRuns = nikeSyncService.syncActivities(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully synced activities from Nike Run Club");
            response.put("syncedCount", syncedRuns.size());
            response.put("runs", syncedRuns);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to sync Nike activities for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to sync activities: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get Nike connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNikeStatus(
            @RequestHeader("X-User-Id") Long userId) {

        log.debug("Getting Nike status for user ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("connected", user.isNikeConnected());
            response.put("nikeUserId", user.getNikeUserId());
            response.put("connectedAt", user.getNikeConnectedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get Nike status for user ID: {}", userId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("connected", false);
            response.put("error", e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get instructions for obtaining Nike access token
     */
    @GetMapping("/instructions")
    public ResponseEntity<Map<String, Object>> getInstructions() {
        Map<String, Object> response = new HashMap<>();
        response.put("title", "How to Get Your Nike Access Token");
        response.put("steps", List.of(
                "1. Open your browser and log in to nike.com",
                "2. Open Developer Tools (F12 or Right-click → Inspect)",
                "3. Go to the Network tab in Developer Tools",
                "4. Visit https://www.nike.com/us/en_us/e/nike-plus-membership",
                "5. In the Network tab, look for a request to 'unite.nike.com/getUser' or 'api.nike.com'",
                "6. Click on that request and find the 'Authorization' header in the Request Headers section",
                "7. Copy the token value (it starts with 'Bearer ', but only copy the part AFTER 'Bearer ')",
                "8. Paste the token into the connection form below"
        ));
        response.put("note", "This token is temporary and Nike does not provide a public API. " +
                "You may need to refresh the token periodically by repeating these steps.");

        return ResponseEntity.ok(response);
    }
}
