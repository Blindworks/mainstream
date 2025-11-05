package com.mainstream.fitfile.processor;

import com.garmin.fit.Mesg;
import com.mainstream.fitfile.entity.FitFileUpload;

import java.util.Map;

/**
 * Interface for processing FIT messages
 * Each message type should have its own processor implementation
 */
public interface MessageProcessor {

    /**
     * Get the name of the message type this processor handles
     * (e.g., "file_id", "session", "record", "device_info")
     */
    String getMessageType();

    /**
     * Get the FIT global message number this processor handles
     * Returns null if processor handles multiple message types
     */
    Integer getGlobalMessageNumber();

    /**
     * Process a FIT message
     * @param mesg The FIT message to process
     * @param fitFileUpload The upload entity this message belongs to
     * @param sequenceNumber The sequence number of this message in the file
     * @return ProcessingResult containing the processed data
     */
    ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber);

    /**
     * Check if this processor can handle the given message
     */
    boolean canProcess(Mesg mesg);

    /**
     * Get processor priority (higher = processed first)
     * Useful for messages that need to be processed in order
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Result of message processing
     */
    class ProcessingResult {
        private final boolean success;
        private final Map<String, Object> data;
        private final Map<String, Object> developerFields;
        private final String notes;
        private final boolean fullyParsed;

        public ProcessingResult(boolean success, Map<String, Object> data,
                                Map<String, Object> developerFields,
                                String notes, boolean fullyParsed) {
            this.success = success;
            this.data = data;
            this.developerFields = developerFields;
            this.notes = notes;
            this.fullyParsed = fullyParsed;
        }

        public static ProcessingResult success(Map<String, Object> data) {
            return new ProcessingResult(true, data, null, null, true);
        }

        public static ProcessingResult success(Map<String, Object> data, Map<String, Object> developerFields) {
            return new ProcessingResult(true, data, developerFields, null, true);
        }

        public static ProcessingResult partial(Map<String, Object> data, String notes) {
            return new ProcessingResult(true, data, null, notes, false);
        }

        public static ProcessingResult failure(String notes) {
            return new ProcessingResult(false, null, null, notes, false);
        }

        public boolean isSuccess() {
            return success;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public Map<String, Object> getDeveloperFields() {
            return developerFields;
        }

        public String getNotes() {
            return notes;
        }

        public boolean isFullyParsed() {
            return fullyParsed;
        }
    }
}
