package com.safework.api.domain.department.service;

import com.safework.api.domain.department.dto.CreateDepartmentRequest;
import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.dto.UpdateDepartmentRequest;
import com.safework.api.domain.department.mapper.DepartmentMapper;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private Organization organization;
    private Organization otherOrganization;
    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private User managerUser;
    private Department department;
    private DepartmentDto departmentDto;
    private DepartmentSummaryDto departmentSummaryDto;
    private CreateDepartmentRequest createRequest;
    private UpdateDepartmentRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Tech Corp");

        otherOrganization = new Organization();
        otherOrganization = createMockOrganization(2L, "Other Corp");

        adminUser = new User();
        setEntityId(adminUser, 1L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@techcorp.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);

        supervisorUser = new User();
        setEntityId(supervisorUser, 2L);
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@techcorp.com");
        supervisorUser.setPassword("hashedPassword");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);

        inspectorUser = new User();
        setEntityId(inspectorUser, 3L);
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@techcorp.com");
        inspectorUser.setPassword("hashedPassword");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);

        managerUser = new User();
        setEntityId(managerUser, 4L);
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@techcorp.com");
        managerUser.setPassword("hashedPassword");
        managerUser.setRole(UserRole.ADMIN);
        managerUser.setOrganization(organization);

        department = new Department();
        department = createMockDepartment(1L, "Information Technology");
        department.setDescription("IT department");
        department.setCode("IT");
        department.setOrganization(organization);
        department.setManager(managerUser);
        department.setEmployeeCount(3);

        departmentDto = new DepartmentDto(
                1L, "Information Technology", "IT department", "IT",
                1L, 4L, "Manager User", 3,
                LocalDateTime.now(), LocalDateTime.now()
        );

        departmentSummaryDto = new DepartmentSummaryDto(
                1L, "Information Technology", "IT", "Manager User", 3
        );

        createRequest = new CreateDepartmentRequest(
                "New Department",
                "New department description",
                "NEWDEPT",
                4L // managerId
        );

        updateRequest = new UpdateDepartmentRequest(
                "Updated Department",
                "Updated description",
                "UPDEPT",
                4L // managerId
        );
    }

    private Organization createMockOrganization(Long id, String name) {
        Organization org = new Organization();
        org.setName(name);
        try {
            Field idField = Organization.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(org, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set organization ID", e);
        }
        return org;
    }

    private Department createMockDepartment(Long id, String name) {
        Department dept = new Department();
        dept.setName(name);
        try {
            Field idField = Department.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dept, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department ID", e);
        }
        return dept;
    }

    private void setEntityId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }
    }

    @Test
    void createDepartment_ShouldCreateDepartment_WhenValidRequest() {
        // Given
        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(false);
        given(userRepository.findById(4L)).willReturn(Optional.of(managerUser));
        given(departmentRepository.save(any(Department.class))).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.createDepartment(createRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(departmentRepository).should().existsByOrganizationIdAndName(1L, "New Department");
        then(departmentRepository).should().existsByOrganizationIdAndCode(1L, "NEWDEPT");
        then(userRepository).should().findById(4L);
        then(departmentRepository).should().save(any(Department.class));
        then(departmentMapper).should().toDto(department);
    }

    @Test
    void createDepartment_ShouldCreateWithoutManager_WhenManagerIdIsNull() {
        // Given
        CreateDepartmentRequest requestWithoutManager = new CreateDepartmentRequest(
                "New Department", "Description", "NEWDEPT", null
        );

        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(false);
        given(departmentRepository.save(any(Department.class))).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.createDepartment(requestWithoutManager, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(userRepository).should(never()).findById(any());
        then(departmentRepository).should().save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldCreateWithoutCode_WhenCodeIsNull() {
        // Given
        CreateDepartmentRequest requestWithoutCode = new CreateDepartmentRequest(
                "New Department", "Description", null, 4L
        );

        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(userRepository.findById(4L)).willReturn(Optional.of(managerUser));
        given(departmentRepository.save(any(Department.class))).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.createDepartment(requestWithoutCode, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(departmentRepository).should().existsByOrganizationIdAndName(1L, "New Department");
        then(departmentRepository).should(never()).existsByOrganizationIdAndCode(any(), any());
        then(departmentRepository).should().save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldThrowConflictException_WhenNameAlreadyExists() {
        // Given
        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(true);

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(createRequest, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department with name 'New Department' already exists in this organization");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldThrowConflictException_WhenCodeAlreadyExists() {
        // Given
        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(true);

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(createRequest, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department with code 'NEWDEPT' already exists in this organization");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldThrowResourceNotFoundException_WhenManagerNotFound() {
        // Given
        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(false);
        given(userRepository.findById(4L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(createRequest, adminUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 4");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldThrowIllegalArgumentException_WhenManagerHasInvalidRole() {
        // Given
        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(false);
        given(userRepository.findById(3L)).willReturn(Optional.of(inspectorUser));

        CreateDepartmentRequest requestWithInspectorManager = new CreateDepartmentRequest(
                "New Department", "Description", "NEWDEPT", 3L
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(requestWithInspectorManager, adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manager must have ADMIN or SUPERVISOR role");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void createDepartment_ShouldThrowAccessDeniedException_WhenManagerFromDifferentOrganization() {
        // Given
        User otherOrgManager = new User();
        setEntityId(otherOrgManager, 5L);
        otherOrgManager.setName("Other Manager");
        otherOrgManager.setRole(UserRole.ADMIN);
        otherOrgManager.setOrganization(otherOrganization);

        given(departmentRepository.existsByOrganizationIdAndName(1L, "New Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "NEWDEPT")).willReturn(false);
        given(userRepository.findById(5L)).willReturn(Optional.of(otherOrgManager));

        CreateDepartmentRequest requestWithOtherOrgManager = new CreateDepartmentRequest(
                "New Department", "Description", "NEWDEPT", 5L
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(requestWithOtherOrgManager, adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User does not belong to your organization");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void findAllByOrganization_ShouldReturnPagedDepartments() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Department> departments = List.of(department);
        Page<Department> departmentPage = new PageImpl<>(departments, pageable, 1);

        given(departmentRepository.findAllByOrganizationId(1L, pageable)).willReturn(departmentPage);
        given(departmentMapper.toSummaryDto(department)).willReturn(departmentSummaryDto);

        // When
        Page<DepartmentSummaryDto> result = departmentService.findAllByOrganization(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(departmentSummaryDto);
        assertThat(result.getTotalElements()).isEqualTo(1);

        then(departmentRepository).should().findAllByOrganizationId(1L, pageable);
        then(departmentMapper).should().toSummaryDto(department);
    }

    @Test
    void findDepartmentById_ShouldReturnDepartment_WhenUserHasAccess() {
        // Given
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.findDepartmentById(1L, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(departmentRepository).should().findById(1L);
        then(departmentMapper).should().toDto(department);
    }

    @Test
    void findDepartmentById_ShouldThrowResourceNotFoundException_WhenDepartmentNotFound() {
        // Given
        given(departmentRepository.findById(999L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> departmentService.findDepartmentById(999L, adminUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found with id: 999");

        then(departmentMapper).should(never()).toDto(any(Department.class));
    }

    @Test
    void findDepartmentById_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given
        User otherOrgUser = new User();
        setEntityId(otherOrgUser, 6L);
        otherOrgUser.setOrganization(otherOrganization);

        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

        // When/Then
        assertThatThrownBy(() -> departmentService.findDepartmentById(1L, otherOrgUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");

        then(departmentMapper).should(never()).toDto(any(Department.class));
    }

    @Test
    void updateDepartment_ShouldUpdateDepartment_WhenValidRequest() {
        // Given
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(departmentRepository.existsByOrganizationIdAndName(1L, "Updated Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "UPDEPT")).willReturn(false);
        given(userRepository.findById(4L)).willReturn(Optional.of(managerUser));
        given(departmentRepository.save(department)).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, updateRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(departmentRepository).should().findById(1L);
        then(departmentRepository).should().save(department);
        then(departmentMapper).should().toDto(department);
    }

    @Test
    void updateDepartment_ShouldClearManager_WhenManagerIdIsNull() {
        // Given
        UpdateDepartmentRequest requestWithoutManager = new UpdateDepartmentRequest(
                "Updated Department", "Description", "UPDEPT", null
        );

        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(departmentRepository.existsByOrganizationIdAndName(1L, "Updated Department")).willReturn(false);
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "UPDEPT")).willReturn(false);
        given(departmentRepository.save(department)).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, requestWithoutManager, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);
        assertThat(department.getManager()).isNull();

        then(userRepository).should(never()).findById(any());
        then(departmentRepository).should().save(department);
    }

    @Test
    void updateDepartment_ShouldNotCheckNameConflict_WhenNameUnchanged() {
        // Given
        UpdateDepartmentRequest sameNameRequest = new UpdateDepartmentRequest(
                "Information Technology", // Same name
                "Updated description", "UPDEPT", 4L
        );

        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(departmentRepository.existsByOrganizationIdAndCode(1L, "UPDEPT")).willReturn(false);
        given(userRepository.findById(4L)).willReturn(Optional.of(managerUser));
        given(departmentRepository.save(department)).willReturn(department);
        given(departmentMapper.toDto(department)).willReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, sameNameRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(departmentDto);

        then(departmentRepository).should(never()).existsByOrganizationIdAndName(any(), any());
        then(departmentRepository).should().save(department);
    }

    @Test
    void updateDepartment_ShouldThrowConflictException_WhenNewNameExists() {
        // Given
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(departmentRepository.existsByOrganizationIdAndName(1L, "Updated Department")).willReturn(true);

        // When/Then
        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateRequest, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department with name 'Updated Department' already exists in this organization");

        then(departmentRepository).should(never()).save(any(Department.class));
    }

    @Test
    void deleteDepartment_ShouldDeleteDepartment_WhenNoEmployees() {
        // Given
        department.setEmployeeCount(0);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.countByDepartmentId(1L)).willReturn(0L);

        // When
        departmentService.deleteDepartment(1L, adminUser);

        // Then
        then(departmentRepository).should().findById(1L);
        then(userRepository).should().countByDepartmentId(1L);
        then(departmentRepository).should().delete(department);
    }

    @Test
    void deleteDepartment_ShouldThrowConflictException_WhenEmployeeCountGreaterThanZero() {
        // Given
        department.setEmployeeCount(3);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

        // When/Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(1L, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete department with employees. Please reassign employees first.");

        then(departmentRepository).should(never()).delete(any(Department.class));
    }

    @Test
    void deleteDepartment_ShouldThrowConflictException_WhenActualUserCountGreaterThanZero() {
        // Given
        department.setEmployeeCount(0); // Cached count is 0
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.countByDepartmentId(1L)).willReturn(2L); // But actual count is 2

        // When/Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(1L, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete department with 2 employee(s). Please reassign employees first.");

        then(departmentRepository).should(never()).delete(any(Department.class));
    }

    @Test
    void updateEmployeeCount_ShouldUpdateCount_WhenDepartmentExists() {
        // Given
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.countByDepartmentId(1L)).willReturn(5L);
        given(departmentRepository.save(department)).willReturn(department);

        // When
        departmentService.updateEmployeeCount(1L);

        // Then
        assertThat(department.getEmployeeCount()).isEqualTo(5);

        then(departmentRepository).should().findById(1L);
        then(userRepository).should().countByDepartmentId(1L);
        then(departmentRepository).should().save(department);
    }

    @Test
    void updateEmployeeCount_ShouldThrowResourceNotFoundException_WhenDepartmentNotFound() {
        // Given
        given(departmentRepository.findById(999L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> departmentService.updateEmployeeCount(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found with id: 999");

        then(userRepository).should(never()).countByDepartmentId(any());
        then(departmentRepository).should(never()).save(any(Department.class));
    }
}