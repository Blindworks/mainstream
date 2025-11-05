package com.mainstream.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Leaderboard entry DTO for displaying rankings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private Integer position;
    private Long userId;
    private String userName;
    private BigDecimal score;
    private BigDecimal bestPerformance;
    private String performanceUnit; // e.g., "km", "minutes", "runs"
    private Boolean isCurrentUser;
}
