package com.safework.api.domain.asset.controller;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating PostgreSQL JSONB query capabilities for assets.
 * Provides examples of advanced JSON querying features.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets/search")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'USER')")
public class AssetJsonbController {

    private final AssetRepository assetRepository;

    /**
     * Search assets by JSON key existence.
     * Example: GET /api/v1/assets/search/by-key?key=manufacturer
     */
    @GetMapping("/by-key")
    public ResponseEntity<List<Asset>> searchByJsonKey(
            @RequestParam String key,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByCustomAttributesContainsKey(
            currentUser.getOrganization().getId(), key);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Search assets by specific JSON value.
     * Example: GET /api/v1/assets/search/by-value?path=manufacturer&value=CAT
     */
    @GetMapping("/by-value")
    public ResponseEntity<List<Asset>> searchByJsonValue(
            @RequestParam String path,
            @RequestParam String value,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByCustomAttributesJsonValue(
            currentUser.getOrganization().getId(), path, value);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Search assets by numeric range in JSON fields.
     * Example: GET /api/v1/assets/search/by-numeric-range?path=specs.power&min=100&max=200
     */
    @GetMapping("/by-numeric-range")
    public ResponseEntity<List<Asset>> searchByNumericRange(
            @RequestParam String path,
            @RequestParam Double min,
            @RequestParam Double max,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByCustomAttributesNumericRange(
            currentUser.getOrganization().getId(), path, min, max);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Search assets by JSON containment.
     * Example: POST /api/v1/assets/search/by-contains
     * Body: {"manufacturer": "CAT", "year": 2022}
     */
    @PostMapping("/by-contains")
    public ResponseEntity<List<Asset>> searchByJsonContains(
            @RequestBody String jsonPath,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByCustomAttributesContains(
            currentUser.getOrganization().getId(), jsonPath);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Full-text search within JSON attributes.
     * Example: GET /api/v1/assets/search/full-text?term=excavator
     */
    @GetMapping("/full-text")
    public ResponseEntity<List<Asset>> fullTextSearch(
            @RequestParam String term,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.searchCustomAttributesFullText(
            currentUser.getOrganization().getId(), term);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Search assets with multiple required JSON keys.
     * Example: GET /api/v1/assets/search/by-multiple-keys?keys=manufacturer,model,year
     */
    @GetMapping("/by-multiple-keys")
    public ResponseEntity<List<Asset>> searchByMultipleKeys(
            @RequestParam List<String> keys,
            @AuthenticationPrincipal User currentUser) {
        
        String[] keyArray = keys.toArray(new String[0]);
        List<Asset> assets = assetRepository.findByCustomAttributesContainsAllKeys(
            currentUser.getOrganization().getId(), keyArray);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Search assets by JSON array contains value.
     * Example: GET /api/v1/assets/search/by-array-contains?arrayPath=certifications&value=ISO14001
     */
    @GetMapping("/by-array-contains")
    public ResponseEntity<List<Asset>> searchByJsonArrayContains(
            @RequestParam String arrayPath,
            @RequestParam String value,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByCustomAttributesArrayContains(
            currentUser.getOrganization().getId(), arrayPath, value);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Complex JSON criteria search with pagination.
     * Example: POST /api/v1/assets/search/complex?page=0&size=10
     * Body: {"manufacturer": "CAT", "specs.power": "122"}
     */
    @PostMapping("/complex")
    public ResponseEntity<Page<Asset>> complexJsonSearch(
            @RequestBody Map<String, Object> criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Asset> assets = assetRepository.findByComplexJsonCriteria(
            currentUser.getOrganization().getId(), criteria, pageable);
        
        return ResponseEntity.ok(assets);
    }

    /**
     * Get aggregated data by JSON attribute.
     * Example: GET /api/v1/assets/search/aggregate?attribute=manufacturer
     */
    @GetMapping("/aggregate")
    public ResponseEntity<Map<String, Long>> aggregateByAttribute(
            @RequestParam String attribute,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Long> aggregation = assetRepository.aggregateByJsonAttribute(
            currentUser.getOrganization().getId(), attribute);
        
        return ResponseEntity.ok(aggregation);
    }

    /**
     * Get statistics for numeric JSON field.
     * Example: GET /api/v1/assets/search/statistics?field=specs.power
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Double>> getFieldStatistics(
            @RequestParam String field,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Double> statistics = assetRepository.calculateJsonFieldStatistics(
            currentUser.getOrganization().getId(), field);
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Update JSON attribute for an asset.
     * Example: PUT /api/v1/assets/search/update-attribute/123?path=specs.power&value=130
     */
    @PutMapping("/update-attribute/{assetId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> updateJsonAttribute(
            @PathVariable Long assetId,
            @RequestParam String path,
            @RequestParam String value,
            @AuthenticationPrincipal User currentUser) {
        
        // Verify asset belongs to user's organization
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new RuntimeException("Asset not found"));
        
        if (!asset.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            return ResponseEntity.status(403).build();
        }
        
        int updatedRows = assetRepository.updateJsonAttribute(assetId, path, value);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", updatedRows > 0);
        response.put("updatedRows", updatedRows);
        response.put("message", updatedRows > 0 ? "Attribute updated successfully" : "No changes made");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search assets by regex pattern in JSON values.
     * Example: GET /api/v1/assets/search/by-regex?path=model&pattern=^3[0-9]{2}E$
     */
    @GetMapping("/by-regex")
    public ResponseEntity<List<Asset>> searchByRegex(
            @RequestParam String path,
            @RequestParam String pattern,
            @AuthenticationPrincipal User currentUser) {
        
        List<Asset> assets = assetRepository.findByJsonValueRegex(
            currentUser.getOrganization().getId(), path, pattern);
        
        return ResponseEntity.ok(assets);
    }
}