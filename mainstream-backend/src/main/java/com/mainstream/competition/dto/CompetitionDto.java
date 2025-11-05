package com.mainstream.competition.dto;

import com.mainstream.competition.entity.Competition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full competition details DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDto {
    private Long id;
    private String title;
    private String description;
    private Competition.CompetitionType type;
    private Competition.CompetitionStatus status;
    private Competition.DifficultyLevel difficulty;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String prizeDescription;
    private String rules;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String iconUrl;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isUserParticipating;
}
