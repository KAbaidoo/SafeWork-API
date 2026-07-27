package com.safework.api.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.Map;

/**
 * Utility class for JSON validation in entity lifecycle hooks.
 * Ensures JSON data is valid before persisting to database.
 */
public class JsonValidator {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Validates that a Map can be serialized to valid JSON.
     * @param jsonData the Map to validate
     * @param fieldName name of the field for error messages
     * @throws IllegalArgumentException if the data cannot be serialized to JSON
     */
    public static void validateJson(Map<String, Object> jsonData, String fieldName) {
        if (jsonData == null) {
            return; // Null is allowed for optional fields
        }
        
        try {
            // Attempt to serialize to JSON to validate structure
            objectMapper.writeValueAsString(jsonData);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                String.format("Invalid JSON data in field '%s': %s", fieldName, e.getMessage()), e
            );
        }
    }
    
    /**
     * Safely clones a JSON Map to prevent external modification.
     * @param source the Map to clone
     * @return a deep copy of the Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepCopy(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        
        try {
            String json = objectMapper.writeValueAsString(source);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to copy JSON data", e);
        }
    }
}