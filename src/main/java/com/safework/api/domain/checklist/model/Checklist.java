package com.safework.api.domain.checklist.model;

import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a reusable inspection template or checklist.
 * Administrators design these templates, which are then used to conduct inspections.
 */
@Data
@Entity
@Table(name = "checklists")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChecklistStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, name = "template_data")
    private Map<String, Object> templateData;

    @Version
    private int version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---

    @OneToMany(mappedBy = "checklist", fetch = FetchType.LAZY)
    private List<Inspection> inspections;
}