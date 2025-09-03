package com.safework.api.domain.asset.model;

import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "asset_types")
public class AssetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name; // e.g., "Laptop", "Vehicle", "Policy Document"

    @OneToMany(mappedBy = "assetType", fetch = FetchType.LAZY)
    private List<Asset> assets;
}
