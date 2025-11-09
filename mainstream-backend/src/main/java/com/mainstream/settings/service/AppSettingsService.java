package com.mainstream.settings.service;

import com.mainstream.settings.dto.AppSettingsDto;
import com.mainstream.settings.entity.AppSettings;
import com.mainstream.settings.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppSettingsService {

    private final AppSettingsRepository settingsRepository;

    /**
     * Get all application settings
     */
    public List<AppSettingsDto> getAllSettings() {
        return settingsRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific setting by key
     */
    public AppSettingsDto getSetting(String key) {
        AppSettings settings = settingsRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));
        return toDto(settings);
    }

    /**
     * Update a setting value
     */
    @Transactional
    public AppSettingsDto updateSetting(String key, String value) {
        AppSettings settings = settingsRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        settings.setValue(value);
        AppSettings saved = settingsRepository.save(settings);

        log.info("Updated setting: {} = {}", key, value);
        return toDto(saved);
    }

    /**
     * Check if maintenance mode is enabled
     */
    public boolean isMaintenanceModeEnabled() {
        try {
            AppSettings settings = settingsRepository.findByKey("maintenance_mode")
                    .orElse(AppSettings.builder()
                            .key("maintenance_mode")
                            .value("false")
                            .build());
            return Boolean.parseBoolean(settings.getValue());
        } catch (Exception e) {
            log.error("Error checking maintenance mode", e);
            return false;
        }
    }

    /**
     * Convert entity to DTO
     */
    private AppSettingsDto toDto(AppSettings entity) {
        return AppSettingsDto.builder()
                .key(entity.getKey())
                .value(entity.getValue())
                .description(entity.getDescription())
                .build();
    }
}
