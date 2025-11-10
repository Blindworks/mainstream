package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for trophy progress information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrophyProgressDto {
    private Long trophyId;
    private long currentValue;
    private long targetValue;
    private int percentage;
    private boolean isComplete;
    private boolean isEarned;
}
