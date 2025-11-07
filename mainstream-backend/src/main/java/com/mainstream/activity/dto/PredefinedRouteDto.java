package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedRouteDto {
    private Long id;
    private String name;
    private String description;
    private String originalFilename;
    private BigDecimal distanceMeters;
    private BigDecimal elevationGainMeters;
    private BigDecimal elevationLossMeters;
    private BigDecimal startLatitude;
    private BigDecimal startLongitude;
    private Boolean isActive;
    private Integer trackPointCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RouteTrackPointDto> trackPoints;
}
