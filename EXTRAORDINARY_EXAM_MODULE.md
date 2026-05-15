# Extraordinary Exam Module Documentation

## Overview

The Extraordinary Exam Module manages extraordinary exams for students who need to retake a course. It handles scheduling, grading, payment tracking, and examiner assignment.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 536-567).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (RESTRICT)
- `course_id` - Reference to course (RESTRICT)
- `academic_semester_id` - Reference to academic semester (SET NULL)
- `attempt_number` - Attempt counter (default: 1)
- `status` - SCHEDULED, APPLIED, APPROVED, FAILED, CANCELLED, NO_SHOW
- `scheduled_date` / `application_date` - Key dates
- `application_time` / `application_location` - Logistics
- `previous_grade` / `grade` / `grade_letter` - Grades
- `examiner_id` - Reference to teacher (examiner)
- `cost` - Exam cost (default: 0)
- `payment_receipt` / `payment_folio` - Payment info

### Constraints

- `UNIQUE (student_id, course_id, attempt_number)`

## API Endpoints

### Get All Exams
```
GET /api/extraordinary-exams?page=0&size=10
```
**Auth:** Any authenticated user

### Get Exam by ID
```
GET /api/extraordinary-exams/{id}
```
**Auth:** Any authenticated user

### Get Exams by Student
```
GET /api/extraordinary-exams/by-student/{studentId}
```
**Auth:** Any authenticated user

### Get Exams by Course
```
GET /api/extraordinary-exams/by-course/{courseId}
```
**Auth:** Any authenticated user

### Create Exam (Admin)
```
POST /api/extraordinary-exams
Content-Type: application/json
```
**Auth:** `ADMIN` role
```json
{
  "studentId": "uuid-of-student",
  "courseId": "uuid-of-course",
  "academicSemesterId": "uuid-of-semester",
  "attemptNumber": 1,
  "scheduledDate": "2025-07-15",
  "applicationTime": "10:00",
  "applicationLocation": "Aula 101",
  "previousGrade": 55.0,
  "examinerId": "uuid-of-teacher",
  "cost": 500.00,
  "paymentReceipt": "recibo.pdf",
  "paymentFolio": "PAY-001"
}
```

### Update Exam (Admin)
```
PUT /api/extraordinary-exams/{id}
```
**Auth:** `ADMIN` role
```json
{
  "status": "APPROVED",
  "applicationDate": "2025-07-15",
  "grade": 78.5,
  "gradeLetter": "C"
}
```

### Delete Exam (Admin)
```
DELETE /api/extraordinary-exams/{id}
```
**Auth:** `ADMIN` role (soft delete)

### Get Deleted Exams (Admin)
```
GET /api/extraordinary-exams/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration

```javascript
// Get student's extraordinary exams
const response = await fetch(`/api/extraordinary-exams/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();

// Create exam (Admin)
await fetch('/api/extraordinary-exams', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    courseId: "uuid-of-course",
    academicSemesterId: "uuid-of-semester",
    scheduledDate: "2025-07-15",
    examinerId: "uuid-of-teacher",
    cost: 500.00
  })
});

// Record grade (Admin)
await fetch(`/api/extraordinary-exams/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: "APPROVED",
    applicationDate: "2025-07-15",
    grade: 85.0,
    gradeLetter: "B"
  })
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe un examen extraordinario para este estudiante, curso e intento" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores: SCHEDULED, APPLIED, APPROVED, FAILED, CANCELLED, NO_SHOW" }

// 404 - Not found
{ "success": false, "message": "Examen extraordinario no encontrado" }
```

## Notes

- Soft delete only
- Unique: one exam per student + course + attempt
- Status workflow: SCHEDULED → APPLIED → APPROVED/FAILED, or CANCELLED/NO_SHOW
- Payment fields: cost, payment_receipt, payment_folio
- Examiner is a Teacher reference