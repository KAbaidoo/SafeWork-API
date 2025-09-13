package com.safework.api.domain.maintenance.dto;

import com.safework.api.domain.maintenance.model.FrequencyUnit;

public record MaintenanceScheduleDto(
        Long id,
        String name,
        Integer frequencyInterval,
        FrequencyUnit frequencyUnit,
        Long organizationId
) {}