# SafeWork Web Dashboard: Development Task List
# Module: Asset Management (Immersive Design)

This document breaks down the development tasks for the six sub-modules and core components of the Asset Management module.

---

## 1. Task List: Global Navigation & Core Technical Requirements

This task list covers the application's main shell, routing, and API setup.

* **Project Setup:**
    * `[ ]` Initialize a new React (TypeScript) application using `create-react-app` or `Vite`.
    * `[ ]` Install core dependencies: `react-router-dom`, `axios`, `@mui/material`, `@mui/icons-material`, `@emotion/react`, `@emotion/styled`.
    * `[ ]` Set up project structure (e.g., `/components`, `/pages`, `/hooks`, `/contexts`, `/api`).
* **API & Auth Setup:**
    * `[ ]` Create an `api.ts` file that exports a pre-configured `axios` instance (with `baseURL`).
    * `[ ]` Create a simple `AuthContext` to store a user's token and role (can be hardcoded for now, e.g., `{ role: 'admin' }`).
    * `[ ]` Add an `axios` request interceptor that attaches the auth token (from `AuthContext`) to every API request.
* **Core Layout Components:**
    * `[ ]` Create a `Header.tsx` component (using MUI `AppBar`) that will hold the module selector.
    * `[ ]` Create a `Sidebar.tsx` component (using MUI `Drawer`) that will be the "Immersive Sidebar."
    * `[ ]` Create a `Layout.tsx` component that arranges `Header`, `Sidebar`, and a "main content" area.
* **Routing Setup:**
    * `[ ]` Configure `react-router-dom` in `App.tsx` with top-level routes.
    * `[ ]` Create a `ProtectedRoute.tsx` component that reads from `AuthContext` and restricts access based on role (e.g., for the "Settings" page).
* **Global Module Selector (Top-Left):**
    * `[ ]` Create a `ModuleContext.tsx` to store and manage the currently selected global module (e.g., "Asset Management").
    * `[ ]` Add an MUI `Select` or `Menu` component to the `Header.tsx` component.
    * `[ ]` Populate the selector with a static list: "Asset Management," "Checklist Builder," etc.
    * `[ ]` Make the `Select` component update the `ModuleContext` when its value changes.
* **Immersive Sidebar (Sub-Module Navigation):**
    * `[ ]` Create a configuration object (e.g., `navigationConfig.ts`) that maps module names to their sub-section links (e.g., `{'Asset Management': [{ name: 'Dashboard', path: '/am/dashboard' }, ...]}`).
    * `[ ]` Update `Sidebar.tsx` to read the current module from `ModuleContext`.
    * `[ ]` Dynamically render a list of MUI `ListItem` components in the sidebar based on the `navigationConfig` for the current module.
    * `[ ]` Use `react-router-dom`'s `NavLink` component for the `ListItem`s to handle navigation and "active" state.

---

## 2. Task List: Asset Management - Dashboard

This covers the 4-widget landing page for the Asset Management module.

* **Layout:**
    * `[ ]` Create a new page component: `pages/AssetDashboard.tsx`.
    * `[ ]` Add the route `/am/dashboard` to `App.tsx` to render this page.
    * `[ ]` Use an MUI `Grid` component to create a 2x2 layout for the widgets.
* **Generic Counter Widget:**
    * `[ ]` Create a reusable component: `components/widgets/CounterWidget.tsx`.
    * `[ ]` The component should accept props: `title` (string), `count` (number), and `color` (string, e.g., 'error', 'warning').
    * `[ ]` Use an MUI `Card` and `Typography` (e.g., `h3` or `h4`) to display the count prominently.
* **Widget 1: "Unassigned Issues" Counter:**
    * `[ ]` Create a data-fetching hook: `hooks/useUnassignedIssues.ts`.
    * `[ ]` Inside the hook, call `GET /v1/issues?status=open&context=assets` and return the data (or count).
    * `[ ]` On the `AssetDashboard` page, use this hook and pass the data to `CounterWidget` with a `warning` color.
* **Widget 2: "Overdue Tasks" Counter:**
    * `[ ]` Create a data-fetching hook: `hooks/useOverdueTasks.ts`.
    * `[ ]` Inside the hook, call `GET /v1/issues?status=overdue&context=assets`.
    * `[ ]` On the `AssetDashboard` page, use this hook and pass the data to `CounterWidget` with an `error` color.
* **Widget 3: "Compliance Scorecard":**
    * `[ ]` Create a new component: `components/widgets/ComplianceWidget.tsx`.
    * `[ ]` Create a hook to fetch data from `GET /analytics/completion-rate`.
    * `[ ]` Inside the component, display the score (e.g., "95%") and use an MUI `Chip` or `LinearProgress` bar to visualize it.
* **Widget 4: "Activity Feed":**
    * `[ ]` Create a new component: `components/widgets/ActivityFeedWidget.tsx`.
    * `[ ]` Create a hook to fetch data from `GET /v1/inspections?limit=10`.
    * `[ ]` Use an MUI `List`, `ListItem`, and `ListItemText` to display the feed of recent inspections.
    * `[ ]` (Bonus) Use a library like `date-fns` to format the timestamps (e.g., "2 hours ago").

---

## 3. Task List: Asset Management - Assets (Inventory)

This is the most detailed module, covering the DataGrid and the Create/Edit/View flows.

* **Page & DataGrid Setup:**
    * `[ ]` Create a new page component: `pages/AssetInventory.tsx`.
    * `[ ]` Add the route `/am/assets` to `App.tsx`.
    * `[ ]` Install `@mui/x-data-grid`.
    * `[ ]` Create a hook `hooks/useAssets.ts` to fetch data from `GET /v1/assets`.
    * `[ ]` Implement the `DataGrid` component on the page.
    * `[ ]` Define the `columns` array for the DataGrid as per `[R-AM-03]`.
    * `[ ]` (UX) Use the `renderCell` property for the "Asset Name" column to make the text bold.
    * `[ ]` Pass the fetched asset data to the `rows` prop.
    * `[ ]` Configure the `DataGrid` for server-side pagination (e.g., `onPageChange`).
* **Search & Filter:**
    * `[ ]` Add an MUI `TextField` (for search) and `Select` (for status filter) above the `DataGrid`.
    * `[ ]` Use `useState` to store the search and filter values.
    * `[ ]` Pass these state values as query parameters to your `useAssets` hook.
* **Asset Detail View (Page):**
    * `[ ]` Create a new page component: `pages/AssetDetail.tsx`.
    * `[ ]` Add the route `/am/assets/:id` to `App.tsx`.
    * `[ ]` Make the `DataGrid` rows navigate to this page on `onRowClick`, passing the asset's ID.
    * `[ ]` On page load, get the `id` from `useParams` and fetch data from `GET /v1/assets/{qr_code_id}`.
    * `[ ]` Display the `Asset Name` (Typography) and `Status` (MUI `Chip`) in the header as per `[R-AM-07]`.
    * `[ ]` Add the MUI `Tabs` component with "Details" and "History" tabs.
    * `[ ]` **Details Tab:** Create a component to render the asset's details, grouped by section (Identification, Status, Maintenance) using MUI `Card`s as per `[R-AM-09]`.
    * `[ ]` **History Tab:** Create a hook to fetch `GET /analytics/asset-history/{id}`.
    * `[ ]` **History Tab:** Render the history using an MUI `List` as per `[R-AM-10]`.
* **Create/Edit Asset (Admin):**
    * `[ ]` Create a component `components/forms/AssetForm.tsx`.
    * `[ ]` Build the form fields as per `[R-AM-14]` (Name, QR Code, Status `Select`, `DatePicker` for dates).
    * `[ ]` Create a component `components/modals/AssetFormModal.tsx` that renders `AssetForm.tsx` inside an MUI `Modal`.
    * `[ ]` Add a "+ Create New Asset" `Button` to the `AssetInventory.tsx` page.
    * `[ ]` Use the `AuthContext` to conditionally render this button *only* if `user.role === 'admin'`.
    * `[ ]` Make the button open the `AssetFormModal`.
    * `[ ]` Implement the `onSubmit` logic in the form to call `POST /v1/assets` for creation.
    * `[ ]` Add logic to handle "edit" mode (pre-filling the form) and calling `PUT /v1/assets/{id}` with the `version` field.

---

## 4. Task List: Asset Management - Maintenance (Tasks)

This covers the Kanban board for task management.

* **Kanban Board Setup:**
    * `[ ]` Create a new page component: `pages/Maintenance.tsx`.
    * `[ ]` Add the route `/am/maintenance` to `App.tsx`.
    * `[ ]` Install a drag-and-drop library (e.g., `@hello-pangea/dnd`).
    * `[ ]` Create a `KanbanColumn.tsx` component (props: `title`, `tasks`).
    * `[ ]` Create a `TaskCard.tsx` component (props: `task`) to display task info.
    * `[ ]` Create a hook `hooks/useIssues.ts` to fetch all issues (`GET /v1/issues`).
    * `[ ]` On the page, organize the fetched issues into columns based on their `status`.
* **Drag-and-Drop:**
    * `[ ]` Wrap the board in the `DragDropContext`.
    * `[ ]` Make `KanbanColumn` a `Droppable` and `TaskCard` a `Draggable`.
    * `[ ]` Implement the `onDragEnd` handler.
    * `[ ]` In `onDragEnd`, update the task's status in your local state.
    * `[ ]` (API) Also in `onDragEnd`, trigger an API call `PUT /v1/issues/{id}` with the new `status`.
* **Task Detail View (Drawer):**
    * `[ ]` Create a `TaskDetailDrawer.tsx` component using MUI `Drawer`.
    * `[ ]` Manage the open/closed state of the drawer in `Maintenance.tsx`.
    * `[ ]` Make `TaskCard.tsx` open the drawer on click, setting the `selectedTask`.
    * `[ ]` Inside the drawer, display all task details as per `[R-AM-M-04]` and `[R-AM-M-05]` (description, photos, etc.).
    * `[ ]` Add a `Select` dropdown to assign a user (fetch users from `GET /users`).
    * `[ ]` Add a `DatePicker` to set a due date.
    * `[ ]` (API) When assignee or due date changes, call `PUT /v1/issues/{id}`.
    * `[ ]` Create a simple `AuditTrail.tsx` component to list status changes.

---

## 5. Task List: Asset Management - Reports

This covers the analytics and charts for the module.

* **Page & Chart Setup:**
    * `[ ]` Create a new page component: `pages/AssetReports.tsx`.
    * `[ ]` Add the route `/am/reports` to `App.tsx`.
    * `[ ]` Install a charting library (e.g., `recharts`).
    * `[ ]` Add filter components (e.g., `DatePicker` for date range) to the top of the page.
* **Chart 1: Trend Analysis:**
    * `[ ]` Create a `components/charts/TrendAnalysisChart.tsx` component.
    * `[ ]` Create a hook to fetch data from `GET /analytics/issue-trends`, passing in any date filters.
    * `[ ]` Use `recharts` (`LineChart`, `XAxis`, `YAxis`, `Tooltip`, `Line`) to render the data.
    * `[ ]` Wrap the chart in an MUI `Card` with a title.
* **Chart 2: Top 5 Assets:**
    * `[ ]` Create a `components/charts/TopAssetsChart.tsx` component.
    * `[ ]` Create a hook to fetch data from `GET /analytics/top-issues?context=assets`.
    * `[ ]` Use `recharts` (`BarChart`, `XAxis`, `YAxis`, `Tooltip`, `Bar`) to render the data.
    * `[ ]` Wrap the chart in an MUI `Card` with a title.
* **Chart 3: Mean Time To Resolve (MTTR):**
    * `[ ]` Create a hook to fetch data from `GET /analytics/mttr`.
    * `[ ]` Reuse the `CounterWidget.tsx` component (from Dashboard) to display this single statistic.
    * `[ ]` Pass the fetched MTTR value (e.g., "2.5 days") as the `count` prop.

---

## 6. Task List: Asset Management - Settings

This covers the configuration area for the module.

* **Page & Tab Setup:**
    * `[ ]` Create a new page component: `pages/AssetSettings.tsx`.
    * `[ ]` Add the route `/am/settings` to `App.tsx` and protect it using `ProtectedRoute` (admin only).
    * `[ ]` Use MUI `Tabs` to create a tabbed interface for "Roles," "Integrations," and "Asset Types."
* **Roles Tab:**
    * `[ ]` Create a hook to fetch `GET /v1/roles?module=assets`.
    * `[ ]` Display the roles in an MUI `List` or simple `Table`.
    * `[ ]` Add a "New Role" button and modal (form) to call `POST /v1/roles?module=assets`.
* **Integrations Tab:**
    * `[ ]` Create a simple placeholder component.
    * `[ ]` Display static text and an icon (e.g., "Telematics and ERP integrations coming soon.").
* **Asset Types Tab:**
    * `[ ]` Create a hook to fetch `GET /v1/config/asset-types`.
    * `[ ]` Display the list of asset types.
    * `[ ]` Add an MUI `TextField` and "Add" `Button`.
    * `[ ]` (API) Implement the `onClick` handler for the button to call `POST /v1/config/asset-types` with the new type name.