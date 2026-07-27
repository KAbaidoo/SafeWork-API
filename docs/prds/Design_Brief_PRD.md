# PRD: Global Design & UI/UX System

## 1. Introduction & Purpose

This document outlines the global design philosophy, visual language, and UI/UX principles for the **SafeWork Web Dashboard**. Its purpose is to ensure a consistent, predictable, and actionable user experience across all modules.

## 2. Core Design Philosophy

[cite_start]The design must be **Clear & Actionable**[cite: 2]. [cite_start]Every element must serve a purpose: to inform a decision or initiate a task[cite: 4]. [cite_start]We will move away from a cluttered or distracting look[cite: 3].

| Principle | Description | Implementation Detail |
| :--- | :--- | :--- |
| **Data Prioritization** | [cite_start]Critical information (alerts, overdue tasks) must be immediately visible[cite: 5]. | [cite_start]Use high-contrast cards and clear numerical badges for Home Screen counters[cite: 5]. |
| **Visual Simplicity** | [cite_start]Clean lines, minimal shadows, and ample whitespace to reduce cognitive load[cite: 5]. | [cite_start]Utilize MUI's `Paper` and `Card` components with minimal elevation[cite: 5]. |
| **Workflow Focus** | [cite_start]The user should know exactly where to click next to resolve an issue[cite: 5]. | [cite_start]Use prominent, clear primary action buttons (e.g., "Assign Task," "Close Issue")[cite: 5]. |

## 3. Color Palette

[cite_start]The color scheme is designed to convey safety, reliability, and urgency[cite: 7].

* **Primary (Action/Branding)**:
    * [cite_start]**Hex**: `#007BFF` (or a deeper navy)[cite: 8].
    * [cite_start]**Usage**: Navigation, primary buttons, and critical UI elements[cite: 9].
* **Success (Safety/Compliant)**:
    * [cite_start]**Hex**: `#28A745`[cite: 10].
    * [cite_start]**Usage**: Positive status indicators (e.g., "Inspection Complete," "Asset In Service")[cite: 10].
* **Warning (Alerts/Urgency)**:
    * [cite_start]**Hex**: `#FFC107`[cite: 11].
    * [cite_start]**Usage**: "Pending" or "Due Soon" tasks[cite: 11].
* **Danger (Critical Issues)**:
    * [cite_start]**Hex**: `#DC3545`[cite: 12].
    * [cite_start]**Usage**: Exclusively for "Overdue Tasks" and high-priority issue flags[cite: 12].
* **Background/Surface**:
    * [cite_start]**Hex**: `White` or `#F8F9FA`[cite: 13].
    * [cite_start]**Usage**: Main app background to maximize contrast and focus on data[cite: 13].

## 4. Layout & Structure

[cite_start]The layout must be consistent across all modules[cite: 15].

| Component | Design Requirement | Recommended MUI Component |
| :--- | :--- | :--- |
| **Navigation** | [cite_start]**Fixed Left Sidebar**[cite: 16]. [cite_start]Must clearly list the five core modules (Home, Tasks, Assets, Checklists, Analytics)[cite: 16]. | [cite_start]`MUI Drawer` (permanent or persistent variant)[cite: 16]. |
| **Main Content** | [cite_start]Structured using a **12-column grid system** for responsiveness[cite: 16]. | [cite_start]`MUI Grid` or `Stack` components[cite: 16]. |
| **Data Tables** | [cite_start]Must be highly scannable[cite: 16]. [cite_start]Primary data (e.g., Asset Name, Issue Description) **should be bold**[cite: 16]. | [cite_start]`MUI DataGrid` (for sorting, filtering, and pagination)[cite: 16]. |

## 5. Component-Specific UX Requirements

### 5.1. Tasks & Corrective Actions (Kanban)

* **Task Cards**: Must be concise. [cite_start]Show only Asset ID, Priority (as a color-coded tag), Issue Status, and Due Date[cite: 19].
* [cite_start]**Drag-and-Drop**: Feedback must be smooth and immediate[cite: 20]. [cite_start]The drop target column must be subtly highlighted during the drag operation[cite: 21].

### 5.2. Analytics Module

* [cite_start]**Data Density**: Prioritize charts (Line, Bar) over tables to represent data trends[cite: 23, 24].
* [cite_start]**Interactivity**: Charts must be interactive (e.g., show tooltips on hover) but not distracting[cite: 25].
* [cite_start]**Filtering**: All charts and reports must be filterable by date range and location[cite: 26].

### 5.3. Checklist Builder (Configuration)

* **UX**: This is a complex creation tool. [cite_start]It must use clear icons (e.g., a camera icon for "Photo Required") and a large workspace for drag-and-drop[cite: 28].
* [cite_start]**Layout**: Use `MUI Tabs` or `MUI Accordions` to separate the different sections of the form builder[cite: 29].