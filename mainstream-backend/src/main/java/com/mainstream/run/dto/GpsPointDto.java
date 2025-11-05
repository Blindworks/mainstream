package com.mainstream.run.dto;

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
public class GpsPointDto {

    private Long id;
    private LocalDateTime timestamp;
    private Integer sequenceNumber;
    
    // Position data
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double altitude;
    
    // Distance and speed
    private Double distance; // cumulative distance in meters
    private Double speed; // m/s
    private Double speedKmh;
    
    // Health metrics
    private Integer heartRate;
    private Integer cadence;
    
    // Running dynamics
    private Integer runningPower;
    private Double verticalOscillation;
    private Double groundContactTime;
    private Double groundContactBalance;
    private Double stepLength;
    
    // Environmental
    private Integer temperature;
    
    // GPS quality
    private Integer gpsAccuracy;
    private String gpsFixType;
    private Integer satellites;
}