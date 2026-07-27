package com.safework.api.domain.inspection.model;

/**
 * Represents the final state of a submitted inspection.
 */
public enum InspectionStatus {
    /**
     * The inspection was completed and all checklist items passed.
     */
    PASSED,

    /**
     * The inspection was completed, but one or more items failed,
     * resulting in the creation of Issues.
     */
    COMPLETED_WITH_ISSUES
}
