package com.mainstream.competition.dto;

import com.mainstream.competition.entity.CompetitionParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Competition participant DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionParticipantDto {
    private Long id;
    private Long competitionId;
    private Long userId;
    private String userName;
    private LocalDateTime joinedDate;
    private CompetitionParticipant.ParticipantStatus status;
    private BigDecimal finalScore;
    private BigDecimal bestPerformance;
    private Integer position;
    private Integer currentPosition;
    private BigDecimal currentScore;
}
