# SafeWork API Postman Collection

This directory contains Postman collection and environment files for testing the SafeWork API.

## Files

- **SafeWork_API_Collection.postman_collection.json** - Complete API collection with all endpoints
- **SafeWork_Local_Environment.postman_environment.json** - Environment variables for local development

## How to Import into Postman

1. Open Postman application
2. Click the **Import** button in the top left
3. Select both JSON files:
   - `SafeWork_API_Collection.postman_collection.json`
   - `SafeWork_Local_Environment.postman_environment.json`
4. Click **Import**

## Setting Up the Environment

1. In Postman, select **SafeWork Local Development** from the environment dropdown (top right)
2. The environment is pre-configured with:
   - `baseUrl`: http://localhost:8081/api
   - Variables for storing tokens and IDs

## Collection Structure

The collection is organized into folders:

### 1. **Authentication**
- Login as Admin (sets token automatically)
- Login as Supervisor (for testing role-based access)
- Login as Inspector
- Login with Invalid Credentials (error testing)

### 2. **Assets**
- Get All Assets (with pagination)
- Get Asset by ID
- Create Asset (Admin only)
- Update Asset
- Delete Asset
- Create Asset as Supervisor (should fail - testing RBAC)

### 3. **Users**
- Get All Users
- Get User by ID
- Create User
- Update User
- Delete User

### 4. **Departments**
- Get All Departments
- Create Department
- Get Department by ID
- Update Department
- Delete Department

### 5. **Locations**
- Get All Locations
- Create Location
- Get Location by ID
- Update Location
- Delete Location

### 6. **Suppliers**
- Get All Suppliers
- Create Supplier
- Get Supplier by ID
- Update Supplier
- Delete Supplier

### 7. **Error Cases**
- Unauthorized (no token)
- Not Found (invalid ID)
- Bad Request (invalid data)

## Testing Workflow

### Quick Start
1. Start the SafeWork application
2. Run **Authentication > Login as Admin** first (this sets the token)
3. Test any other endpoint - the token is automatically included

### Complete Test Suite
1. Run the entire **Authentication** folder
2. Run the **Assets** folder to test CRUD operations
3. Test other domains as needed
4. Run **Error Cases** to verify error handling

### Role-Based Access Testing
1. Login as Admin to get admin token
2. Login as Supervisor to get supervisor token
3. Try "Create Asset (Supervisor - Should Fail)" to verify RBAC

## Features

### Automatic Token Management
- Login requests automatically save tokens to environment variables
- All authenticated requests use `{{token}}` variable
- Different tokens for different roles (admin, supervisor, inspector)

### Dynamic Data
- Uses Postman variables like `{{$randomInt}}` and `{{$randomUUID}}` for unique test data
- Automatically captures and stores IDs from create operations

### Test Scripts
- Each request includes test scripts to verify:
  - Correct status codes
  - Response structure
  - Business logic (e.g., role permissions)

### Environment Variables
The collection uses these environment variables:
- `baseUrl` - API base URL
- `token` - JWT token for current user
- `supervisorToken` - Token for supervisor role
- `inspectorToken` - Token for inspector role
- `assetId`, `userId`, `departmentId`, etc. - IDs for testing

## Troubleshooting

### 401 Unauthorized
- Run the login request first to get a fresh token
- Check that the environment is selected

### 403 Forbidden
- This is expected for role-based restrictions
- Admin role is required for create/update/delete operations

### Connection Refused
- Ensure the SafeWork application is running
- Default port is 8081
- Check the `baseUrl` in environment settings

## Test Users

Default users created by DataSeeder:
- **Admin**: admin@apex.com / password
- **Supervisor**: supervisor@apex.com / password
- **Inspector**: inspector@apex.com / password

## Notes

- Tokens expire after 24 hours
- The collection includes pagination parameters for list endpoints
- All timestamps are in ISO 8601 format
- Version field is used for optimistic locking in updates