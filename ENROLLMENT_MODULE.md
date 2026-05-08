# Enrollment Module Documentation

## Overview

The Enrollment Module manages student enrollments (inscriptions) in courses within academic periods. Each enrollment links a student to a course during a specific academic period, with an optional group assignment.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 275-290).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (REQUIRED)
- `course_id` - Reference to course (REQUIRED)
- `academic_period_id` - Reference to academic period (REQUIRED)
- `group_id` - Reference to academic group (optional)
- `status` - Enrollment status: ENROLLED, APPROVED, FAILED, WITHDRAWN
- `is_active` - Active status
- `is_deleted` - Soft delete flag

### Constraints

- `UNIQUE (student_id, course_id, academic_period_id)` - A student cannot be enrolled in the same course twice in the same period
- `status IN ('ENROLLED', 'APPROVED', 'FAILED', 'WITHDRAWN')`

## API Endpoints

### Get All Enrollments (Active)
```
GET /api/enrollments?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<EnrollmentDTO>`

### Get Enrollment by ID
```
GET /api/enrollments/{id}
```
**Auth:** Any authenticated user  
**Response:** `EnrollmentDTO`

### Create Enrollment (Admin)
```
POST /api/enrollments
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateEnrollmentRequest`
```json
{
  "studentId": "uuid-of-student",
  "courseId": "uuid-of-course",
  "academicPeriodId": "uuid-of-period",
  "groupId": "uuid-of-group",
  "status": "ENROLLED"
}
```

### Update Enrollment (Admin)
```
PUT /api/enrollments/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateEnrollmentRequest` (all fields optional)

### Delete Enrollment (Admin)
```
DELETE /api/enrollments/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Enrollments (Admin)
```
GET /api/enrollments/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<EnrollmentDTO>` (paginated)

## DTOs

### EnrollmentDTO (Response)
```json
{
  "id": "uuid",
  "status": "ENROLLED",
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2025-01-15",
  "studentId": "uuid",
  "studentName": "Juan Pérez López",
  "enrollmentNumber": "2025-001",
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación",
  "academicPeriodId": "uuid",
  "academicPeriodName": "2025-1",
  "groupId": "uuid",
  "groupName": "A"
}
```

### CreateEnrollmentRequest
| Field | Required | Description |
|-------|----------|-------------|
| `studentId` | ✅ | Student UUID |
| `courseId` | ✅ | Course UUID |
| `academicPeriodId` | ✅ | Academic period UUID |
| `groupId` | ❌ | Academic group UUID |
| `status` | ❌ | Status (default: ENROLLED) |

### UpdateEnrollmentRequest
All fields are optional.

## Validations

- **Unique enrollment** - A student cannot be enrolled in the same course twice in the same period
- **All FKs required** - Student, Course, and Academic Period must exist
- **Group optional** - Must reference an existing group if provided
- **Status valid** - Must be one of: ENROLLED, APPROVED, FAILED, WITHDRAWN
- **Soft delete** - Records are marked, never physically deleted

## Frontend Integration Guide

### 1. List All Enrollments
```javascript
const response = await fetch('/api/enrollments?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Each item has resolved student name, course info, period name, group name
```

### 2. Create Enrollment (Admin)
```javascript
const response = await fetch('/api/enrollments', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    courseId: "uuid-of-course",
    academicPeriodId: "uuid-of-period",
    groupId: "uuid-of-group",
    status: "ENROLLED"
  })
});
```

### 3. Update Enrollment Status (Admin)
```javascript
const response = await fetch(`/api/enrollments/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ status: "APPROVED" })
});
```

### 4. Delete Enrollment (Admin)
```javascript
const response = await fetch(`/api/enrollments/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

## Error Responses

```json
// 400 - Duplicate enrollment
{ "success": false, "message": "El estudiante ya está inscrito en este curso para el período seleccionado" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores permitidos: ENROLLED, APPROVED, FAILED, WITHDRAWN" }

// 404 - Not found
{ "success": false, "message": "Inscripción no encontrada" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- Core module that links Student → Course → Period
- DTO includes resolved names for student, course, period, and group
- Enrollments with existing `grade`, `attendance`, `kardex` records cannot be deleted due to ON DELETE RESTRICT
- Status workflow: ENROLLED → APPROVED/FAILED/WITHDRAWN
- Paginated responses for list endpoints
