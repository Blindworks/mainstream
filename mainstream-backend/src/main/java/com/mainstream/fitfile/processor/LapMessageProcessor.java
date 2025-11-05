package com.mainstream.fitfile.processor;

import com.garmin.fit.LapMesg;
import com.garmin.fit.Mesg;
import com.mainstream.fitfile.entity.FitFileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Processor for FIT lap messages
 * Contains summary data for each lap in an activity
 */
@Slf4j
@Component
public class LapMessageProcessor extends AbstractMessageProcessor {

    @Override
    public String getMessageType() {
        return "lap";
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return 19; // lap is message number 19
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber) {
        if (!(mesg instanceof LapMesg)) {
            return ProcessingResult.failure("Message is not a LapMesg");
        }

        try {
            LapMesg lapMesg = (LapMesg) mesg;
            Map<String, Object> fields = extractAllFields(mesg);

            // Store position coordinates with special handling
            if (lapMesg.getStartPositionLat() != null && lapMesg.getStartPositionLong() != null) {
                fields.put("start_position_lat", convertSemicirclesToDegrees(lapMesg.getStartPositionLat()));
                fields.put("start_position_long", convertSemicirclesToDegrees(lapMesg.getStartPositionLong()));
            }
            if (lapMesg.getEndPositionLat() != null && lapMesg.getEndPositionLong() != null) {
                fields.put("end_position_lat", convertSemicirclesToDegrees(lapMesg.getEndPositionLat()));
                fields.put("end_position_long", convertSemicirclesToDegrees(lapMesg.getEndPositionLong()));
            }

            log.debug("Processed lap: index={}, distance={}, time={}",
                fields.get("message_index"),
                fields.get("total_distance"),
                fields.get("total_timer_time"));

            return ProcessingResult.success(fields, extractDeveloperFields(mesg));

        } catch (Exception e) {
            log.error("Error processing lap message: {}", e.getMessage(), e);
            return ProcessingResult.partial(extractAllFields(mesg), e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 500; // Medium-high priority
    }
}
