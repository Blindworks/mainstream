package com.mainstream.config;

import com.mainstream.run.service.RunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Application runner that performs data migrations on startup.
 * This is used for one-time data updates that need to be applied to existing data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements ApplicationRunner {

    private final RunService runService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting data migration tasks...");

        // Recalculate and persist pace values for all existing runs
        // This ensures that pace values are pre-computed and stored in the database
        // rather than being calculated on-the-fly during each request
        try {
            int updatedRuns = runService.recalculatePaceForAllRuns();
            log.info("Data migration completed: {} runs updated with calculated pace values", updatedRuns);
        } catch (Exception e) {
            log.error("Error during pace recalculation migration: {}", e.getMessage(), e);
            // Don't fail startup if migration fails
        }
    }
}
