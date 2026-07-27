# Module PRD: Asset Management - Dashboard

## 1. Introduction & Purpose

* **Module**: Asset Management
* **Sub-Module**: Dashboard
* **Purpose**: To provide supervisors with a high-level overview of asset-related KPIs, maintenance status, and critical alerts.
* **Target User**: Supervisor, Administrator.

## 2. Functional Requirements (EARS Format)

* **[R-AM-D-01]** The dashboard **shall** display a prominent, real-time counter for all **Unassigned Issues** that requires immediate supervisor action.
* **[R-AM-D-02]** The dashboard **shall** display a prominent, real-time counter for all **Overdue Tasks** that are past their resolution deadline.
* **[R-AM-D-03]** The dashboard **shall** display a visual **Compliance Scorecard** (e.g., Green/Yellow/Red status) derived from inspection completion rates and open high-priority issues.
* **[R-AM-D-04]** The dashboard **shall** display a chronological **Activity Feed** of the latest submitted inspections and closed corrective actions.

## 3. API Integration & Data

* **Data Source**: All data is filtered to be relevant to the Asset Management context.
* **Key Endpoints**:
    * `GET /v1/issues?status=open&context=assets` (or similar)
    * `GET /v1/issues?status=overdue&context=assets` (or similar)
    * `GET /analytics/completion-rate`
    * `GET /v1/inspections?limit=10`