package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store device information from FIT files.
 * Captures data from DeviceInfo messages including hardware/software details,
 * battery status, and device capabilities.
 */
@Entity
@Table(name = "fit_device_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitDeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    // Device identification
    @Column(name = "device_index")
    private Integer deviceIndex;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "garmin_product")
    private String garminProduct;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "serial_number")
    private Long serialNumber;

    // Software version information
    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "hardware_version")
    private Integer hardwareVersion;

    @Column(name = "cum_operating_time")
    private Long cumOperatingTime; // seconds

    // Battery information
    @Column(name = "battery_voltage")
    private Integer batteryVoltage; // millivolts

    @Enumerated(EnumType.STRING)
    @Column(name = "battery_status")
    private BatteryStatus batteryStatus;

    // Device capabilities and features
    @Column(name = "ant_transmission_type")
    private Integer antTransmissionType;

    @Column(name = "ant_device_number")
    private Integer antDeviceNumber;

    @Column(name = "ant_network")
    private String antNetwork;

    @Column(name = "source_type")
    private String sourceType;

    // Timestamps
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Additional device-specific fields
    @Column(name = "descriptor")
    private String descriptor;

    @Column(name = "favero_product")
    private String faveroProduct;

    public enum BatteryStatus {
        NEW,
        GOOD,
        OK,
        LOW,
        CRITICAL,
        CHARGING,
        UNKNOWN
    }

    // Utility methods
    public boolean isPrimaryDevice() {
        return deviceIndex != null && deviceIndex == 0;
    }

    public boolean isGarminDevice() {
        return "GARMIN".equalsIgnoreCase(manufacturer);
    }

    public String getFormattedOperatingTime() {
        if (cumOperatingTime == null) return "Unknown";
        
        long hours = cumOperatingTime / 3600;
        long minutes = (cumOperatingTime % 3600) / 60;
        
        return String.format("%d:%02d hours", hours, minutes);
    }

    public Double getBatteryVoltageVolts() {
        return batteryVoltage != null ? batteryVoltage / 1000.0 : null;
    }
}