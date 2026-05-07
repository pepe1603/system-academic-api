# Student Module Documentation

## Overview

The Student Module manages student records in the system. Each student has a unique CURP (Mexican ID) and enrollment number, and belongs to a generation. This module provides CRUD operations with soft delete and pagination.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 232-268).

### Key Fields

- `id` - UUID primary key
- `user_id` - Reference to app user (optional)
- `enrollment_number` - Unique student enrollment number
- `curp` - Unique CURP (Mexican ID)
- `first_name` - Student first name
- `last_name` - Student last name
- `institutional_email` - School email
- `phone` - Contact phone
- `birth_date` - Date of birth
- `gender` - Gender (M, F, O)
- `enrollment_date` - Date of enrollment
- `generation_id` - Reference to generation
- `is_active` - Active status
- `is_deleted` - Soft delete flag

## API Endpoints

### Get All Students (Active)
```
GET /api/students?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<StudentDTO>`

### Get Student by ID
```
GET /api/students/{id}
```
**Auth:** Any authenticated user  
**Response:** `StudentDTO`

### Create Student (Admin)
```
POST /api/students
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateStudentRequest`
```json
{
  "curp": "PEPJ000101HDFRRN01",
  "enrollmentNumber": "2025-001",
  "firstName": "Juan",
  "lastName": "Pérez López",
  "generationId": "uuid-of-generation",
  "institutionalEmail": "juan.perez@enez.edu.mx",
  "phone": "9611234567",
  "birthDate": "2000-01-01",
  "gender": "M",
  "enrollmentDate": "2025-01-15"
}
```

### Update Student (Admin)
```
PUT /api/students/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateStudentRequest` (all fields optional)

### Delete Student (Admin)
```
DELETE /api/students/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Students (Admin)
```
GET /api/students/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<StudentDTO>` (paginated)

## DTOs

### StudentDTO (Response)
```json
{
  "id": "uuid",
  "userId": "uuid",
  "enrollmentNumber": "2025-001",
  "curp": "PEPJ000101HDFRRN01",
  "firstName": "Juan",
  "lastName": "Pérez López",
  "institutionalEmail": "juan.perez@enez.edu.mx",
  "phone": "9611234567",
  "birthDate": "2000-01-01",
  "gender": "M",
  "enrollmentDate": "2025-01-15",
  "generationId": "uuid",
  "generationName": "Generación 2025",
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2025-01-10"
}
```

### CreateStudentRequest
| Field | Required | Description |
|-------|----------|-------------|
| `curp` | ✅ | Unique CURP (18 chars) |
| `enrollmentNumber` | ✅ | Unique enrollment number |
| `firstName` | ✅ | Student first name |
| `lastName` | ✅ | Student last name |
| `generationId` | ✅ | Generation UUID |
| `userId` | ❌ | App user UUID |
| `institutionalEmail` | ❌ | School email |
| `phone` | ❌ | Contact phone |
| `birthDate` | ❌ | Date of birth |
| `gender` | ❌ | Gender (M, F, O) |
| `enrollmentDate` | ❌ | Enrollment date |

### UpdateStudentRequest
All fields are optional.

## Validations

- **CURP unique** - No duplicate CURP across active students (auto-uppercased)
- **Enrollment number unique** - No duplicate enrollment numbers (auto-uppercased)
- **CURP required** - Must not be blank
- **Name required** - Must not be blank
- **Generation required** - Must reference an existing generation
- **Soft delete** - Records are marked, never physically deleted

## Existing Seed Data

No student seed data is pre-loaded. Students are typically created through the registration flow or via this admin module.

## Relationships (for future modules)

- `enrollment` → depends on `student` (ON DELETE RESTRICT)
- `kardex` → depends on `student` (ON DELETE RESTRICT)
- `report_card` → depends on `student` (ON DELETE RESTRICT)
- `extraordinary_exam` → depends on `student` (ON DELETE RESTRICT)
- `certificate` → depends on `student` (ON DELETE RESTRICT)
- `guardian` → depends on `student` (ON DELETE CASCADE)

## Frontend Integration Guide

### 1. List All Students
```javascript
const response = await fetch('/api/students?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of StudentDTO with generation name
```

### 2. Create Student (Admin)
```javascript
const response = await fetch('/api/students', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    curp: "PEPJ000101HDFRRN01",
    enrollmentNumber: "2025-001",
    firstName: "Juan",
    lastName: "Pérez López",
    generationId: "uuid-of-generation",
    institutionalEmail: "juan.perez@enez.edu.mx",
    phone: "9611234567",
    birthDate: "2000-01-01",
    gender: "M"
  })
});
```

### 3. Update Student (Admin)
```javascript
const response = await fetch(`/api/students/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ phone: "9619876543", isActive: true })
});
```

### 4. Delete Student (Admin)
```javascript
const response = await fetch(`/api/students/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

### 5. View Deleted Students (Admin)
```javascript
const response = await fetch('/api/students/deleted?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
const { data } = await response.json();
// data is array of deleted StudentDTO
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un estudiante con ese CURP" }

// 404 - Not found
{ "success": false, "message": "Estudiante no encontrado" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- CURP and enrollment number are automatically uppercased
- Students with existing `enrollment`, `kardex`, or `report_card` records cannot be deleted due to ON DELETE RESTRICT
- DTO includes resolved generation name for frontend convenience
- Paginated responses for list endpoints
