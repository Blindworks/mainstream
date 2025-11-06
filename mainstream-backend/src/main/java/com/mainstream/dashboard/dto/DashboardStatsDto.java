package com.mainstream.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard statistics DTO containing aggregated stats for today, month, and year
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {

    private PeriodStats today;
    private PeriodStats thisMonth;
    private PeriodStats thisYear;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodStats {
        private Long activeUsers;      // Users who completed runs
        private Long competitions;     // Active competitions
        private Long runs;             // Completed runs
        private Long trophies;         // Trophies earned
    }
}
