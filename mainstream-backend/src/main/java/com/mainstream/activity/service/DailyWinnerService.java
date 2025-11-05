package com.mainstream.activity.service;

import com.mainstream.activity.entity.DailyWinner;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.DailyWinnerRepository;
import com.mainstream.activity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and managing daily winners in various categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyWinnerService {

    private final DailyWinnerRepository dailyWinnerRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * Calculate daily winners for a specific date.
     * This should be called at the end of each day or manually triggered.
     *
     * @param date The date to calculate winners for
     */
    @Transactional
    public void calculateDailyWinners(LocalDate date) {
        log.info("Calculating daily winners for date: {}", date);

        List<UserActivity> todaysActivities = getActivitiesForDate(date);

        if (todaysActivities.isEmpty()) {
            log.info("No activities found for date: {}", date);
            return;
        }

        // Calculate winners for each category
        calculateEarliestRun(date, todaysActivities);
        calculateLatestRun(date, todaysActivities);
        calculateLongestStreak(date, todaysActivities);
        calculateMostRuns(date, todaysActivities);
        calculateMostRoutes(date, todaysActivities);
        calculateLongestDistance(date, todaysActivities);

        log.info("Completed daily winner calculation for date: {}", date);
    }

    /**
     * Get all activities for a specific date.
     */
    private List<UserActivity> getActivitiesForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return userActivityRepository.findAll().stream()
            .filter(activity -> activity.getActivityStartTime() != null &&
                              !activity.getActivityStartTime().isBefore(startOfDay) &&
                              !activity.getActivityStartTime().isAfter(endOfDay))
            .collect(Collectors.toList());
    }

    /**
     * Calculate earliest run winner (Frühaufsteher).
     */
    private void calculateEarliestRun(LocalDate date, List<UserActivity> activities) {
        Optional<UserActivity> earliest = activities.stream()
            .filter(a -> a.getActivityStartTime() != null)
            .min(Comparator.comparing(UserActivity::getActivityStartTime));

        if (earliest.isPresent()) {
            UserActivity activity = earliest.get();
            saveWinner(date, DailyWinner.WinnerCategory.EARLIEST_RUN,
                      activity.getUser(), activity,
                      BigDecimal.valueOf(activity.getActivityStartTime().toLocalTime().toSecondOfDay()),
                      String.format("Earliest run at %s", activity.getActivityStartTime().toLocalTime()));
        }
    }

    /**
     * Calculate latest run winner (Nachteule).
     */
    private void calculateLatestRun(LocalDate date, List<UserActivity> activities) {
        Optional<UserActivity> latest = activities.stream()
            .filter(a -> a.getActivityStartTime() != null)
            .max(Comparator.comparing(UserActivity::getActivityStartTime));

        if (latest.isPresent()) {
            UserActivity activity = latest.get();
            saveWinner(date, DailyWinner.WinnerCategory.LATEST_RUN,
                      activity.getUser(), activity,
                      BigDecimal.valueOf(activity.getActivityStartTime().toLocalTime().toSecondOfDay()),
                      String.format("Latest run at %s", activity.getActivityStartTime().toLocalTime()));
        }
    }

    /**
     * Calculate longest streak winner.
     */
    private void calculateLongestStreak(LocalDate date, List<UserActivity> activities) {
        Map<Long, Integer> userStreaks = new HashMap<>();

        // Get unique users from today's activities
        Set<Long> activeUsers = activities.stream()
            .map(a -> a.getUser().getId())
            .collect(Collectors.toSet());

        // Calculate streak for each active user
        for (Long userId : activeUsers) {
            int streak = calculateStreakForUser(userId, date);
            userStreaks.put(userId, streak);
        }

        // Find user with longest streak
        Optional<Map.Entry<Long, Integer>> maxStreak = userStreaks.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (maxStreak.isPresent() && maxStreak.get().getValue() > 0) {
            Long userId = maxStreak.getKey();
            Integer streakDays = maxStreak.get().getValue();

            UserActivity userActivity = activities.stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);

            if (userActivity != null) {
                saveWinner(date, DailyWinner.WinnerCategory.LONGEST_STREAK,
                          userActivity.getUser(), userActivity,
                          BigDecimal.valueOf(streakDays),
                          String.format("Streak of %d consecutive days", streakDays));
            }
        }
    }

    /**
     * Calculate most runs winner (Consistency King).
     */
    private void calculateMostRuns(LocalDate date, List<UserActivity> activities) {
        // Count runs per user for the past week
        LocalDateTime weekAgo = date.atStartOfDay().minusDays(7);
        Map<Long, Long> userRunCounts = activities.stream()
            .filter(a -> a.getActivityStartTime().isAfter(weekAgo))
            .collect(Collectors.groupingBy(
                a -> a.getUser().getId(),
                Collectors.counting()
            ));

        Optional<Map.Entry<Long, Long>> maxRuns = userRunCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (maxRuns.isPresent() && maxRuns.get().getValue() > 0) {
            Long userId = maxRuns.getKey();
            Long runCount = maxRuns.get().getValue();

            UserActivity userActivity = activities.stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);

            if (userActivity != null) {
                saveWinner(date, DailyWinner.WinnerCategory.MOST_RUNS,
                          userActivity.getUser(), userActivity,
                          BigDecimal.valueOf(runCount),
                          String.format("%d runs this week", runCount));
            }
        }
    }

    /**
     * Calculate most routes winner (Explorer).
     */
    private void calculateMostRoutes(LocalDate date, List<UserActivity> activities) {
        Map<Long, Long> userRouteCounts = activities.stream()
            .filter(a -> a.getMatchedRoute() != null)
            .collect(Collectors.groupingBy(
                a -> a.getUser().getId(),
                Collectors.mapping(a -> a.getMatchedRoute().getId(), Collectors.toSet())
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (long) e.getValue().size()
            ));

        Optional<Map.Entry<Long, Long>> maxRoutes = userRouteCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (maxRoutes.isPresent() && maxRoutes.get().getValue() > 0) {
            Long userId = maxRoutes.getKey();
            Long routeCount = maxRoutes.get().getValue();

            UserActivity userActivity = activities.stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);

            if (userActivity != null) {
                saveWinner(date, DailyWinner.WinnerCategory.MOST_ROUTES,
                          userActivity.getUser(), userActivity,
                          BigDecimal.valueOf(routeCount),
                          String.format("Ran %d different routes", routeCount));
            }
        }
    }

    /**
     * Calculate longest distance winner.
     */
    private void calculateLongestDistance(LocalDate date, List<UserActivity> activities) {
        Map<Long, Double> userDistances = activities.stream()
            .filter(a -> a.getDistanceMeters() != null)
            .collect(Collectors.groupingBy(
                a -> a.getUser().getId(),
                Collectors.summingDouble(a -> a.getDistanceMeters().doubleValue())
            ));

        Optional<Map.Entry<Long, Double>> maxDistance = userDistances.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (maxDistance.isPresent() && maxDistance.get().getValue() > 0) {
            Long userId = maxDistance.getKey();
            Double distanceMeters = maxDistance.get().getValue();

            UserActivity userActivity = activities.stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);

            if (userActivity != null) {
                saveWinner(date, DailyWinner.WinnerCategory.LONGEST_DISTANCE,
                          userActivity.getUser(), userActivity,
                          BigDecimal.valueOf(distanceMeters),
                          String.format("Total distance: %.2f km", distanceMeters / 1000.0));
            }
        }
    }

    /**
     * Calculate consecutive day streak for a user up to a specific date.
     */
    private int calculateStreakForUser(Long userId, LocalDate upToDate) {
        int streak = 0;
        LocalDate checkDate = upToDate;

        for (int i = 0; i < 365; i++) { // Check up to a year back
            LocalDateTime startOfDay = checkDate.atStartOfDay();
            LocalDateTime endOfDay = checkDate.atTime(LocalTime.MAX);

            boolean hasActivity = userActivityRepository.findByUserIdAndActivityStartTimeBetween(
                userId, startOfDay, endOfDay
            ).size() > 0;

            if (hasActivity) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (i > 0) {
                // Break on first missing day (after checking the current day)
                break;
            } else {
                // No activity on the date we're checking
                break;
            }
        }

        return streak;
    }

    /**
     * Save a daily winner to the database.
     */
    private void saveWinner(LocalDate date, DailyWinner.WinnerCategory category,
                           com.mainstream.user.entity.User user, UserActivity activity,
                           BigDecimal achievementValue, String description) {
        // Check if winner already exists for this date and category
        Optional<DailyWinner> existing = dailyWinnerRepository.findByWinnerDateAndCategory(date, category);
        if (existing.isPresent()) {
            log.debug("Winner already exists for date {} and category {}, skipping", date, category);
            return;
        }

        DailyWinner winner = new DailyWinner();
        winner.setWinnerDate(date);
        winner.setCategory(category);
        winner.setUser(user);
        winner.setActivity(activity);
        winner.setAchievementValue(achievementValue);
        winner.setAchievementDescription(description);

        dailyWinnerRepository.save(winner);
        log.info("Saved daily winner: {} for category: {} on date: {}", user.getId(), category, date);
    }

    /**
     * Get all winners for a specific date.
     */
    public List<DailyWinner> getWinnersForDate(LocalDate date) {
        return dailyWinnerRepository.findByWinnerDateOrderByCategoryAsc(date);
    }

    /**
     * Get recent winners (last N days).
     */
    public List<DailyWinner> getRecentWinners(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return dailyWinnerRepository.findRecentWinners(startDate);
    }

    /**
     * Scheduled task to calculate daily winners at end of each day.
     * Runs at 23:59 every day.
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void scheduledDailyWinnerCalculation() {
        log.info("Running scheduled daily winner calculation");
        calculateDailyWinners(LocalDate.now());
    }
}
