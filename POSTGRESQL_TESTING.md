# PostgreSQL Testing Strategy - SafeWork API

This document outlines the comprehensive testing strategy for the PostgreSQL migration of the SafeWork API.

## Test Architecture

### 1. Test Profiles

#### H2 Profile (Default for Unit Tests)
- **Profile**: `test`
- **Database**: H2 in-memory
- **Purpose**: Fast unit tests, CI/CD compatibility
- **Usage**: `./mvnw test`

#### PostgreSQL Integration Profile
- **Profile**: `postgresql-test`
- **Database**: PostgreSQL (safeworkdb_test)
- **Purpose**: Real PostgreSQL integration testing
- **Usage**: `./mvnw test -Dspring.profiles.active=postgresql-test`

### 2. Test Categories

#### PostgreSQL-Specific Integration Tests
Located in `src/test/java/com/safework/api/postgresql/`

1. **AssetJsonbPostgreSQLIntegrationTest**
   - Tests all JSONB query methods in AssetRepository
   - Validates PostgreSQL JSONB operators (@>, ?, ?&, ?|, ->>, #>)
   - Tests multi-tenant data isolation
   - Performance testing with GIN indexes
   - **Coverage**: 102 assets, 12 different query types

2. **InspectionChecklistPostgreSQLIntegrationTest**
   - Tests JSONB functionality for Inspection and Checklist entities
   - Validates complex nested JSON queries
   - Tests array containment queries
   - Multi-tenant isolation for inspection data
   - **Coverage**: Complex nested JSON structures, compliance data

3. **DataSeederPostgreSQLIntegrationTest**
   - Validates DataSeeder works correctly with PostgreSQL
   - Tests idempotent behavior (multiple runs don't duplicate data)
   - Verifies all entity relationships are properly created
   - Validates JSONB data is populated correctly
   - **Coverage**: Full data model creation and relationships

## Test Data Patterns

### JSONB Test Data Examples

#### Asset Custom Attributes
```json
{
  "manufacturer": "PrintCorp",
  "model": "PC-3000", 
  "specifications": {
    "speed": "1000 pages/hour",
    "resolution": "600 DPI",
    "connectivity": ["USB", "Ethernet", "WiFi"]
  },
  "maintenance": {
    "lastService": "2024-01-15",
    "nextService": "2024-04-15",
    "serviceHistory": [
      {"date": "2024-01-15", "type": "preventive", "technician": "John Doe"},
      {"date": "2023-10-15", "type": "repair", "technician": "Jane Smith"}
    ]
  },
  "location": {
    "building": "Main Factory",
    "floor": 2,
    "room": "Production Floor A"
  }
}
```

#### Inspection Report Data
```json
{
  "overall_status": "PASS",
  "safety_checks": {
    "pressure_gauge": {
      "status": "NORMAL",
      "reading": "200 PSI",
      "notes": "Within acceptable range"
    },
    "pin_seal": {
      "status": "INTACT", 
      "condition": "GOOD"
    }
  },
  "deficiencies": [],
  "recommendations": [
    "Schedule next inspection in 6 months",
    "Consider relocating to more visible location"
  ],
  "compliance": {
    "nfpa_standard": "NFPA 10",
    "compliant": true,
    "certification_date": "2024-01-15"
  }
}
```

#### Checklist Items Data
```json
{
  "checklist_info": {
    "version": "1.2",
    "created_by": "Safety Department"
  },
  "inspection_items": [
    {
      "id": "item_001",
      "category": "Visual Inspection",
      "description": "Check pressure gauge reading",
      "type": "measurement",
      "required": true,
      "acceptable_range": "180-220 PSI"
    }
  ],
  "scoring": {
    "total_items": 3,
    "critical_items": 2,
    "pass_threshold": 90
  },
  "compliance_standards": ["NFPA 10", "OSHA 1910.157"]
}
```

## Query Testing Coverage

### JSONB Operators Tested

| Operator | Purpose | Test Coverage |
|----------|---------|---------------|
| `@>` | Contains JSON | ✅ findByCustomAttributesContains |
| `?` | Key exists | ✅ findByCustomAttributesContainsKey |
| `?&` | All keys exist | ✅ findByCustomAttributesContainsAllKeys |
| `?|` | Any key exists | ✅ findByCustomAttributesContainsAnyKeys |
| `->>` | Extract text value | ✅ findByNestedJsonValue |
| `#>` | Extract at path | ✅ Complex nested queries |
| `jsonb_set` | Update JSON | ✅ updateJsonAttribute |

### Multi-Tenant Security Testing

All PostgreSQL tests validate organization-based data isolation:
- Assets from different organizations never cross-contaminate
- Queries always filter by `organization_id`
- Repository methods enforce tenant boundaries
- No data leakage between organizations

### Performance Testing

Tests include performance validation:
- **GIN Index Effectiveness**: Queries with 100+ records complete in <1 second
- **Complex Query Performance**: Nested JSON queries remain fast
- **Bulk Operation Testing**: DataSeeder creates 20+ assets quickly

## Running PostgreSQL Tests

### Prerequisites
```bash
# 1. Ensure PostgreSQL is running
brew services start postgresql@16

# 2. Create test database
psql postgres -c "CREATE DATABASE safeworkdb_test;"
psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE safeworkdb_test TO safework_dev_user;"

# 3. Set environment variable
export DB_DEV_PASSWORD=Warhammer2000
```

### Test Execution

#### Individual Test Classes
```bash
# Asset JSONB functionality
./mvnw test -Dtest=AssetJsonbPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test

# DataSeeder validation  
./mvnw test -Dtest=DataSeederPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test

# Inspection/Checklist JSONB
./mvnw test -Dtest=InspectionChecklistPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test
```

#### Complete Test Suite
```bash
# Run all PostgreSQL tests
./mvnw test -Dtest="com.safework.api.postgresql.**" -Dspring.profiles.active=postgresql-test

# Run automated test script
./test-postgresql.sh
```

#### Compatibility Testing
```bash
# Ensure existing tests still pass
./mvnw test -Dspring.profiles.active=test
```

## Test Results Validation

### Success Criteria

✅ **JSONB Functionality**
- All 8 JSONB query methods work correctly
- Complex nested queries return expected results
- Array containment queries function properly
- JSON path extraction works as expected

✅ **Data Seeding**
- DataSeeder creates all entities (1 org, 4 users, 20 assets, etc.)
- JSONB data is properly populated
- Multiple runs are idempotent (no duplicates)
- All relationships are correctly established

✅ **Multi-Tenant Security**
- Organization isolation is maintained
- Cross-organization data access is prevented
- All queries properly filter by organization

✅ **Performance**
- GIN indexes improve query performance
- Large dataset queries complete quickly
- Application startup time remains reasonable

✅ **Compatibility**
- Existing H2-based tests continue to pass
- No regression in existing functionality
- All controller/service/repository tests work

## Continuous Integration

### Test Strategy for CI/CD

1. **Unit Tests**: Continue using H2 for speed
2. **Integration Tests**: Add PostgreSQL testing stage
3. **Performance Tests**: Benchmark JSONB query performance
4. **Data Migration Tests**: Validate DataSeeder functionality

### Docker Test Environment (Future)
```yaml
# Future enhancement: Docker Compose for CI
services:
  postgres-test:
    image: postgres:16
    environment:
      POSTGRES_DB: safeworkdb_test
      POSTGRES_USER: safework_test_user
      POSTGRES_PASSWORD: test_password
    ports:
      - "5433:5432"
```

## Monitoring and Metrics

### Query Performance Monitoring
- Track JSONB query execution times
- Monitor GIN index usage
- Alert on query performance degradation

### Data Integrity Checks
- Validate JSONB data structure consistency
- Monitor for data corruption
- Verify multi-tenant isolation integrity

## Troubleshooting

### Common Issues

1. **Connection Refused**
   ```bash
   # Start PostgreSQL
   brew services start postgresql@16
   ```

2. **Database Doesn't Exist**
   ```bash
   # Create test database
   psql postgres -c "CREATE DATABASE safeworkdb_test;"
   ```

3. **Permission Denied**
   ```bash
   # Grant privileges
   psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE safeworkdb_test TO safework_dev_user;"
   ```

4. **Environment Variable Not Set**
   ```bash
   # Set password
   export DB_DEV_PASSWORD=Warhammer2000
   ```

## Next Steps

1. **Automated Testing**: Integrate PostgreSQL tests into CI/CD pipeline
2. **Performance Benchmarking**: Establish baseline performance metrics
3. **Production Testing**: Validate on staging environment
4. **Monitoring Setup**: Implement PostgreSQL-specific monitoring
5. **Documentation**: Update team documentation for PostgreSQL usage

This comprehensive testing strategy ensures the PostgreSQL migration is robust, performant, and maintains all existing functionality while adding powerful new JSONB capabilities.