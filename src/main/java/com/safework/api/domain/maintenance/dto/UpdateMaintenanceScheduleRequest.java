package com.safework.api.domain.maintenance.dto;

import com.safework.api.domain.maintenance.model.FrequencyUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMaintenanceScheduleRequest(
        @NotBlank(message = "Maintenance schedule name is required")
        @Size(min = 2, max = 100, message = "Schedule name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Frequency interval is required")
        @Min(value = 1, message = "Frequency interval must be at least 1")
        Integer frequencyInterval,

        @NotNull(message = "Frequency unit is required")
        FrequencyUnit frequencyUnit
) {}