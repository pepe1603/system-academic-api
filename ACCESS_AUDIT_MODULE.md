# Access Audit Module Documentation

## Overview

The Access Audit Module records all access events in the system for security and compliance. Each log entry captures who performed what action, on which module, from which IP address, and whether it succeeded.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 730-741).

### Key Fields

- `id` - UUID primary key
- `user_id` - Reference to the user who performed the action
- `action` - Action performed (LOGIN, CREATE, UPDATE, DELETE, etc.)
- `module` - Module name (AUTH, STUDENTS, COURSES, etc.)
- `ip_address` - Client IP address (IPv4 or IPv6)
- `success` - Whether the action succeeded
- `metadata` - Additional JSON data
- `created_at` - Timestamp of the event

## API Endpoints

### Get All Audit Logs (Paginated)
```
GET /api/access-audit?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `Page<AccessAuditDTO>`

### Get Audit Logs with Filters
```
GET /api/access-audit?userId={uuid}&page=0&size=10
GET /api/access-audit?module=AUTH&page=0&size=10
GET /api/access-audit?action=LOGIN&page=0&size=10
GET /api/access-audit?success=true&page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `Page<AccessAuditDTO>`

### Get Audit Log by ID
```
GET /api/access-audit/{id}
```
**Auth:** Any authenticated user  
**Response:** `AccessAuditDTO`

### Delete Audit Log (Admin only)
```
DELETE /api/access-audit/{id}
```
**Auth:** `ADMIN` role  
**Response:** Success message

## DTOs

### AccessAuditDTO (Response)
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userEmail": "juan.perez@academic.com",
  "action": "LOGIN",
  "module": "AUTH",
  "ipAddress": "192.168.1.100",
  "success": true,
  "metadata": "{\"userAgent\": \"Mozilla/5.0...\"}",
  "createdAt": "2025-01-15T10:30:00"
}
```

## Validations

- This is an append-only log; records are created automatically by the system
- No manual creation or update endpoints exposed
- Admin can delete old records for cleanup
- The `metadata` field stores arbitrary JSON for extensibility

## Service Methods

### AccessAuditService

- `getAllAuditLogs(Pageable)` - Get all audit logs (paginated, newest first)
- `getAuditLogById(String id)` - Get audit log by ID
- `getAuditLogsByUserId(String userId, Pageable)` - Filter by user
- `getAuditLogsByModule(String module, Pageable)` - Filter by module
- `getAuditLogsByAction(String action, Pageable)` - Filter by action
- `getAuditLogsBySuccess(Boolean success, Pageable)` - Filter by success/failure
- `createAuditLog(AccessAuditDTO)` - Create audit log (system use)
- `deleteAuditLog(String id)` - Hard delete audit log (admin cleanup)

## Frontend Integration Guide

### 1. Get All Audit Logs
```javascript
const response = await fetch(`/api/access-audit?page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
// data.content is an array of AccessAuditDTO
```

### 2. Get Audit Logs by User
```javascript
const response = await fetch(`/api/access-audit?userId=${userId}&page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 3. Get Audit Logs by Module
```javascript
const response = await fetch(`/api/access-audit?module=AUTH&page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 4. Get Failed Actions
```javascript
const response = await fetch(`/api/access-audit?success=false&page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 5. Get Audit Log by ID
```javascript
const response = await fetch(`/api/access-audit/${auditId}`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 6. Delete Audit Log (Admin)
```javascript
const response = await fetch(`/api/access-audit/${auditId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

## Notes

- Audit logs are immutable (append-only); no update endpoint
- Records are created automatically by the system via `sp_log_access()` function
- Supports filtering by userId, module, action, and success status
- No soft delete; admin can hard-delete old records for cleanup
- Useful for security auditing, compliance, and troubleshooting
- The `metadata` JSONB field stores extensible context data

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler`:

- `IllegalArgumentException` → 400 Bad Request
- Generic exceptions → 500 Internal Server Error

Example error response:
```json
{
  "success": false,
  "message": "Registro de auditoría no encontrado"
}
```
