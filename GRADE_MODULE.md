# Grade Module Documentation

## Overview

The Grade Module manages student grades for each evaluation type within an enrollment. Each grade links a specific evaluation (e.g., "Primer Parcial") to a student's enrollment, with a score from 0 to 100.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 310-319).

### Key Fields

- `id` - UUID primary key
- `enrollment_id` - Reference to enrollment
- `evaluation_type_id` - Reference to evaluation type
- `score` - Numeric grade (0-100)
- `recorded_by` - User who recorded the grade
- `recorded_at` - Timestamp of recording

### Constraints

- `UNIQUE (enrollment_id, evaluation_type_id)` - Only one grade per evaluation per enrollment
- `CHECK (score BETWEEN 0 AND 100)`

## API Endpoints

### Get All Grades
```
GET /api/grades?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<GradeDTO>`

### Get Grade by ID
```
GET /api/grades/{id}
```
**Auth:** Any authenticated user  
**Response:** `GradeDTO`

### Get Grades by Enrollment
```
GET /api/grades/by-enrollment/{enrollmentId}
```
**Auth:** Any authenticated user  
**Response:** `List<GradeDTO>`

### Create Grade (Admin)
```
POST /api/grades
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateGradeRequest`
```json
{
  "enrollmentId": "uuid-of-enrollment",
  "evaluationTypeId": "uuid-of-evaluation-type",
  "score": 85.5
}
```

### Update Grade (Admin)
```
PUT /api/grades/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateGradeRequest`
```json
{
  "score": 90.0
}
```

### Delete Grade (Admin)
```
DELETE /api/grades/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Grades (Admin)
```
GET /api/grades/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<GradeDTO>` (paginated)

## DTOs

### GradeDTO (Response)
```json
{
  "id": "uuid",
  "score": 85.50,
  "recordedAt": "2025-03-15",
  "isDeleted": false,
  "enrollmentId": "uuid",
  "studentName": "Juan Pérez López",
  "enrollmentNumber": "2025-001",
  "evaluationTypeId": "uuid",
  "evaluationCode": "P1",
  "evaluationName": "Primer Parcial",
  "evaluationWeight": 25.00,
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación"
}
```

### CreateGradeRequest
| Field | Required | Validation |
|-------|----------|------------|
| `enrollmentId` | ✅ | Must exist |
| `evaluationTypeId` | ✅ | Must exist |
| `score` | ✅ | 0-100 |

### UpdateGradeRequest
| Field | Required | Validation |
|-------|----------|------------|
| `score` | ❌ | 0-100 |

## Validations

- **Unique per enrollment** - Only one grade per evaluation type per enrollment
- **Score range** - Must be between 0 and 100
- **Evaluation belongs to course** - The evaluation type must match the enrollment's course
- **Soft delete** - Records are marked, never physically deleted

## Frontend Integration Guide

### 1. List All Grades
```javascript
const response = await fetch('/api/grades?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
```

### 2. Get Grades by Enrollment
```javascript
const response = await fetch(`/api/grades/by-enrollment/${enrollmentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns all grades for that enrollment with evaluation type info
```

### 3. Create Grade (Admin)
```javascript
const response = await fetch('/api/grades', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    enrollmentId: "uuid-of-enrollment",
    evaluationTypeId: "uuid-of-evaluation-type",
    score: 85.5
  })
});
```

### 4. Update Grade (Admin)
```javascript
const response = await fetch(`/api/grades/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ score: 92.0 })
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe una calificación para esta evaluación en la inscripción seleccionada" }

// 400 - Mismatch
{ "success": false, "message": "El tipo de evaluación no pertenece al curso de la inscripción" }

// 404 - Not found
{ "success": false, "message": "Calificación no encontrada" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- One grade per evaluation type per enrollment (UNIQUE constraint)
- Evaluation type must belong to the same course as the enrollment (cross-validation)
- DTO includes resolved student name, course info, and evaluation type details
- Extra endpoint: `GET /by-enrollment/{enrollmentId}` to get all grades for a specific enrollment
