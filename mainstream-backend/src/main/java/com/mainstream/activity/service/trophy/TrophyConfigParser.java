package com.mainstream.activity.service.trophy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainstream.activity.service.trophy.config.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for parsing JSON trophy configuration strings into typed config objects.
 */
@Component
@Slf4j
public class TrophyConfigParser {

    private final ObjectMapper objectMapper;

    public TrophyConfigParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parse trophy criteria config JSON into a typed config object.
     *
     * @param configJson JSON string containing configuration
     * @param configClass Target config class
     * @return Parsed config object
     * @throws IllegalArgumentException if JSON is invalid
     */
    public <T extends TrophyConfig> T parseConfig(String configJson, Class<T> configClass) {
        if (configJson == null || configJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Trophy config JSON cannot be null or empty");
        }

        try {
            return objectMapper.readValue(configJson, configClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse trophy config JSON: {}", configJson, e);
            throw new IllegalArgumentException("Invalid trophy configuration JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert config object to JSON string.
     *
     * @param config Configuration object
     * @return JSON string
     */
    public String toJson(TrophyConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize trophy config to JSON", e);
            throw new IllegalArgumentException("Failed to serialize trophy config: " + e.getMessage(), e);
        }
    }
}
