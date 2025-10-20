# Module PRD: Asset Management - Settings

## 1. Introduction & Purpose

* **Module**: Asset Management
* **Sub-Module**: Settings
* **Purpose**: To allow administrators to configure settings specific to the Asset Management module, such as user roles and integrations.
* **Target User**: Administrator.

## 2. Functional Requirements (EARS Format)

* **[R-AM-S-01]** The system **shall** allow users with the "admin" role to manage asset-specific user permissions (e.g., "Asset Manager" vs. "Technician").
* **[R-AM-S-02]** The system **shall** provide a view for configuring asset-related integrations (e.g., telematics, ERP systems).
* **[R-AM-S-03]** The system **shall** allow admins to define custom `asset_type` categories for use in filtering.

## 3. API Integration & Data

* **Key Endpoints**:
    * `GET /v1/roles?module=assets`
    * `POST /v1/roles?module=assets`
    * `GET /v1/config/asset-types`
    * `POST /v1/config/asset-types`