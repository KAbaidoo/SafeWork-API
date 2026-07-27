package com.safework.api.domain.maintenance.service;

import com.safework.api.domain.maintenance.dto.CreateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleDto;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleSummaryDto;
import com.safework.api.domain.maintenance.dto.UpdateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.mapper.MaintenanceScheduleMapper;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.maintenance.repository.MaintenanceScheduleRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceScheduleMapper maintenanceScheduleMapper;

    public MaintenanceScheduleDto createMaintenanceSchedule(CreateMaintenanceScheduleRequest request, User currentUser) {
        // Check if schedule name already exists within the organization
        if (maintenanceScheduleRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Maintenance schedule with name '" + request.name() + "' already exists in this organization");
        }

        MaintenanceSchedule newSchedule = new MaintenanceSchedule();
        newSchedule.setOrganization(currentUser.getOrganization());
        newSchedule.setName(request.name());
        newSchedule.setFrequencyInterval(request.frequencyInterval());
        newSchedule.setFrequencyUnit(request.frequencyUnit());

        MaintenanceSchedule savedSchedule = maintenanceScheduleRepository.save(newSchedule);
        return maintenanceScheduleMapper.toDto(savedSchedule);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceScheduleSummaryDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findAllByOrganizationId(organizationId, pageable);
        return schedules.map(maintenanceScheduleMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public MaintenanceScheduleDto findMaintenanceScheduleById(Long id, User currentUser) {
        MaintenanceSchedule schedule = getMaintenanceScheduleForUser(id, currentUser);
        return maintenanceScheduleMapper.toDto(schedule);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceScheduleSummaryDto> searchMaintenanceSchedulesByName(String name, User currentUser, Pageable pageable) {
        Page<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                currentUser.getOrganization().getId(), name, pageable);
        return schedules.map(maintenanceScheduleMapper::toSummaryDto);
    }

    public MaintenanceScheduleDto updateMaintenanceSchedule(Long id, UpdateMaintenanceScheduleRequest request, User currentUser) {
        MaintenanceSchedule scheduleToUpdate = getMaintenanceScheduleForUser(id, currentUser);

        // Check if name is being changed and if it already exists
        if (!scheduleToUpdate.getName().equals(request.name()) && 
            maintenanceScheduleRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Maintenance schedule with name '" + request.name() + "' already exists in this organization");
        }

        scheduleToUpdate.setName(request.name());
        scheduleToUpdate.setFrequencyInterval(request.frequencyInterval());
        scheduleToUpdate.setFrequencyUnit(request.frequencyUnit());

        MaintenanceSchedule savedSchedule = maintenanceScheduleRepository.save(scheduleToUpdate);
        return maintenanceScheduleMapper.toDto(savedSchedule);
    }

    public void deleteMaintenanceSchedule(Long id, User currentUser) {
        MaintenanceSchedule scheduleToDelete = getMaintenanceScheduleForUser(id, currentUser);
        
        // TODO: Add check if schedule has associated assets before deletion
        // This will be implemented when Asset relationships are fully integrated
        
        maintenanceScheduleRepository.delete(scheduleToDelete);
    }

    /**
     * Helper method to retrieve a maintenance schedule and validate user access.
     * Ensures the user can only access schedules within their organization.
     */
    private MaintenanceSchedule getMaintenanceScheduleForUser(Long id, User currentUser) {
        MaintenanceSchedule schedule = maintenanceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with ID: " + id));

        // Ensure user can only access schedules from their organization
        if (!schedule.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to maintenance schedule");
        }

        return schedule;
    }
}