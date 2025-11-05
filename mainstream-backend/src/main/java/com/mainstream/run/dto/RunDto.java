package com.mainstream.run.dto;

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
public class RunDto {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    
    // Time information
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private String formattedDuration;
    
    // Distance and pace
    private BigDecimal distanceMeters;
    private Double distanceKm;
    private Double averagePaceSecondsPerKm;
    private String formattedPace;
    
    // Speed
    private BigDecimal averageSpeedKmh;
    private BigDecimal maxSpeedKmh;
    
    // Health metrics
    private Integer caloriesBurned;
    private Integer averageHeartRate;
    private Integer maxHeartRate;
    
    // Running dynamics
    private Integer averageCadence;
    private Integer maxCadence;
    private Integer totalSteps;
    private BigDecimal averageStrideLength;
    
    // Elevation
    private BigDecimal elevationGainMeters;
    private BigDecimal elevationLossMeters;
    
    // Environmental
    private String weatherCondition;
    private Integer temperatureCelsius;
    
    // Run metadata
    private String runType;
    private String status;
    private Boolean isPublic;
    
    // GPS data
    private List<GpsPointDto> gpsPoints;
    
    // FIT-specific enhanced data
    private Integer averageRunningPower;
    private Integer maxRunningPower;
    private BigDecimal averageVerticalOscillation;
    private Integer averageGroundContactTime;
    private BigDecimal averageGroundContactBalance;
    private Integer trainingStressScore;
    
    // Zone data
    private Integer hrZone1Time;
    private Integer hrZone2Time;
    private Integer hrZone3Time;
    private Integer hrZone4Time;
    private Integer hrZone5Time;
    
    // Device information
    private String deviceManufacturer;
    private String deviceProduct;
    private String deviceSerial;
    
    // Source information
    private String dataSource; // "FIT", "MANUAL", "GPS_WATCH", etc.
    private Long fitFileUploadId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}