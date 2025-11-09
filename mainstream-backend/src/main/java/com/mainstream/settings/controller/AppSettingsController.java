package com.mainstream.settings.controller;

import com.mainstream.settings.dto.AppSettingsDto;
import com.mainstream.settings.service.AppSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class AppSettingsController {

    private final AppSettingsService settingsService;

    /**
     * Get all settings (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppSettingsDto>> getAllSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    /**
     * Get a specific setting (admin only)
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppSettingsDto> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(settingsService.getSetting(key));
    }

    /**
     * Update a setting (admin only)
     */
    @PatchMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppSettingsDto> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> payload) {
        String value = payload.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(settingsService.updateSetting(key, value));
    }

    /**
     * Check maintenance mode status (public endpoint)
     */
    @GetMapping("/maintenance-mode")
    public ResponseEntity<Map<String, Boolean>> getMaintenanceMode() {
        boolean isEnabled = settingsService.isMaintenanceModeEnabled();
        return ResponseEntity.ok(Map.of("enabled", isEnabled));
    }
}
