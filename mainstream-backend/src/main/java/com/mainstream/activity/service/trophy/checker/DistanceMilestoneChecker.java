package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.DistanceConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Checker for DISTANCE_MILESTONE trophies.
 * Supports both single activity and total distance achievements.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DistanceMilestoneChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            DistanceConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), DistanceConfig.class);

            if (config.getDistanceMeters() == null) {
                log.warn("Distance milestone trophy {} has no distanceMeters configured", trophy.getCode());
                return false;
            }

            String scope = config.getScope() != null ? config.getScope() : "TOTAL";

            if ("SINGLE_ACTIVITY".equals(scope)) {
                // Check if this specific activity meets the distance requirement
                if (activity == null) {
                    return false;
                }
                return activity.getDistanceMeters() != null
                    && activity.getDistanceMeters().compareTo(java.math.BigDecimal.valueOf(config.getDistanceMeters())) >= 0;
            } else {
                // TOTAL scope: Check total distance across all activities
                Long totalDistance = userActivityRepository.getTotalDistanceForUser(user.getId());
                if (totalDistance == null) {
                    totalDistance = 0L;
                }
                return totalDistance >= config.getDistanceMeters();
            }
        } catch (Exception e) {
            log.error("Error checking distance milestone trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            DistanceConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), DistanceConfig.class);

            if (config.getDistanceMeters() == null) {
                return new TrophyProgress(0, 0);
            }

            String scope = config.getScope() != null ? config.getScope() : "TOTAL";

            if ("SINGLE_ACTIVITY".equals(scope)) {
                // For single activity trophies, we can't really track progress
                // Return 0 progress (user needs to complete it in one go)
                return new TrophyProgress(0, config.getDistanceMeters());
            } else {
                // TOTAL scope: Return total distance progress
                Long totalDistance = userActivityRepository.getTotalDistanceForUser(user.getId());
                if (totalDistance == null) {
                    totalDistance = 0L;
                }
                return new TrophyProgress(totalDistance, config.getDistanceMeters());
            }
        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.DISTANCE_MILESTONE;
    }
}
