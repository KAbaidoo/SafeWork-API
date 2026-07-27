package com.safework.api.domain.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceLogSummaryDto(
        Long id,
        Long assetId,
        String assetName,
        String assetTag,
        LocalDate serviceDate,
        BigDecimal cost
) {}