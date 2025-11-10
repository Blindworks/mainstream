package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.SpecialConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Checker for SPECIAL trophies.
 * Handles various special achievement types like birthday runs, date-based events,
 * performance achievements, and first-time accomplishments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpecialChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            SpecialConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), SpecialConfig.class);

            if (config.getSpecialType() == null) {
                log.warn("Special trophy {} has no specialType configured", trophy.getCode());
                return false;
            }

            return switch (config.getSpecialType()) {
                case "BIRTHDAY_RUN" -> checkBirthdayRun(user, activity);
                case "DATE_BASED" -> checkDateBased(activity, config);
                case "PERFORMANCE" -> checkPerformance(activity, config);
                case "FIRST_ACTIVITY" -> checkFirstActivity(user);
                default -> {
                    log.warn("Unknown special type: {}", config.getSpecialType());
                    yield false;
                }
            };

        } catch (Exception e) {
            log.error("Error checking special trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            SpecialConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), SpecialConfig.class);

            // Most special trophies are binary (achieved or not)
            // Exception: Performance trophies can show progress
            if ("PERFORMANCE".equals(config.getSpecialType()) && config.getDistanceMeters() != null) {
                // Find best performance
                LocalDateTime lookbackDate = LocalDateTime.now().minusYears(1);
                List<UserActivity> activities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

                long bestTime = activities.stream()
                    .filter(act -> act.getDistanceMeters() != null
                        && act.getDistanceMeters().compareTo(java.math.BigDecimal.valueOf(config.getDistanceMeters())) >= 0
                        && act.getDurationSeconds() != null)
                    .mapToLong(UserActivity::getDurationSeconds)
                    .min()
                    .orElse(0);

                if (bestTime > 0) {
                    // Show progress as: how much faster than target
                    long target = config.getMaxDurationSeconds();
                    return new TrophyProgress(Math.max(0, target - bestTime), target);
                }
            }

            // For other special types, just binary
            boolean achieved = checkCriteria(user, null, trophy);
            return new TrophyProgress(achieved ? 1 : 0, 1);

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.SPECIAL;
    }

    /**
     * Check if activity was on user's birthday.
     */
    private boolean checkBirthdayRun(User user, UserActivity activity) {
        if (activity == null || user.getDateOfBirth() == null) {
            return false;
        }

        LocalDate activityDate = activity.getActivityStartTime().toLocalDate();
        LocalDate birthdate = user.getDateOfBirth();

        // Check if activity was on user's birthday (month and day match)
        return activityDate.getMonthValue() == birthdate.getMonthValue()
            && activityDate.getDayOfMonth() == birthdate.getDayOfMonth();
    }

    /**
     * Check if activity was on a specific date.
     */
    private boolean checkDateBased(UserActivity activity, SpecialConfig config) {
        if (activity == null || config.getMonth() == null || config.getDay() == null) {
            return false;
        }

        LocalDate activityDate = activity.getActivityStartTime().toLocalDate();

        return activityDate.getMonthValue() == config.getMonth()
            && activityDate.getDayOfMonth() == config.getDay();
    }

    /**
     * Check if activity meets performance criteria.
     * Example: 10km under 45 minutes
     */
    private boolean checkPerformance(UserActivity activity, SpecialConfig config) {
        if (activity == null
            || config.getDistanceMeters() == null
            || config.getMaxDurationSeconds() == null) {
            return false;
        }

        // Check distance requirement
        if (activity.getDistanceMeters() == null
            || activity.getDistanceMeters().compareTo(java.math.BigDecimal.valueOf(config.getDistanceMeters())) < 0) {
            return false;
        }

        // Check duration requirement
        if (activity.getDurationSeconds() == null) {
            return false;
        }

        return activity.getDurationSeconds() <= config.getMaxDurationSeconds();
    }

    /**
     * Check if this is user's first activity ever.
     * This trophy is awarded once the user has completed their first activity.
     */
    private boolean checkFirstActivity(User user) {
        List<UserActivity> allActivities = userActivityRepository.findByUserIdOrderByActivityStartTimeDesc(user.getId());
        return !allActivities.isEmpty(); // User has at least one activity
    }
}
