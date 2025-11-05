package com.mainstream.fitfile.processor;

import com.garmin.fit.Mesg;
import com.garmin.fit.RecordMesg;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitTrackPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor for FIT record messages (track points)
 * Contains time-series data points recorded during activity
 */
@Slf4j
@Component
public class RecordMessageProcessor extends AbstractMessageProcessor {

    private static final ThreadLocal<Integer> RECORD_COUNT = ThreadLocal.withInitial(() -> 0);

    @Override
    public String getMessageType() {
        return "record";
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return 20; // record is message number 20
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber) {
        if (!(mesg instanceof RecordMesg)) {
            return ProcessingResult.failure("Message is not a RecordMesg");
        }

        try {
            RecordMesg recordMesg = (RecordMesg) mesg;
            Map<String, Object> fields = extractAllFields(mesg);

            // Log only first few records to avoid log spam
            int count = RECORD_COUNT.get();
            if (count < 5) {
                log.debug("Processed record #{}: timestamp={}, lat={}, lon={}, hr={}",
                    sequenceNumber,
                    fields.get("timestamp"),
                    fields.get("position_lat"),
                    fields.get("position_long"),
                    fields.get("heart_rate"));
                RECORD_COUNT.set(count + 1);
            }

            // Store coordinates with special handling
            if (recordMesg.getPositionLat() != null && recordMesg.getPositionLong() != null) {
                fields.put("position_lat", convertSemicirclesToDegrees(recordMesg.getPositionLat()));
                fields.put("position_long", convertSemicirclesToDegrees(recordMesg.getPositionLong()));
            }

            return ProcessingResult.success(fields, extractDeveloperFields(mesg));

        } catch (Exception e) {
            log.warn("Error processing record message at seq {}: {}", sequenceNumber, e.getMessage());
            return ProcessingResult.partial(extractAllFields(mesg), e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 100; // Medium priority
    }

    /**
     * Reset record count for new file
     */
    public static void resetRecordCount() {
        RECORD_COUNT.set(0);
    }
}
