package com.safework.api.domain.asset.repository;

import com.safework.api.domain.asset.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Asset entities.
 * Extends JpaRepository to provide standard CRUD operations.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Finds an asset by its unique QR code ID.
     * This is a critical method for the mobile app's scanning feature.
     *
     * @param qrCodeId The QR code identifier to search for.
     * @return An Optional containing the found asset, or empty if not found.
     */
    Optional<Asset> findByQrCodeId(String qrCodeId);

    /**
     * Finds all assets belonging to a specific organization, with pagination support.
     * This is essential for multi-tenancy, ensuring users only see their own company's assets.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information (page number, size, and sorting).
     * @return A Page of assets for the given organization.
     */
    Page<Asset> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds all assets at a specific location.
     * Used for location-based asset management and transfers.
     *
     * @param locationId The ID of the location.
     * @return A list of assets at the specified location.
     */
    List<Asset> findByLocationId(Long locationId);
}
