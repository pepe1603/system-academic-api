# Teacher Module Documentation

## Overview

The Teacher Module manages teacher/docent records with personal information, RFC, CURP, and contact details.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 173-189).

### Key Fields

- `id` - UUID primary key
- `user_id` - Reference to app user (SET NULL, UNIQUE)
- `employee_number` - Employee number (UNIQUE)
- `rfc` - Mexican tax ID (UNIQUE)
- `curp` - Mexican CURP (UNIQUE)
- `first_name` / `last_name` - Full name
- `institutional_email` / `secondary_email` - Email addresses
- `phone` / `secondary_phone` - Phone numbers
- `is_active` - Active status

## API Endpoints

### Get All Teachers
```
GET /api/teachers?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<TeacherDTO>` (paginated)

### Get Teacher by ID
```
GET /api/teachers/{id}
```
**Auth:** Any authenticated user  
**Response:** `TeacherDTO`

### Create Teacher (Admin)
```
POST /api/teachers
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateTeacherRequest`
```json
{
  "firstName": "María",
  "lastName": "García López",
  "employeeNumber": "DOC-001",
  "rfc": "GALM850101XXX",
  "curp": "GALM850101MDFRRN01",
  "institutionalEmail": "maria.garcia@academia.edu",
  "phone": "555-1234-5678"
}
```

### Update Teacher (Admin)
```
PUT /api/teachers/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateTeacherRequest` (all fields optional)

### Delete Teacher (Admin)
```
DELETE /api/teachers/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Teachers (Admin)
```
GET /api/teachers/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration Guide

### 1. List Teachers
```javascript
const response = await fetch('/api/teachers?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is an array of teachers
```

### 2. Create Teacher (Admin)
```javascript
const response = await fetch('/api/teachers', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    firstName: "Juan",
    lastName: "Pérez López",
    employeeNumber: "DOC-002",
    rfc: "PELJ850101XXX",
    curp: "PELJ850101HDFRRN00",
    institutionalEmail: "juan.perez@academia.edu",
    phone: "555-9876-5432"
  })
});
```

### 3. Update Teacher (Admin)
```javascript
const response = await fetch(`/api/teachers/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    phone: "555-1111-2222",
    institutionalEmail: "nuevo.email@academia.edu"
  })
});
```

## Error Responses

```json
// 400 - Duplicate RFC
{ "success": false, "message": "El RFC ya está registrado" }

// 400 - Duplicate CURP
{ "success": false, "message": "El CURP ya está registrado" }

// 400 - Duplicate employee number
{ "success": false, "message": "El número de empleado ya está registrado" }

// 404 - Not found
{ "success": false, "message": "Docente no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- RFC, CURP, and employee_number are unique
- Teacher can be linked to a User (for system login) via `user_id`
- This module is a dependency for: Certificate (signers), Extraordinary Exam (examiner)