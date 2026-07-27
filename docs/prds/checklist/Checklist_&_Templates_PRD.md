# Module PRD: Checklists & Templates (Configuration)

## 1. Introduction & Purpose

[cite_start]This document outlines the requirements for the **Checklists & Templates** module of the SafeWork Web Dashboard[cite: 274, 279].

* [cite_start]**Purpose**: To provide administrators with a tool to create and manage all inspection templates used by the mobile app[cite: 279].
* [cite_start]**Target User**: Administrator[cite: 279, 315].
* [cite_start]**Key User Action**: Build custom and standard audit forms[cite: 279].

## 2. Functional Requirements (EARS Format)

* [cite_start]**[R-C-01]** The system **shall** provide a **drag-and-drop checklist builder** for creating new templates[cite: 299].
* [cite_start]**[R-C-02]** The system **shall** allow administrators to define various **question types** (e.g., Yes/No, Pass/Fail, Text Input, Numeric Input, Photo Required)[cite: 300].
* [cite_start]**[R-C-03]** The system **shall** store a library of **standard audit templates** (e.g., NEBOSH, ISO) that administrators can clone and customize[cite: 301, 334].
* [cite_start]**[R-C-04]** The system **shall** allow administrators to **publish or unpublish** checklist templates for mobile use[cite: 302].

## 3. Design & User Experience (UX)

* [cite_start]**UX Challenge**: This is a complex creation tool, so simplicity is key[cite: 224].
* [cite_start]**Layout**: The builder must use a large workspace for the drag-and-drop actions[cite: 224].
* **Component Usage**:
    * [cite_start]Use clear icons (e.g., a camera icon) to represent "Photo Required" questions[cite: 224, 300].
    * [cite_start]Use MUI `Tabs` or `Accordions` to separate different sections of the form builder (e.g., "Settings," "Builder," "Permissions")[cite: 225].
* [cite_start]**Data Fields**: The builder must allow admins to configure the content that will be stored in the `template_data (JSON object)`[cite: 245].

## 4. API Integration & Data

* [cite_start]**Technology**: **React (with TypeScript)** and **MUI** components (specifically `Tabs` or `Accordions`)[cite: 276, 225].
* [cite_start]**Data Source**: **SafeWork-API**[cite: 318].
* [cite_start]**Security**: Access to this module **shall** be restricted to users with the **"admin" role**[cite: 315].
* **Data Model**:
    * [cite_start]**Checklist**: `id, name, template_data (JSON object), version, created_at`[cite: 245].
* **Key Endpoints**:
    * [cite_start]`GET /v1/checklists`: To list all available templates[cite: 256].
    * [cite_start]`POST /v1/checklists`: To create a new checklist template[cite: 257]. [cite_start](Requires admin role [cite: 257]).
    * [cite_start]`PUT /v1/checklists/{id}`: To update a template (including publishing/unpublishing)[cite: 258]. [cite_start](Requires admin role [cite: 258]).