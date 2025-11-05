package com.mainstream.fitfile.processor;

import com.garmin.fit.Mesg;
import com.garmin.fit.SessionMesg;
import com.mainstream.fitfile.entity.FitFileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor for FIT session messages
 * Contains summary data for an activity session
 */
@Slf4j
@Component
public class SessionMessageProcessor extends AbstractMessageProcessor {

    @Override
    public String getMessageType() {
        return "session";
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return 18; // session is message number 18
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber) {
        if (!(mesg instanceof SessionMesg)) {
            return ProcessingResult.failure("Message is not a SessionMesg");
        }

        try {
            SessionMesg sessionMesg = (SessionMesg) mesg;
            Map<String, Object> fields = extractAllFields(mesg);

            // Update the main FitFileUpload entity with session data
            updateFitFileUpload(sessionMesg, fitFileUpload, fields);

            log.info("Processed session: sport={}, distance={}, duration={}",
                fields.get("sport"), fields.get("total_distance"), fields.get("total_timer_time"));

            return ProcessingResult.success(fields, extractDeveloperFields(mesg));

        } catch (Exception e) {
            log.error("Error processing session message: {}", e.getMessage(), e);
            return ProcessingResult.partial(extractAllFields(mesg), e.getMessage());
        }
    }

    private void updateFitFileUpload(SessionMesg mesg, FitFileUpload upload, Map<String, Object> fields) {
        // Basic timing
        if (mesg.getStartTime() != null) {
            upload.setActivityStartTime(convertToLocalDateTime(mesg.getStartTime()));
        }
        if (mesg.getTotalElapsedTime() != null) {
            upload.setTotalElapsedTime(mesg.getTotalElapsedTime().intValue());
        }
        if (mesg.getTotalTimerTime() != null) {
            upload.setTotalTimerTime(mesg.getTotalTimerTime().intValue());
        }

        // Calculate end time
        if (upload.getActivityStartTime() != null && upload.getTotalElapsedTime() != null) {
            upload.setActivityEndTime(
                upload.getActivityStartTime().plusSeconds(upload.getTotalElapsedTime())
            );
        }

        // Sport type
        if (mesg.getSport() != null) {
            upload.setSport(mesg.getSport().toString());
        }
        if (mesg.getSubSport() != null) {
            upload.setSubSport(mesg.getSubSport().toString());
        }

        // Distance and speed
        if (mesg.getTotalDistance() != null) {
            upload.setTotalDistance(new BigDecimal(mesg.getTotalDistance().toString()));
        }
        if (mesg.getAvgSpeed() != null) {
            upload.setAvgSpeed(new BigDecimal(mesg.getAvgSpeed().toString()));
        }
        if (mesg.getMaxSpeed() != null) {
            upload.setMaxSpeed(new BigDecimal(mesg.getMaxSpeed().toString()));
        }
        if (mesg.getEnhancedAvgSpeed() != null) {
            upload.setEnhancedAvgSpeed(new BigDecimal(mesg.getEnhancedAvgSpeed().toString()));
        }
        if (mesg.getEnhancedMaxSpeed() != null) {
            upload.setEnhancedMaxSpeed(new BigDecimal(mesg.getEnhancedMaxSpeed().toString()));
        }

        // Heart rate
        if (mesg.getAvgHeartRate() != null) {
            upload.setAvgHeartRate(mesg.getAvgHeartRate().intValue());
        }
        if (mesg.getMaxHeartRate() != null) {
            upload.setMaxHeartRate(mesg.getMaxHeartRate().intValue());
        }

        // Cadence
        if (mesg.getAvgCadence() != null) {
            upload.setAvgCadence(mesg.getAvgCadence().intValue());
        }
        if (mesg.getMaxCadence() != null) {
            upload.setMaxCadence(mesg.getMaxCadence().intValue());
        }
        if (mesg.getAvgRunningCadence() != null) {
            upload.setAvgRunningCadence(mesg.getAvgRunningCadence().intValue());
        }
        if (mesg.getMaxRunningCadence() != null) {
            upload.setMaxRunningCadence(mesg.getMaxRunningCadence().intValue());
        }

        // Elevation
        if (mesg.getTotalAscent() != null) {
            upload.setTotalAscent(new BigDecimal(mesg.getTotalAscent().toString()));
        }
        if (mesg.getTotalDescent() != null) {
            upload.setTotalDescent(new BigDecimal(mesg.getTotalDescent().toString()));
        }
        if (mesg.getEnhancedAvgAltitude() != null) {
            upload.setEnhancedAvgAltitude(new BigDecimal(mesg.getEnhancedAvgAltitude().toString()));
        }
        if (mesg.getEnhancedMaxAltitude() != null) {
            upload.setEnhancedMaxAltitude(new BigDecimal(mesg.getEnhancedMaxAltitude().toString()));
        }
        if (mesg.getEnhancedMinAltitude() != null) {
            upload.setEnhancedMinAltitude(new BigDecimal(mesg.getEnhancedMinAltitude().toString()));
        }

        // Calories and work
        if (mesg.getTotalCalories() != null) {
            upload.setTotalCalories(mesg.getTotalCalories().intValue());
        }
        if (mesg.getTotalWork() != null) {
            upload.setTotalWork(mesg.getTotalWork().longValue());
        }

        // Power
        if (mesg.getAvgPower() != null) {
            upload.setAvgPower(new BigDecimal(mesg.getAvgPower().toString()));
        }
        if (mesg.getMaxPower() != null) {
            upload.setMaxPower(mesg.getMaxPower().intValue());
        }
        if (mesg.getNormalizedPower() != null) {
            upload.setNormalizedPower(mesg.getNormalizedPower().intValue());
        }
        if (mesg.getThresholdPower() != null) {
            upload.setThresholdPower(mesg.getThresholdPower().intValue());
        }

        // Training metrics
        if (mesg.getTrainingStressScore() != null) {
            upload.setTrainingStressScore(mesg.getTrainingStressScore().intValue());
        }
        if (mesg.getIntensityFactor() != null) {
            upload.setIntensityFactor(new BigDecimal(mesg.getIntensityFactor().toString()));
        }
        if (mesg.getTotalTrainingEffect() != null) {
            upload.setTotalTrainingEffect(new BigDecimal(mesg.getTotalTrainingEffect().toString()));
        }
        if (mesg.getTotalAnaerobicTrainingEffect() != null) {
            upload.setTotalAnaerobicTrainingEffect(new BigDecimal(mesg.getTotalAnaerobicTrainingEffect().toString()));
        }

        // Running dynamics
        if (mesg.getAvgStanceTime() != null) {
            upload.setAvgStanceTime(new BigDecimal(mesg.getAvgStanceTime().toString()));
        }
        if (mesg.getAvgStanceTimePercent() != null) {
            upload.setAvgStanceTimePercent(new BigDecimal(mesg.getAvgStanceTimePercent().toString()));
        }
        if (mesg.getAvgStanceTimeBalance() != null) {
            upload.setAvgStanceTimeBalance(new BigDecimal(mesg.getAvgStanceTimeBalance().toString()));
        }
        if (mesg.getAvgVerticalOscillation() != null) {
            upload.setAvgVerticalOscillation(new BigDecimal(mesg.getAvgVerticalOscillation().toString()));
        }

        // Temperature
        if (mesg.getAvgTemperature() != null) {
            upload.setAvgTemperature(mesg.getAvgTemperature().intValue());
        }
        if (mesg.getMaxTemperature() != null) {
            upload.setMaxTemperature(mesg.getMaxTemperature().intValue());
        }
        if (mesg.getMinTemperature() != null) {
            upload.setMinTemperature(mesg.getMinTemperature().intValue());
        }

        // Lap information
        if (mesg.getFirstLapIndex() != null) {
            upload.setFirstLapIndex(mesg.getFirstLapIndex().intValue());
        }
        if (mesg.getNumLaps() != null) {
            upload.setNumLaps(mesg.getNumLaps().intValue());
        }
    }

    @Override
    public int getPriority() {
        return 900; // High priority - should be processed early
    }
}
