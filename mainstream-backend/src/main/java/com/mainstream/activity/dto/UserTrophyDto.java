package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTrophyDto {
    private Long id;
    private Long userId;
    private String userName;
    private TrophyDto trophy;
    private Long activityId;
    private LocalDateTime earnedAt;
    private String metadata;
}
