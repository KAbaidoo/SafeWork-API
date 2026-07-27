package com.safework.api.domain.supplier.service;

import com.safework.api.domain.supplier.dto.CreateSupplierRequest;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.dto.UpdateSupplierRequest;
import com.safework.api.domain.supplier.mapper.SupplierMapper;
import com.safework.api.domain.supplier.model.Supplier;
import com.safework.api.domain.supplier.repository.SupplierRepository;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierDto createSupplier(CreateSupplierRequest request, User currentUser) {
        // Check if supplier name already exists within the organization
        if (supplierRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Supplier with name '" + request.name() + "' already exists in this organization");
        }

        Supplier newSupplier = new Supplier();
        newSupplier.setOrganization(currentUser.getOrganization());
        newSupplier.setName(request.name());
        newSupplier.setContactPerson(request.contactPerson());
        newSupplier.setEmail(request.email());
        newSupplier.setPhoneNumber(request.phoneNumber());
        newSupplier.setAddress(request.address());

        Supplier savedSupplier = supplierRepository.save(newSupplier);
        return supplierMapper.toDto(savedSupplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierSummaryDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findAllByOrganizationId(organizationId, pageable);
        return suppliers.map(supplierMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public SupplierDto findSupplierById(Long id, User currentUser) {
        Supplier supplier = getSupplierForUser(id, currentUser);
        return supplierMapper.toDto(supplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierSummaryDto> searchSuppliersByName(String name, User currentUser, Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                currentUser.getOrganization().getId(), name, pageable);
        return suppliers.map(supplierMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<SupplierSummaryDto> searchSuppliersByEmail(String email, User currentUser, Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findByOrganizationIdAndEmailContainingIgnoreCase(
                currentUser.getOrganization().getId(), email, pageable);
        return suppliers.map(supplierMapper::toSummaryDto);
    }

    public SupplierDto updateSupplier(Long id, UpdateSupplierRequest request, User currentUser) {
        Supplier supplierToUpdate = getSupplierForUser(id, currentUser);

        // Check if name is being changed and if it already exists
        if (!supplierToUpdate.getName().equals(request.name()) && 
            supplierRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Supplier with name '" + request.name() + "' already exists in this organization");
        }

        supplierToUpdate.setName(request.name());
        supplierToUpdate.setContactPerson(request.contactPerson());
        supplierToUpdate.setEmail(request.email());
        supplierToUpdate.setPhoneNumber(request.phoneNumber());
        supplierToUpdate.setAddress(request.address());

        Supplier savedSupplier = supplierRepository.save(supplierToUpdate);
        return supplierMapper.toDto(savedSupplier);
    }

    public void deleteSupplier(Long id, User currentUser) {
        Supplier supplierToDelete = getSupplierForUser(id, currentUser);
        
        // TODO: Add check if supplier has associated assets before deletion
        // This will be implemented when Asset relationships are fully integrated
        
        supplierRepository.delete(supplierToDelete);
    }

    /**
     * Helper method to fetch a supplier and verify the user has permission to access it.
     */
    private Supplier getSupplierForUser(Long supplierId, User currentUser) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        // Multi-tenancy security check - users can only access suppliers in their organization
        if (!supplier.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this supplier");
        }

        return supplier;
    }
}