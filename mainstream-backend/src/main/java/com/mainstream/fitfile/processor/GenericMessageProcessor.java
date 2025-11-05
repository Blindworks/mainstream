package com.mainstream.fitfile.processor;

import com.garmin.fit.Mesg;
import com.mainstream.fitfile.entity.FitFileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Generic processor that can handle ANY FIT message type
 * Used as a fallback when no specific processor is available
 * Ensures zero data loss by capturing all fields generically
 */
@Slf4j
@Component
public class GenericMessageProcessor extends AbstractMessageProcessor {

    @Override
    public String getMessageType() {
        return "generic"; // Special identifier for generic processor
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber) {
        if (mesg == null) {
            return ProcessingResult.failure("Null message provided");
        }

        try {
            log.debug("Processing generic message: {} (num: {})", mesg.getName(), mesg.getNum());

            // Extract all standard fields
            Map<String, Object> fields = extractAllFields(mesg);

            // Extract developer fields
            Map<String, Object> developerFields = extractDeveloperFields(mesg);

            // Add metadata about the message
            fields.put("_message_name", mesg.getName());
            fields.put("_message_num", mesg.getNum());
            fields.put("_field_count", mesg.getFields().size());

            // Count developer fields (Iterable doesn't have size())
            int devFieldCount = 0;
            for (@SuppressWarnings("unused") var field : mesg.getDeveloperFields()) {
                devFieldCount++;
            }
            fields.put("_developer_field_count", devFieldCount);

            String notes = String.format("Generic processing of message type '%s' (num: %d) with %d fields",
                mesg.getName(), mesg.getNum(), fields.size());

            log.debug("Successfully processed generic message: {} with {} fields",
                mesg.getName(), fields.size());

            return new ProcessingResult(
                true,
                fields,
                developerFields.isEmpty() ? null : developerFields,
                notes,
                true // Considered fully parsed since we captured everything available
            );

        } catch (Exception e) {
            log.error("Error processing generic message {}: {}", mesg.getName(), e.getMessage(), e);
            return ProcessingResult.failure("Failed to process: " + e.getMessage());
        }
    }

    @Override
    public boolean canProcess(Mesg mesg) {
        // Generic processor can process ANY message
        return mesg != null;
    }

    @Override
    public int getPriority() {
        // Lowest priority - only used as fallback
        return Integer.MIN_VALUE;
    }
}
