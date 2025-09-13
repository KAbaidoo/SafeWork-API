package com.safework.api.domain.maintenance.controller;

import com.safework.api.domain.maintenance.dto.CreateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleDto;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleSummaryDto;
import com.safework.api.domain.maintenance.dto.UpdateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.service.MaintenanceScheduleService;
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
@RequestMapping("/v1/maintenance-schedules")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService maintenanceScheduleService;

    /**
     * Creates a new maintenance schedule. Requires ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MaintenanceScheduleDto> createMaintenanceSchedule(
            @Valid @RequestBody CreateMaintenanceScheduleRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceScheduleDto newSchedule = maintenanceScheduleService.createMaintenanceSchedule(request, currentUser);
        return new ResponseEntity<>(newSchedule, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all maintenance schedules for the current user's organization.
     * All authenticated users can view schedules.
     */
    @GetMapping
    public ResponseEntity<Page<MaintenanceScheduleSummaryDto>> getMaintenanceSchedulesByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceScheduleSummaryDto> schedules = maintenanceScheduleService.findAllByOrganization(
                currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Retrieves a single maintenance schedule by its unique ID.
     * All authenticated users can view schedule details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceScheduleDto> getMaintenanceScheduleById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceScheduleDto schedule = maintenanceScheduleService.findMaintenanceScheduleById(id, currentUser);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Search maintenance schedules by name within the current user's organization.
     * All authenticated users can search schedules.
     */
    @GetMapping("/search/name")
    public ResponseEntity<Page<MaintenanceScheduleSummaryDto>> searchMaintenanceSchedulesByName(
            @RequestParam String name,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceScheduleSummaryDto> schedules = maintenanceScheduleService.searchMaintenanceSchedulesByName(name, currentUser, pageable);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Updates an existing maintenance schedule. Requires ADMIN role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MaintenanceScheduleDto> updateMaintenanceSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceScheduleRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceScheduleDto updatedSchedule = maintenanceScheduleService.updateMaintenanceSchedule(id, request, currentUser);
        return ResponseEntity.ok(updatedSchedule);
    }

    /**
     * Deletes a maintenance schedule by its unique ID. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteMaintenanceSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        maintenanceScheduleService.deleteMaintenanceSchedule(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}