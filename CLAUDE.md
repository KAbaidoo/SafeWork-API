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
- ✅ **Fully Implemented**: Asset Management, Authentication
- 🔶 **Partially Implemented**: User, Organization (models + repositories only)
- 📋 **Models Only**: All other domains (Department, Location, Issue, Inspection, etc.)

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
**Entities**: Use `@Data` for all JPA entities (generates getters, setters, toString, equals, hashCode)
```java
@Data
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}
```

**Controllers/Services**: Use `@RequiredArgsConstructor` for dependency injection
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assets")
public class AssetController {
    private final AssetService assetService; // final fields injected by Lombok
}
```

**Audit Fields**: Use Hibernate annotations on entities
```java
@CreationTimestamp
private LocalDateTime createdAt;

@UpdateTimestamp  
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
- `@Data` annotation for all entities (generates boilerplate code)
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-incrementing IDs
- `@Enumerated(EnumType.STRING)` for enum fields (stores as strings, not ordinals)
- `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- Use `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn` for foreign keys
- Custom attributes stored as JSON via `@JdbcTypeCode(SqlTypes.JSON)`

## Testing Strategy

### Test Configuration
- Uses H2 in-memory database with MySQL compatibility mode
- Test profile automatically active during testing
- Flyway disabled for tests (`spring.flyway.enabled: false`)
- SQL logging enabled for debugging

### Test Types
- **Repository Tests**: `@DataJpaTest` for database layer testing
- **Integration Tests**: Full Spring context with `@SpringBootTest`
- **Test Data**: Use builders or factory methods, avoid hardcoded IDs

### Running Tests
```bash
# All tests
./mvnw test

# Specific test categories
./mvnw test -Dtest="*RepositoryTest"
./mvnw test -Dtest="*IntegrationTest"
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