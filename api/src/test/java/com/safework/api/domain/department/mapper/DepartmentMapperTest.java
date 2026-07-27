package com.safework.api.domain.department.mapper;

import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentMapperTest {

    private DepartmentMapper departmentMapper;
    private Department department;
    private Organization organization;
    private User manager;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        departmentMapper = new DepartmentMapper();
        
        testCreatedAt = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        testUpdatedAt = LocalDateTime.of(2023, 12, 1, 15, 45, 30);

        // Create organization
        organization = new Organization();
        organization.setName("Test Organization");
        setEntityId(organization, 1L);

        // Create manager user
        manager = new User();
        manager.setName("Manager User");
        manager.setEmail("manager@testorg.com");
        manager.setRole(UserRole.ADMIN);
        manager.setOrganization(organization);
        setEntityId(manager, 2L);

        // Create department with all fields
        department = new Department();
        department.setName("Information Technology");
        department.setDescription("IT department handling all technology needs");
        department.setCode("IT");
        department.setOrganization(organization);
        department.setManager(manager);
        department.setEmployeeCount(5);
        
        // Set ID and timestamps using reflection
        setEntityId(department, 1L);
        setTimestamp(department, "createdAt", testCreatedAt);
        setTimestamp(department, "updatedAt", testUpdatedAt);
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

    private void setTimestamp(Object entity, String fieldName, LocalDateTime timestamp) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, timestamp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set timestamp", e);
        }
    }

    @Test
    void toDto_ShouldMapAllFields_WhenDepartmentHasAllFields() {
        // When
        DepartmentDto result = departmentMapper.toDto(department);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Information Technology");
        assertThat(result.description()).isEqualTo("IT department handling all technology needs");
        assertThat(result.code()).isEqualTo("IT");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.managerId()).isEqualTo(2L);
        assertThat(result.managerName()).isEqualTo("Manager User");
        assertThat(result.employeeCount()).isEqualTo(5);
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Department minimalDepartment = new Department();
        minimalDepartment.setName("Minimal Department");
        minimalDepartment.setOrganization(organization);
        setEntityId(minimalDepartment, 2L);
        setTimestamp(minimalDepartment, "createdAt", testCreatedAt);
        setTimestamp(minimalDepartment, "updatedAt", testUpdatedAt);
        // description, code, manager, employeeCount are null

        // When
        DepartmentDto result = departmentMapper.toDto(minimalDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Minimal Department");
        assertThat(result.description()).isNull();
        assertThat(result.code()).isNull();
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.managerId()).isNull();
        assertThat(result.managerName()).isNull();
        assertThat(result.employeeCount()).isNull();
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldHandleNullOrganization() {
        // Given
        Department deptWithoutOrg = new Department();
        deptWithoutOrg.setName("Orphaned Department");
        deptWithoutOrg.setManager(manager);
        setEntityId(deptWithoutOrg, 3L);
        setTimestamp(deptWithoutOrg, "createdAt", testCreatedAt);
        setTimestamp(deptWithoutOrg, "updatedAt", testUpdatedAt);

        // When
        DepartmentDto result = departmentMapper.toDto(deptWithoutOrg);

        // Then
        assertThat(result.organizationId()).isNull();
        assertThat(result.managerId()).isEqualTo(2L);
        assertThat(result.managerName()).isEqualTo("Manager User");
    }

    @Test
    void toDto_ShouldHandleNullManager() {
        // Given
        Department deptWithoutManager = new Department();
        deptWithoutManager.setName("Managerless Department");
        deptWithoutManager.setDescription("No manager assigned");
        deptWithoutManager.setCode("NOMAN");
        deptWithoutManager.setOrganization(organization);
        deptWithoutManager.setEmployeeCount(3);
        setEntityId(deptWithoutManager, 4L);
        setTimestamp(deptWithoutManager, "createdAt", testCreatedAt);
        setTimestamp(deptWithoutManager, "updatedAt", testUpdatedAt);

        // When
        DepartmentDto result = departmentMapper.toDto(deptWithoutManager);

        // Then
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.name()).isEqualTo("Managerless Department");
        assertThat(result.description()).isEqualTo("No manager assigned");
        assertThat(result.code()).isEqualTo("NOMAN");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.managerId()).isNull();
        assertThat(result.managerName()).isNull();
        assertThat(result.employeeCount()).isEqualTo(3);
    }

    @Test
    void toSummaryDto_ShouldMapSummaryFields_WhenDepartmentHasAllFields() {
        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(department);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Information Technology");
        assertThat(result.code()).isEqualTo("IT");
        assertThat(result.managerName()).isEqualTo("Manager User");
        assertThat(result.employeeCount()).isEqualTo(5);
    }

    @Test
    void toSummaryDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Department minimalDepartment = new Department();
        minimalDepartment.setName("Summary Test Department");
        setEntityId(minimalDepartment, 3L);
        // code, manager, employeeCount are null

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(minimalDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.name()).isEqualTo("Summary Test Department");
        assertThat(result.code()).isNull();
        assertThat(result.managerName()).isNull();
        assertThat(result.employeeCount()).isNull();
    }

    @Test
    void toSummaryDto_ShouldHandleNullManager() {
        // Given
        Department deptWithoutManager = new Department();
        deptWithoutManager.setName("No Manager Dept");
        deptWithoutManager.setCode("NOMGR");
        deptWithoutManager.setEmployeeCount(2);
        setEntityId(deptWithoutManager, 4L);

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(deptWithoutManager);

        // Then
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.name()).isEqualTo("No Manager Dept");
        assertThat(result.code()).isEqualTo("NOMGR");
        assertThat(result.managerName()).isNull();
        assertThat(result.employeeCount()).isEqualTo(2);
    }

    @Test
    void toSummaryDto_ShouldHandleZeroEmployeeCount() {
        // Given
        Department emptyDepartment = new Department();
        emptyDepartment.setName("Empty Department");
        emptyDepartment.setCode("EMPTY");
        emptyDepartment.setManager(manager);
        emptyDepartment.setEmployeeCount(0);
        setEntityId(emptyDepartment, 5L);

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(emptyDepartment);

        // Then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Empty Department");
        assertThat(result.code()).isEqualTo("EMPTY");
        assertThat(result.managerName()).isEqualTo("Manager User");
        assertThat(result.employeeCount()).isEqualTo(0);
    }

    @Test
    void mappingConsistency_BothMethodsShouldMapCommonFieldsIdentically() {
        // When
        DepartmentDto fullDto = departmentMapper.toDto(department);
        DepartmentSummaryDto summaryDto = departmentMapper.toSummaryDto(department);

        // Then - Common fields should be identical
        assertThat(fullDto.id()).isEqualTo(summaryDto.id());
        assertThat(fullDto.name()).isEqualTo(summaryDto.name());
        assertThat(fullDto.code()).isEqualTo(summaryDto.code());
        assertThat(fullDto.managerName()).isEqualTo(summaryDto.managerName());
        assertThat(fullDto.employeeCount()).isEqualTo(summaryDto.employeeCount());
    }

    @Test
    void toDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        department.setName("Research & Development (R&D)");
        department.setDescription("R&D department with special chars: @#$%^&*()");
        department.setCode("R&D123");

        User specialManager = new User();
        specialManager.setName("Manager & Director");
        setEntityId(specialManager, 3L);
        department.setManager(specialManager);

        // When
        DepartmentDto result = departmentMapper.toDto(department);

        // Then
        assertThat(result.name()).isEqualTo("Research & Development (R&D)");
        assertThat(result.description()).isEqualTo("R&D department with special chars: @#$%^&*()");
        assertThat(result.code()).isEqualTo("R&D123");
        assertThat(result.managerName()).isEqualTo("Manager & Director");
    }

    @Test
    void toSummaryDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        department.setName("Finance & Accounting");
        department.setCode("F&A");

        User specialManager = new User();
        specialManager.setName("CFO & Controller");
        setEntityId(specialManager, 4L);
        department.setManager(specialManager);

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(department);

        // Then
        assertThat(result.name()).isEqualTo("Finance & Accounting");
        assertThat(result.code()).isEqualTo("F&A");
        assertThat(result.managerName()).isEqualTo("CFO & Controller");
    }

    @Test
    void toDto_ShouldHandleEmptyStringsAsValues() {
        // Given
        department.setDescription("");
        department.setCode("");

        User emptyNameManager = new User();
        emptyNameManager.setName("");
        setEntityId(emptyNameManager, 5L);
        department.setManager(emptyNameManager);

        // When
        DepartmentDto result = departmentMapper.toDto(department);

        // Then
        assertThat(result.description()).isEqualTo("");
        assertThat(result.code()).isEqualTo("");
        assertThat(result.managerName()).isEqualTo("");
    }

    @Test
    void toSummaryDto_ShouldHandleEmptyStrings() {
        // Given
        department.setCode("");

        User emptyNameManager = new User();
        emptyNameManager.setName("");
        setEntityId(emptyNameManager, 6L);
        department.setManager(emptyNameManager);

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(department);

        // Then
        assertThat(result.code()).isEqualTo("");
        assertThat(result.managerName()).isEqualTo("");
    }

    @Test
    void toDto_ShouldHandleLargeEmployeeCount() {
        // Given
        department.setEmployeeCount(Integer.MAX_VALUE);

        // When
        DepartmentDto result = departmentMapper.toDto(department);

        // Then
        assertThat(result.employeeCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void toSummaryDto_ShouldHandleLargeEmployeeCount() {
        // Given
        department.setEmployeeCount(Integer.MAX_VALUE);

        // When
        DepartmentSummaryDto result = departmentMapper.toSummaryDto(department);

        // Then
        assertThat(result.employeeCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void mappingPerformance_ShouldHandleLargeDepartmentNames() {
        // Given
        String longName = "A".repeat(100); // 100 character name
        String longDescription = "B".repeat(500); // 500 character description
        String longCode = "C".repeat(10); // 10 character code

        department.setName(longName);
        department.setDescription(longDescription);
        department.setCode(longCode);

        // When
        DepartmentDto fullDto = departmentMapper.toDto(department);
        DepartmentSummaryDto summaryDto = departmentMapper.toSummaryDto(department);

        // Then
        assertThat(fullDto.name()).isEqualTo(longName);
        assertThat(fullDto.description()).isEqualTo(longDescription);
        assertThat(fullDto.code()).isEqualTo(longCode);

        assertThat(summaryDto.name()).isEqualTo(longName);
        assertThat(summaryDto.code()).isEqualTo(longCode);
    }
}