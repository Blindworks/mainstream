package com.mainstream.fitfile.processor;

import com.garmin.fit.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for message processors
 * Provides common utility methods for field extraction and conversion
 */
@Slf4j
public abstract class AbstractMessageProcessor implements MessageProcessor {

    /**
     * Extract all fields from a FIT message into a Map
     * This provides a generic way to capture all data
     */
    protected Map<String, Object> extractAllFields(Mesg mesg) {
        Map<String, Object> fields = new HashMap<>();

        if (mesg == null) {
            return fields;
        }

        // Iterate through all fields in the message
        for (Field field : mesg.getFields()) {
            try {
                String fieldName = field.getName();
                Object value = extractFieldValue(field);

                if (value != null) {
                    fields.put(fieldName, value);
                }
            } catch (Exception e) {
                log.warn("Error extracting field {}: {}", field.getName(), e.getMessage());
            }
        }

        return fields;
    }

    /**
     * Extract developer fields from a message
     */
    protected Map<String, Object> extractDeveloperFields(Mesg mesg) {
        Map<String, Object> developerFields = new HashMap<>();

        if (mesg == null) {
            return developerFields;
        }

        try {
            for (DeveloperField devField : mesg.getDeveloperFields()) {
                String fieldName = devField.getName() != null ?
                    devField.getName() : "dev_field_" + devField.getNum();
                Object value = extractDeveloperFieldValue(devField);

                if (value != null) {
                    developerFields.put(fieldName, value);
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting developer fields: {}", e.getMessage());
        }

        return developerFields;
    }

    /**
     * Extract value from a FIT field with proper type conversion
     */
    protected Object extractFieldValue(Field field) {
        if (field == null || field.getValue() == null) {
            return null;
        }

        Object value = field.getValue();

        // Handle DateTime specially
        if (value instanceof DateTime) {
            return convertToLocalDateTime((DateTime) value);
        }

        // Handle numeric types
        if (value instanceof Number) {
            return convertNumber((Number) value, field);
        }

        // Handle enums
        if (value instanceof Enum) {
            return value.toString();
        }

        // Handle arrays
        if (value.getClass().isArray()) {
            return convertArray(value);
        }

        // Return as-is for strings and other types
        return value;
    }

    /**
     * Extract value from a developer field
     */
    protected Object extractDeveloperFieldValue(DeveloperField field) {
        if (field == null) {
            return null;
        }

        try {
            // Get the value count
            int numValues = field.getNumValues();
            if (numValues == 0) {
                return null;
            }

            // Single value
            if (numValues == 1) {
                return field.getValue(0);
            }

            // Multiple values - return as array
            Object[] values = new Object[numValues];
            for (int i = 0; i < numValues; i++) {
                values[i] = field.getValue(i);
            }
            return values;
        } catch (Exception e) {
            log.warn("Error extracting developer field value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert a Number to appropriate type based on field properties
     */
    protected Object convertNumber(Number number, Field field) {
        // For fields that represent coordinates in semicircles
        if (field.getName().contains("position_lat") || field.getName().contains("position_long")) {
            return convertSemicirclesToDegrees(number.intValue());
        }

        // For fields with scale/offset
        if (field.getScale() != null && field.getScale() != 1.0f) {
            double scaled = number.doubleValue() / field.getScale();
            if (field.getOffset() != null && field.getOffset() != 0.0f) {
                scaled -= field.getOffset();
            }
            return BigDecimal.valueOf(scaled).setScale(6, RoundingMode.HALF_UP);
        }

        // Return as-is
        return number;
    }

    /**
     * Convert array to a more JSON-friendly format
     */
    protected Object convertArray(Object array) {
        // For now, convert to string representation
        // Could be enhanced to handle specific array types better
        return array.toString();
    }

    /**
     * Convert FIT DateTime to LocalDateTime
     */
    protected LocalDateTime convertToLocalDateTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        try {
            // FIT epoch is 1989-12-31T00:00:00Z (631065600 seconds from Unix epoch)
            return LocalDateTime.ofEpochSecond(
                dateTime.getTimestamp() + 631065600L,
                0,
                ZoneOffset.UTC
            );
        } catch (Exception e) {
            log.warn("Error converting DateTime: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert semicircles to degrees
     * FIT stores coordinates in semicircles (2^31 semicircles = 180 degrees)
     */
    protected BigDecimal convertSemicirclesToDegrees(Integer semicircles) {
        if (semicircles == null) {
            return null;
        }

        double degrees = semicircles * (180.0 / Math.pow(2, 31));
        return BigDecimal.valueOf(degrees).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * Convert degrees to semicircles (inverse operation)
     */
    protected Integer convertDegreesToSemicircles(BigDecimal degrees) {
        if (degrees == null) {
            return null;
        }

        return (int) (degrees.doubleValue() * (Math.pow(2, 31) / 180.0));
    }

    /**
     * Get a field value as String with null safety
     */
    protected String getFieldAsString(Map<String, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * Get a field value as Integer with null safety
     */
    protected Integer getFieldAsInteger(Map<String, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Get a field value as BigDecimal with null safety
     */
    protected BigDecimal getFieldAsBigDecimal(Map<String, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }

    /**
     * Get a field value as LocalDateTime with null safety
     */
    protected LocalDateTime getFieldAsDateTime(Map<String, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }

    @Override
    public boolean canProcess(Mesg mesg) {
        if (mesg == null) {
            return false;
        }

        String msgName = mesg.getName();
        Integer msgNum = mesg.getNum();

        // Check by name
        if (getMessageType() != null && getMessageType().equals(msgName)) {
            return true;
        }

        // Check by global message number
        if (getGlobalMessageNumber() != null && getGlobalMessageNumber().equals(msgNum)) {
            return true;
        }

        return false;
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return null; // Override in subclasses if needed
    }
}
