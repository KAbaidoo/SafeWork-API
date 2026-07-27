package com.safework.api.domain.organization.service;

import com.safework.api.domain.organization.dto.CreateOrganizationRequest;
import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.OrganizationSummaryDto;
import com.safework.api.domain.organization.dto.UpdateOrganizationRequest;
import com.safework.api.domain.organization.mapper.OrganizationMapper;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.model.OrganizationSize;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationDto createOrganization(CreateOrganizationRequest request, User currentUser) {
        // Check if organization name already exists
        if (organizationRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Organization with name '" + request.name() + "' already exists");
        }

        Organization newOrganization = new Organization();
        newOrganization.setName(request.name());
        newOrganization.setAddress(request.address());
        newOrganization.setPhone(request.phone());
        newOrganization.setWebsite(request.website());
        newOrganization.setIndustry(request.industry());
        
        if (request.size() != null) {
            try {
                newOrganization.setSize(OrganizationSize.valueOf(request.size()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid organization size: " + request.size());
            }
        }

        Organization savedOrganization = organizationRepository.save(newOrganization);
        return organizationMapper.toDto(savedOrganization);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationSummaryDto> findAllOrganizations(Pageable pageable) {
        Page<Organization> organizations = organizationRepository.findAll(pageable);
        return organizations.map(organizationMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public OrganizationDto findOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        return organizationMapper.toDto(organization);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getCurrentUserOrganization(User currentUser) {
        return organizationMapper.toDto(currentUser.getOrganization());
    }

    public OrganizationDto updateOrganization(Long id, UpdateOrganizationRequest request, User currentUser) {
        Organization organizationToUpdate = getOrganizationForUser(id, currentUser);

        // Check if name is being changed and if it already exists
        if (!organizationToUpdate.getName().equals(request.name()) && 
            organizationRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Organization with name '" + request.name() + "' already exists");
        }

        organizationToUpdate.setName(request.name());
        organizationToUpdate.setAddress(request.address());
        organizationToUpdate.setPhone(request.phone());
        organizationToUpdate.setWebsite(request.website());
        organizationToUpdate.setIndustry(request.industry());
        
        if (request.size() != null) {
            try {
                organizationToUpdate.setSize(OrganizationSize.valueOf(request.size()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid organization size: " + request.size());
            }
        } else {
            organizationToUpdate.setSize(null);
        }

        Organization savedOrganization = organizationRepository.save(organizationToUpdate);
        return organizationMapper.toDto(savedOrganization);
    }

    public void deleteOrganization(Long id, User currentUser) {
        Organization organizationToDelete = getOrganizationForUser(id, currentUser);
        
        // Additional validation: prevent deletion if organization has users other than the current user
        if (organizationToDelete.getUsers() != null && organizationToDelete.getUsers().size() > 1) {
            throw new ConflictException("Cannot delete organization with multiple users. Please remove all other users first.");
        }

        organizationRepository.delete(organizationToDelete);
    }

    /**
     * Helper method to fetch an organization and verify the user has permission to modify it.
     * Only ADMIN users can modify their own organization.
     */
    private Organization getOrganizationForUser(Long organizationId, User currentUser) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        // Multi-tenancy security check - users can only access their own organization
        if (!organization.getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this organization");
        }

        // Additional check - only ADMIN users can modify organization details
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only administrators can modify organization details");
        }

        return organization;
    }
}