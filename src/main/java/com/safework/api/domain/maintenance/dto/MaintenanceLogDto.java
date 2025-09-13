package com.safework.api.domain.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceLogDto(
        Long id,
        Long assetId,
        String assetName,
        String assetTag,
        Long technicianId,
        String technicianName,
        LocalDate serviceDate,
        String notes,
        BigDecimal cost
) {}