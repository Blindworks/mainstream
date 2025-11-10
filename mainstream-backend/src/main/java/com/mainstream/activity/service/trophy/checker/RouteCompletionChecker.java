package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.PredefinedRoute;
import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.PredefinedRouteRepository;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.RouteCompletionConfig;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for ROUTE_COMPLETION trophies.
 * Awards trophies for completing predefined routes.
 * Supports both specific route completion and multiple unique routes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RouteCompletionChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;
    private final PredefinedRouteRepository predefinedRouteRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            RouteCompletionConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), RouteCompletionConfig.class);

            // Mode 1: Specific route completion
            if (config.getRouteId() != null) {
                return checkSpecificRouteCompletion(user, activity, config);
            }

            // Mode 2: Multiple unique routes
            if (config.getUniqueRoutesCount() != null) {
                return checkUniqueRoutesCompletion(user, config);
            }

            log.warn("Route completion trophy {} has neither routeId nor uniqueRoutesCount configured", trophy.getCode());
            return false;

        } catch (Exception e) {
            log.error("Error checking route completion trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            RouteCompletionConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), RouteCompletionConfig.class);

            // Mode 1: Specific route (binary: completed or not)
            if (config.getRouteId() != null) {
                boolean completed = checkSpecificRouteCompletion(user, null, config);
                return new TrophyProgress(completed ? 1 : 0, 1);
            }

            // Mode 2: Multiple unique routes
            if (config.getUniqueRoutesCount() != null) {
                int completedRoutes = countCompletedUniqueRoutes(user, config);
                return new TrophyProgress(completedRoutes, config.getUniqueRoutesCount());
            }

            return new TrophyProgress(0, 0);

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.ROUTE_COMPLETION;
    }

    /**
     * Check if user completed a specific route.
     */
    private boolean checkSpecificRouteCompletion(User user, UserActivity activity, RouteCompletionConfig config) {
        // Verify route exists
        PredefinedRoute route = predefinedRouteRepository.findById(config.getRouteId()).orElse(null);
        if (route == null) {
            log.warn("Route {} not found", config.getRouteId());
            return false;
        }

        // If activity is provided, check just this activity
        if (activity != null && activity.getMatchedRoute() != null) {
            boolean routeMatches = activity.getMatchedRoute().getId().equals(config.getRouteId());
            boolean completionMet = activity.getRouteCompletionPercentage() != null
                && activity.getRouteCompletionPercentage() >= getMinMatchPercentage(config);
            return routeMatches && completionMet;
        }

        // Otherwise check all user activities
        LocalDateTime lookbackDate = LocalDateTime.now().minusYears(1);
        List<UserActivity> activities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

        return activities.stream()
            .anyMatch(act -> act.getMatchedRoute() != null
                && act.getMatchedRoute().getId().equals(config.getRouteId())
                && act.getRouteCompletionPercentage() != null
                && act.getRouteCompletionPercentage() >= getMinMatchPercentage(config));
    }

    /**
     * Check if user completed the required number of unique routes.
     */
    private boolean checkUniqueRoutesCompletion(User user, RouteCompletionConfig config) {
        int completedRoutes = countCompletedUniqueRoutes(user, config);
        return completedRoutes >= config.getUniqueRoutesCount();
    }

    /**
     * Count how many unique routes the user has completed.
     */
    private int countCompletedUniqueRoutes(User user, RouteCompletionConfig config) {
        LocalDateTime lookbackDate = LocalDateTime.now().minusYears(1);
        List<UserActivity> activities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

        Set<Long> completedRouteIds = new HashSet<>();
        int minMatchPercentage = getMinMatchPercentage(config);

        for (UserActivity activity : activities) {
            if (activity.getMatchedRoute() != null
                && activity.getRouteCompletionPercentage() != null
                && activity.getRouteCompletionPercentage() >= minMatchPercentage) {
                completedRouteIds.add(activity.getMatchedRoute().getId());
            }
        }

        return completedRouteIds.size();
    }

    /**
     * Get minimum match percentage from config (default: 80%)
     */
    private int getMinMatchPercentage(RouteCompletionConfig config) {
        return config.getMinMatchPercentage() != null ? config.getMinMatchPercentage() : 80;
    }
}
