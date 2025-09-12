# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Maven Commands
```bash
# Build and run all tests
./mvnw clean test

# Run the application (dev profile active by default)
./mvnw spring-boot:run

# Build JAR file
./mvnw clean package

# Run specific test class
./mvnw test -Dtest=AssetRepositoryTest

# Run specific test method
./mvnw test -Dtest=AssetRepositoryTest#shouldCreateAssetWithRelationships
```

### Database Setup
MariaDB is required for development:
```bash
# Required environment variables
export DB_PASSWORD=your_dev_password
export DB_DEV_PASSWORD=your_dev_password
export JWT_SECRET=your_jwt_secret_minimum_256_bits
```

## Architecture Overview

**SafeWork API** is a Spring Boot 3.5.4 application implementing workplace safety asset management with:

- **Domain-Driven Design**: Each business domain (asset, auth, user, etc.) has its own package structure
- **Multi-Tenant Architecture**: Organization-based data isolation using `@AuthenticationPrincipal User`
- **JWT Authentication**: Role-based access control with `@PreAuthorize` annotations
- **API Versioning**: All endpoints prefixed with `/api/v1/`

### Implementation Status
- ✅ **Fully Implemented**: Asset Management, Authentication, Organization, Department
- 🔶 **Partially Implemented**: User (missing integration tests)
- 📋 **Models Only**: All other domains (Location, Issue, Inspection, etc.)

### Test Coverage Status
**Complete Test Suites** (Repository, Service, Controller, Integration, Mapper):
- ✅ **Asset Domain**: 5 test classes with comprehensive coverage
- ✅ **Organization Domain**: 5 test classes (117 total tests)
- ✅ **Department Domain**: 5 test classes (117 total tests)

**Partial Test Coverage**:
- 🔶 **User Domain**: Repository, Service, Controller, Mapper tests (missing integration tests)

### Key Domain Relationships
```
Organization → Department → User (with UserRole)
Organization → Location → Asset (with AssetType, AssetStatus)
Asset → Maintenance, Inspection, Issue (tracking relationships)
```

## Authentication & Security

### JWT Token Flow
1. Login via `POST /api/v1/auth/login` returns JWT token
2. Include token in requests: `Authorization: Bearer <token>`
3. Current user injected via `@AuthenticationPrincipal User currentUser`
4. Organization-scoped data access enforced in service layer

### Security Configuration
- JWT tokens expire in 24 hours (configurable via `safework.jwt.expiration-ms`)
- Role-based endpoints using `@PreAuthorize("hasAuthority('ADMIN')")`
- CORS enabled for cross-origin requests
- Custom authentication entry point handles unauthorized access

## Development Patterns

### Domain Package Structure
Each domain follows this pattern:
```
domain/{domain-name}/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── model/          # JPA entities
├── dto/           # Data transfer objects
└── mapper/        # Entity↔DTO conversion (where needed)
```

### Lombok Conventions

**Entities**: Use selective Lombok annotations instead of `@Data` to follow JPA best practices
```java
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"organization", "assetType", "department"}) // Exclude lazy associations
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)           // Prevent accidental ID modification
    @EqualsAndHashCode.Include          // Only use ID for equality/hashCode
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;   // Excluded from toString to prevent lazy loading issues
    
    // ... other fields
}
```

**Why not @Data for entities?**
- `@Data` includes ALL fields in `equals/hashCode`, causing issues when entities are modified in HashSets/HashMaps
- `@Data` includes lazy associations in `toString()`, causing `LazyInitializationException` when accessed outside Hibernate sessions
- `@Data` generates setters for all fields, including IDs and audit fields that shouldn't be modified

**Controllers/Services**: Use `@RequiredArgsConstructor` for dependency injection
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assets")
public class AssetController {
    private final AssetService assetService; // final fields injected by Lombok
}
```

**Audit Fields**: Use Hibernate annotations with protected setters
```java
@CreationTimestamp
@Column(nullable = false, updatable = false)
@Setter(AccessLevel.NONE)           // Prevent manual modification
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(nullable = false)
@Setter(AccessLevel.NONE)           // Prevent manual modification
private LocalDateTime updatedAt;
```

### Java Records for DTOs
**All DTOs use Java records** for immutability and conciseness:

**Request DTOs** (with validation):
```java
public record CreateAssetRequest(
    @NotBlank(message = "Asset tag is required")
    @Size(min = 3, max = 50, message = "Asset tag must be between 3 and 50 characters")
    String assetTag,
    
    @NotNull(message = "Asset type ID is required")
    Long assetTypeId
) {}
```

**Response DTOs** (simple records):
```java
public record AssetDto(
    Long id,
    String assetTag,
    String name,
    String status,
    int version
) {}
```

**Error Responses** (also records):
```java
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
```

### Validation Annotations
Use Jakarta validation with meaningful error messages:
- `@NotBlank(message = "Field is required")` - for strings that cannot be null or empty
- `@NotNull(message = "Field is required")` - for objects/IDs that cannot be null  
- `@Email(message = "Email should be a valid email address")` - for email validation
- `@Size(min = 3, max = 50, message = "Must be between 3 and 50 characters")` - for string length

**Important**: Add `@Valid` annotation to controller method parameters for validation:
```java
@PostMapping
public ResponseEntity<AssetDto> createAsset(@Valid @RequestBody CreateAssetRequest request) {
    // validation automatically applied
}
```

### Controller Conventions
- Use `@RequestMapping("/v1/{resource}")` for versioning
- Inject current user via `@AuthenticationPrincipal User currentUser`
- Apply organization-based filtering in service layer
- Return appropriate HTTP status codes (201 for creation, etc.)
- Use `@RequiredArgsConstructor` for dependency injection

### Service Layer Patterns
- Enforce organization-based data isolation
- Validate user permissions before operations
- Use repository layer for data access only
- Throw domain-specific exceptions (ResourceNotFoundException, ConflictException)
- Use `@RequiredArgsConstructor` for dependency injection

### Entity Conventions
- **Selective Lombok annotations**: Use `@Getter`, `@Setter`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`, `@ToString(exclude = {...})` instead of `@Data`
- **ID Protection**: Use `@Setter(AccessLevel.NONE)` and `@EqualsAndHashCode.Include` on ID fields
- **Audit Field Protection**: Use `@Setter(AccessLevel.NONE)` on `@CreationTimestamp` and `@UpdateTimestamp` fields
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-incrementing IDs
- `@Enumerated(EnumType.STRING)` for enum fields (stores as strings, not ordinals)
- Use `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn` for foreign keys
- **toString Safety**: Exclude lazy-loaded associations to prevent `LazyInitializationException`
- Custom attributes stored as JSON via `@JdbcTypeCode(SqlTypes.JSON)`

## Testing Strategy

### Test Configuration
- Uses H2 in-memory database with MySQL compatibility mode
- Test profile automatically active during testing
- Flyway disabled for tests (`spring.flyway.enabled: false`)
- SQL logging enabled for debugging

### Test Architecture Pattern

Each fully implemented domain follows a **5-layer test structure**:

1. **Repository Tests** (`@DataJpaTest`)
   - Database layer testing with TestEntityManager
   - Custom query method validation
   - Constraint and relationship testing
   - ~25-30 tests per domain

2. **Service Tests** (`@ExtendWith(MockitoExtension.class)`)
   - Business logic testing with mocked dependencies
   - Multi-tenant security validation
   - Error scenario coverage
   - ~20-25 tests per domain

3. **Controller Tests** (`@WebMvcTest`)
   - REST endpoint testing with MockMvc
   - Security integration (`@WithMockUser`)
   - Request/response validation
   - ~25-30 tests per domain

4. **Integration Tests** (`@SpringBootTest`)
   - End-to-end testing with full Spring context
   - Real database transactions
   - Complete workflow validation
   - ~15-20 tests per domain

5. **Mapper Tests** (Plain JUnit)
   - DTO conversion testing
   - Null handling and edge cases
   - Field mapping consistency
   - ~15-20 tests per domain

### Testing Patterns and Conventions

**Protected Field Testing**:
```java
// Helper method for setting @Setter(AccessLevel.NONE) fields
private void setEntityId(Object entity, Long id) {
    try {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    } catch (Exception e) {
        throw new RuntimeException("Failed to set entity ID", e);
    }
}
```

**Multi-Tenant Security Testing**:
- Always test cross-organization data isolation
- Validate that users can only access their organization's data
- Test role-based permissions (ADMIN, SUPERVISOR, INSPECTOR)

**Comprehensive Coverage Goals**:
- **Happy path scenarios**: Standard CRUD operations
- **Edge cases**: Empty strings, null values, boundary conditions  
- **Error scenarios**: Constraint violations, not found, access denied
- **Security scenarios**: Cross-organization access, role permissions

### Running Tests
```bash
# All tests
./mvnw test

# Specific test categories
./mvnw test -Dtest="*RepositoryTest"
./mvnw test -Dtest="*ServiceTest"
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*IntegrationTest"
./mvnw test -Dtest="*MapperTest"

# Domain-specific tests
./mvnw test -Dtest="com.safework.api.domain.organization.**"
./mvnw test -Dtest="com.safework.api.domain.department.**"
./mvnw test -Dtest="com.safework.api.domain.asset.**"

# Specific test methods
./mvnw test -Dtest=DepartmentServiceTest#shouldCreateDepartment_WhenValidRequest
```

### Test Utilities and Helpers

**Reflection Utilities** (for entities with protected setters):
```java
// Set entity IDs for testing
setEntityId(organization, 1L);

// Set audit timestamps for testing
setTimestamp(department, "createdAt", LocalDateTime.now());
setTimestamp(department, "updatedAt", LocalDateTime.now());
```

**Security Test Setup**:
```java
@WithMockUser(username = "admin@testorg.com", authorities = {"ADMIN"})
void testAdminEndpoint() {
    // Test admin-specific functionality
}
```

**Pagination Testing**:
```java
// Test pagination boundaries and content
Page<Department> result = repository.findAllByOrganizationId(orgId, PageRequest.of(0, 5));
assertThat(result.getContent()).hasSize(5);
assertThat(result.getTotalElements()).isEqualTo(expectedTotal);
```

## Configuration Profiles

### Development (`dev` profile)
- Port: 8081
- Database: MariaDB with `safeworkdb_dev`
- Hibernate DDL: `update` (adds new schema automatically)
- SQL logging enabled with formatting

### Test Profile
- In-memory H2 database
- Hibernate DDL: `create-drop` (clean slate per test)
- Reduced logging to minimize noise

### Environment Variables
```bash
# Required for all profiles
JWT_SECRET=your-secret-key-minimum-256-bits

# Development
DB_DEV_PASSWORD=your-dev-database-password

# Production (when implemented)
DB_PASSWORD=your-production-database-password
```

## Data Seeding

The `DataSeeder` component (runs only in `dev` profile) creates:
- Default organization (Apex Global Logistics)  
- Test users with different roles (ADMIN, SUPERVISOR, USER)
- Sample asset types and assets
- Runs idempotently (checks if data exists before seeding)

## Key Technical Notes

- **API Context Path**: All endpoints prefixed with `/api` (configurable in application.yml)
- **JSON Configuration**: Non-null serialization, ISO date formatting
- **Database**: Uses MariaDB dialect with Hypersistence Utils for JSON support
- **Security**: BCrypt password encoding, JWT token validation filter
- **Validation**: Bean validation with `@Valid` annotations on request DTOs