### **Product Requirements Document: SafeWork Backend API**

#### **1\. Introduction**

This document outlines the requirements for the **SafeWork Backend API**, the central nervous system of the SafeWork platform. This API will serve as the **single source of truth** for all application data, supporting both the mobile-first frontend and the web-based admin dashboard. It is designed with scalability and data integrity in mind, with a core focus on supporting a robust **offline-first** user experience.

---

#### **2\. Authentication & Authorization**

All API requests will require authentication. We will use **JWT (JSON Web Token)** for stateless authentication.

* **User Roles**: The API will recognize at least two core roles: admin and inspector.  
* **Endpoints**:  
  * POST /auth/register: Create a new user.  
  * POST /auth/login: Authenticate a user and return a JWT.  
  * GET /auth/me: Validate the token and return the current user's profile.

---

#### **3\. Core Data Models**

These are the primary data structures the API will manage. Each model that is subject to offline updates (Assets, Checklists, Inspections) will include a version field for synchronization.

* **User**: id, name, email, role, created\_at  
* **Asset**: id, name, qr\_code\_id, status, version, created\_at  
* **Checklist**: id, name, template\_data (JSON object), version, created\_at  
* **Inspection**: id, checklist\_id, asset\_id, user\_id, report\_data (JSON object), created\_at  
* **Issue**: id, inspection\_id, asset\_id, description, photo\_url, status (open, in\_progress, resolved), created\_at

---

#### 

#### **4\. API Endpoints**

All endpoints will be versioned (e.g., /v1/). All successful responses will use a consistent JSON format, and errors will return appropriate HTTP status codes (e.g., 400 for bad request, 401 for unauthorized, 404 for not found).

**4.1. Assets**

* GET /v1/assets: Retrieve a paginated list of all assets for the user's organization.  
* GET /v1/assets/{id}: Retrieve a single asset by its database ID.
* GET /v1/assets/qr/{qr\_code\_id}: Retrieve a single asset by its QR code ID.  
* POST /v1/assets: Create a new asset. Requires admin role.  
* PUT /v1/assets/{id}: Update an asset. Requires version field in the request.
* DELETE /v1/assets/{id}: Delete an asset. Requires admin role.

**4.2. Checklists**

* GET /v1/checklists: Retrieve a list of all checklists.  
* POST /v1/checklists: Create a new checklist. Requires admin role.  
* PUT /v1/checklists/{id}: Update a checklist. Requires version field in the request.

**4.3. Inspections**

* POST /v1/inspections: Submit a new inspection report. Request body must include checklist\_id, asset\_id, and report\_data.  
* GET /v1/inspections/{id}: Retrieve a single inspection report.  
* GET /v1/inspections?asset\_id={id}: Retrieve all inspections for a specific asset.

**4.4. Issues**

* POST /v1/issues: Create a new issue report.  
* GET /v1/issues?status={status}: Retrieve a list of issues filtered by status.  
* PUT /v1/issues/{id}: Update an issue's status or details.

---

#### **5\. Synchronization Logic**

The API's synchronization logic is crucial. When a mobile client sends an update for a record, the server will check the version number in the request against the current version in the database.

* **Successful Sync**: If the versions match, the server accepts the update, increments the version, and responds with 200 OK.  
* **Conflict Detected**: If the versions do not match, the server returns a 409 Conflict status code and a JSON response containing the latest version of the record. This triggers the mobile app's user-driven conflict resolution process.