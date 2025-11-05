package com.mainstream.competition.dto;

import com.mainstream.competition.entity.Competition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO for competition lists
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionSummaryDto {
    private Long id;
    private String title;
    private String description;
    private Competition.CompetitionType type;
    private Competition.CompetitionStatus status;
    private Competition.DifficultyLevel difficulty;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String iconUrl;
    private Boolean isUserParticipating;
}
