# PRD: Global Navigation & Core Technical Requirements

## 1. Introduction & Technology Stack

This document serves as the technical blueprint for the SafeWork Web Dashboard.

| Component | Technology | Alignment with API |
| :--- | :--- | :--- |
| **Frontend Framework** | React (with TypeScript) | Uses TypeScript for type safety and smooth API integration. |
| **UI Components** | Material UI (MUI) | Provides robust components (e.g., DataGrid) for enterprise-grade dashboards. |
| **Charting/Visualization** | Recharts or Nivo | Specialized libraries for generating Trend Analysis and MTTR graphs. |
| **Data Source** | SafeWork-API | All data operations are handled via API endpoints. |

## 2. Navigation & Immersive Redesign

The dashboard utilizes a two-tiered navigation system to create an immersive, focused user experience.

### 2.1. Global Module Selector

* **Location**: Top-Left (Next to the app title).
* **Function**: Allows the user to switch between major systems, such as:
    * Asset Management (Primary Focus)
    * Checklist Builder
    * User Management
    * Analytics (Global)
* **[R-NAV-01]** The system **shall** update the left navigation panel to display only the sub-sections relevant to the currently selected module.

### 2.2. Immersive Sidebar (Sub-Module Navigation)

When a module (e.g., Asset Management) is selected, a fixed left sidebar appears with sub-sections for deep navigation.

| Module (Example) | Sub-Sections | Key Function |
| :--- | :--- | :--- |
| **Asset Management** | Dashboard | High-level KPIs, critical status alerts. |
| | Assets | The primary asset inventory list (table view). |
| | Maintenance | Task management and corrective actions (Kanban board). |
| | Reports | Module-specific analytics and compliance reports. |
| | Settings | User roles, integrations, and configuration. |

## 3. Technical & Cross-Functional Requirements

### 3.1. Security & Access

* **[R-SEC-01]** The system **shall** enforce role-based access control (RBAC), restricting asset creation and checklist modification to users authenticated as `admin`.
* **[R-SEC-02]** The system **shall** require the highest security protocols (SSO/SCIM) for all Enterprise Tier clients.

### 3.2. API Integration

* **[R-API-01]** The Web Dashboard **shall** consume the **SafeWork-API** for all data operations.
* **[R-API-02]** The dashboard **shall** utilize real-time **WebSocket or polling** to ensure the Alerts & Issues counter is updated instantly when a new issue is submitted from the mobile app.
* **[R-API-03]** The system **shall** allow administrators to update an existing checklist via `PUT /v1/checklists/{id}`, including the `version` field in the request body to comply with the API's synchronization logic.