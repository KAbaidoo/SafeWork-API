package com.safework.api.domain.user.model;

/**
 * Defines user roles and permissions within an organization.
 */
public enum UserRole {
    ADMIN,      // Manages users, billing, and organization settings
    SUPERVISOR, // Manages assets, schedules, and issues
    INSPECTOR   // Performs inspections and reports issues
}
