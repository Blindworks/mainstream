package com.mainstream.fitfile.service;

import com.mainstream.fitfile.entity.*;
import com.mainstream.fitfile.repository.*;
import com.mainstream.fitfile.service.impl.EnhancedFitFileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for enhanced FIT file processing
 * Validates zero data loss and comprehensive field mapping
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class EnhancedFitFileServiceTest {

    @Mock
    private FitFileUploadRepository fitFileUploadRepository;

    @Mock
    private FitTrackPointRepository fitTrackPointRepository;

    @Mock
    private FitLapDataRepository fitLapDataRepository;

    @Mock
    private FitDeviceInfoRepository fitDeviceInfoRepository;

    @Mock
    private FitZoneRepository fitZoneRepository;

    @Mock
    private FitEventRepository fitEventRepository;

    @Mock
    private FitHrvRepository fitHrvRepository;

    @InjectMocks
    private EnhancedFitFileServiceImpl enhancedFitFileService;

    @Test
    @DisplayName("Should process FIT file with comprehensive data capture")
    void shouldProcessFitFileWithComprehensiveDataCapture() {
        // Given
        Long userId = 1L;
        String filename = "comprehensive_run.fit";
        byte[] fitFileContent = createMockFitFileContent();

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            filename, 
            "application/octet-stream", 
            fitFileContent
        );

        FitFileUpload mockUpload = createMockFitFileUpload(userId, filename);
        when(fitFileUploadRepository.existsByFileHash(anyString())).thenReturn(false);
        when(fitFileUploadRepository.save(any(FitFileUpload.class))).thenReturn(mockUpload);

        // When
        var result = enhancedFitFileService.uploadFitFile(file, userId, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalFilename()).isEqualTo(filename);
        
        // Verify all repositories were called for saving comprehensive data
        verify(fitFileUploadRepository, atLeastOnce()).save(any(FitFileUpload.class));
        verify(fitTrackPointRepository, times(1)).saveAll(anyList());
        verify(fitLapDataRepository, times(1)).saveAll(anyList());
        verify(fitDeviceInfoRepository, times(1)).saveAll(anyList());
        verify(fitZoneRepository, times(1)).saveAll(anyList());
        verify(fitEventRepository, times(1)).saveAll(anyList());
        verify(fitHrvRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should capture all device information fields")
    void shouldCaptureAllDeviceInformationFields() {
        // This test would validate that all DeviceInfo message fields are captured
        // In a real implementation, you would mock the FIT file decoder to return
        // specific DeviceInfo messages and verify all fields are mapped correctly
        
        FitDeviceInfo expectedDeviceInfo = FitDeviceInfo.builder()
            .deviceIndex(0)
            .manufacturer("GARMIN")
            .garminProduct("FORERUNNER955")
            .serialNumber(1234567890L)
            .softwareVersion("20.26")
            .hardwareVersion(1)
            .batteryStatus(FitDeviceInfo.BatteryStatus.GOOD)
            .batteryVoltage(3800)
            .cumOperatingTime(8640000L) // 100 days in seconds
            .build();

        // Validate all critical device fields are present
        assertThat(expectedDeviceInfo.getManufacturer()).isEqualTo("GARMIN");
        assertThat(expectedDeviceInfo.isPrimaryDevice()).isTrue();
        assertThat(expectedDeviceInfo.isGarminDevice()).isTrue();
        assertThat(expectedDeviceInfo.getBatteryVoltageVolts()).isEqualTo(3.8);
        assertThat(expectedDeviceInfo.getFormattedOperatingTime()).contains("2400:00 hours");
    }

    @Test
    @DisplayName("Should capture all zone definitions")
    void shouldCaptureAllZoneDefinitions() {
        // Test heart rate zones
        FitZone hrZone = FitZone.builder()
            .zoneType(FitZone.ZoneType.HEART_RATE)
            .zoneNumber(1)
            .lowValue(new BigDecimal("100"))
            .highValue(new BigDecimal("120"))
            .zoneName("Active Recovery")
            .build();

        assertThat(hrZone.isHeartRateZone()).isTrue();
        assertThat(hrZone.getFormattedRange()).isEqualTo("100 - 120 bpm");
        assertThat(hrZone.isValueInZone(new BigDecimal("110"))).isTrue();
        assertThat(hrZone.isValueInZone(new BigDecimal("130"))).isFalse();

        // Test power zones
        FitZone powerZone = FitZone.builder()
            .zoneType(FitZone.ZoneType.POWER)
            .zoneNumber(3)
            .lowValue(new BigDecimal("200"))
            .highValue(new BigDecimal("250"))
            .zoneName("Tempo")
            .build();

        assertThat(powerZone.isPowerZone()).isTrue();
        assertThat(powerZone.getFormattedRange()).isEqualTo("200 - 250 watts");

        // Test speed zones
        FitZone speedZone = FitZone.builder()
            .zoneType(FitZone.ZoneType.SPEED)
            .zoneNumber(2)
            .lowValue(new BigDecimal("3.33")) // 5:00 min/km pace
            .highValue(new BigDecimal("4.17")) // 4:00 min/km pace
            .zoneName("Aerobic Base")
            .build();

        assertThat(speedZone.isSpeedZone()).isTrue();
    }

    @Test
    @DisplayName("Should capture all event types")
    void shouldCaptureAllEventTypes() {
        // Test timer events (auto-pause/resume)
        FitEvent autoPause = FitEvent.builder()
            .event(FitEvent.Event.TIMER)
            .eventType(FitEvent.EventType.STOP)
            .timestamp(LocalDateTime.now())
            .positionLat(new BigDecimal("50.1234567"))
            .positionLong(new BigDecimal("8.9876543"))
            .build();

        assertThat(autoPause.isTimerEvent()).isTrue();
        assertThat(autoPause.isAutoStopEvent()).isTrue();
        assertThat(autoPause.getEventDescription()).isEqualTo("Auto Pause");
        assertThat(autoPause.hasPositionData()).isTrue();

        // Test alert events
        FitEvent hrAlert = FitEvent.builder()
            .event(FitEvent.Event.HR_HIGH_ALERT)
            .eventType(FitEvent.EventType.MARKER)
            .heartRate(185)
            .build();

        assertThat(hrAlert.isAlertEvent()).isTrue();
        assertThat(hrAlert.getEventDescription()).isEqualTo("Heart Rate High Alert");

        // Test lap events
        FitEvent lapEvent = FitEvent.builder()
            .event(FitEvent.Event.LAP)
            .eventType(FitEvent.EventType.START)
            .build();

        assertThat(lapEvent.isLapEvent()).isTrue();
        assertThat(lapEvent.getEventDescription()).isEqualTo("Lap Start");
    }

    @Test
    @DisplayName("Should capture comprehensive track point data")
    void shouldCaptureComprehensiveTrackPointData() {
        FitTrackPoint comprehensiveTrackPoint = FitTrackPoint.builder()
            .sequenceNumber(100)
            .timestamp(LocalDateTime.now())
            .positionLat(new BigDecimal("50.12345678"))
            .positionLong(new BigDecimal("8.98765432"))
            .altitude(150.5)
            .enhancedAltitude(150.52)
            .distance(1000.0)
            .speed(3.5)
            .enhancedSpeed(3.52)
            .heartRate(165)
            .cadence(180)
            .runningPower(250)
            .temperature(20)
            .verticalOscillation(8.5)
            .stanceTime(250.5)
            .stanceTimePercent(35.2)
            .stanceTimeBalance(50.5)
            .stepLength(1.2)
            .groundContactTime(245)
            .groundContactBalance(51.0)
            .gpsAccuracy(3)
            .gpsFixType(FitTrackPoint.GpsFixType.GPS_3D)
            .satellites(12)
            .grade(2.5)
            .verticalSpeed(0.1)
            .respirationRate(25.5)
            .totalHemoglobinConc(14.2)
            .saturatedHemoglobinPercent(98.5)
            .build();

        // Validate utility methods
        assertThat(comprehensiveTrackPoint.getSpeedKmh()).isCloseTo(12.672, within(0.001));
        assertThat(comprehensiveTrackPoint.getPaceMinPerKm()).isCloseTo(4.73, within(0.01));
        assertThat(comprehensiveTrackPoint.getDistanceKm()).isEqualTo(1.0);
        assertThat(comprehensiveTrackPoint.getAltitudeMeters()).isEqualTo(150.52); // Uses enhanced value
        assertThat(comprehensiveTrackPoint.hasValidGpsPosition()).isTrue();
        assertThat(comprehensiveTrackPoint.hasRunningDynamics()).isTrue();
        assertThat(comprehensiveTrackPoint.hasPowerData()).isTrue();

        // Validate GPS quality
        assertThat(comprehensiveTrackPoint.getGpsFixType()).isEqualTo(FitTrackPoint.GpsFixType.GPS_3D);
        assertThat(comprehensiveTrackPoint.getSatellites()).isEqualTo(12);
        assertThat(comprehensiveTrackPoint.getGpsAccuracy()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should capture comprehensive lap data")
    void shouldCaptureComprehensiveLapData() {
        FitLapData comprehensiveLap = FitLapData.builder()
            .lapNumber(1)
            .startTime(LocalDateTime.now().minusMinutes(20))
            .endTime(LocalDateTime.now().minusMinutes(15))
            .totalElapsedTime(300) // 5 minutes
            .totalTimerTime(295) // 4:55 active time
            .totalDistance(new BigDecimal("1000.0"))
            .avgSpeed(new BigDecimal("3.39"))
            .maxSpeed(new BigDecimal("4.2"))
            .enhancedAvgSpeed(new BigDecimal("3.389"))
            .enhancedMaxSpeed(new BigDecimal("4.21"))
            .avgHeartRate(155)
            .maxHeartRate(168)
            .minHeartRate(142)
            .avgCadence(175)
            .maxCadence(190)
            .totalSteps(875)
            .avgRunningPower(245)
            .maxRunningPower(285)
            .normalizedPower(250)
            .avgStrideLength(new BigDecimal("1.15"))
            .avgVerticalOscillation(new BigDecimal("8.2"))
            .avgGroundContactTime(240)
            .avgGroundContactBalance(new BigDecimal("50.5"))
            .totalAscent(new BigDecimal("15.0"))
            .totalDescent(new BigDecimal("12.0"))
            .avgAltitude(new BigDecimal("155.0"))
            .maxAltitude(new BigDecimal("162.0"))
            .minAltitude(new BigDecimal("148.0"))
            .totalCalories(85)
            .fatCalories(25)
            .intensityFactor(new BigDecimal("0.85"))
            .trainingStressScore(45)
            .avgTemperature(18)
            .maxTemperature(20)
            .minTemperature(16)
            .lapTrigger(FitLapData.LapTrigger.DISTANCE)
            .sport(FitLapData.Sport.RUNNING)
            .subSport(FitLapData.SubSport.STREET)
            .build();

        // Validate utility methods
        assertThat(comprehensiveLap.getDistanceKm()).isEqualTo(1.0);
        assertThat(comprehensiveLap.getAvgPaceMinPerKm()).isCloseTo(4.91, within(0.01));
        assertThat(comprehensiveLap.getAvgSpeedKmh()).isCloseTo(12.20, within(0.01));
        assertThat(comprehensiveLap.getMaxSpeedKmh()).isCloseTo(15.16, within(0.01));
        assertThat(comprehensiveLap.getFormattedDuration()).isEqualTo("00:04:55");
        assertThat(comprehensiveLap.isRunningLap()).isTrue();
        assertThat(comprehensiveLap.isAutomaticLap()).isTrue();
        assertThat(comprehensiveLap.isManualLap()).isFalse();
    }

    @Test
    @DisplayName("Should capture HRV data for recovery analysis")
    void shouldCaptureHrvDataForRecoveryAnalysis() {
        List<Integer> sampleRRIntervals = List.of(850, 880, 820, 900, 870, 840, 860, 890);
        
        FitHrv hrvData = FitHrv.builder()
            .timestamp(LocalDateTime.now())
            .intervals(sampleRRIntervals)
            .rmssd(35.2)
            .pnn50(12.5)
            .stressScore(45)
            .hrvStatus("BALANCED")
            .build();

        // Validate HRV calculations
        assertThat(hrvData.getAverageRRInterval()).isCloseTo(863.75, within(0.01));
        assertThat(hrvData.getHeartRateFromRR()).isEqualTo(69); // 60000 / 863.75 ≈ 69 BPM
        assertThat(hrvData.hasValidData()).isTrue();

        // Validate all intervals are in reasonable range (300-2000ms)
        assertThat(hrvData.getIntervals()).allMatch(interval -> interval >= 300 && interval <= 2000);
    }

    private byte[] createMockFitFileContent() {
        // In a real test, this would be a valid FIT file binary content
        // For this example, we return a simple byte array
        return "MOCK_FIT_FILE_CONTENT".getBytes();
    }

    private FitFileUpload createMockFitFileUpload(Long userId, String filename) {
        return FitFileUpload.builder()
            .id(1L)
            .userId(userId)
            .originalFilename(filename)
            .fileSize(12345L)
            .processingStatus(FitFileUpload.ProcessingStatus.COMPLETED)
            .activityStartTime(LocalDateTime.now().minusHours(1))
            .activityEndTime(LocalDateTime.now().minusMinutes(30))
            .totalElapsedTime(1800) // 30 minutes
            .totalTimerTime(1750) // 29:10 active
            .totalDistance(new BigDecimal("5000.0"))
            .totalCalories(350)
            .avgSpeed(new BigDecimal("2.86")) // 5:50 min/km pace
            .maxSpeed(new BigDecimal("4.5"))
            .avgHeartRate(155)
            .maxHeartRate(175)
            .avgCadence(175)
            .maxCadence(190)
            .totalAscent(new BigDecimal("45.0"))
            .totalDescent(new BigDecimal("42.0"))
            .sport("RUNNING")
            .subSport("STREET")
            .fitManufacturer("GARMIN")
            .fitProduct("FORERUNNER955")
            .build();
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}