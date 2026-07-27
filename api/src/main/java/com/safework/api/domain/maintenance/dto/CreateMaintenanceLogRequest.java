package com.safework.api.domain.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMaintenanceLogRequest(
        @NotNull(message = "Asset ID is required")
        Long assetId,

        Long technicianId,

        @NotNull(message = "Service date is required")
        LocalDate serviceDate,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be non-negative")
        BigDecimal cost
) {}