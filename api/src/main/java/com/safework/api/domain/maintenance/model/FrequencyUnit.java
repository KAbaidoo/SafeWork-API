package com.safework.api.domain.maintenance.model;

/**
 * Defines the units for maintenance schedule frequencies.
 */
public enum FrequencyUnit {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    HOUR, // For usage-based maintenance (e.g., machinery)
    MILE  // For usage-based maintenance (e.g., vehicles)
}
