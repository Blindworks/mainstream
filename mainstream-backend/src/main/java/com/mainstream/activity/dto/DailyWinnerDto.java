package com.mainstream.activity.dto;

import com.mainstream.activity.entity.DailyWinner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyWinnerDto {
    private Long id;
    private LocalDate winnerDate;
    private DailyWinner.WinnerCategory category;
    private Long userId;
    private String userName;
    private String userFirstName;
    private String userLastName;
    private Long activityId;
    private BigDecimal achievementValue;
    private String achievementDescription;
    private LocalDateTime createdAt;
}
