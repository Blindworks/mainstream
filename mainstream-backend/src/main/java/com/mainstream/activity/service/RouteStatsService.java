package com.mainstream.activity.service;

import com.mainstream.activity.dto.RouteStatsDto;
import com.mainstream.activity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Service for calculating route usage statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteStatsService {

    private final UserActivityRepository userActivityRepository;

    /**
     * Calculate statistics for a specific route.
     */
    public RouteStatsDto calculateRouteStats(Long routeId) {
        LocalDateTime now = LocalDateTime.now();

        // Today (from start of day)
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        long todayCount = userActivityRepository.countByRouteAndTimeRange(routeId, todayStart);

        // This week (from Monday)
        LocalDateTime weekStart = now.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        long thisWeekCount = userActivityRepository.countByRouteAndTimeRange(routeId, weekStart);

        // This month (from first day of month)
        LocalDateTime monthStart = now.toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay();
        long thisMonthCount = userActivityRepository.countByRouteAndTimeRange(routeId, monthStart);

        // This year (from first day of year)
        LocalDateTime yearStart = now.toLocalDate()
                .withDayOfYear(1)
                .atStartOfDay();
        long thisYearCount = userActivityRepository.countByRouteAndTimeRange(routeId, yearStart);

        // Total count
        long totalCount = userActivityRepository.countByRoute(routeId);

        return RouteStatsDto.builder()
                .routeId(routeId)
                .todayCount(todayCount)
                .thisWeekCount(thisWeekCount)
                .thisMonthCount(thisMonthCount)
                .thisYearCount(thisYearCount)
                .totalCount(totalCount)
                .build();
    }
}
