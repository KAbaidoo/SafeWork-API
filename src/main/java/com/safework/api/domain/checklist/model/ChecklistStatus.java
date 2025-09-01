package com.safework.api.domain.checklist.model;

/**
 * Represents the availability of a checklist template for new inspections.
 */
public enum ChecklistStatus {
    /**
     * The checklist is active and can be used for new inspections.
     */
    ACTIVE,

    /**
     * The checklist is archived and cannot be used for new inspections,
     * but is kept for historical data integrity.
     */
    ARCHIVED
}
