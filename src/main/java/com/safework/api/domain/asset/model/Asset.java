package com.safework.api.domain.asset.model;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.issue.model.Issue;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.maintenance.model.MaintenanceLog;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.model.Supplier;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.util.JsonValidator;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a physical asset that can be inspected and managed.
 * This is the core entity for tracking equipment like forklifts, machinery, etc.
 */

@Data
@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organizationId", "qrCodeId"})
})
public class Asset {
    // --- Core Identification ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assetTag; // Company-wide unique identifier

    @Column(nullable = false)
    private String name; // Human-readable name

    @Column(name = "qr_code_id", unique = true)
    private String qrCodeId; // Scannable ID for SafeWork mobile app [cite: 281]

    // --- Categorization & Ownership ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // --- Assignment & Location ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    // --- Status & Compliance ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status; // Operational status [cite: 281]

    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus;

    // --- Financial Information ---
    @Column
    private LocalDate purchaseDate;

    @Column
    private BigDecimal purchaseCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // --- Maintenance & Lifecycle ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_schedule_id")
    private MaintenanceSchedule maintenanceSchedule;

    @Column
    private LocalDate nextServiceDate;

    @Column
    private LocalDate warrantyExpiryDate;

    @Column
    private LocalDate disposalDate;

    // --- Custom & Sync Fields ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> customAttributes;

    @Version
    private int version; // For offline synchronization

    // --- Auditing ---
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- Historical Relationships ---
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inspection> inspections;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Issue> issues;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceLog> maintenanceLogs;
    
    // --- Lifecycle Hooks ---
    
    @PrePersist
    @PreUpdate
    private void validateJsonData() {
        JsonValidator.validateJson(this.customAttributes, "customAttributes");
    }
    
    // --- Setters with validation ---
    
    public void setCustomAttributes(Map<String, Object> customAttributes) {
        JsonValidator.validateJson(customAttributes, "customAttributes");
        this.customAttributes = customAttributes;
    }
}