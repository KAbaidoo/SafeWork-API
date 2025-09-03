package com.safework.api.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.util.JsonValidator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JSON validation functionality.
 */
public class SimpleJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJsonValidatorWithValidData() {
        // Given
        Map<String, Object> validData = new HashMap<>();
        validData.put("name", "Test");
        validData.put("value", 123);
        validData.put("nested", Map.of("key", "value"));
        
        // When/Then - should not throw
        JsonValidator.validateJson(validData, "testField");
    }

    @Test
    void testJsonValidatorWithNullData() {
        // When/Then - null should be allowed
        JsonValidator.validateJson(null, "testField");
    }

    @Test
    void testJsonValidatorWithComplexData() {
        // Given
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("sections", List.of(
            Map.of(
                "name", "Section 1",
                "items", List.of(
                    Map.of("id", 1, "text", "Item 1"),
                    Map.of("id", 2, "text", "Item 2")
                )
            )
        ));
        
        // When/Then - should not throw
        JsonValidator.validateJson(complexData, "complexField");
    }

    @Test
    void testJsonValidatorWithCyclicReference() {
        // Given - create cyclic reference
        Map<String, Object> cyclicData = new HashMap<>();
        cyclicData.put("self", cyclicData);
        
        // When/Then
        assertThatThrownBy(() -> JsonValidator.validateJson(cyclicData, "cyclicField"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid JSON data");
    }

    @Test
    void testDeepCopyWithSimpleData() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key", "value");
        original.put("number", 42);
        
        // When
        Map<String, Object> copy = JsonValidator.deepCopy(original);
        
        // Then
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
        
        // Verify deep copy by modifying original
        original.put("key", "modified");
        assertThat(copy.get("key")).isEqualTo("value");
    }

    @Test
    void testDeepCopyWithNestedData() {
        // Given
        Map<String, Object> nested = new HashMap<>();
        nested.put("inner", "value");
        
        Map<String, Object> original = new HashMap<>();
        original.put("nested", nested);
        original.put("list", List.of(1, 2, 3));
        
        // When
        Map<String, Object> copy = JsonValidator.deepCopy(original);
        
        // Then
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> copiedNested = (Map<String, Object>) copy.get("nested");
        assertThat(copiedNested).isNotSameAs(nested);
    }

    @Test
    void testDeepCopyWithNull() {
        // When
        Map<String, Object> copy = JsonValidator.deepCopy(null);
        
        // Then
        assertThat(copy).isNull();
    }

    @Test
    void testJsonSerializationCompatibility() throws Exception {
        // Given - complex structure that should work with both H2 and MariaDB
        Map<String, Object> data = new HashMap<>();
        data.put("version", "1.0");
        data.put("timestamp", System.currentTimeMillis());
        data.put("config", Map.of(
            "enabled", true,
            "threshold", 0.95,
            "options", List.of("A", "B", "C")
        ));
        
        // When - serialize to JSON string (as would be stored in H2)
        String jsonString = objectMapper.writeValueAsString(data);
        
        // Then - should be valid JSON that can be deserialized
        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = objectMapper.readValue(jsonString, Map.class);
        assertThat(deserialized.get("version")).isEqualTo("1.0");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) deserialized.get("config");
        assertThat(config.get("enabled")).isEqualTo(true);
        assertThat(config.get("threshold")).isEqualTo(0.95);
    }
}