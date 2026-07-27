# Module PRD: Asset Management - Maintenance (Tasks)

## 1. Introduction & Purpose

* **Module**: Asset Management
* **Sub-Module**: Maintenance
* **Purpose**: To serve as the core task management interface for supervisors to assign, track, and resolve all asset-related issues.
* **Target User**: Supervisor.

## 2. Functional Requirements (EARS Format)

### 2.1. Main View (Kanban)

* **[R-AM-M-01]** The system **shall** present all tasks in a **Kanban board view** with columns for "New Issues," "Assigned," "In Progress," and "Resolved/To Be Verified."
* **[R-AM-M-02]** The system **shall** allow supervisors to **drag-and-drop** tasks between the Kanban columns, updating the issue `status` field via `PUT /v1/issues/{id}`.
* **[R-AM-M-03]** The system **shall** allow supervisors to filter tasks by `priority`, `asset`, `location`, and `assignee`.

### 2.2. Task Detail View

* **[R-AM-M-04]** The Task Detail View **shall** display the **Issue Context** (description, asset ID, location, and reporter name).
* **[R-AM-M-05]** The Task Detail View **shall** display all **Evidence** attached by the inspector, including photos and original notes.
* **[R-AM-M-06]** The system **shall** allow supervisors to **assign** the corrective action and set a **due date**, updating the record via `PUT /v1/issues/{id}`.
* **[R-AM-M-07]** The system **shall** record and display an **immutable Audit Trail** of all status changes, notes, and attachments added to the task.

## 3. API Integration & Data

* **Key Endpoints**:
    * `GET /v1/issues?status={status}` (to populate Kanban columns)
    * `PUT /v1/issues/{id}` (for status updates, assignment, etc.)
    * `GET /v1/inspections/{id}` (to fetch original evidence)