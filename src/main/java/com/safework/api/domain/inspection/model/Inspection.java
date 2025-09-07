package com.safework.api.domain.inspection.model;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.checklist.model.Checklist;
import com.safework.api.domain.issue.model.Issue;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.util.JsonValidator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a completed inspection report for a specific asset.
 * This is an immutable record of a check performed at a point in time.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"asset", "user", "checklist", "issues"})
@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false, name = "report_data")
    private Map<String, Object> reportData;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime completedAt;

    // --- Relationships ---

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Issue> issues;
    
    // --- Lifecycle Hooks ---
    
    @PrePersist
    @PreUpdate
    private void validateJsonData() {
        JsonValidator.validateJson(this.reportData, "reportData");
    }
    
    // --- Setters with validation ---
    
    public void setReportData(Map<String, Object> reportData) {
        JsonValidator.validateJson(reportData, "reportData");
        this.reportData = reportData;
    }
}
