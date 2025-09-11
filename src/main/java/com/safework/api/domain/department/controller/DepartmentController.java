package com.safework.api.domain.department.controller;

import com.safework.api.domain.department.dto.CreateDepartmentRequest;
import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.dto.UpdateDepartmentRequest;
import com.safework.api.domain.department.service.DepartmentService;
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
@RequestMapping("/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Creates a new department. Requires ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        DepartmentDto newDepartment = departmentService.createDepartment(request, currentUser);
        return new ResponseEntity<>(newDepartment, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all departments for the current user's organization.
     * All authenticated users can view departments.
     */
    @GetMapping
    public ResponseEntity<Page<DepartmentSummaryDto>> getDepartmentsByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<DepartmentSummaryDto> departments = departmentService.findAllByOrganization(
                currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(departments);
    }

    /**
     * Retrieves a single department by its unique ID.
     * All authenticated users can view department details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        DepartmentDto department = departmentService.findDepartmentById(id, currentUser);
        return ResponseEntity.ok(department);
    }

    /**
     * Updates an existing department. Requires ADMIN or SUPERVISOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        DepartmentDto updatedDepartment = departmentService.updateDepartment(id, request, currentUser);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Deletes a department. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        departmentService.deleteDepartment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}