-- Sample data for development environment
-- This migration provides the same data that was previously in DataSeeder.java
-- Only runs in development to provide test data for API exploration

-- 1. Insert sample organization
INSERT INTO organizations (name, address, website, industry, created_at, updated_at)
VALUES (
    'Apex Global Logistics',
    '123 Industrial Blvd, Warehouse District, Logistics City, LC 12345',
    'https://apexglobal.com',
    'Logistics and Warehousing',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2. Insert sample users with different roles
-- BCrypt hash for "password" (this is for dev only - never use in production!)
-- Generated using BCryptPasswordEncoder with default strength
INSERT INTO users (organization_id, name, email, password, role, created_at, updated_at)
VALUES 
    -- Admin user
    (
        (SELECT id FROM organizations WHERE name = 'Apex Global Logistics'),
        'Alice Admin',
        'admin@apex.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  -- BCrypt for "password"
        'ADMIN',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Supervisor user  
    (
        (SELECT id FROM organizations WHERE name = 'Apex Global Logistics'),
        'Bob Supervisor',
        'supervisor@apex.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  -- BCrypt for "password"
        'SUPERVISOR',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Inspector user
    (
        (SELECT id FROM organizations WHERE name = 'Apex Global Logistics'),
        'Charlie Inspector', 
        'inspector@apex.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  -- BCrypt for "password"
        'INSPECTOR',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- 3. Insert sample asset type
INSERT INTO asset_types (organization_id, name, description, created_at, updated_at)
VALUES (
    (SELECT id FROM organizations WHERE name = 'Apex Global Logistics'),
    'Forklift',
    'Material handling equipment for warehouse operations',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4. Insert sample asset with JSONB custom attributes
INSERT INTO assets (
    organization_id, 
    asset_type_id, 
    asset_tag, 
    name, 
    qr_code_id, 
    status, 
    assigned_to, 
    purchase_date, 
    purchase_cost, 
    custom_attributes,
    created_at, 
    updated_at
)
VALUES (
    (SELECT id FROM organizations WHERE name = 'Apex Global Logistics'),
    (SELECT id FROM asset_types WHERE name = 'Forklift'),
    'APX-FL-001',
    'Warehouse Forklift #1',
    'SN-APX-FL-001',
    'ACTIVE',
    (SELECT id FROM users WHERE email = 'inspector@apex.com'),
    '2024-05-10',
    25000.00,
    '{"model": "Hyster H50FT", "fuelType": "LPG"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Note: This migration should only be applied in development environments
-- In production, remove this file or use conditional logic based on environment