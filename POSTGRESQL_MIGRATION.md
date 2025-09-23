# PostgreSQL Design Implementation for SafeWork API (No Docker)

## Prerequisites
- Install PostgreSQL 16+ on macOS via Homebrew or Postgres.app
- Working on feature branch for clean implementation

## Phase 1: PostgreSQL Setup & Configuration

### 1.1 Install PostgreSQL on macOS
```bash
# Option A: Homebrew
brew install postgresql@16
brew services start postgresql@16

# Option B: Download Postgres.app from postgresapp.com
```

### 1.2 Create Database and User
```sql
psql postgres
CREATE DATABASE safeworkdb;
CREATE DATABASE safeworkdb_dev;
CREATE USER safework_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE safeworkdb TO safework_user;
GRANT ALL PRIVILEGES ON DATABASE safeworkdb_dev TO safework_user;
\q
```

### 1.3 Update Maven Dependencies (pom.xml)
- Replace `mariadb-java-client` with `postgresql` driver
- Keep existing Hypersistence Utils (supports PostgreSQL)

### 1.4 Create PostgreSQL Configuration
- Create `application-postgres.yml` for PostgreSQL profile
- Configure JSONB support and PostgreSQL dialect

## Phase 2: PostgreSQL Schema Design

### 2.1 Create Flyway Migration Structure
```
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_jsonb_columns.sql  
├── V3__add_indexes.sql
└── V4__add_constraints.sql
```

### 2.2 Schema Differences to Handle
- Change `json` columns to `jsonb` type
- Add GIN indexes for JSONB columns
- Update auto-increment to PostgreSQL sequences

### 2.3 Entity Updates
- Update `@JdbcTypeCode` annotations for PostgreSQL JSONB
- Ensure dialect compatibility

## Phase 3: Code Updates

### 3.1 Configuration Files
- `application-postgres.yml` - New PostgreSQL profile
- `application-dev.yml` - Add profile toggle
- Keep `application-test.yml` with H2 for tests

### 3.2 Repository Enhancements
- Add JSONB query methods
- Native queries for complex JSON operations
- Full-text search capabilities

### 3.3 Service Layer
- No changes needed (abstracted via repositories)

## Phase 4: Data Setup (Development)

### 4.1 No Data Migration Needed
Since the project uses `DataSeeder` for test data generation:
- No export/import required from MariaDB
- PostgreSQL database will start empty
- `DataSeeder` will populate fresh data automatically

### 4.2 DataSeeder Verification
```bash
# Start application with PostgreSQL profile
./mvnw spring-boot:run -Dspring.profiles.active=postgres,dev

# DataSeeder runs automatically and creates:
# - Default organization (Apex Global Logistics)
# - Test users with different roles
# - Sample asset types and assets
# - All relationships and JSON data
```

### 4.3 Validate Generated Data
- Verify JSONB column creation
- Test JSON queries on seeded data
- Confirm all relationships work correctly

## Phase 5: Testing Strategy

### 5.1 Keep Existing Test Setup
- H2 for unit tests (no changes)
- Add PostgreSQL integration tests

### 5.2 PostgreSQL-Focused Testing
- Test all functionality with PostgreSQL
- Validate JSONB query performance
- Ensure DataSeeder works correctly

## Phase 6: Deployment

### 6.1 Development Environment
```bash
# Start with PostgreSQL profile
./mvnw spring-boot:run -Dspring.profiles.active=postgres,dev
```

### 6.2 Fresh PostgreSQL Implementation
1. Complete PostgreSQL setup with DataSeeder
2. Verify all functionality works correctly
3. Commit PostgreSQL branch when stable
4. Merge to main branch (replaces MariaDB design)
5. Deploy PostgreSQL as the primary database

## Key Benefits of PostgreSQL Design

1. **JSONB Performance**: 10-100x faster queries with GIN indexes
2. **Advanced Queries**: JSON path expressions, operators (->>, @>, ?)
3. **Better Analytics**: Window functions, CTEs, advanced aggregations
4. **Full-Text Search**: Built-in for inspection reports
5. **Future Features**: PostGIS for locations, pg_cron for scheduling

## Files to Create/Modify

### New Files
- `src/main/resources/application-postgres.yml`
- `src/main/resources/db/migration/V*.sql`
- `POSTGRESQL_MIGRATION.md` (this document)

### Modified Files
- `pom.xml` - Dependencies
- `application.yml` - Profile management
- `application-dev.yml` - Database toggle
- Entity classes - JSONB annotations

## Environment Variables
```bash
# PostgreSQL (primary database)
export POSTGRES_DB_PASSWORD=your_postgres_password
export POSTGRES_DB_HOST=localhost
export POSTGRES_DB_PORT=5432

# Keep JWT secret
export JWT_SECRET=your_jwt_secret_minimum_256_bits
```

## Branch Strategy
- Feature branch: `feature/migrate-to-postgresql`
- Safe to experiment without affecting main
- Drop branch if implementation fails
- Merge when PostgreSQL design is complete

## Implementation Checklist

- [ ] Install PostgreSQL locally
- [ ] Create databases and users
- [ ] Update pom.xml dependencies
- [ ] Create application-postgres.yml
- [ ] Create Flyway migration scripts
- [ ] Update entity JSON mappings
- [ ] Add PostgreSQL repository methods
- [ ] Test with PostgreSQL profile
- [ ] Verify DataSeeder creates test data
- [ ] Run comprehensive testing
- [ ] Performance benchmarks
- [ ] Documentation update
- [ ] Team training
- [ ] Production migration plan
- [ ] Monitoring setup

This plan implements PostgreSQL as a fresh design choice, leveraging its advanced features from the start.