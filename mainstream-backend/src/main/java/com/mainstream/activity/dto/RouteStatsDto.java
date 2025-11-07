package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing route statistics for different time periods.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStatsDto {
    private Long routeId;
    private Long todayCount;
    private Long thisWeekCount;
    private Long thisMonthCount;
    private Long thisYearCount;
    private Long totalCount;
}
