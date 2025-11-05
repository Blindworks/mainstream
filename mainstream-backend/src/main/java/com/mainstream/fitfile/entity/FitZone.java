package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity to store zone definitions from FIT files.
 * Supports Heart Rate, Power, and Speed/Pace zones with thresholds.
 */
@Entity
@Table(name = "fit_zones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false)
    private ZoneType zoneType;

    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber; // 1-based zone number

    @Column(name = "zone_name")
    private String zoneName;

    // Zone boundaries (values depend on zone type)
    @Column(name = "high_value", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal highValue;

    @Column(name = "low_value", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal lowValue;

    // Heart Rate Zone specific fields
    @Column(name = "hr_calc_type")
    private String hrCalcType; // percent_max_hr, percent_hrr, etc.

    @Column(name = "hr_zone_calc_type")
    private String hrZoneCalcType;

    // Power Zone specific fields
    @Column(name = "pwr_calc_type")
    private String pwrCalcType; // percent_ftp, watts_per_kg, etc.

    // Speed Zone specific fields
    @Column(name = "speed_calc_type")
    private String speedCalcType; // percent_max, time_per_distance, etc.

    // Additional zone properties
    @Column(name = "message_index")
    private Integer messageIndex;

    public enum ZoneType {
        HEART_RATE,
        POWER, 
        SPEED,
        CADENCE
    }

    // Utility methods
    public boolean isHeartRateZone() {
        return zoneType == ZoneType.HEART_RATE;
    }

    public boolean isPowerZone() {
        return zoneType == ZoneType.POWER;
    }

    public boolean isSpeedZone() {
        return zoneType == ZoneType.SPEED;
    }

    public String getFormattedRange() {
        if (lowValue == null || highValue == null) {
            return "Undefined";
        }

        switch (zoneType) {
            case HEART_RATE:
                return String.format("%.0f - %.0f bpm", lowValue, highValue);
            case POWER:
                return String.format("%.0f - %.0f watts", lowValue, highValue);
            case SPEED:
                // Convert m/s to min/km for pace
                double lowPace = 1000.0 / (lowValue.doubleValue() * 60.0);
                double highPace = 1000.0 / (highValue.doubleValue() * 60.0);
                return String.format("%.1f - %.1f min/km", highPace, lowPace); // Note: high/low swapped for pace
            case CADENCE:
                return String.format("%.0f - %.0f spm", lowValue, highValue);
            default:
                return String.format("%.2f - %.2f", lowValue, highValue);
        }
    }

    public boolean isValueInZone(BigDecimal value) {
        if (value == null || lowValue == null || highValue == null) {
            return false;
        }
        return value.compareTo(lowValue) >= 0 && value.compareTo(highValue) <= 0;
    }
}