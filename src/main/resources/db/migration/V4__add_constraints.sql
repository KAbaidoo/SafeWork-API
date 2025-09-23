-- Foreign key constraints and data integrity rules for SafeWork API
-- Applied after indexes to ensure optimal constraint checking performance

-- === Core Organization Relationships ===

-- Users belong to organizations
ALTER TABLE users 
    ADD CONSTRAINT fk_users_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Users can belong to departments (optional)
ALTER TABLE users 
    ADD CONSTRAINT fk_users_department 
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL;

-- === Organizational Structure Constraints ===

-- Departments belong to organizations
ALTER TABLE departments 
    ADD CONSTRAINT fk_departments_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Departments can have managers (users)
ALTER TABLE departments 
    ADD CONSTRAINT fk_departments_manager 
    FOREIGN KEY (manager_id) REFERENCES users (id) ON DELETE SET NULL;

-- Locations belong to organizations
ALTER TABLE locations 
    ADD CONSTRAINT fk_locations_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Locations can have parent locations (hierarchy)
ALTER TABLE locations 
    ADD CONSTRAINT fk_locations_parent 
    FOREIGN KEY (parent_location_id) REFERENCES locations (id) ON DELETE SET NULL;

-- Suppliers belong to organizations
ALTER TABLE suppliers 
    ADD CONSTRAINT fk_suppliers_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Asset types belong to organizations
ALTER TABLE asset_types 
    ADD CONSTRAINT fk_asset_types_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Maintenance schedules belong to organizations
ALTER TABLE maintenance_schedules 
    ADD CONSTRAINT fk_maintenance_schedules_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- === Asset Management Constraints ===

-- Assets belong to organizations (mandatory)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- Assets must have asset types
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_asset_type 
    FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE RESTRICT;

-- Assets can be assigned to departments (optional)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_department 
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL;

-- Assets can be assigned to users (optional)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_assigned_user 
    FOREIGN KEY (assigned_to_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Assets can have locations (optional)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_location 
    FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL;

-- Assets can have suppliers (optional)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_supplier 
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE SET NULL;

-- Assets can have maintenance schedules (optional)
ALTER TABLE assets 
    ADD CONSTRAINT fk_assets_maintenance_schedule 
    FOREIGN KEY (maintenance_schedule_id) REFERENCES maintenance_schedules (id) ON DELETE SET NULL;

-- === Maintenance and Inspection Constraints ===

-- Maintenance logs must belong to assets
ALTER TABLE maintenance_logs 
    ADD CONSTRAINT fk_maintenance_logs_asset 
    FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE;

-- Maintenance can be performed by users (optional)
ALTER TABLE maintenance_logs 
    ADD CONSTRAINT fk_maintenance_logs_performed_by 
    FOREIGN KEY (performed_by_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Inspections must belong to assets
ALTER TABLE inspections 
    ADD CONSTRAINT fk_inspections_asset 
    FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE;

-- Inspections must be performed by users
ALTER TABLE inspections 
    ADD CONSTRAINT fk_inspections_inspector 
    FOREIGN KEY (inspector_user_id) REFERENCES users (id) ON DELETE RESTRICT;

-- Inspections can use checklists (optional)
ALTER TABLE inspections 
    ADD CONSTRAINT fk_inspections_checklist 
    FOREIGN KEY (checklist_id) REFERENCES checklists (id) ON DELETE SET NULL;

-- === Issue Tracking Constraints ===

-- Issues must belong to assets
ALTER TABLE issues 
    ADD CONSTRAINT fk_issues_asset 
    FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE;

-- Issues must be reported by users
ALTER TABLE issues 
    ADD CONSTRAINT fk_issues_reported_by 
    FOREIGN KEY (reported_by_user_id) REFERENCES users (id) ON DELETE RESTRICT;

-- Issues can be assigned to users (optional)
ALTER TABLE issues 
    ADD CONSTRAINT fk_issues_assigned_to 
    FOREIGN KEY (assigned_to_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Issues can be resolved by users (optional)
ALTER TABLE issues 
    ADD CONSTRAINT fk_issues_resolved_by 
    FOREIGN KEY (resolved_by_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Issues can originate from inspections (optional)
ALTER TABLE issues 
    ADD CONSTRAINT fk_issues_inspection 
    FOREIGN KEY (inspection_id) REFERENCES inspections (id) ON DELETE SET NULL;

-- === Checklist Constraints ===

-- Checklists belong to organizations
ALTER TABLE checklists 
    ADD CONSTRAINT fk_checklists_organization 
    FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

-- === Data Integrity Check Constraints ===

-- Ensure positive values for costs and frequencies
ALTER TABLE assets 
    ADD CONSTRAINT chk_assets_purchase_cost_positive 
    CHECK (purchase_cost IS NULL OR purchase_cost >= 0);

ALTER TABLE maintenance_logs 
    ADD CONSTRAINT chk_maintenance_logs_cost_positive 
    CHECK (cost IS NULL OR cost >= 0);

ALTER TABLE maintenance_schedules 
    ADD CONSTRAINT chk_maintenance_schedules_frequency_positive 
    CHECK (frequency_value > 0);

-- Ensure logical date ordering
ALTER TABLE assets 
    ADD CONSTRAINT chk_assets_warranty_after_purchase 
    CHECK (warranty_expiry_date IS NULL OR purchase_date IS NULL OR warranty_expiry_date >= purchase_date);

ALTER TABLE issues 
    ADD CONSTRAINT chk_issues_resolved_after_reported 
    CHECK (resolved_date IS NULL OR resolved_date >= reported_date);

-- Ensure valid enum-like values (as additional safety for application enums)
ALTER TABLE assets 
    ADD CONSTRAINT chk_assets_status_valid 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DISPOSED'));

ALTER TABLE assets 
    ADD CONSTRAINT chk_assets_compliance_status_valid 
    CHECK (compliance_status IS NULL OR compliance_status IN ('COMPLIANT', 'NON_COMPLIANT', 'PENDING', 'UNKNOWN'));

ALTER TABLE issues 
    ADD CONSTRAINT chk_issues_status_valid 
    CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'));

ALTER TABLE issues 
    ADD CONSTRAINT chk_issues_priority_valid 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

-- Ensure organization data consistency (users and their assets must be in same org)
-- Note: This would be enforced at application level for performance