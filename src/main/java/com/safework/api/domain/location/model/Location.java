package com.safework.api.domain.location.model;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"organization", "parentLocation", "childLocations", "assets"})
@Entity
@Table(name = "locations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "name"})
})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name; // e.g., "Warehouse A - Section 1"

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

    // --- Address Information ---
    @Column(length = 500)
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    // --- Facility Details ---
    @Column
    private String buildingName;

    @Column
    private String floor;

    @Column
    private String zone; // Section, Bay, Aisle, etc.

    // --- GPS Coordinates ---
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // --- Capacity Management ---
    @Column
    private Integer maxAssetCapacity;

    @Column
    private Integer currentAssetCount; // Cached count for performance

    // --- Status ---
    @Column(nullable = false)
    private Boolean active = true;

    // --- Hierarchy Support ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_location_id")
    private Location parentLocation;

    @OneToMany(mappedBy = "parentLocation", fetch = FetchType.LAZY)
    private List<Location> childLocations;

    // --- Audit Fields ---
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    // --- Relationships ---
    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<Asset> assets;
}
