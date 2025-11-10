package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.TimeBasedConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Checker for TIME_BASED trophies.
 * Awards trophies for activities at specific times of day (e.g., early morning, late night).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TimeBasedChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            TimeBasedConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), TimeBasedConfig.class);

            if (config.getStartHour() == null || config.getEndHour() == null || config.getRequiredCount() == null) {
                log.warn("Time-based trophy {} missing required configuration", trophy.getCode());
                return false;
            }

            // Count all activities in the time window
            long count = countActivitiesInTimeWindow(user, config);
            return count >= config.getRequiredCount();

        } catch (Exception e) {
            log.error("Error checking time-based trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            TimeBasedConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), TimeBasedConfig.class);

            if (config.getRequiredCount() == null) {
                return new TrophyProgress(0, 0);
            }

            long count = countActivitiesInTimeWindow(user, config);
            return new TrophyProgress(count, config.getRequiredCount());

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.TIME_BASED;
    }

    /**
     * Count activities within the configured time window.
     */
    private long countActivitiesInTimeWindow(User user, TimeBasedConfig config) {
        // Get all user activities (we'll filter in memory)
        // In a production system, you'd want to add a repository method for this
        LocalDateTime lookbackDate = LocalDateTime.now().minusYears(1);
        List<UserActivity> allActivities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

        return allActivities.stream()
            .filter(act -> matchesTimeWindow(act, config))
            .count();
    }

    /**
     * Check if activity matches the time window configuration.
     */
    private boolean matchesTimeWindow(UserActivity activity, TimeBasedConfig config) {
        LocalDateTime startTime = activity.getActivityStartTime();
        int hour = startTime.getHour();

        // Check time window
        if (hour < config.getStartHour() || hour >= config.getEndHour()) {
            return false;
        }

        // Check day of week if specified
        if (config.getDaysOfWeek() != null && !config.getDaysOfWeek().isEmpty()) {
            int dayOfWeek = startTime.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
            if (!config.getDaysOfWeek().contains(dayOfWeek)) {
                return false;
            }
        }

        // Check minimum distance if specified
        if (config.getMinimumDistance() != null && config.getMinimumDistance() > 0) {
            if (activity.getDistanceMeters() == null
                || activity.getDistanceMeters().compareTo(java.math.BigDecimal.valueOf(config.getMinimumDistance())) < 0) {
                return false;
            }
        }

        return true;
    }
}
