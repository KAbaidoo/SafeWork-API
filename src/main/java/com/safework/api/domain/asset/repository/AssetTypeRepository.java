package com.safework.api.domain.asset.repository;

import org.springframework.stereotype.Repository;
import com.safework.api.domain.asset.model.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing AssetType entities.
 * Extends JpaRepository to provide standard CRUD operations.
 */
@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {

    /**
     * Finds all asset types belonging to a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @return A list of asset types.
     */
    List<AssetType> findAllByOrganizationId(Long organizationId);

    /**
     * Finds an asset type by its name within a specific organization to prevent duplicates.
     *
     * @param name           The name of the asset type.
     * @param organizationId The ID of the organization.
     * @return An Optional containing the found asset type, or empty if not found.
     */
    Optional<AssetType> findByNameAndOrganizationId(String name, Long organizationId);
}
