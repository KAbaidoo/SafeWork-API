package com.safework.api.domain.department.service;

import com.safework.api.domain.department.dto.CreateDepartmentRequest;
import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.dto.UpdateDepartmentRequest;
import com.safework.api.domain.department.mapper.DepartmentMapper;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentDto createDepartment(CreateDepartmentRequest request, User currentUser) {
        // Check if department name already exists within the organization
        if (departmentRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Department with name '" + request.name() + "' already exists in this organization");
        }

        // Check if department code already exists within the organization (if provided)
        if (request.code() != null && departmentRepository.existsByOrganizationIdAndCode(currentUser.getOrganization().getId(), request.code())) {
            throw new ConflictException("Department with code '" + request.code() + "' already exists in this organization");
        }

        Department newDepartment = new Department();
        newDepartment.setOrganization(currentUser.getOrganization());
        newDepartment.setName(request.name());
        newDepartment.setDescription(request.description());
        newDepartment.setCode(request.code());
        newDepartment.setEmployeeCount(0); // Initialize with 0

        // Set manager if provided
        if (request.managerId() != null) {
            User manager = getUserForOrganization(request.managerId(), currentUser);
            // Validate that the manager has appropriate role
            if (!manager.getRole().equals(UserRole.ADMIN) && !manager.getRole().equals(UserRole.SUPERVISOR)) {
                throw new IllegalArgumentException("Manager must have ADMIN or SUPERVISOR role");
            }
            newDepartment.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(newDepartment);
        return departmentMapper.toDto(savedDepartment);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentSummaryDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<Department> departments = departmentRepository.findAllByOrganizationId(organizationId, pageable);
        return departments.map(departmentMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public DepartmentDto findDepartmentById(Long id, User currentUser) {
        Department department = getDepartmentForUser(id, currentUser);
        return departmentMapper.toDto(department);
    }

    public DepartmentDto updateDepartment(Long id, UpdateDepartmentRequest request, User currentUser) {
        Department departmentToUpdate = getDepartmentForUser(id, currentUser);

        // Check if name is being changed and if it already exists
        if (!departmentToUpdate.getName().equals(request.name()) && 
            departmentRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Department with name '" + request.name() + "' already exists in this organization");
        }

        // Check if code is being changed and if it already exists
        if (request.code() != null && !request.code().equals(departmentToUpdate.getCode()) &&
            departmentRepository.existsByOrganizationIdAndCode(currentUser.getOrganization().getId(), request.code())) {
            throw new ConflictException("Department with code '" + request.code() + "' already exists in this organization");
        }

        departmentToUpdate.setName(request.name());
        departmentToUpdate.setDescription(request.description());
        departmentToUpdate.setCode(request.code());

        // Update manager if provided
        if (request.managerId() != null) {
            User manager = getUserForOrganization(request.managerId(), currentUser);
            // Validate that the manager has appropriate role
            if (!manager.getRole().equals(UserRole.ADMIN) && !manager.getRole().equals(UserRole.SUPERVISOR)) {
                throw new IllegalArgumentException("Manager must have ADMIN or SUPERVISOR role");
            }
            departmentToUpdate.setManager(manager);
        } else {
            departmentToUpdate.setManager(null);
        }

        Department savedDepartment = departmentRepository.save(departmentToUpdate);
        return departmentMapper.toDto(savedDepartment);
    }

    public void deleteDepartment(Long id, User currentUser) {
        Department departmentToDelete = getDepartmentForUser(id, currentUser);
        
        // Check if department has employees
        if (departmentToDelete.getEmployeeCount() != null && departmentToDelete.getEmployeeCount() > 0) {
            throw new ConflictException("Cannot delete department with employees. Please reassign employees first.");
        }

        // Additional check by counting actual users in the department
        long userCount = userRepository.countByDepartmentId(departmentToDelete.getId());
        if (userCount > 0) {
            throw new ConflictException("Cannot delete department with " + userCount + " employee(s). Please reassign employees first.");
        }

        departmentRepository.delete(departmentToDelete);
    }

    /**
     * Updates the employee count for a department.
     * This method should be called when users are assigned/unassigned to departments.
     */
    public void updateEmployeeCount(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        long employeeCount = userRepository.countByDepartmentId(departmentId);
        department.setEmployeeCount((int) employeeCount);
        departmentRepository.save(department);
    }

    /**
     * Helper method to fetch a department and verify the user has permission to access it.
     */
    private Department getDepartmentForUser(Long departmentId, User currentUser) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        // Multi-tenancy security check - users can only access departments in their organization
        if (!department.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this department");
        }

        return department;
    }

    /**
     * Helper method to fetch a user and verify they belong to the same organization.
     */
    private User getUserForOrganization(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Multi-tenancy security check
        if (!user.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("User does not belong to your organization");
        }
        
        return user;
    }
}