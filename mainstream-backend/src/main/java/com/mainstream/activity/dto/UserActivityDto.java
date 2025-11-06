package com.mainstream.activity.dto;

import com.mainstream.activity.entity.UserActivity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long fitFileUploadId;
    private Long runId;
    private Long matchedRouteId;
    private String matchedRouteName;
    private UserActivity.RunDirection direction;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Integer durationSeconds;
    private BigDecimal distanceMeters;
    private BigDecimal matchedDistanceMeters;
    private BigDecimal routeCompletionPercentage;
    private BigDecimal averageMatchingAccuracyMeters;
    private Boolean isCompleteRoute;
    private LocalDateTime createdAt;
}
