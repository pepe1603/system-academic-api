# Kardex Module Documentation

## Overview

The Kardex Module manages the academic history (kardex) of students. Each record represents a student's attempt at a course within a specific academic semester, tracking grades, status, and officialization.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 327-358).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (RESTRICT)
- `course_id` - Reference to course (RESTRICT)
- `academic_semester_id` - Reference to academic semester (RESTRICT)
- `enrollment_id` - Reference to enrollment (SET NULL)
- `final_grade` - Numeric grade (0-100)
- `letter_grade` - Letter grade (A, B, C, D, F)
- `status` - ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE
- `attempt_number` - Attempt counter (default: 1)
- `enrollment_date` - Date of enrollment
- `approval_date` - Date of approval
- `official_folio` - Official folio number (UNIQUE)
- `is_officialized` - Whether the record is official
- `observations` - Notes

### Constraints

- `UNIQUE (student_id, course_id, academic_semester_id, attempt_number)`

## API Endpoints

### Get All Kardex Records
```
GET /api/kardex?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<KardexDTO>`

### Get Kardex by ID
```
GET /api/kardex/{id}
```
**Auth:** Any authenticated user  
**Response:** `KardexDTO`

### Get Kardex by Student
```
GET /api/kardex/by-student/{studentId}
```
**Auth:** Any authenticated user  
**Response:** `List<KardexDTO>`

### Create Kardex (Admin)
```
POST /api/kardex
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateKardexRequest`
```json
{
  "studentId": "uuid-of-student",
  "courseId": "uuid-of-course",
  "academicSemesterId": "uuid-of-semester",
  "enrollmentId": "uuid-of-enrollment",
  "status": "ENROLLED",
  "finalGrade": null,
  "attemptNumber": 1,
  "observations": ""
}
```

### Update Kardex (Admin)
```
PUT /api/kardex/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateKardexRequest` (all fields optional)

### Delete Kardex (Admin)
```
DELETE /api/kardex/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Kardex Records (Admin)
```
GET /api/kardex/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<KardexDTO>` (paginated)

## DTOs

### KardexDTO (Response)
```json
{
  "id": "uuid",
  "finalGrade": 85.50,
  "letterGrade": "B",
  "status": "APPROVED",
  "attemptNumber": 1,
  "enrollmentDate": "2025-01-15",
  "approvalDate": "2025-06-15",
  "officialFolio": "KAR-2025-001",
  "isOfficialized": true,
  "observations": "",
  "isDeleted": false,
  "createdAt": "2025-01-15",
  "studentId": "uuid",
  "studentName": "Juan Pérez López",
  "enrollmentNumber": "2025-001",
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación",
  "courseCredits": 8,
  "academicSemesterId": "uuid",
  "academicSemesterName": "2025-1",
  "enrollmentId": "uuid"
}
```

## Frontend Integration Guide

### 1. Get Student's Academic History
```javascript
const response = await fetch(`/api/kardex/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns all kardex records for that student with course info
```

### 2. Create Kardex Record (Admin)
```javascript
const response = await fetch('/api/kardex', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    courseId: "uuid-of-course",
    academicSemesterId: "uuid-of-semester",
    enrollmentId: "uuid-of-enrollment",
    status: "ENROLLED",
    attemptNumber: 1
  })
});
```

### 3. Update Grade (Admin)
```javascript
const response = await fetch(`/api/kardex/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    finalGrade: 92.5,
    letterGrade: "A",
    status: "APPROVED",
    approvalDate: "2025-06-15",
    isOfficialized: true,
    officialFolio: "KAR-2025-001"
  })
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe un registro kardex para este estudiante, curso, semestre e intento" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores: ENROLLED, APPROVED, FAILED, EXTRAORDINARY, DROPPED, VALIDATED, EQUIVALENCE" }

// 404 - Not found
{ "success": false, "message": "Registro kardex no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- Unique constraint: one record per student+course+semester+attempt
- Supports multiple attempts (retake courses)
- Officialization flow: set `is_officialized=true` + `officialFolio` for official records
- DTO includes resolved student name, course info, and semester name
- Extra endpoint: `GET /by-student/{studentId}`
