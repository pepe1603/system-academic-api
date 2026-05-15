# Retake Exam Module Documentation

## Overview

The Retake Exam Module manages students who need to retake a course (repite materia). It tracks which students are retaking which courses in which semester.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 572-588).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (RESTRICT)
- `course_id` - Reference to course (RESTRICT)
- `academic_semester_id` - Current semester (RESTRICT)
- `origin_semester_id` - Original semester where course was failed
- `previous_average` - Previous failing grade
- `status` - ENROLLED, etc.

### Constraints

- `UNIQUE (student_id, course_id, academic_semester_id)`

## API Endpoints

### Get All Retake Exams
```
GET /api/retake-exams?page=0&size=10
```
**Auth:** Any authenticated user

### Get Retake Exam by ID
```
GET /api/retake-exams/{id}
```
**Auth:** Any authenticated user

### Get by Student
```
GET /api/retake-exams/by-student/{studentId}
```
**Auth:** Any authenticated user

### Get by Course
```
GET /api/retake-exams/by-course/{courseId}
```
**Auth:** Any authenticated user

### Get by Semester
```
GET /api/retake-exams/by-semester/{semesterId}
```
**Auth:** Any authenticated user

### Create (Admin)
```
POST /api/retake-exams
Content-Type: application/json
```
**Auth:** `ADMIN` role
```json
{
  "studentId": "uuid-of-student",
  "courseId": "uuid-of-course",
  "academicSemesterId": "uuid-of-semester",
  "originSemesterId": "uuid-of-origin-semester",
  "previousAverage": 55.0
}
```

### Update (Admin)
```
PUT /api/retake-exams/{id}
```
**Auth:** `ADMIN` role

### Delete (Admin)
```
DELETE /api/retake-exams/{id}
```
**Auth:** `ADMIN` role (soft delete)

### Get Deleted (Admin)
```
GET /api/retake-exams/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration

```javascript
// Get retake exams for a student
const response = await fetch(`/api/retake-exams/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();

// Create retake (Admin)
await fetch('/api/retake-exams', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    courseId: "uuid-of-course",
    academicSemesterId: "uuid-of-semester",
    previousAverage: 55.0
  })
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe un registro de retake para este estudiante, curso y semestre" }

// 404 - Not found
{ "success": false, "message": "Registro de retake no encontrado" }
```

## Notes

- Soft delete only
- Unique: one retake per student + course + semester
- `originSemesterId` tracks which semester the course was originally failed in