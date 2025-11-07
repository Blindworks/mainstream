package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteTrackPointDto {
    private Long id;
    private Integer sequenceNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal elevation;
    private BigDecimal distanceFromStartMeters;
}
