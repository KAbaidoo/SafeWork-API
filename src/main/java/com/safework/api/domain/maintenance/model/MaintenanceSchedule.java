package com.safework.api.domain.maintenance.model;

import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "maintenance_schedules")
public class MaintenanceSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name; // e.g., "Annual Fire Extinguisher Check"

    @Column(nullable = false)
    private int frequencyInterval; // e.g., 3

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyUnit frequencyUnit; // e.g., MONTH
}