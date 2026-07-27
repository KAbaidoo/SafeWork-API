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
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private Organization organization;
    private Organization otherOrganization;
    private OrganizationDto organizationDto;
    private OrganizationSummaryDto organizationSummaryDto;
    private CreateOrganizationRequest createRequest;
    private UpdateOrganizationRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Test Organization");
        organization.setAddress("123 Test Street");
        organization.setPhone("+1-555-0123");
        organization.setWebsite("https://testorg.com");
        organization.setIndustry("Technology");
        organization.setSize(OrganizationSize.MEDIUM);

        otherOrganization = new Organization();
        otherOrganization = createMockOrganization(2L, "Other Organization");

        adminUser = new User();
        setEntityId(adminUser, 1L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@testorg.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);

        supervisorUser = new User();
        setEntityId(supervisorUser, 2L);
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@testorg.com");
        supervisorUser.setPassword("hashedPassword");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);

        inspectorUser = new User();
        setEntityId(inspectorUser, 3L);
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@testorg.com");
        inspectorUser.setPassword("hashedPassword");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);

        organizationDto = new OrganizationDto(
                1L, "Test Organization", "123 Test Street", "+1-555-0123",
                "https://testorg.com", "Technology", "MEDIUM",
                LocalDateTime.now(), LocalDateTime.now()
        );

        organizationSummaryDto = new OrganizationSummaryDto(
                1L, "Test Organization", "Technology", "MEDIUM"
        );

        createRequest = new CreateOrganizationRequest(
                "New Organization",
                "456 New Street",
                "+1-555-0456",
                "https://neworg.com",
                "Manufacturing",
                "LARGE"
        );

        updateRequest = new UpdateOrganizationRequest(
                "Updated Organization",
                "789 Updated Street",
                "+1-555-0789",
                "https://updatedorg.com",
                "Healthcare",
                "ENTERPRISE"
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
    void createOrganization_ShouldCreateOrganization_WhenValidRequest() {
        // Given
        Organization savedOrganization = new Organization();
        savedOrganization.setName("New Organization");
        savedOrganization.setAddress("456 New Street");
        savedOrganization.setPhone("+1-555-0456");
        savedOrganization.setWebsite("https://neworg.com");
        savedOrganization.setIndustry("Manufacturing");
        savedOrganization.setSize(OrganizationSize.LARGE);

        given(organizationRepository.findByName("New Organization")).willReturn(Optional.empty());
        given(organizationRepository.save(any(Organization.class))).willReturn(savedOrganization);
        given(organizationMapper.toDto(savedOrganization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.createOrganization(createRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);

        then(organizationRepository).should().findByName("New Organization");
        then(organizationRepository).should().save(any(Organization.class));
        then(organizationMapper).should().toDto(savedOrganization);
    }

    @Test
    void createOrganization_ShouldThrowConflictException_WhenNameAlreadyExists() {
        // Given
        given(organizationRepository.findByName("New Organization")).willReturn(Optional.of(organization));

        // When/Then
        assertThatThrownBy(() -> organizationService.createOrganization(createRequest, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Organization with name 'New Organization' already exists");

        then(organizationRepository).should(never()).save(any(Organization.class));
        then(organizationMapper).should(never()).toDto(any(Organization.class));
    }

    @Test
    void createOrganization_ShouldThrowIllegalArgumentException_WhenInvalidSize() {
        // Given
        CreateOrganizationRequest invalidSizeRequest = new CreateOrganizationRequest(
                "New Organization", "456 New Street", "+1-555-0456",
                "https://neworg.com", "Manufacturing", "INVALID_SIZE"
        );

        given(organizationRepository.findByName("New Organization")).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> organizationService.createOrganization(invalidSizeRequest, adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid organization size: INVALID_SIZE");

        then(organizationRepository).should(never()).save(any(Organization.class));
    }

    @Test
    void createOrganization_ShouldCreateWithoutSize_WhenSizeIsNull() {
        // Given
        CreateOrganizationRequest requestWithoutSize = new CreateOrganizationRequest(
                "New Organization", "456 New Street", "+1-555-0456",
                "https://neworg.com", "Manufacturing", null
        );

        Organization savedOrganization = new Organization();
        savedOrganization.setName("New Organization");

        given(organizationRepository.findByName("New Organization")).willReturn(Optional.empty());
        given(organizationRepository.save(any(Organization.class))).willReturn(savedOrganization);
        given(organizationMapper.toDto(savedOrganization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.createOrganization(requestWithoutSize, adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);
        then(organizationRepository).should().save(any(Organization.class));
    }

    @Test
    void findAllOrganizations_ShouldReturnPagedOrganizations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Organization> organizations = List.of(organization);
        Page<Organization> organizationPage = new PageImpl<>(organizations, pageable, 1);

        given(organizationRepository.findAll(pageable)).willReturn(organizationPage);
        given(organizationMapper.toSummaryDto(organization)).willReturn(organizationSummaryDto);

        // When
        Page<OrganizationSummaryDto> result = organizationService.findAllOrganizations(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(organizationSummaryDto);
        assertThat(result.getTotalElements()).isEqualTo(1);

        then(organizationRepository).should().findAll(pageable);
        then(organizationMapper).should().toSummaryDto(organization);
    }

    @Test
    void findOrganizationById_ShouldReturnOrganization_WhenExists() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));
        given(organizationMapper.toDto(organization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.findOrganizationById(1L);

        // Then
        assertThat(result).isEqualTo(organizationDto);

        then(organizationRepository).should().findById(1L);
        then(organizationMapper).should().toDto(organization);
    }

    @Test
    void findOrganizationById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> organizationService.findOrganizationById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organization not found with id: 1");

        then(organizationMapper).should(never()).toDto(any(Organization.class));
    }

    @Test
    void getCurrentUserOrganization_ShouldReturnUserOrganization() {
        // Given
        given(organizationMapper.toDto(organization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.getCurrentUserOrganization(adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);

        then(organizationMapper).should().toDto(organization);
    }

    @Test
    void updateOrganization_ShouldUpdateOrganization_WhenValidAdminRequest() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));
        given(organizationRepository.findByName("Updated Organization")).willReturn(Optional.empty());
        given(organizationRepository.save(organization)).willReturn(organization);
        given(organizationMapper.toDto(organization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.updateOrganization(1L, updateRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);
        assertThat(organization.getName()).isEqualTo("Updated Organization");
        assertThat(organization.getAddress()).isEqualTo("789 Updated Street");
        assertThat(organization.getPhone()).isEqualTo("+1-555-0789");
        assertThat(organization.getWebsite()).isEqualTo("https://updatedorg.com");
        assertThat(organization.getIndustry()).isEqualTo("Healthcare");
        assertThat(organization.getSize()).isEqualTo(OrganizationSize.ENTERPRISE);

        then(organizationRepository).should().findById(1L);
        then(organizationRepository).should().save(organization);
        then(organizationMapper).should().toDto(organization);
    }

    @Test
    void updateOrganization_ShouldThrowResourceNotFoundException_WhenOrganizationNotFound() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> organizationService.updateOrganization(1L, updateRequest, adminUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organization not found with id: 1");

        then(organizationRepository).should(never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given - organization with ID 2, but user belongs to organization with ID 1
        Organization differentOrg = createMockOrganization(2L, "Different Organization");
        given(organizationRepository.findById(1L)).willReturn(Optional.of(differentOrg));

        // When/Then
        assertThatThrownBy(() -> organizationService.updateOrganization(1L, updateRequest, adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this organization");

        then(organizationRepository).should(never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowAccessDeniedException_WhenUserIsNotAdmin() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));

        // When/Then
        assertThatThrownBy(() -> organizationService.updateOrganization(1L, updateRequest, supervisorUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only administrators can modify organization details");

        then(organizationRepository).should(never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowConflictException_WhenNewNameAlreadyExists() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));
        given(organizationRepository.findByName("Updated Organization")).willReturn(Optional.of(otherOrganization));

        // When/Then
        assertThatThrownBy(() -> organizationService.updateOrganization(1L, updateRequest, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Organization with name 'Updated Organization' already exists");

        then(organizationRepository).should(never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldNotCheckNameConflict_WhenNameUnchanged() {
        // Given
        UpdateOrganizationRequest sameNameRequest = new UpdateOrganizationRequest(
                "Test Organization", "789 Updated Street", "+1-555-0789",
                "https://updatedorg.com", "Healthcare", "ENTERPRISE"
        );

        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));
        given(organizationRepository.save(organization)).willReturn(organization);
        given(organizationMapper.toDto(organization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.updateOrganization(1L, sameNameRequest, adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);

        then(organizationRepository).should(never()).findByName("Test Organization");
        then(organizationRepository).should().save(organization);
    }

    @Test
    void updateOrganization_ShouldClearSize_WhenSizeIsNull() {
        // Given
        UpdateOrganizationRequest requestWithNullSize = new UpdateOrganizationRequest(
                "Updated Organization", "789 Updated Street", "+1-555-0789",
                "https://updatedorg.com", "Healthcare", null
        );

        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));
        given(organizationRepository.findByName("Updated Organization")).willReturn(Optional.empty());
        given(organizationRepository.save(organization)).willReturn(organization);
        given(organizationMapper.toDto(organization)).willReturn(organizationDto);

        // When
        OrganizationDto result = organizationService.updateOrganization(1L, requestWithNullSize, adminUser);

        // Then
        assertThat(result).isEqualTo(organizationDto);
        assertThat(organization.getSize()).isNull();

        then(organizationRepository).should().save(organization);
    }

    @Test
    void deleteOrganization_ShouldDeleteOrganization_WhenValidRequest() {
        // Given
        organization.setUsers(List.of(adminUser)); // Only one user
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));

        // When
        organizationService.deleteOrganization(1L, adminUser);

        // Then
        then(organizationRepository).should().findById(1L);
        then(organizationRepository).should().delete(organization);
    }

    @Test
    void deleteOrganization_ShouldDeleteOrganization_WhenNoUsers() {
        // Given
        organization.setUsers(List.of()); // No users
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));

        // When
        organizationService.deleteOrganization(1L, adminUser);

        // Then
        then(organizationRepository).should().delete(organization);
    }

    @Test
    void deleteOrganization_ShouldThrowConflictException_WhenMultipleUsers() {
        // Given
        organization.setUsers(List.of(adminUser, supervisorUser)); // Multiple users
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));

        // When/Then
        assertThatThrownBy(() -> organizationService.deleteOrganization(1L, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete organization with multiple users. Please remove all other users first.");

        then(organizationRepository).should(never()).delete(any(Organization.class));
    }

    @Test
    void deleteOrganization_ShouldThrowAccessDeniedException_WhenUserIsNotAdmin() {
        // Given
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization));

        // When/Then
        assertThatThrownBy(() -> organizationService.deleteOrganization(1L, inspectorUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only administrators can modify organization details");

        then(organizationRepository).should(never()).delete(any(Organization.class));
    }
}