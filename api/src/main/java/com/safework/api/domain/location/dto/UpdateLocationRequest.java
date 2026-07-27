package com.safework.api.domain.location.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateLocationRequest(
        @NotBlank(message = "Location name is required")
        @Size(min = 2, max = 100, message = "Location name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Location type is required")
        @Pattern(regexp = "^(WAREHOUSE|OFFICE|YARD|FACTORY_FLOOR|LOADING_DOCK|STORAGE|MAINTENANCE|OTHER)$", 
                 message = "Location type must be one of: WAREHOUSE, OFFICE, YARD, FACTORY_FLOOR, LOADING_DOCK, STORAGE, MAINTENANCE, OTHER")
        String locationType,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @Size(max = 100, message = "Building name must not exceed 100 characters")
        String buildingName,

        @Size(max = 50, message = "Floor must not exceed 50 characters")
        String floor,

        @Size(max = 50, message = "Zone must not exceed 50 characters")
        String zone,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        @Digits(integer = 2, fraction = 8, message = "Latitude must have at most 2 integer digits and 8 decimal places")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @Digits(integer = 3, fraction = 8, message = "Longitude must have at most 3 integer digits and 8 decimal places")
        BigDecimal longitude,

        @Min(value = 1, message = "Max asset capacity must be at least 1")
        @Max(value = 10000, message = "Max asset capacity must not exceed 10000")
        Integer maxAssetCapacity,

        @NotNull(message = "Active status is required")
        Boolean active,

        Long parentLocationId
) {}