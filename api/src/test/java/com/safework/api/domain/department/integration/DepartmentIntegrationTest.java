package com.safework.api.domain.department.integration;

import com.safework.api.domain.department.dto.CreateDepartmentRequest;
import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.dto.UpdateDepartmentRequest;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.department.service.DepartmentService;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class DepartmentIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private DepartmentService departmentService;

    private Organization organization1;
    private Organization organization2;
    private User adminUser1;
    private User supervisorUser1;
    private User inspectorUser1;
    private User adminUser2;
    private User employee1;
    private User employee2;

    @BeforeEach
    void setUp() {
        // Create department service with real dependencies
        departmentService = new DepartmentService(
                departmentRepository,
                userRepository,
                new com.safework.api.domain.department.mapper.DepartmentMapper()
        );

        // Setup test organizations
        organization1 = new Organization();
        organization1.setName("Tech Solutions Inc");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Manufacturing Corp");
        entityManager.persistAndFlush(organization2);

        // Setup test users
        adminUser1 = new User();
        adminUser1.setName("Admin User 1");
        adminUser1.setEmail("admin1@techsolutions.com");
        adminUser1.setPassword("hashedPassword1");
        adminUser1.setRole(UserRole.ADMIN);
        adminUser1.setOrganization(organization1);
        entityManager.persistAndFlush(adminUser1);

        supervisorUser1 = new User();
        supervisorUser1.setName("Supervisor User 1");
        supervisorUser1.setEmail("supervisor1@techsolutions.com");
        supervisorUser1.setPassword("hashedPassword2");
        supervisorUser1.setRole(UserRole.SUPERVISOR);
        supervisorUser1.setOrganization(organization1);
        entityManager.persistAndFlush(supervisorUser1);

        inspectorUser1 = new User();
        inspectorUser1.setName("Inspector User 1");
        inspectorUser1.setEmail("inspector1@techsolutions.com");
        inspectorUser1.setPassword("hashedPassword3");
        inspectorUser1.setRole(UserRole.INSPECTOR);
        inspectorUser1.setOrganization(organization1);
        entityManager.persistAndFlush(inspectorUser1);

        adminUser2 = new User();
        adminUser2.setName("Admin User 2");
        adminUser2.setEmail("admin2@mfgcorp.com");
        adminUser2.setPassword("hashedPassword4");
        adminUser2.setRole(UserRole.ADMIN);
        adminUser2.setOrganization(organization2);
        entityManager.persistAndFlush(adminUser2);

        employee1 = new User();
        employee1.setName("Employee 1");
        employee1.setEmail("employee1@techsolutions.com");
        employee1.setPassword("hashedPassword5");
        employee1.setRole(UserRole.INSPECTOR);
        employee1.setOrganization(organization1);
        entityManager.persistAndFlush(employee1);

        employee2 = new User();
        employee2.setName("Employee 2");
        employee2.setEmail("employee2@techsolutions.com");
        employee2.setPassword("hashedPassword6");
        employee2.setRole(UserRole.INSPECTOR);
        employee2.setOrganization(organization1);
        entityManager.persistAndFlush(employee2);

        entityManager.clear();
    }

    @Test
    void createDepartment_ShouldCreateWithAllFields_AndPersistToDatabase() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Information Technology",
                "IT department handling all technology needs",
                "IT",
                adminUser1.getId()
        );

        // When
        DepartmentDto result = departmentService.createDepartment(request, adminUser1);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Information Technology");
        assertThat(result.description()).isEqualTo("IT department handling all technology needs");
        assertThat(result.code()).isEqualTo("IT");
        assertThat(result.managerId()).isEqualTo(adminUser1.getId());
        assertThat(result.managerName()).isEqualTo("Admin User 1");
        assertThat(result.organizationId()).isEqualTo(organization1.getId());
        assertThat(result.employeeCount()).isEqualTo(0);
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();

        // Verify persistence
        Department saved = departmentRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Information Technology");
        assertThat(saved.getManager().getId()).isEqualTo(adminUser1.getId());
    }

    @Test
    void createDepartment_ShouldCreateWithMinimalFields() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Human Resources",
                null, // no description
                null, // no code
                null  // no manager
        );

        // When
        DepartmentDto result = departmentService.createDepartment(request, adminUser1);

        // Then
        assertThat(result.name()).isEqualTo("Human Resources");
        assertThat(result.description()).isNull();
        assertThat(result.code()).isNull();
        assertThat(result.managerId()).isNull();
        assertThat(result.managerName()).isNull();
        assertThat(result.employeeCount()).isEqualTo(0);

        // Verify persistence
        Department saved = departmentRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.getCode()).isNull();
        assertThat(saved.getManager()).isNull();
    }

    @Test
    void createDepartment_ShouldAllowSupervisorAsManager() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Quality Assurance",
                "QA department",
                "QA",
                supervisorUser1.getId()
        );

        // When
        DepartmentDto result = departmentService.createDepartment(request, adminUser1);

        // Then
        assertThat(result.managerId()).isEqualTo(supervisorUser1.getId());
        assertThat(result.managerName()).isEqualTo("Supervisor User 1");
    }

    @Test
    void createDepartment_ShouldThrowConflictException_WhenNameExistsInSameOrganization() {
        // Given - Create first department
        CreateDepartmentRequest firstRequest = new CreateDepartmentRequest(
                "Information Technology", "IT dept", "IT", adminUser1.getId()
        );
        departmentService.createDepartment(firstRequest, adminUser1);

        // Try to create second department with same name
        CreateDepartmentRequest duplicateRequest = new CreateDepartmentRequest(
                "Information Technology", "Different description", "IT2", supervisorUser1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(duplicateRequest, adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department with name 'Information Technology' already exists in this organization");
    }

    @Test
    void createDepartment_ShouldThrowConflictException_WhenCodeExistsInSameOrganization() {
        // Given - Create first department
        CreateDepartmentRequest firstRequest = new CreateDepartmentRequest(
                "Information Technology", "IT dept", "IT", adminUser1.getId()
        );
        departmentService.createDepartment(firstRequest, adminUser1);

        // Try to create second department with same code
        CreateDepartmentRequest duplicateCodeRequest = new CreateDepartmentRequest(
                "Internet Technologies", "Different name", "IT", supervisorUser1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(duplicateCodeRequest, adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department with code 'IT' already exists in this organization");
    }

    @Test
    void createDepartment_ShouldAllowSameNameAndCode_InDifferentOrganizations() {
        // Given
        CreateDepartmentRequest org1Request = new CreateDepartmentRequest(
                "Information Technology", "IT dept", "IT", adminUser1.getId()
        );
        CreateDepartmentRequest org2Request = new CreateDepartmentRequest(
                "Information Technology", "IT dept", "IT", adminUser2.getId()
        );

        // When
        DepartmentDto org1Dept = departmentService.createDepartment(org1Request, adminUser1);
        DepartmentDto org2Dept = departmentService.createDepartment(org2Request, adminUser2);

        // Then
        assertThat(org1Dept.organizationId()).isEqualTo(organization1.getId());
        assertThat(org2Dept.organizationId()).isEqualTo(organization2.getId());
        assertThat(org1Dept.name()).isEqualTo(org2Dept.name());
        assertThat(org1Dept.code()).isEqualTo(org2Dept.code());
    }

    @Test
    void createDepartment_ShouldThrowIllegalArgumentException_WhenManagerHasInvalidRole() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Quality Assurance", "QA department", "QA", inspectorUser1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(request, adminUser1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manager must have ADMIN or SUPERVISOR role");
    }

    @Test
    void findAllByOrganization_ShouldReturnOrganizationDepartments_WithPagination() {
        // Given - Create departments in org1
        CreateDepartmentRequest itRequest = new CreateDepartmentRequest(
                "Information Technology", "IT dept", "IT", adminUser1.getId()
        );
        CreateDepartmentRequest hrRequest = new CreateDepartmentRequest(
                "Human Resources", "HR dept", "HR", supervisorUser1.getId()
        );
        CreateDepartmentRequest finRequest = new CreateDepartmentRequest(
                "Finance", "Finance dept", "FIN", null
        );

        departmentService.createDepartment(itRequest, adminUser1);
        departmentService.createDepartment(hrRequest, adminUser1);
        departmentService.createDepartment(finRequest, adminUser1);

        // Create department in org2 (should not appear in results)
        CreateDepartmentRequest org2Request = new CreateDepartmentRequest(
                "Manufacturing", "Mfg dept", "MFG", adminUser2.getId()
        );
        departmentService.createDepartment(org2Request, adminUser2);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DepartmentSummaryDto> result = departmentService.findAllByOrganization(
                organization1.getId(), pageable
        );

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting(DepartmentSummaryDto::name)
                .containsExactlyInAnyOrder("Information Technology", "Human Resources", "Finance");
    }

    @Test
    void findDepartmentById_ShouldReturnCompleteDetails() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Information Technology", "IT department", "IT", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(request, adminUser1);

        // When
        DepartmentDto result = departmentService.findDepartmentById(created.id(), adminUser1);

        // Then
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo("Information Technology");
        assertThat(result.description()).isEqualTo("IT department");
        assertThat(result.code()).isEqualTo("IT");
        assertThat(result.managerId()).isEqualTo(adminUser1.getId());
        assertThat(result.managerName()).isEqualTo("Admin User 1");
        assertThat(result.organizationId()).isEqualTo(organization1.getId());
    }

    @Test
    void findDepartmentById_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> departmentService.findDepartmentById(999L, adminUser1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found with id: 999");
    }

    @Test
    void updateDepartment_ShouldUpdateAllFields_AndPersistChanges() {
        // Given
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Information Technology", "IT department", "IT", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        UpdateDepartmentRequest updateRequest = new UpdateDepartmentRequest(
                "Information Technology Services",
                "Enhanced IT services department",
                "ITS",
                supervisorUser1.getId()
        );

        // When
        DepartmentDto result = departmentService.updateDepartment(
                created.id(), updateRequest, adminUser1
        );

        // Then
        assertThat(result.name()).isEqualTo("Information Technology Services");
        assertThat(result.description()).isEqualTo("Enhanced IT services department");
        assertThat(result.code()).isEqualTo("ITS");
        assertThat(result.managerId()).isEqualTo(supervisorUser1.getId());
        assertThat(result.managerName()).isEqualTo("Supervisor User 1");

        // Verify persistence
        entityManager.flush();
        entityManager.clear();
        Department updated = departmentRepository.findById(created.id()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Information Technology Services");
        assertThat(updated.getCode()).isEqualTo("ITS");
        assertThat(updated.getManager().getId()).isEqualTo(supervisorUser1.getId());
    }

    @Test
    void updateDepartment_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Information Technology", "IT department", "IT", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        UpdateDepartmentRequest updateRequest = new UpdateDepartmentRequest(
                "Hacked Department", "Evil description", "HACK", adminUser2.getId()
        );

        // When/Then - User from organization2 trying to update department in organization1
        assertThatThrownBy(() -> departmentService.updateDepartment(
                created.id(), updateRequest, adminUser2
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");
    }

    @Test
    void deleteDepartment_ShouldDeleteSuccessfully_WhenNoEmployees() {
        // Given
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Empty Department", "No employees", "EMPTY", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        // When
        departmentService.deleteDepartment(created.id(), adminUser1);

        // Then
        entityManager.flush();
        entityManager.clear();
        assertThat(departmentRepository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteDepartment_ShouldThrowConflictException_WhenEmployeesExist() {
        // Given - Create department and assign employees
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Staffed Department", "Has employees", "STAFF", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        // Assign employees to department
        employee1.setDepartment(departmentRepository.findById(created.id()).get());
        employee2.setDepartment(departmentRepository.findById(created.id()).get());
        entityManager.merge(employee1);
        entityManager.merge(employee2);
        entityManager.flush();

        // When/Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(created.id(), adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete department with 2 employee(s). Please reassign employees first.");
    }

    @Test
    void updateEmployeeCount_ShouldSyncWithActualUserCount() {
        // Given - Create department
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Test Department", "Test description", "TEST", adminUser1.getId()
        );
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        // Assign employees to department
        employee1.setDepartment(departmentRepository.findById(created.id()).get());
        employee2.setDepartment(departmentRepository.findById(created.id()).get());
        entityManager.merge(employee1);
        entityManager.merge(employee2);
        entityManager.flush();

        // When
        departmentService.updateEmployeeCount(created.id());

        // Then
        Department updated = departmentRepository.findById(created.id()).get();
        assertThat(updated.getEmployeeCount()).isEqualTo(2);
    }

    @Test
    void departmentLifecycle_ShouldMaintainRelationshipsWithUsersAndOrganization() {
        // Given - Create complete department setup
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest(
                "Complete Department", "Full test department", "COMP", adminUser1.getId()
        );

        // When - Create department
        DepartmentDto created = departmentService.createDepartment(createRequest, adminUser1);

        // Assign employees
        Department department = departmentRepository.findById(created.id()).get();
        employee1.setDepartment(department);
        employee2.setDepartment(department);
        entityManager.merge(employee1);
        entityManager.merge(employee2);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify all relationships
        Department reloaded = departmentRepository.findById(created.id()).get();

        // Check organization relationship
        assertThat(reloaded.getOrganization()).isNotNull();
        assertThat(reloaded.getOrganization().getId()).isEqualTo(organization1.getId());

        // Check manager relationship
        assertThat(reloaded.getManager()).isNotNull();
        assertThat(reloaded.getManager().getId()).isEqualTo(adminUser1.getId());
        assertThat(reloaded.getManager().getName()).isEqualTo("Admin User 1");

        // Check employee relationships
        assertThat(reloaded.getEmployees()).hasSize(2);
        assertThat(reloaded.getEmployees()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("employee1@techsolutions.com", "employee2@techsolutions.com");

        // Check employee count sync
        departmentService.updateEmployeeCount(created.id());
        Department afterSync = departmentRepository.findById(created.id()).get();
        assertThat(afterSync.getEmployeeCount()).isEqualTo(2);
    }

    @Test
    void multiTenantSecurity_ShouldEnforceOrganizationBoundaries() {
        // Given - Create departments in both organizations
        CreateDepartmentRequest org1Request = new CreateDepartmentRequest(
                "Org1 Department", "Department in org1", "ORG1", adminUser1.getId()
        );
        CreateDepartmentRequest org2Request = new CreateDepartmentRequest(
                "Org2 Department", "Department in org2", "ORG2", adminUser2.getId()
        );

        DepartmentDto org1Dept = departmentService.createDepartment(org1Request, adminUser1);
        DepartmentDto org2Dept = departmentService.createDepartment(org2Request, adminUser2);

        // When/Then - Users should only access departments in their organization
        DepartmentDto org1Result = departmentService.findDepartmentById(org1Dept.id(), adminUser1);
        assertThat(org1Result).isNotNull();

        // Cross-organization access should fail
        assertThatThrownBy(() -> departmentService.findDepartmentById(org1Dept.id(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");

        assertThatThrownBy(() -> departmentService.findDepartmentById(org2Dept.id(), adminUser1))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");

        // Update and delete should also respect organization boundaries
        UpdateDepartmentRequest updateRequest = new UpdateDepartmentRequest(
                "Cross-org attack", "Malicious update", "HACK", adminUser2.getId()
        );

        assertThatThrownBy(() -> departmentService.updateDepartment(
                org1Dept.id(), updateRequest, adminUser2
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");

        assertThatThrownBy(() -> departmentService.deleteDepartment(org1Dept.id(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this department");
    }

    @Test
    void crossOrganizationManagerAssignment_ShouldBeBlocked() {
        // Given - Try to create department with manager from different organization
        CreateDepartmentRequest invalidManagerRequest = new CreateDepartmentRequest(
                "Invalid Manager Dept", "Should fail", "FAIL", adminUser2.getId() // Wrong org
        );

        // When/Then
        assertThatThrownBy(() -> departmentService.createDepartment(invalidManagerRequest, adminUser1))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User does not belong to your organization");
    }
}