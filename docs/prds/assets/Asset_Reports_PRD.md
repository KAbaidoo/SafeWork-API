# Module PRD: Asset Management - Reports

## 1. Introduction & Purpose

* **Module**: Asset Management
* **Sub-Module**: Reports
* **Purpose**: To provide administrators with analytics on asset performance, maintenance trends, and compliance.
* **Target User**: Administrator.

## 2. Functional Requirements (EARS Format)

* **[R-AM-R-01]** The system **shall** display a **Trend Analysis** graph showing the volume of new issues created over a selected period.
* **[R-AM-R-02]** The system **shall** calculate and display the **Mean Time To Resolve (MTTR)** for all corrective actions.
* **[R-AM-R-03]** The system **shall** display a chart of the **Top 5 Assets with the Most Issues** to identify problematic equipment.
* **[R-AM-R-04]** The system **shall** allow administrators to **filter and drill down** into compliance data by `team`, `location`, and `asset_type`.

## 3. API Integration & Data

* **Key Endpoints**:
    * `GET /analytics/issue-trends`
    * `GET /analytics/mttr`
    * `GET /analytics/top-issues?context=assets`
    * `GET /analytics/completion-rate`