# Module PRD: Asset Management

## 1. Introduction & Purpose

This document outlines the specific requirements for the **Asset Management** module of the SafeWork Web Dashboard.

* **Purpose**: To serve as the central hub for supervisors and administrators to create, view, manage, and track the complete inventory and operational history of all physical assets within the system.
* **Target Users**:
    * **Administrator**: Full CRUD (Create, Read, Update, Delete) access.
    * **Supervisor**: Read-only access (View asset list and details).
* **Key User Actions**:
    * Look up an asset's details and service history.
    * Search for a specific asset by its name, QR code, or serial number.
    * (Admin) Create, edit, and update asset information.

## 2. Functional Requirements (EARS Format)

### 2.1. Main View (Asset List / DataGrid)

* **[R-AM-01]** The system **shall** display all registered assets in a paginated, sortable, and filterable table.
* **[R-AM-02]** The system **shall** use the **MUI DataGrid** component to render the asset list, enabling server-side pagination, sorting, and filtering for performance.
* **[R-AM-03]** The DataGrid **shall** display the following columns by default:
    * Asset Name (Primary, bold text)
    * QR Code ID
    * Status (e.g., "In Service," "Maintenance," "Out of Service")
    * Current Location
    * Last Inspection Date
* **[R-AM-04]** The system **shall** provide a persistent search bar above the DataGrid that allows users to search for assets by **Asset Name**, **QR Code ID**, or **Serial Number**.
* **[R-AM-05]** The system **shall** display a primary action button ("+ Create New Asset") that is visible **only** to users with the "admin" role.
* **[R-AM-06]** The system **shall** navigate the user to the **Asset Detail View** when a user clicks on any asset row in the DataGrid.

### 2.2. Asset Detail View

* **[R-AM-07]** The system **shall** display a clear header with the `Asset Name` and its current `Status` (e.g., using a color-coded MUI `Chip` component).
* **[R-AM-08]** The system **shall** organize all asset information using **MUI Tabs** to reduce cognitive load. The tabs shall be:
    * Tab 1: **Details**
    * Tab 2: **History**

* **[R-AM-09] (Details Tab)** The "Details" tab **shall** display the following data fields, grouped by section:
    * **Identification**:
        * Asset Name
        * QR Code ID
        * Serial Number
    * **Status**:
        * Status (In Service, Maintenance, etc.)
        * Current Location
    * **Maintenance**:
        * Last Inspection Date
        * Next Service Date
        * Warranty Expiration
    * **Custom Data**:
        * All key-value pairs from the asset's `Custom Attributes` (JSON object) shall be displayed in a clean, readable definition list (e.g., "Key: Value").

* **[R-AM-10] (History Tab)** The "History" tab **shall** display a chronological, reverse-sorted list (newest first) of all inspections and issues associated with the asset.
* **[R-AM-11] (History Tab)** Each item in the history feed **shall** be scannable, displaying its type (e.g., "Inspection" or "Issue"), date, and a brief description, and **shall** link to the full detail view for that specific inspection or issue.

### 2.3. Create / Edit Asset Functionality (Admin Only)

* **[R-AM-12]** The system **shall** provide a form (e.g., in an **MUI Modal**) for creating and editing asset details.
* **[R-AM-13]** This form **shall** be accessible **only** to users with the "admin" role.
* **[R-AM-14]** The form **shall** include fields for all editable asset properties:
    * `Asset Name` (Required)
    * `QR Code ID` (Required, unique)
    * `Serial Number`
    * `Status` (Dropdown: In Service, Maintenance, Out of Service)
    * `Current Location`
    * `Next Service Date` (Date picker)
    * `Warranty Expiration` (Date picker)
    * `Custom Attributes` (A simple JSON editor or key-value pair adder)
* **[R-AM-15]** When **updating** an existing asset, the system **shall** send the asset's current `version` number in the `PUT` request payload to support optimistic concurrency control, as defined in the API specification.

## 3. Design & User Experience (UX)

* **Design Philosophy**: **Clear & Actionable**. The module must be highly scannable and provide immediate access to an asset's complete history.
* **Component Usage**:
    * **MUI DataGrid**: Must be used for the main asset list to provide robust, out-of-the-box sorting, filtering, and pagination.
    * **MUI `Tabs`**: Must be used in the Asset Detail View to separate core details from the chronological history.
    * **MUI `Card` / `Paper`**: Used to contain the sections (Identification, Status, etc.) within the "Details" tab.
    * **MUI `Chip`**: Used to display asset `Status` with appropriate colors (e.g., Green for "In Service," Red for "Out of Service").
* **Visuals**: As per the design brief, primary data in the DataGrid (e.g., `Asset Name`) should be **bold** to improve scannability.

## 4. API Integration & Data

### 4.1. Data Model (Asset)

The frontend components will interact with the `Asset` data model, which must contain the following fields:

```json
{
  "id": "string",
  "name": "string",
  "qr_code_id": "string",
  "serial_number": "string",
  "status": "string (e.g., 'in_service', 'maintenance')",
  "location": "string",
  "last_inspection_date": "date-time",
  "next_service_date": "date-time",
  "warranty_expiration": "date-time",
  "custom_attributes_json": "object",
  "version": "integer",
  "created_at": "date-time"
}