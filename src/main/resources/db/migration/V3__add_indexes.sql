-- Performance indexes for SafeWork API PostgreSQL schema
-- Includes GIN indexes for JSONB columns and B-tree indexes for frequent queries

-- === JSONB GIN Indexes for Advanced Querying ===

-- GIN index on assets.custom_attributes for JSON queries
-- Supports operators: @>, ?, ?&, ?|, #>, #>>
CREATE INDEX idx_assets_custom_attributes_gin ON assets USING GIN (custom_attributes);

-- Note: Indexes for inspections, checklists, etc. will be added when those tables are created

-- === Multi-Tenant Organization Indexes ===

-- Organization-based filtering (most common query pattern)
CREATE INDEX idx_assets_organization_id ON assets (organization_id);
CREATE INDEX idx_users_organization_id ON users (organization_id);
CREATE INDEX idx_departments_organization_id ON departments (organization_id);
CREATE INDEX idx_locations_organization_id ON locations (organization_id);
CREATE INDEX idx_suppliers_organization_id ON suppliers (organization_id);
CREATE INDEX idx_asset_types_organization_id ON asset_types (organization_id);
-- Note: maintenance_schedules and checklists indexes will be added when those tables exist

-- === Foreign Key Relationship Indexes ===

-- Asset relationships (heavily queried)
CREATE INDEX idx_assets_asset_type_id ON assets (asset_type_id);
CREATE INDEX idx_assets_department_id ON assets (department_id);
CREATE INDEX idx_assets_assigned_to_user_id ON assets (assigned_to_user_id);
CREATE INDEX idx_assets_location_id ON assets (location_id);
CREATE INDEX idx_assets_supplier_id ON assets (supplier_id);
-- Note: maintenance_schedule_id index will be added when maintenance_schedules table exists

-- User and department relationships
CREATE INDEX idx_users_department_id ON users (department_id);
CREATE INDEX idx_departments_manager_id ON departments (manager_id);

-- Location hierarchy
CREATE INDEX idx_locations_parent_location_id ON locations (parent_location_id);

-- Note: Inspection, maintenance, and issue tracking indexes will be added when those tables exist

-- === Performance Indexes for Common Queries ===

-- Asset status and compliance filtering
CREATE INDEX idx_assets_status ON assets (status);
CREATE INDEX idx_assets_compliance_status ON assets (compliance_status);

-- Date-based queries for maintenance and inspections
CREATE INDEX idx_assets_next_service_date ON assets (next_service_date);
CREATE INDEX idx_assets_warranty_expiry_date ON assets (warranty_expiry_date);

-- Note: inspection and maintenance date indexes will be added when those tables exist

-- User email for authentication
CREATE INDEX idx_users_email ON users (email);

-- === Composite Indexes for Complex Queries ===

-- Organization + Status filtering (very common)
CREATE INDEX idx_assets_org_status ON assets (organization_id, status);

-- Asset + Date combinations for maintenance scheduling
CREATE INDEX idx_assets_org_next_service ON assets (organization_id, next_service_date)
    WHERE next_service_date IS NOT NULL;

-- User role-based access patterns
CREATE INDEX idx_users_org_role ON users (organization_id, role);

-- === Partial Indexes for Specific Conditions ===

-- Active assets only (most queries filter out disposed assets)
CREATE INDEX idx_assets_active ON assets (organization_id, status, asset_type_id)
    WHERE status != 'DISPOSED';

-- Note: Overdue maintenance index would need custom function to work with CURRENT_DATE

-- Note: Issue and inspection indexes will be added when those tables exist