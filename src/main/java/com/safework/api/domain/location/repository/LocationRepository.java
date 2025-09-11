package com.safework.api.domain.location.repository;

import com.safework.api.domain.location.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Finds all locations belonging to a specific organization, with pagination.
     */
    Page<Location> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds all active locations belonging to a specific organization.
     */
    Page<Location> findAllByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable);

    /**
     * Finds a location by name within a specific organization.
     */
    Optional<Location> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Checks if a location with the given name exists within a specific organization.
     */
    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Finds all child locations of a parent location.
     */
    List<Location> findByParentLocationId(Long parentLocationId);

    /**
     * Finds all child locations of a parent location with pagination.
     */
    Page<Location> findByParentLocationId(Long parentLocationId, Pageable pageable);

    /**
     * Finds all root locations (locations without a parent) for an organization.
     */
    Page<Location> findByOrganizationIdAndParentLocationIsNull(Long organizationId, Pageable pageable);

    /**
     * Counts assets at a specific location.
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.location.id = :locationId")
    long countAssetsByLocationId(@Param("locationId") Long locationId);

    /**
     * Finds locations that have available capacity (current count < max capacity).
     */
    @Query("SELECT l FROM Location l WHERE l.organization.id = :organizationId " +
           "AND l.active = true " +
           "AND l.maxAssetCapacity IS NOT NULL " +
           "AND l.currentAssetCount < l.maxAssetCapacity")
    Page<Location> findAvailableLocationsByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);

    /**
     * Finds locations by type within an organization.
     */
    @Query("SELECT l FROM Location l WHERE l.organization.id = :organizationId AND l.locationType = :locationType")
    Page<Location> findByOrganizationIdAndLocationType(@Param("organizationId") Long organizationId, 
                                                       @Param("locationType") String locationType, 
                                                       Pageable pageable);
}