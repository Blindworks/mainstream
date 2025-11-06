package com.mainstream.dashboard.service;

import com.mainstream.activity.repository.UserTrophyRepository;
import com.mainstream.competition.repository.CompetitionRepository;
import com.mainstream.dashboard.dto.DashboardStatsDto;
import com.mainstream.dashboard.dto.DashboardStatsDto.PeriodStats;
import com.mainstream.run.repository.RunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final RunRepository runRepository;
    private final CompetitionRepository competitionRepository;
    private final UserTrophyRepository userTrophyRepository;

    /**
     * Get dashboard statistics for today, this month, and this year
     */
    public DashboardStatsDto getDashboardStats() {
        log.debug("Fetching dashboard statistics");

        LocalDateTime now = LocalDateTime.now();

        // Calculate date ranges
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();
        LocalDateTime endOfYear = startOfYear.plusYears(1);

        // Get stats for today
        PeriodStats todayStats = PeriodStats.builder()
            .activeUsers(runRepository.countDistinctUsersWithRunsInPeriod(startOfToday, endOfToday))
            .competitions(competitionRepository.countActiveCompetitionsInPeriod(startOfToday, endOfToday))
            .runs(runRepository.countCompletedRunsInPeriod(startOfToday, endOfToday))
            .trophies(userTrophyRepository.countTrophiesEarnedInPeriod(startOfToday, endOfToday))
            .build();

        // Get stats for this month
        PeriodStats monthStats = PeriodStats.builder()
            .activeUsers(runRepository.countDistinctUsersWithRunsInPeriod(startOfMonth, endOfMonth))
            .competitions(competitionRepository.countActiveCompetitionsInPeriod(startOfMonth, endOfMonth))
            .runs(runRepository.countCompletedRunsInPeriod(startOfMonth, endOfMonth))
            .trophies(userTrophyRepository.countTrophiesEarnedInPeriod(startOfMonth, endOfMonth))
            .build();

        // Get stats for this year
        PeriodStats yearStats = PeriodStats.builder()
            .activeUsers(runRepository.countDistinctUsersWithRunsInPeriod(startOfYear, endOfYear))
            .competitions(competitionRepository.countActiveCompetitionsInPeriod(startOfYear, endOfYear))
            .runs(runRepository.countCompletedRunsInPeriod(startOfYear, endOfYear))
            .trophies(userTrophyRepository.countTrophiesEarnedInPeriod(startOfYear, endOfYear))
            .build();

        DashboardStatsDto stats = DashboardStatsDto.builder()
            .today(todayStats)
            .thisMonth(monthStats)
            .thisYear(yearStats)
            .build();

        log.debug("Dashboard stats - Today: {} users, {} competitions, {} runs, {} trophies",
            todayStats.getActiveUsers(), todayStats.getCompetitions(),
            todayStats.getRuns(), todayStats.getTrophies());

        return stats;
    }
}
