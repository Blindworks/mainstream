package com.mainstream.run.service;

import com.mainstream.fitfile.dto.LapDto;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitLapData;
import com.mainstream.fitfile.repository.FitFileUploadRepository;
import com.mainstream.fitfile.repository.FitLapDataRepository;
import com.mainstream.run.dto.RunDto;
import com.mainstream.run.dto.RunStatsDto;
import com.mainstream.run.entity.Run;
import com.mainstream.run.mapper.FitToRunMapper;
import com.mainstream.run.repository.RunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RunService {

    private final RunRepository runRepository;
    private final FitFileUploadRepository fitFileUploadRepository;
    private final FitLapDataRepository fitLapDataRepository;
    private final FitToRunMapper fitToRunMapper;
    private final com.mainstream.activity.service.UserActivityService userActivityService;
    private final com.mainstream.user.repository.UserRepository userRepository;
    private final com.mainstream.run.repository.GpsPointRepository gpsPointRepository;

    /**
     * Get all runs for a user, including both manual runs and FIT-imported runs
     */
    @Transactional(readOnly = true)
    public Page<RunDto> getRunsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching runs for user: {}", userId);
        
        // Get manual runs
        Page<Run> manualRuns = runRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
        List<RunDto> manualRunDtos = manualRuns.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // Get FIT-based runs with track points for speed calculation
        List<FitFileUpload> fitFiles = fitFileUploadRepository.findByUserIdAndProcessingStatusWithTrackPoints(
            userId, FitFileUpload.ProcessingStatus.COMPLETED);

        List<RunDto> fitRunDtos = fitFiles.stream()
            .map(fitToRunMapper::fitFileToRunDto)
            .collect(Collectors.toList());

        // Combine and sort by start time
        List<RunDto> allRuns = combineAndSortRuns(manualRunDtos, fitRunDtos);
        
        // Apply pagination manually since we're combining from different sources
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRuns.size());
        
        if (start >= allRuns.size()) {
            return new PageImpl<>(List.of(), pageable, allRuns.size());
        }
        
        List<RunDto> paginatedRuns = allRuns.subList(start, end);
        
        return new PageImpl<>(paginatedRuns, pageable, allRuns.size());
    }

    /**
     * Get a specific run by ID, checking both manual runs and FIT files
     */
    @Transactional(readOnly = true)
    public Optional<RunDto> getRunById(Long runId, Long userId) {
        log.debug("Fetching run {} for user {}", runId, userId);
        
        // First check manual runs
        Optional<Run> manualRun = runRepository.findByIdAndUserId(runId, userId);
        if (manualRun.isPresent()) {
            return Optional.of(convertToDto(manualRun.get()));
        }
        
        // Then check FIT files (using ID as fitFileUploadId) with track points for speed calculation
        Optional<FitFileUpload> fitFile = fitFileUploadRepository.findByIdAndUserIdWithTrackPoints(runId, userId);
        if (fitFile.isPresent() && fitFile.get().isProcessed()) {
            return Optional.of(fitToRunMapper.fitFileToRunDto(fitFile.get()));
        }
        
        return Optional.empty();
    }

    /**
     * Create a new manual run
     */
    public RunDto createRun(Run run) {
        log.debug("Creating manual run for user: {}", run.getUserId());

        run.setCreatedAt(LocalDateTime.now());
        run.setUpdatedAt(LocalDateTime.now());

        Run savedRun = runRepository.save(run);

        // Automatically match route if run is completed and has GPS points
        if (savedRun.isCompleted()) {
            log.info("Run {} is completed, attempting automatic route matching", savedRun.getId());
            attemptAutoRouteMatching(savedRun);
        }

        return convertToDto(savedRun);
    }

    /**
     * Update an existing manual run
     */
    public Optional<RunDto> updateRun(Long runId, Long userId, Run runUpdates) {
        log.debug("Updating run {} for user {}", runId, userId);

        Optional<Run> existingRun = runRepository.findByIdAndUserId(runId, userId);
        if (existingRun.isPresent()) {
            Run run = existingRun.get();
            boolean wasCompleted = run.isCompleted();

            updateRunFields(run, runUpdates);
            run.setUpdatedAt(LocalDateTime.now());

            Run savedRun = runRepository.save(run);

            // Automatically match route if run was just completed
            if (!wasCompleted && savedRun.isCompleted()) {
                log.info("Run {} status changed to COMPLETED, attempting automatic route matching", savedRun.getId());
                attemptAutoRouteMatching(savedRun);
            }

            return Optional.of(convertToDto(savedRun));
        }

        return Optional.empty();
    }

    /**
     * Delete a run (either manual or FIT file upload)
     */
    public boolean deleteRun(Long runId, Long userId) {
        log.debug("Deleting run {} for user {}", runId, userId);

        // First try to delete a manual run
        Optional<Run> run = runRepository.findByIdAndUserId(runId, userId);
        if (run.isPresent()) {
            log.debug("Found manual run {}, deleting", runId);

            // Delete associated user activities first to avoid FK constraint violation
            Optional<com.mainstream.activity.entity.UserActivity> activity =
                userActivityService.findByRunId(runId);
            activity.ifPresent(a -> {
                log.debug("Deleting associated user activity {}", a.getId());
                userActivityService.deleteActivity(a.getId());
            });

            runRepository.delete(run.get());
            return true;
        }

        // If not a manual run, try to find and delete a FIT file upload
        Optional<FitFileUpload> fitFile = fitFileUploadRepository.findByIdAndUserId(runId, userId);
        if (fitFile.isPresent()) {
            log.debug("Found FIT file upload {}, deleting", runId);

            // Delete associated user activities first to avoid FK constraint violation
            Optional<com.mainstream.activity.entity.UserActivity> activity =
                userActivityService.findByFitFileUploadId(runId);
            activity.ifPresent(a -> {
                log.debug("Deleting associated user activity {}", a.getId());
                userActivityService.deleteActivity(a.getId());
            });

            // Delete the FIT file (cascades to track points, laps, etc.)
            fitFileUploadRepository.delete(fitFile.get());
            return true;
        }

        log.debug("Run {} not found for user {}", runId, userId);
        return false;
    }

    /**
     * Get runs within a date range
     */
    @Transactional(readOnly = true)
    public List<RunDto> getRunsInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching runs for user {} between {} and {}", userId, startDate, endDate);
        
        // Get manual runs in date range
        List<Run> manualRuns = runRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
            userId, startDate, endDate);
        List<RunDto> manualRunDtos = manualRuns.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // Get FIT runs in date range with track points for speed calculation
        List<FitFileUpload> fitFiles = fitFileUploadRepository
            .findByUserIdAndProcessingStatusAndActivityStartTimeBetweenWithTrackPoints(
                userId, FitFileUpload.ProcessingStatus.COMPLETED, startDate, endDate);
        List<RunDto> fitRunDtos = fitFiles.stream()
            .map(fitToRunMapper::fitFileToRunDto)
            .collect(Collectors.toList());

        return combineAndSortRuns(manualRunDtos, fitRunDtos);
    }

    /**
     * Get running statistics for a user
     */
    @Transactional(readOnly = true)
    public RunStatsDto getRunningStats(Long userId) {
        log.debug("Calculating running stats for user: {}", userId);

        List<RunDto> allRuns = getAllRunsForUser(userId);

        return RunStatsDto.builder()
            .totalRuns(allRuns.size())
            .totalDistance(allRuns.stream()
                .mapToDouble(run -> run.getDistanceKm() != null ? run.getDistanceKm() : 0.0)
                .sum())
            .totalDuration(allRuns.stream()
                .mapToInt(run -> run.getDurationSeconds() != null ? run.getDurationSeconds() : 0)
                .sum())
            .averagePace(calculateAveragePace(allRuns))
            .bestPace(calculateBestPace(allRuns))
            .longestRun(calculateLongestRun(allRuns))
            .build();
    }

    /**
     * Get count of distinct users who have completed runs today
     */
    @Transactional(readOnly = true)
    public Long getTodayActiveUsersCount() {
        log.debug("Counting users with completed runs today");

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Long count = runRepository.countDistinctUsersWithRunsToday(startOfDay, endOfDay);
        log.debug("Found {} users with completed runs today", count);

        return count;
    }

    // Private helper methods

    private List<RunDto> getAllRunsForUser(Long userId) {
        List<Run> manualRuns = runRepository.findByUserIdOrderByStartTimeDesc(userId);
        List<RunDto> manualRunDtos = manualRuns.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        List<FitFileUpload> fitFiles = fitFileUploadRepository.findByUserIdAndProcessingStatusWithTrackPoints(
            userId, FitFileUpload.ProcessingStatus.COMPLETED);
        List<RunDto> fitRunDtos = fitFiles.stream()
            .map(fitToRunMapper::fitFileToRunDto)
            .collect(Collectors.toList());

        return combineAndSortRuns(manualRunDtos, fitRunDtos);
    }

    private List<RunDto> combineAndSortRuns(List<RunDto> manualRuns, List<RunDto> fitRuns) {
        List<RunDto> combined = manualRuns.stream().collect(Collectors.toList());
        combined.addAll(fitRuns);
        
        return combined.stream()
            .sorted((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()))
            .collect(Collectors.toList());
    }

    private RunDto convertToDto(Run run) {
        Double pace = calculatePaceIfMissing(run);
        log.debug("Converting run {} - original pace: {}, calculated pace: {}, distance: {}m, duration: {}s", 
                  run.getId(), run.getAveragePaceSecondsPerKm(), pace, 
                  run.getDistanceMeters(), run.getDurationSeconds());
        return RunDto.builder()
            .id(run.getId())
            .userId(run.getUserId())
            .title(run.getTitle())
            .description(run.getDescription())
            .startTime(run.getStartTime())
            .endTime(run.getEndTime())
            .durationSeconds(run.getDurationSeconds())
            .formattedDuration(run.getFormattedDuration())
            .distanceMeters(run.getDistanceMeters())
            .distanceKm(run.getDistanceKm())
            .averagePaceSecondsPerKm(pace)
            .formattedPace(formatPace(pace))
            .averageSpeedKmh(run.getAverageSpeedKmh())
            .maxSpeedKmh(run.getMaxSpeedKmh())
            .caloriesBurned(run.getCaloriesBurned())
            .elevationGainMeters(run.getElevationGainMeters())
            .elevationLossMeters(run.getElevationLossMeters())
            .runType(run.getRunType().name())
            .status(run.getStatus().name())
            .weatherCondition(run.getWeatherCondition())
            .temperatureCelsius(run.getTemperatureCelsius())
            .isPublic(run.getIsPublic())
            .dataSource("MANUAL")
            .createdAt(run.getCreatedAt())
            .updatedAt(run.getUpdatedAt())
            .build();
    }

    private String formatPace(Double averagePaceSecondsPerKm) {
        if (averagePaceSecondsPerKm != null) {
            int totalSeconds = averagePaceSecondsPerKm.intValue();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%d:%02d min/km", minutes, seconds);
        }
        return null;
    }

    private Double calculatePaceIfMissing(Run run) {
        // First try to calculate from distance and duration (most reliable)
        if (run.getDistanceMeters() != null && run.getDurationSeconds() != null 
            && run.getDistanceMeters().doubleValue() > 0 && run.getDurationSeconds() > 0) {
            double distanceKm = run.getDistanceMeters().doubleValue() / 1000.0;
            double paceSecondsPerKm = run.getDurationSeconds() / distanceKm;
            log.debug("Calculated pace from distance/duration: {} s/km", paceSecondsPerKm);
            return paceSecondsPerKm;
        }
        
        // Try to calculate from average speed if available
        if (run.getAverageSpeedKmh() != null && run.getAverageSpeedKmh().doubleValue() > 0) {
            double speedKmh = run.getAverageSpeedKmh().doubleValue();
            double paceMinPerKm = 60.0 / speedKmh;
            double paceSecondsPerKm = paceMinPerKm * 60;
            log.debug("Calculated pace from average speed: {} s/km", paceSecondsPerKm);
            return paceSecondsPerKm;
        }
        
        // Fall back to stored pace if calculation not possible
        if (run.getAveragePaceSecondsPerKm() != null && run.getAveragePaceSecondsPerKm() > 0) {
            log.debug("Using stored pace: {} s/km", run.getAveragePaceSecondsPerKm());
            return run.getAveragePaceSecondsPerKm();
        }
        
        log.debug("No valid pace data available for run {}", run.getId());
        return null;
    }

    private void updateRunFields(Run run, Run updates) {
        if (updates.getTitle() != null) run.setTitle(updates.getTitle());
        if (updates.getDescription() != null) run.setDescription(updates.getDescription());
        if (updates.getStartTime() != null) run.setStartTime(updates.getStartTime());
        if (updates.getEndTime() != null) run.setEndTime(updates.getEndTime());
        if (updates.getDurationSeconds() != null) run.setDurationSeconds(updates.getDurationSeconds());
        if (updates.getDistanceMeters() != null) run.setDistanceMeters(updates.getDistanceMeters());
        if (updates.getAveragePaceSecondsPerKm() != null) run.setAveragePaceSecondsPerKm(updates.getAveragePaceSecondsPerKm());
        if (updates.getMaxSpeedKmh() != null) run.setMaxSpeedKmh(updates.getMaxSpeedKmh());
        if (updates.getAverageSpeedKmh() != null) run.setAverageSpeedKmh(updates.getAverageSpeedKmh());
        if (updates.getCaloriesBurned() != null) run.setCaloriesBurned(updates.getCaloriesBurned());
        if (updates.getElevationGainMeters() != null) run.setElevationGainMeters(updates.getElevationGainMeters());
        if (updates.getElevationLossMeters() != null) run.setElevationLossMeters(updates.getElevationLossMeters());
        if (updates.getRunType() != null) run.setRunType(updates.getRunType());
        if (updates.getStatus() != null) run.setStatus(updates.getStatus());
        if (updates.getWeatherCondition() != null) run.setWeatherCondition(updates.getWeatherCondition());
        if (updates.getTemperatureCelsius() != null) run.setTemperatureCelsius(updates.getTemperatureCelsius());
        if (updates.getIsPublic() != null) run.setIsPublic(updates.getIsPublic());
    }

    private Double calculateAveragePace(List<RunDto> runs) {
        return runs.stream()
            .filter(run -> run.getAveragePaceSecondsPerKm() != null)
            .mapToDouble(run -> run.getAveragePaceSecondsPerKm().doubleValue())
            .average()
            .orElse(0.0);
    }

    private Double calculateBestPace(List<RunDto> runs) {
        return runs.stream()
            .filter(run -> run.getAveragePaceSecondsPerKm() != null)
            .mapToDouble(run -> run.getAveragePaceSecondsPerKm().doubleValue())
            .min()
            .orElse(0.0);
    }

    private Double calculateLongestRun(List<RunDto> runs) {
        return runs.stream()
            .filter(run -> run.getDistanceKm() != null)
            .mapToDouble(RunDto::getDistanceKm)
            .max()
            .orElse(0.0);
    }

    /**
     * Get lap data for a specific run
     */
    @Transactional(readOnly = true)
    public List<LapDto> getRunLaps(Long runId, Long userId) {
        log.debug("Fetching laps for run {} and user {}", runId, userId);

        // Check if this is a FIT file run
        Optional<FitFileUpload> fitFile = fitFileUploadRepository.findByIdAndUserIdWithTrackPoints(runId, userId);

        if (fitFile.isPresent() && fitFile.get().isProcessed()) {
            List<FitLapData> laps = fitLapDataRepository.findByFitFileUploadIdOrderByLapNumber(runId);
            return laps.stream()
                    .map(this::convertLapToDto)
                    .collect(Collectors.toList());
        }

        // Manual runs don't have lap data
        return new ArrayList<>();
    }

    private LapDto convertLapToDto(FitLapData lap) {
        return LapDto.builder()
                .id(lap.getId())
                .lapNumber(lap.getLapNumber())
                .startTime(lap.getStartTime())
                .endTime(lap.getEndTime())
                .totalTimerTime(lap.getTotalTimerTime())
                .formattedDuration(lap.getFormattedDuration())
                .totalDistance(lap.getTotalDistance())
                .distanceKm(lap.getDistanceKm())
                .avgSpeed(lap.getAvgSpeed())
                .maxSpeed(lap.getMaxSpeed())
                .avgSpeedKmh(lap.getAvgSpeedKmh())
                .maxSpeedKmh(lap.getMaxSpeedKmh())
                .avgPace(formatPaceFromSpeed(lap.getAvgSpeedKmh()))
                .avgHeartRate(lap.getAvgHeartRate())
                .maxHeartRate(lap.getMaxHeartRate())
                .avgCadence(lap.getAvgCadence())
                .totalSteps(lap.getTotalSteps())
                .avgStrideLength(lap.getAvgStrideLength())
                .totalAscent(lap.getTotalAscent())
                .totalDescent(lap.getTotalDescent())
                .totalCalories(lap.getTotalCalories())
                .lapTrigger(lap.getLapTrigger() != null ? lap.getLapTrigger().name() : null)
                .sport(lap.getSport() != null ? lap.getSport().name() : null)
                .build();
    }

    private String formatPaceFromSpeed(Double speedKmh) {
        if (speedKmh == null || speedKmh <= 0) {
            return "--:--";
        }

        // Convert speed (km/h) to pace (min/km)
        double paceMinPerKm = 60.0 / speedKmh;
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);

        return String.format("%d:%02d min/km", minutes, seconds);
    }

    /**
     * Attempt to automatically match a run to predefined routes and persist the result.
     * This is called when a run is completed.
     */
    private void attemptAutoRouteMatching(Run run) {
        try {
            // Check if run has GPS points
            long gpsPointCount = gpsPointRepository.countByRunId(run.getId());
            if (gpsPointCount == 0) {
                log.info("Run {} has no GPS points - skipping automatic route matching", run.getId());
                return;
            }

            log.info("Run {} has {} GPS points, attempting route matching", run.getId(), gpsPointCount);

            // Load user
            Optional<com.mainstream.user.entity.User> userOpt = userRepository.findById(run.getUserId());
            if (userOpt.isEmpty()) {
                log.error("User {} not found for run {}", run.getUserId(), run.getId());
                return;
            }

            // Attempt route matching and persist result
            com.mainstream.activity.entity.UserActivity activity =
                userActivityService.processAndCreateActivityFromRun(userOpt.get(), run);

            if (activity != null && activity.getMatchedRoute() != null) {
                log.info("Successfully matched run {} to route: {} ({}% complete)",
                         run.getId(),
                         activity.getMatchedRoute().getName(),
                         activity.getRouteCompletionPercentage());
            } else {
                log.info("Run {} did not match any predefined route", run.getId());
            }
        } catch (Exception e) {
            log.error("Error during automatic route matching for run {}: {}", run.getId(), e.getMessage(), e);
            // Don't fail the run operation if route matching fails
        }
    }
}