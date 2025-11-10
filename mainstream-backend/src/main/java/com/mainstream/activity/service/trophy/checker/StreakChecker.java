package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.StreakConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for STREAK trophies.
 * Checks for consecutive days of activity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StreakChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            StreakConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), StreakConfig.class);

            if (config.getConsecutiveDays() == null) {
                log.warn("Streak trophy {} has no consecutiveDays configured", trophy.getCode());
                return false;
            }

            int currentStreak = calculateCurrentStreak(user, config.getMinimumDistancePerDay());
            return currentStreak >= config.getConsecutiveDays();

        } catch (Exception e) {
            log.error("Error checking streak trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            StreakConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), StreakConfig.class);

            if (config.getConsecutiveDays() == null) {
                return new TrophyProgress(0, 0);
            }

            int currentStreak = calculateCurrentStreak(user, config.getMinimumDistancePerDay());
            return new TrophyProgress(currentStreak, config.getConsecutiveDays());

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.STREAK;
    }

    /**
     * Calculate current consecutive day streak for a user.
     *
     * @param user User to check
     * @param minimumDistancePerDay Optional minimum distance per day (in meters)
     * @return Number of consecutive days with activities
     */
    private int calculateCurrentStreak(User user, Integer minimumDistancePerDay) {
        // Look back up to 365 days for streak calculation
        LocalDateTime lookbackDate = LocalDateTime.now().minusDays(365);
        List<UserActivity> recentActivities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

        if (recentActivities.isEmpty()) {
            return 0;
        }

        // Filter by minimum distance if specified
        if (minimumDistancePerDay != null && minimumDistancePerDay > 0) {
            recentActivities = recentActivities.stream()
                .filter(activity -> activity.getDistanceMeters() != null
                    && activity.getDistanceMeters().compareTo(java.math.BigDecimal.valueOf(minimumDistancePerDay)) >= 0)
                .toList();
        }

        // Collect unique activity dates
        Set<String> activityDates = new HashSet<>();
        for (UserActivity activity : recentActivities) {
            activityDates.add(activity.getActivityStartTime().toLocalDate().toString());
        }

        // Count backwards from today
        int streak = 0;
        LocalDateTime date = LocalDateTime.now();

        // Check backwards from today
        for (int i = 0; i < 365; i++) {
            String dateStr = date.toLocalDate().toString();
            if (activityDates.contains(dateStr)) {
                streak++;
            } else if (i > 0) {
                // Break on first missing day (after checking today)
                // Allow today to be missing (streak continues from yesterday)
                break;
            }
            date = date.minusDays(1);
        }

        return streak;
    }
}
