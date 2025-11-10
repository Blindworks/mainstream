package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.ConsistencyConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Checker for CONSISTENCY trophies.
 * Awards trophies for regular training patterns (e.g., 3x per week for 4 weeks).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsistencyChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            ConsistencyConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), ConsistencyConfig.class);

            if (config.getMinActivitiesPerWeek() == null || config.getNumberOfWeeks() == null) {
                log.warn("Consistency trophy {} missing required configuration", trophy.getCode());
                return false;
            }

            int consecutiveWeeks = countConsecutiveWeeksWithMinActivity(user, config);
            return consecutiveWeeks >= config.getNumberOfWeeks();

        } catch (Exception e) {
            log.error("Error checking consistency trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            ConsistencyConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), ConsistencyConfig.class);

            if (config.getNumberOfWeeks() == null) {
                return new TrophyProgress(0, 0);
            }

            int consecutiveWeeks = countConsecutiveWeeksWithMinActivity(user, config);
            return new TrophyProgress(consecutiveWeeks, config.getNumberOfWeeks());

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.CONSISTENCY;
    }

    /**
     * Count consecutive weeks where user met the minimum activity requirement.
     */
    private int countConsecutiveWeeksWithMinActivity(User user, ConsistencyConfig config) {
        // Look back enough to find the required number of weeks
        int weeksToCheck = Math.max(config.getNumberOfWeeks() * 2, 12);
        LocalDateTime startDate = LocalDateTime.now().minusWeeks(weeksToCheck);

        List<UserActivity> activities = userActivityRepository.findUserActivitiesSince(user.getId(), startDate);

        // Filter by minimum distance if specified
        if (config.getMinDistancePerActivity() != null && config.getMinDistancePerActivity() > 0) {
            activities = activities.stream()
                .filter(act -> act.getDistanceMeters() != null
                    && act.getDistanceMeters() >= config.getMinDistancePerActivity())
                .toList();
        }

        // Group activities by week
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<Integer, Long> activitiesPerWeek = activities.stream()
            .collect(Collectors.groupingBy(
                act -> act.getActivityStartTime().get(weekFields.weekOfWeekBasedYear()),
                Collectors.counting()
            ));

        // Count consecutive weeks from current week backwards
        int consecutiveWeeks = 0;
        LocalDateTime currentWeek = LocalDateTime.now();

        for (int i = 0; i < weeksToCheck; i++) {
            int weekNumber = currentWeek.get(weekFields.weekOfWeekBasedYear());
            Long weekCount = activitiesPerWeek.getOrDefault(weekNumber, 0L);

            if (weekCount >= config.getMinActivitiesPerWeek()) {
                consecutiveWeeks++;
            } else if (i > 0) {
                // Break on first week that doesn't meet requirement (after current week)
                // Allow current week to not meet requirement yet
                break;
            }

            currentWeek = currentWeek.minusWeeks(1);
        }

        return consecutiveWeeks;
    }
}
