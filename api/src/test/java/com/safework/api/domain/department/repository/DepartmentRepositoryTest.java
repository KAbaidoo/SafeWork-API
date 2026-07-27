package com.safework.api.domain.department.repository;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Organization organization1;
    private Organization organization2;
    private User managerUser;
    private User supervisorUser;
    private User employeeUser;
    private Department testDepartment1;
    private Department testDepartment2;

    @BeforeEach
    void setUp() {
        // Create test organizations
        organization1 = new Organization();
        organization1.setName("Tech Corp");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Manufacturing Inc");
        entityManager.persistAndFlush(organization2);

        // Create test users
        managerUser = new User();
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@techcorp.com");
        managerUser.setPassword("hashedPassword");
        managerUser.setRole(UserRole.ADMIN);
        managerUser.setOrganization(organization1);
        entityManager.persistAndFlush(managerUser);

        supervisorUser = new User();
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@techcorp.com");
        supervisorUser.setPassword("hashedPassword");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization1);
        entityManager.persistAndFlush(supervisorUser);

        employeeUser = new User();
        employeeUser.setName("Employee User");
        employeeUser.setEmail("employee@techcorp.com");
        employeeUser.setPassword("hashedPassword");
        employeeUser.setRole(UserRole.INSPECTOR);
        employeeUser.setOrganization(organization1);
        entityManager.persistAndFlush(employeeUser);

        // Create test departments
        testDepartment1 = new Department();
        testDepartment1.setName("Information Technology");
        testDepartment1.setDescription("IT department handling all technology needs");
        testDepartment1.setCode("IT");
        testDepartment1.setOrganization(organization1);
        testDepartment1.setManager(managerUser);
        testDepartment1.setEmployeeCount(3);
        entityManager.persistAndFlush(testDepartment1);

        testDepartment2 = new Department();
        testDepartment2.setName("Human Resources");
        testDepartment2.setDescription("HR department handling employee relations");
        testDepartment2.setCode("HR");
        testDepartment2.setOrganization(organization1);
        testDepartment2.setManager(supervisorUser);
        testDepartment2.setEmployeeCount(2);
        entityManager.persistAndFlush(testDepartment2);

        entityManager.clear();
    }

    @Test
    void findAllByOrganizationId_ShouldReturnPaginatedDepartments_WhenOrganizationExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Department> result = departmentRepository.findAllByOrganizationId(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Department::getName)
                .containsExactlyInAnyOrder("Information Technology", "Human Resources");
    }

    @Test
    void findAllByOrganizationId_ShouldReturnEmpty_WhenOrganizationHasNoDepartments() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Department> result = departmentRepository.findAllByOrganizationId(organization2.getId(), pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findAllByOrganizationId_ShouldRespectPagination() {
        // Given - Create additional departments
        Department dept3 = new Department();
        dept3.setName("Finance");
        dept3.setCode("FIN");
        dept3.setOrganization(organization1);
        dept3.setEmployeeCount(1);
        entityManager.persistAndFlush(dept3);

        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<Department> firstResult = departmentRepository.findAllByOrganizationId(organization1.getId(), firstPage);
        Page<Department> secondResult = departmentRepository.findAllByOrganizationId(organization1.getId(), secondPage);

        // Then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(firstResult.getTotalElements()).isEqualTo(3);
        assertThat(firstResult.hasNext()).isTrue();

        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(secondResult.getTotalElements()).isEqualTo(3);
        assertThat(secondResult.hasNext()).isFalse();
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnDepartment_WhenExists() {
        // When
        Optional<Department> result = departmentRepository.findByOrganizationIdAndName(
                organization1.getId(), "Information Technology");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Information Technology");
        assertThat(result.get().getCode()).isEqualTo("IT");
        assertThat(result.get().getDescription()).isEqualTo("IT department handling all technology needs");
        assertThat(result.get().getEmployeeCount()).isEqualTo(3);
        assertThat(result.get().getManager()).isEqualTo(managerUser);
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnEmpty_WhenNotFound() {
        // When
        Optional<Department> result = departmentRepository.findByOrganizationIdAndName(
                organization1.getId(), "Non-existent Department");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnEmpty_WhenDifferentOrganization() {
        // When - Looking for IT department in organization2
        Optional<Department> result = departmentRepository.findByOrganizationIdAndName(
                organization2.getId(), "Information Technology");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrganizationIdAndCode_ShouldReturnDepartment_WhenExists() {
        // When
        Optional<Department> result = departmentRepository.findByOrganizationIdAndCode(
                organization1.getId(), "HR");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Human Resources");
        assertThat(result.get().getCode()).isEqualTo("HR");
        assertThat(result.get().getManager()).isEqualTo(supervisorUser);
    }

    @Test
    void findByOrganizationIdAndCode_ShouldReturnEmpty_WhenNotFound() {
        // When
        Optional<Department> result = departmentRepository.findByOrganizationIdAndCode(
                organization1.getId(), "NONEXIST");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrganizationIdAndCode_ShouldReturnEmpty_WhenDifferentOrganization() {
        // When
        Optional<Department> result = departmentRepository.findByOrganizationIdAndCode(
                organization2.getId(), "IT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnTrue_WhenExists() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndName(
                organization1.getId(), "Information Technology");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndName(
                organization1.getId(), "Non-existent Department");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnFalse_WhenDifferentOrganization() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndName(
                organization2.getId(), "Information Technology");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByOrganizationIdAndCode_ShouldReturnTrue_WhenExists() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndCode(
                organization1.getId(), "IT");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByOrganizationIdAndCode_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndCode(
                organization1.getId(), "NONEXIST");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByOrganizationIdAndCode_ShouldReturnFalse_WhenDifferentOrganization() {
        // When
        boolean result = departmentRepository.existsByOrganizationIdAndCode(
                organization2.getId(), "IT");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void save_ShouldPersistDepartment_WithAllFields() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setName("Research & Development");
        newDepartment.setDescription("R&D department for innovation");
        newDepartment.setCode("RND");
        newDepartment.setOrganization(organization1);
        newDepartment.setManager(supervisorUser);
        newDepartment.setEmployeeCount(5);

        // When
        Department savedDepartment = departmentRepository.save(newDepartment);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedDepartment.getId()).isNotNull();
        assertThat(savedDepartment.getCreatedAt()).isNotNull();
        assertThat(savedDepartment.getUpdatedAt()).isNotNull();

        // Verify it can be found
        Optional<Department> foundDepartment = departmentRepository.findById(savedDepartment.getId());
        assertThat(foundDepartment).isPresent();
        assertThat(foundDepartment.get().getName()).isEqualTo("Research & Development");
        assertThat(foundDepartment.get().getDescription()).isEqualTo("R&D department for innovation");
        assertThat(foundDepartment.get().getCode()).isEqualTo("RND");
        assertThat(foundDepartment.get().getEmployeeCount()).isEqualTo(5);
        assertThat(foundDepartment.get().getManager().getId()).isEqualTo(supervisorUser.getId());
        assertThat(foundDepartment.get().getOrganization().getId()).isEqualTo(organization1.getId());
    }

    @Test
    void save_ShouldPersistDepartment_WithMinimalFields() {
        // Given
        Department minimalDepartment = new Department();
        minimalDepartment.setName("Minimal Department");
        minimalDepartment.setOrganization(organization1);
        minimalDepartment.setEmployeeCount(0);

        // When
        Department savedDepartment = departmentRepository.save(minimalDepartment);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Department> foundDepartment = departmentRepository.findById(savedDepartment.getId());
        assertThat(foundDepartment).isPresent();
        assertThat(foundDepartment.get().getName()).isEqualTo("Minimal Department");
        assertThat(foundDepartment.get().getDescription()).isNull();
        assertThat(foundDepartment.get().getCode()).isNull();
        assertThat(foundDepartment.get().getManager()).isNull();
        assertThat(foundDepartment.get().getEmployeeCount()).isEqualTo(0);
    }

    @Test
    void save_ShouldThrowException_WhenNameIsNull() {
        // Given
        Department invalidDepartment = new Department();
        invalidDepartment.setOrganization(organization1);

        // When/Then
        assertThatThrownBy(() -> {
            departmentRepository.save(invalidDepartment);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenOrganizationIsNull() {
        // Given
        Department invalidDepartment = new Department();
        invalidDepartment.setName("Invalid Department");

        // When/Then
        assertThatThrownBy(() -> {
            departmentRepository.save(invalidDepartment);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateNameInSameOrganization() {
        // Given
        Department duplicateDepartment = new Department();
        duplicateDepartment.setName("Information Technology"); // Same as testDepartment1
        duplicateDepartment.setOrganization(organization1);

        // When/Then
        assertThatThrownBy(() -> {
            departmentRepository.save(duplicateDepartment);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldAllowSameName_InDifferentOrganizations() {
        // Given
        Department sameName = new Department();
        sameName.setName("Information Technology"); // Same name as in org1
        sameName.setCode("IT2"); // Different code
        sameName.setOrganization(organization2); // Different organization
        sameName.setEmployeeCount(0);

        // When
        Department savedDepartment = departmentRepository.save(sameName);
        entityManager.flush();

        // Then
        assertThat(savedDepartment.getId()).isNotNull();
        assertThat(savedDepartment.getName()).isEqualTo("Information Technology");
    }

    @Test
    void departmentWithEmployees_ShouldMaintainRelationships() {
        // Given - Assign employee to department
        employeeUser.setDepartment(testDepartment1);
        entityManager.merge(employeeUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Department loadedDepartment = departmentRepository.findById(testDepartment1.getId()).get();

        // Then
        assertThat(loadedDepartment.getEmployees()).hasSize(1);
        assertThat(loadedDepartment.getEmployees().get(0).getEmail()).isEqualTo("employee@techcorp.com");
    }

    @Test
    void departmentWithManager_ShouldMaintainManagerRelationship() {
        // When
        Department loadedDepartment = departmentRepository.findById(testDepartment1.getId()).get();

        // Then
        assertThat(loadedDepartment.getManager()).isNotNull();
        assertThat(loadedDepartment.getManager().getName()).isEqualTo("Manager User");
        assertThat(loadedDepartment.getManager().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void update_ShouldUpdateTimestamp() throws InterruptedException {
        // Given
        Department department = departmentRepository.findById(testDepartment1.getId()).get();
        var originalUpdatedAt = department.getUpdatedAt();

        Thread.sleep(100); // Ensure time difference

        // When
        department.setDescription("Updated description");
        Department updatedDepartment = departmentRepository.save(department);
        entityManager.flush();

        // Then
        assertThat(updatedDepartment.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedDepartment.getCreatedAt()).isEqualTo(department.getCreatedAt());
    }

    @Test
    void delete_ShouldRemoveDepartment() {
        // Given
        Long departmentId = testDepartment1.getId();
        assertThat(departmentRepository.findById(departmentId)).isPresent();

        // When
        departmentRepository.delete(testDepartment1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(departmentRepository.findById(departmentId)).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllDepartments() {
        // When
        List<Department> departments = departmentRepository.findAll();

        // Then
        assertThat(departments).hasSize(2);
        assertThat(departments).extracting(Department::getName)
                .containsExactlyInAnyOrder("Information Technology", "Human Resources");
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = departmentRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}