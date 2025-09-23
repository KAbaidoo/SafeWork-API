-- Performance indexes for SafeWork API PostgreSQL schema
-- Includes GIN indexes for JSONB columns and B-tree indexes for frequent queries

-- === JSONB GIN Indexes for Advanced Querying ===

-- GIN index on assets.custom_attributes for JSON queries
-- Supports operators: @>, ?, ?&, ?|, #>, #>>
CREATE INDEX idx_assets_custom_attributes_gin ON assets USING GIN (custom_attributes);

-- GIN index on inspections.report_data for inspection report queries
CREATE INDEX idx_inspections_report_data_gin ON inspections USING GIN (report_data);

-- GIN index on checklists.template_data for template queries
CREATE INDEX idx_checklists_template_data_gin ON checklists USING GIN (template_data);

-- === Multi-Tenant Organization Indexes ===

-- Organization-based filtering (most common query pattern)
CREATE INDEX idx_assets_organization_id ON assets (organization_id);
CREATE INDEX idx_users_organization_id ON users (organization_id);
CREATE INDEX idx_departments_organization_id ON departments (organization_id);
CREATE INDEX idx_locations_organization_id ON locations (organization_id);
CREATE INDEX idx_suppliers_organization_id ON suppliers (organization_id);
CREATE INDEX idx_asset_types_organization_id ON asset_types (organization_id);
CREATE INDEX idx_maintenance_schedules_organization_id ON maintenance_schedules (organization_id);
CREATE INDEX idx_checklists_organization_id ON checklists (organization_id);

-- === Foreign Key Relationship Indexes ===

-- Asset relationships (heavily queried)
CREATE INDEX idx_assets_asset_type_id ON assets (asset_type_id);
CREATE INDEX idx_assets_department_id ON assets (department_id);
CREATE INDEX idx_assets_assigned_to_user_id ON assets (assigned_to_user_id);
CREATE INDEX idx_assets_location_id ON assets (location_id);
CREATE INDEX idx_assets_supplier_id ON assets (supplier_id);
CREATE INDEX idx_assets_maintenance_schedule_id ON assets (maintenance_schedule_id);

-- User and department relationships
CREATE INDEX idx_users_department_id ON users (department_id);
CREATE INDEX idx_departments_manager_id ON departments (manager_id);

-- Location hierarchy
CREATE INDEX idx_locations_parent_location_id ON locations (parent_location_id);

-- Inspection and maintenance relationships
CREATE INDEX idx_inspections_asset_id ON inspections (asset_id);
CREATE INDEX idx_inspections_inspector_user_id ON inspections (inspector_user_id);
CREATE INDEX idx_inspections_checklist_id ON inspections (checklist_id);
CREATE INDEX idx_maintenance_logs_asset_id ON maintenance_logs (asset_id);
CREATE INDEX idx_maintenance_logs_performed_by_user_id ON maintenance_logs (performed_by_user_id);

-- Issue tracking relationships
CREATE INDEX idx_issues_asset_id ON issues (asset_id);
CREATE INDEX idx_issues_reported_by_user_id ON issues (reported_by_user_id);
CREATE INDEX idx_issues_assigned_to_user_id ON issues (assigned_to_user_id);
CREATE INDEX idx_issues_resolved_by_user_id ON issues (resolved_by_user_id);
CREATE INDEX idx_issues_inspection_id ON issues (inspection_id);

-- === Performance Indexes for Common Queries ===

-- Asset status and compliance filtering
CREATE INDEX idx_assets_status ON assets (status);
CREATE INDEX idx_assets_compliance_status ON assets (compliance_status);

-- Date-based queries for maintenance and inspections
CREATE INDEX idx_assets_next_service_date ON assets (next_service_date);
CREATE INDEX idx_assets_warranty_expiry_date ON assets (warranty_expiry_date);
CREATE INDEX idx_inspections_inspection_date ON inspections (inspection_date);
CREATE INDEX idx_maintenance_logs_performed_date ON maintenance_logs (performed_date);

-- Issue status and priority filtering
CREATE INDEX idx_issues_status ON issues (status);
CREATE INDEX idx_issues_priority ON issues (priority);
CREATE INDEX idx_issues_reported_date ON issues (reported_date);

-- User email for authentication
CREATE INDEX idx_users_email ON users (email);

-- === Composite Indexes for Complex Queries ===

-- Organization + Status filtering (very common)
CREATE INDEX idx_assets_org_status ON assets (organization_id, status);
CREATE INDEX idx_issues_org_status ON issues (asset_id, status) 
    WHERE asset_id IN (SELECT id FROM assets); -- Partial index

-- Asset + Date combinations for maintenance scheduling
CREATE INDEX idx_assets_org_next_service ON assets (organization_id, next_service_date)
    WHERE next_service_date IS NOT NULL;

-- Inspection status with date for reporting
CREATE INDEX idx_inspections_asset_date_status ON inspections (asset_id, inspection_date, status);

-- User role-based access patterns
CREATE INDEX idx_users_org_role ON users (organization_id, role);

-- === Partial Indexes for Specific Conditions ===

-- Active assets only (most queries filter out disposed assets)
CREATE INDEX idx_assets_active ON assets (organization_id, status, asset_type_id)
    WHERE status != 'DISPOSED';

-- Pending issues (high-priority queries)
CREATE INDEX idx_issues_pending ON issues (asset_id, priority, reported_date)
    WHERE status IN ('OPEN', 'IN_PROGRESS');

-- Overdue maintenance (time-sensitive queries)
CREATE INDEX idx_assets_overdue_maintenance ON assets (organization_id, next_service_date)
    WHERE next_service_date < CURRENT_DATE AND status = 'ACTIVE';