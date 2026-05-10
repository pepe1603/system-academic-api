# Attendance Module Documentation

## Overview

The Attendance Module tracks student attendance for each enrollment. Each record represents a student's presence status on a specific date, with support for justifications.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 448-467).

### Key Fields

- `id` - UUID primary key
- `enrollment_id` - Reference to enrollment (NOT NULL)
- `attendance_date` - Date of class (NOT NULL)
- `status` - PRESENT, ABSENT, JUSTIFIED, LATE
- `class_time` - Time of class
- `subject_code` - Subject code
- `observations` - Notes
- `justified_by` - User who justified the absence
- `justification_date` - When it was justified

### Constraints

- `UNIQUE (enrollment_id, attendance_date)` - Only one record per student per day
- `CHECK (status IN ('PRESENT','ABSENT','JUSTIFIED','LATE'))`

## API Endpoints

### Get All Attendances
```
GET /api/attendances?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<AttendanceDTO>`

### Get Attendance by ID
```
GET /api/attendances/{id}
```
**Auth:** Any authenticated user  
**Response:** `AttendanceDTO`

### Get Attendances by Enrollment
```
GET /api/attendances/by-enrollment/{enrollmentId}
```
**Auth:** Any authenticated user  
**Response:** `List<AttendanceDTO>`

### Create Attendance (Admin)
```
POST /api/attendances
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateAttendanceRequest`
```json
{
  "enrollmentId": "uuid-of-enrollment",
  "attendanceDate": "2025-03-17",
  "status": "PRESENT",
  "classTime": "07:00-09:00",
  "observations": ""
}
```

### Update Attendance (Admin)
```
PUT /api/attendances/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateAttendanceRequest` (all fields optional)

### Delete Attendance (Admin)
```
DELETE /api/attendances/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Attendances (Admin)
```
GET /api/attendances/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<AttendanceDTO>` (paginated)

## DTOs

### AttendanceDTO (Response)
```json
{
  "id": "uuid",
  "attendanceDate": "2025-03-17",
  "status": "PRESENT",
  "classTime": "07:00-09:00",
  "subjectCode": "LEP101",
  "observations": "",
  "justificationDate": null,
  "recordedAt": "2025-03-17",
  "isDeleted": false,
  "enrollmentId": "uuid",
  "studentName": "Juan Pérez López",
  "enrollmentNumber": "2025-001",
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación"
}
```

### CreateAttendanceRequest
| Field | Required | Description |
|-------|----------|-------------|
| `enrollmentId` | ✅ | Enrollment UUID |
| `attendanceDate` | ✅ | Date of class |
| `status` | ❌ | Status (default: PRESENT) |
| `classTime` | ❌ | Time of class |
| `subjectCode` | ❌ | Subject code |
| `observations` | ❌ | Notes |

### UpdateAttendanceRequest
All fields are optional + `justifiedBy`, `justificationDate`.

## Validations

- **Unique per day** - Only one attendance per enrollment per date
- **Enrollment required** - Must reference an existing enrollment
- **Status valid** - Must be one of: PRESENT, ABSENT, JUSTIFIED, LATE
- **Soft delete** - Records are marked, never physically deleted

## Frontend Integration Guide

### 1. List All Attendances
```javascript
const response = await fetch('/api/attendances?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
```

### 2. Get By Enrollment
```javascript
const response = await fetch(`/api/attendances/by-enrollment/${enrollmentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns all attendance records for that enrollment
```

### 3. Create Attendance (Admin)
```javascript
const response = await fetch('/api/attendances', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    enrollmentId: "uuid-of-enrollment",
    attendanceDate: "2025-03-17",
    status: "PRESENT"
  })
});
```

### 4. Justify Absence (Admin)
```javascript
const response = await fetch(`/api/attendances/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: "JUSTIFIED",
    justifiedBy: "uuid-of-user",
    justificationDate: "2025-03-18"
  })
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe un registro de asistencia para esta fecha" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores permitidos: PRESENT, ABSENT, JUSTIFIED, LATE" }

// 404 - Not found
{ "success": false, "message": "Registro de asistencia no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- One record per student per day (UNIQUE constraint)
- DTO includes resolved student name, course info
- Extra endpoint: `GET /by-enrollment/{enrollmentId}`
