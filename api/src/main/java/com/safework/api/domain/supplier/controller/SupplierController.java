package com.safework.api.domain.supplier.controller;

import com.safework.api.domain.supplier.dto.CreateSupplierRequest;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.dto.UpdateSupplierRequest;
import com.safework.api.domain.supplier.service.SupplierService;
import com.safework.api.domain.user.model.User;
import com.safework.api.security.PrincipalUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Creates a new supplier. Requires ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SupplierDto> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        SupplierDto newSupplier = supplierService.createSupplier(request, currentUser);
        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all suppliers for the current user's organization.
     * All authenticated users can view suppliers.
     */
    @GetMapping
    public ResponseEntity<Page<SupplierSummaryDto>> getSuppliersByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<SupplierSummaryDto> suppliers = supplierService.findAllByOrganization(
                currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Retrieves a single supplier by its unique ID.
     * All authenticated users can view supplier details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        SupplierDto supplier = supplierService.findSupplierById(id, currentUser);
        return ResponseEntity.ok(supplier);
    }

    /**
     * Search suppliers by name within the current user's organization.
     * All authenticated users can search suppliers.
     */
    @GetMapping("/search/name")
    public ResponseEntity<Page<SupplierSummaryDto>> searchSuppliersByName(
            @RequestParam String name,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<SupplierSummaryDto> suppliers = supplierService.searchSuppliersByName(name, currentUser, pageable);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Search suppliers by email within the current user's organization.
     * All authenticated users can search suppliers.
     */
    @GetMapping("/search/email")
    public ResponseEntity<Page<SupplierSummaryDto>> searchSuppliersByEmail(
            @RequestParam String email,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<SupplierSummaryDto> suppliers = supplierService.searchSuppliersByEmail(email, currentUser, pageable);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Updates an existing supplier. Requires ADMIN or SUPERVISOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        SupplierDto updatedSupplier = supplierService.updateSupplier(id, request, currentUser);
        return ResponseEntity.ok(updatedSupplier);
    }

    /**
     * Deletes a supplier. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        supplierService.deleteSupplier(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}