# System Configuration Module Documentation

## Overview

The System Configuration Module manages key-value configuration settings for the application. It supports multiple data types (STRING, NUMBER, BOOLEAN, JSON) and module-based grouping.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 714-724).

### Key Fields

- `id` - UUID primary key
- `config_key` - Unique configuration key
- `config_value` - Configuration value
- `data_type` - STRING, NUMBER, BOOLEAN, JSON
- `module` - Module name
- `is_active` - Active status
- `is_deleted` - Soft delete flag

## API Endpoints

### Get All Configurations (Paginated)
```
GET /api/system-configuration?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<SystemConfigurationDTO>`

### Get Configuration by ID
```
GET /api/system-configuration/{id}
```
**Auth:** Any authenticated user  
**Response:** `SystemConfigurationDTO`

### Get Configuration by Key
```
GET /api/system-configuration/key/{key}
```
**Auth:** Any authenticated user  
**Response:** `SystemConfigurationDTO`

### Create Configuration (Admin only)
```
POST /api/system-configuration
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateSystemConfigurationRequest`
```json
{
  "configKey": "MAX_LOGIN_ATTEMPTS",
  "configValue": "5",
  "description": "Maximum login attempts before lockout",
  "dataType": "NUMBER",
  "module": "AUTH"
}
```

### Update Configuration (Admin only)
```
PUT /api/system-configuration/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateSystemConfigurationRequest`
```json
{
  "configValue": "3",
  "description": "Updated max login attempts"
}
```

### Delete Configuration (Admin only)
```
DELETE /api/system-configuration/{id}
```
**Auth:** `ADMIN` role  
**Response:** Success message

### Get Deleted Configurations (Admin only)
```
GET /api/system-configuration/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<SystemConfigurationDTO>`

## DTOs

### SystemConfigurationDTO (Response)
```json
{
  "id": "uuid",
  "configKey": "MAX_LOGIN_ATTEMPTS",
  "configValue": "5",
  "description": "Maximum login attempts before lockout",
  "dataType": "NUMBER",
  "module": "AUTH",
  "isActive": true,
  "isDeleted": false
}
```

### CreateSystemConfigurationRequest
- `configKey` (required) - Unique configuration key (auto-converted to UPPER_SNAKE_CASE)
- `configValue` (required) - Configuration value
- `description` (optional) - Description
- `dataType` (optional) - STRING, NUMBER, BOOLEAN, JSON (default: STRING)
- `module` (optional) - Module name

### UpdateSystemConfigurationRequest
All fields are optional:
- `configValue`
- `description`
- `dataType`
- `module`
- `isActive`

## Validations

- **Config Key unique** - No duplicate keys (case-insensitive, auto-converted to UPPER_SNAKE_CASE)
- **Data Type** - Must be: STRING, NUMBER, BOOLEAN, or JSON
- **NotBlank** - `configKey` and `configValue` are required on create

## Service Methods

### SystemConfigurationService

- `getAllConfigurations(Pageable)` - Get all active configurations (paginated)
- `getConfigurationById(String id)` - Get configuration by ID
- `getConfigurationByKey(String key)` - Get configuration by key
- `createConfiguration(CreateSystemConfigurationRequest)` - Create new configuration
- `updateConfiguration(String id, UpdateSystemConfigurationRequest)` - Update configuration
- `deleteConfiguration(String id)` - Soft delete configuration
- `getDeletedConfigurations(Pageable)` - Get deleted configurations (paginated)

## Frontend Integration Guide

### 1. Get All Configurations
```javascript
const response = await fetch(`/api/system-configuration?page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
// data is an array of SystemConfigurationDTO
```

### 2. Get Configuration by Key
```javascript
const response = await fetch(`/api/system-configuration/key/MAX_LOGIN_ATTEMPTS`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 3. Create Configuration (Admin)
```javascript
const response = await fetch('/api/system-configuration', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    configKey: "MAX_LOGIN_ATTEMPTS",
    configValue: "5",
    description: "Maximum login attempts before lockout",
    dataType: "NUMBER",
    module: "AUTH"
  })
});
const { data } = await response.json();
```

### 4. Update Configuration (Admin)
```javascript
const response = await fetch(`/api/system-configuration/${configId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    configValue: "3",
    description: "Updated max login attempts"
  })
});
```

### 5. Delete Configuration (Admin)
```javascript
const response = await fetch(`/api/system-configuration/${configId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

### 6. Get Deleted Configurations (Admin)
```javascript
const response = await fetch(`/api/system-configuration/deleted?page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
const { data } = await response.json();
```

## Notes

- Configurations are soft-deleted (`is_deleted` flag)
- Unique constraint on `configKey` (case-insensitive, stored as UPPER_SNAKE_CASE)
- `configKey` is auto-converted to uppercase with underscores
- Valid `dataType` values: STRING, NUMBER, BOOLEAN, JSON
- Useful for feature flags, system limits, and module-specific settings

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler`:

- `IllegalArgumentException` → 400 Bad Request
- Generic exceptions → 500 Internal Server Error

Example error response:
```json
{
  "success": false,
  "message": "La clave de configuración ya existe: MAX_LOGIN_ATTEMPTS"
}
```
