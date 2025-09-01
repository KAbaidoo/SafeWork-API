package com.safework.api.domain.asset.model;
import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.issue.model.Issue;
import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "qr_code_id")
    private String qrCodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @Version // Key field for optimistic locking and offline sync conflict detection
    private int version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inspection> inspections;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Issue> issues;
}
