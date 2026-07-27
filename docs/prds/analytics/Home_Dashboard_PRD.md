# Module PRD: Home/Dashboard (Command Center)

## 1. Introduction & Purpose

[cite_start]This document outlines the requirements for the **Home/Dashboard** module of the SafeWork Web Dashboard[cite: 274, 279].

* [cite_start]**Purpose**: To provide supervisors and administrators with an immediate, high-level overview of the facility's operational status and critical alerts[cite: 275, 279].
* [cite_start]**Target User**: Supervisor, Administrator[cite: 279].
* [cite_start]**Key User Action**: Daily review of critical counters and activity to prioritize the day's tasks[cite: 279].

## 2. Functional Requirements (EARS Format)

* [cite_start]**[R-H-01]** The dashboard **shall** display a prominent, real-time counter for all **Unassigned Issues** that requires immediate supervisor action[cite: 282].
* [cite_start]**[R-H-02]** The dashboard **shall** display a prominent, real-time counter for all **Overdue Tasks** that are past their resolution deadline[cite: 283].
* [cite_start]**[R-H-03]** The dashboard **shall** display a visual **Compliance Scorecard** (e.g., Green/Yellow/Red status) derived from inspection completion rates and open high-priority issues[cite: 284].
* [cite_start]**[R-H-04]** The dashboard **shall** display a chronological **Activity Feed** of the latest submitted inspections and closed corrective actions[cite: 285].

## 3. Design & User Experience (UX)

* [cite_start]**Design Philosophy**: The dashboard must be **Clear & Actionable**[cite: 198]. [cite_start]Critical information (alerts, overdue tasks) must be immediately visible[cite: 201].
* [cite_start]**Layout**: Use high-contrast cards (MUI `Paper` or `Card` components) and clear numerical badges for the Home Screen counters[cite: 201, 212]. [cite_start]The layout will be on a 12-column grid[cite: 212].
* **Color Palette**:
    * [cite_start]**Danger (Red, e.g., #DC3545)**: Used exclusively for the "Overdue Tasks" counter and high-priority flags[cite: 208].
    * [cite_start]**Warning (Yellow/Orange, e.g., #FFC107)**: Used for "Unassigned Issues" or "Pending" items[cite: 207].
    * [cite_start]**Success (Green, e.g., #28A745)**: Used for positive status indicators (e.g., high compliance score)[cite: 206].
    * [cite_start]**Background**: White or a very light gray (#F8F9FA)[cite: 209].

## 4. API Integration & Data

* [cite_start]**Technology**: The dashboard will be built in **React (with TypeScript)** and use **MUI** components[cite: 276].
* [cite_start]**Data Source**: All data must be fetched from the **SafeWork-API**[cite: 276, 318].
* [cite_start]**Real-time Updates**: The dashboard **shall** utilize real-time **WebSocket or polling** to ensure the "Unassigned Issues" and "Overdue Tasks" counters are updated instantly[cite: 319].
* **Key Endpoints**:
    * [cite_start]`GET /v1/issues?status=open`: To populate the "Unassigned Issues" counter[cite: 265, 282].
    * [cite_start]`GET /v1/issues?status=overdue`: (Or similar logic) to populate the "Overdue Tasks" counter[cite: 265, 283].
    * [cite_start]`GET /v1/inspections?limit=10`: To retrieve data for the "Activity Feed"[cite: 262, 285].
    * [cite_start]`GET /analytics/completion-rate`: (Or similar) to power the "Compliance Scorecard"[cite: 194, 284].