package com.safework.api.domain.organization.controller;

import com.safework.api.domain.organization.dto.CreateOrganizationRequest;
import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.OrganizationSummaryDto;
import com.safework.api.domain.organization.dto.UpdateOrganizationRequest;
import com.safework.api.domain.organization.service.OrganizationService;
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
@RequestMapping("/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Creates a new organization. Requires SUPER_ADMIN role (system-level).
     * This endpoint would typically be used during initial system setup or by system administrators.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<OrganizationDto> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        OrganizationDto newOrganization = organizationService.createOrganization(request, currentUser);
        return new ResponseEntity<>(newOrganization, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all organizations.
     * Requires SUPER_ADMIN role for system-wide visibility.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<OrganizationSummaryDto>> getAllOrganizations(Pageable pageable) {
        Page<OrganizationSummaryDto> organizations = organizationService.findAllOrganizations(pageable);
        return ResponseEntity.ok(organizations);
    }

    /**
     * Retrieves the current user's organization details.
     * All authenticated users can access their own organization.
     */
    @GetMapping("/me")
    public ResponseEntity<OrganizationDto> getCurrentUserOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        OrganizationDto organization = organizationService.getCurrentUserOrganization(currentUser);
        return ResponseEntity.ok(organization);
    }

    /**
     * Retrieves a specific organization by ID.
     * Requires SUPER_ADMIN role or being a member of the organization.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or @organizationService.getCurrentUserOrganization(authentication.principal.user).id() == #id")
    public ResponseEntity<OrganizationDto> getOrganizationById(@PathVariable Long id) {
        OrganizationDto organization = organizationService.findOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    /**
     * Updates the current user's organization.
     * Requires ADMIN role within the organization.
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<OrganizationDto> updateCurrentUserOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        Long organizationId = currentUser.getOrganization().getId();
        OrganizationDto updatedOrganization = organizationService.updateOrganization(organizationId, request, currentUser);
        return ResponseEntity.ok(updatedOrganization);
    }

    /**
     * Updates a specific organization by ID.
     * Requires SUPER_ADMIN role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        OrganizationDto updatedOrganization = organizationService.updateOrganization(id, request, currentUser);
        return ResponseEntity.ok(updatedOrganization);
    }

    /**
     * Deletes an organization. Requires SUPER_ADMIN role.
     * This is a sensitive operation that should be rarely used.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        organizationService.deleteOrganization(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}