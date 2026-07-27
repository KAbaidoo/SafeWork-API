# Module PRD: Tasks & Corrective Actions

## 1. Introduction & Purpose

[cite_start]This document outlines the requirements for the **Tasks & Corrective Actions** module of the SafeWork Web Dashboard[cite: 274, 279].

* [cite_start]**Purpose**: To allow supervisors to manage the full lifecycle of all open issues and assigned work[cite: 279].
* [cite_start]**Target User**: Supervisor[cite: 279].
* [cite_start]**Key User Action**: Assign, track, and close corrective actions reported from the field[cite: 279].

## 2. Functional Requirements (EARS Format)

### 2.1. Main View (Kanban)

* [cite_start]**[R-T-01]** The system **shall** present all tasks in a **Kanban board view** with columns for "New Issues," "Assigned," "In Progress," and "Resolved/To Be Verified"[cite: 287].
* [cite_start]**[R-T-02]** The system **shall** allow supervisors to **drag-and-drop** tasks between the Kanban columns to update their status (via an API call)[cite: 288].
* [cite_start]**[R-T-03]** The system **shall** allow supervisors to **filter** tasks by priority, asset, location, and assignee[cite: 289].
* [cite_start]**[R-T-04]** The system **shall** present a **Task Detail View** immediately upon clicking a task card[cite: 290].

### 2.2. Task Detail View

* [cite_start]**[R-T-05]** The Task Detail View **shall** display the **Issue Context** (description, asset ID, location, and reporter name)[cite: 292].
* [cite_start]**[R-T-06]** The Task Detail View **shall** display all **Evidence** attached by the inspector, including **photos** and original notes[cite: 293].
* [cite_start]**[R-T-07]** The Task Detail View **shall** display a concise link to the **Asset History**, showing the last three linked issues for the current asset[cite: 294].
* [cite_start]**[R-T-08]** The system **shall** allow supervisors to **assign** the corrective action to a specific team member[cite: 295].
* [cite_start]**[R-T-09]** The system **shall** allow supervisors to set a **priority level** (High, Medium, Low) and a **due date** for the task[cite: 296].
* [cite_start]**[R-T-10]** The system **shall** record and display an **immutable Audit Trail** of all status changes, notes, and attachments added to the task[cite: 297].

## 3. Design & User Experience (UX)

* [cite_start]**Design Philosophy**: The design must be **Workflow-Focused**[cite: 201]. [cite_start]The user should know exactly where to click next to resolve an issue[cite: 201].
* [cite_start]**Kanban Cards**: Task cards must be concise, showing only the Asset ID, Priority (color-coded tag), Issue Status, and Due Date[cite: 215].
* [cite_start]**Drag-and-Drop**: Visual feedback when dragging a task must be smooth and immediate[cite: 216]. [cite_start]The column where the task will drop should be subtly highlighted[cite: 217].
* [cite_start]**Task Detail View**: Must feature prominent, clear primary action buttons (e.g., "Assign Task," "Close Issue," "Set Priority")[cite: 201, 295, 296].
* **Color Palette**:
    * [cite_start]**Danger (Red, #DC3545)**: For "High" priority tags[cite: 208, 296].
    * [cite_start]**Warning (Yellow/Orange, #FFC107)**: For "Medium" priority tags[cite: 207, 296].

## 4. API Integration & Data

* [cite_start]**Technology**: **React (with TypeScript)** and **MUI** components[cite: 276]. Will require a library for drag-and-drop functionality.
* [cite_start]**Data Source**: **SafeWork-API**[cite: 318].
* **Data Models**:
    * [cite_start]**Issue**: `id, inspection_id, asset_id, description, photo_url, status (open, in_progress, resolved), created_at`[cite: 247].
    * [cite_start]**User**: `id, name, email, role` [cite: 243] [cite_start](for the assignee list [cite: 295]).
* **Key Endpoints**:
    * [cite_start]`GET /v1/issues?status={status}`: To populate the different Kanban columns[cite: 265, 287].
    * [cite_start]`PUT /v1/issues/{id}`: To update an issue's status (on drag-and-drop), assignee, priority, or due date[cite: 266, 288, 295, 296].
    * [cite_start]`GET /v1/inspections/{id}`: To retrieve the original inspection report and evidence (photos, notes) for the Task Detail View[cite: 261, 293].
    * [cite_start]`GET /v1/inspections?asset_id={id}`: To populate the "Asset History" section in the detail view[cite: 262, 294].
    * [cite_start]`GET /users`: To populate the dropdown list of team members for assignment[cite: 172, 295].