package com.mainstream.fitfile.dto;

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
public class LapDto {

    private Long id;
    private Integer lapNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Timing
    private Integer totalTimerTime; // seconds (active time)
    private String formattedDuration;

    // Distance
    private BigDecimal totalDistance; // meters
    private Double distanceKm;

    // Speed
    private BigDecimal avgSpeed; // m/s
    private BigDecimal maxSpeed; // m/s
    private Double avgSpeedKmh;
    private Double maxSpeedKmh;

    // Pace
    private String avgPace; // min/km

    // Heart Rate
    private Integer avgHeartRate;
    private Integer maxHeartRate;

    // Running Dynamics
    private Integer avgCadence;
    private Integer totalSteps;
    private BigDecimal avgStrideLength;

    // Elevation
    private BigDecimal totalAscent;
    private BigDecimal totalDescent;

    // Energy
    private Integer totalCalories;

    // Lap Type
    private String lapTrigger;
    private String sport;
}
