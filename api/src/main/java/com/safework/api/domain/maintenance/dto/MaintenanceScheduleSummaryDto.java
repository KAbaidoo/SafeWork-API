package com.safework.api.domain.maintenance.dto;

import com.safework.api.domain.maintenance.model.FrequencyUnit;

public record MaintenanceScheduleSummaryDto(
        Long id,
        String name,
        Integer frequencyInterval,
        FrequencyUnit frequencyUnit
) {}