package com.mainstream.fitfile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mainstream.fitfile.entity.FitFileUpload;
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
public class FitFileUploadDto {

    private Long id;
    private Long userId;
    private String originalFilename;
    private Long fileSize;
    private String fileHash;
    private FitFileUpload.ProcessingStatus processingStatus;
    private String errorMessage;

    // FIT File Header Information
    private Integer fitProtocolVersion;
    private Integer fitProfileVersion;
    private String fitManufacturer;
    private String fitProduct;
    private String fitDeviceSerial;

    // Activity Summary Data
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime activityStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime activityEndTime;

    private Integer totalElapsedTime;
    private Integer totalTimerTime;
    private BigDecimal totalDistance;
    private Integer totalCalories;
    private BigDecimal totalAscent;
    private BigDecimal totalDescent;

    // Running Specific Metrics
    private BigDecimal avgSpeed;
    private BigDecimal maxSpeed;
    private Integer avgHeartRate;
    private Integer maxHeartRate;
    private Integer avgCadence;
    private Integer maxCadence;
    private Integer avgRunningPower;
    private Integer maxRunningPower;
    private Integer totalSteps;
    private BigDecimal avgStrideLength;
    private BigDecimal avgVerticalOscillation;
    private Integer avgGroundContactTime;
    private BigDecimal avgGroundContactBalance;

    // Training Stress and Load
    private Integer trainingStressScore;
    private Integer trainingLoadPeak;
    private Integer recoveryTime;

    // Environmental Data
    private Integer avgTemperature;
    private String weatherCondition;

    // GPS and Location Data
    private BigDecimal startPositionLat;
    private BigDecimal startPositionLong;
    private BigDecimal endPositionLat;
    private BigDecimal endPositionLong;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    // Calculated/Utility fields for API response
    private Double distanceKm;
    private Double avgPaceMinPerKm;
    private Double avgSpeedKmh;
    private Double maxSpeedKmh;
    private String formattedDuration;
    private Boolean isProcessed;
    private Boolean isFailed;
}