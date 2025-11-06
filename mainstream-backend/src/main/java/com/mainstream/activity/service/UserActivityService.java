package com.mainstream.activity.service;

import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.run.entity.GpsPoint;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.GpsPointRepository;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing user activities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final FitTrackPointRepository fitTrackPointRepository;
    private final GpsPointRepository gpsPointRepository;
    private final RouteMatchingService routeMatchingService;
    private final TrophyService trophyService;

    /**
     * Process a FIT file upload and create a user activity with route matching.
     *
     * @param user The user
     * @param fitFileUpload The processed FIT file upload
     * @return Created UserActivity
     */
    @Transactional
    public UserActivity processAndCreateActivity(User user, FitFileUpload fitFileUpload) {
        log.info("Processing activity for user {} from FIT file {}", user.getId(), fitFileUpload.getId());

        // Get track points from FIT file (only those with valid GPS data)
        List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(fitFileUpload.getId());

        if (trackPoints.isEmpty()) {
            log.warn("No track points found for FIT file {}", fitFileUpload.getId());
            return createBasicActivity(user, fitFileUpload, null);
        }

        // Match against predefined routes
        RouteMatchingService.RouteMatchResult matchResult = routeMatchingService.matchRoute(trackPoints);

        // Create activity
        UserActivity activity = createBasicActivity(user, fitFileUpload, matchResult);

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Create a basic user activity with optional route matching result.
     */
    private UserActivity createBasicActivity(User user, FitFileUpload fitFileUpload,
                                             RouteMatchingService.RouteMatchResult matchResult) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setFitFileUpload(fitFileUpload);

        // Set basic activity data from FIT file
        if (fitFileUpload.getActivityStartTime() != null) {
            activity.setActivityStartTime(fitFileUpload.getActivityStartTime());
        }
        if (fitFileUpload.getTotalTimerTime() != null) {
            activity.setDurationSeconds(fitFileUpload.getTotalTimerTime().intValue());
            if (activity.getActivityStartTime() != null) {
                activity.setActivityEndTime(activity.getActivityStartTime().plusSeconds(activity.getDurationSeconds()));
            }
        }
        if (fitFileUpload.getTotalDistance() != null) {
            activity.setDistanceMeters(fitFileUpload.getTotalDistance());
        }

        // Set route matching data if available
        if (matchResult != null && matchResult.getMatchedRoute() != null) {
            activity.setMatchedRoute(matchResult.getMatchedRoute());
            activity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
            activity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
            activity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
            activity.setIsCompleteRoute(matchResult.isCompleteRoute());
            activity.setDirection(matchResult.getDirection());

            log.info("Activity matched to route: {} ({}% complete, direction: {})",
                     matchResult.getMatchedRoute().getName(),
                     matchResult.getRouteCompletionPercentage(),
                     matchResult.getDirection());
        } else {
            activity.setIsCompleteRoute(false);
            activity.setDirection(UserActivity.RunDirection.UNKNOWN);
            log.info("Activity did not match any predefined route");
        }

        return userActivityRepository.save(activity);
    }

    /**
     * Get all activities for a user.
     */
    public List<UserActivity> getUserActivities(Long userId) {
        return userActivityRepository.findByUserIdOrderByActivityStartTimeDesc(userId);
    }

    /**
     * Get activity by ID.
     */
    public UserActivity getActivityById(Long id) {
        return userActivityRepository.findById(id).orElse(null);
    }

    /**
     * Process a manual Run and create a user activity with route matching.
     *
     * @param user The user
     * @param run The completed run
     * @return Created UserActivity, or null if no match found
     */
    @Transactional
    public UserActivity processAndCreateActivityFromRun(User user, Run run) {
        log.info("Processing activity for user {} from run {}", user.getId(), run.getId());

        // Get GPS points from the run
        List<GpsPoint> gpsPoints = gpsPointRepository.findByRunIdOrderBySequenceNumberAsc(run.getId());

        if (gpsPoints.isEmpty()) {
            log.warn("No GPS points found for run {}", run.getId());
            return null;
        }

        // Match against predefined routes
        RouteMatchingService.RouteMatchResult matchResult = routeMatchingService.matchRouteFromGpsPoints(gpsPoints);

        if (matchResult == null || matchResult.getMatchedRoute() == null) {
            log.info("Run {} did not match any predefined route", run.getId());
            return null;
        }

        // Create activity from run
        UserActivity activity = createActivityFromRun(user, run, matchResult);

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Create a user activity from a run with route matching result.
     */
    private UserActivity createActivityFromRun(User user, Run run,
                                               RouteMatchingService.RouteMatchResult matchResult) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setRun(run);

        // Set basic activity data from run
        activity.setActivityStartTime(run.getStartTime());
        activity.setActivityEndTime(run.getEndTime());
        activity.setDurationSeconds(run.getDurationSeconds());
        activity.setDistanceMeters(run.getDistanceMeters());

        // Set route matching data
        activity.setMatchedRoute(matchResult.getMatchedRoute());
        activity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
        activity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
        activity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
        activity.setIsCompleteRoute(matchResult.isCompleteRoute());
        activity.setDirection(matchResult.getDirection());

        log.info("Run {} matched to route: {} ({}% complete, direction: {})",
                 run.getId(),
                 matchResult.getMatchedRoute().getName(),
                 matchResult.getRouteCompletionPercentage(),
                 matchResult.getDirection());

        return userActivityRepository.save(activity);
    }
}
