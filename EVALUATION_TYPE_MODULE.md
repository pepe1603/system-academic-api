# Evaluation Type Module Documentation

## Overview

The Evaluation Type Module manages evaluation types (exams, assignments, projects) for courses. Each course can have multiple evaluation types with different weights that sum to 100%.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 297-306).

### Key Fields

- `id` - UUID primary key
- `course_id` - Reference to course
- `code` - Evaluation code (e.g., "P1", "P2", "EX")
- `name` - Evaluation name (e.g., "Primer Parcial")
- `weight` - Weight percentage (0-100)
- `is_active` - Active status

## API Endpoints

### Get All Evaluation Types (Active)
```
GET /api/evaluation-types?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<EvaluationTypeDTO>`

### Get Evaluation Type by ID
```
GET /api/evaluation-types/{id}
```
**Auth:** Any authenticated user  
**Response:** `EvaluationTypeDTO`

### Get Evaluation Types by Course
```
GET /api/evaluation-types/by-course/{courseId}
```
**Auth:** Any authenticated user  
**Response:** `List<EvaluationTypeDTO>`

### Create Evaluation Type (Admin)
```
POST /api/evaluation-types
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateEvaluationTypeRequest`
```json
{
  "courseId": "uuid-of-course",
  "code": "P1",
  "name": "Primer Parcial",
  "weight": 25
}
```

### Update Evaluation Type (Admin)
```
PUT /api/evaluation-types/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateEvaluationTypeRequest` (all fields optional)

### Delete Evaluation Type (Admin)
```
DELETE /api/evaluation-types/{id}
```
**Auth:** `ADMIN` role  
**Note:** Physical delete from database

### Get Inactive Evaluation Types (Admin)
```
GET /api/evaluation-types/inactive?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<EvaluationTypeDTO>` (paginated)

## DTOs

### EvaluationTypeDTO (Response)
```json
{
  "id": "uuid",
  "code": "P1",
  "name": "Primer Parcial",
  "weight": 25.00,
  "isActive": true,
  "createdAt": "2025-01-10",
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación"
}
```

### CreateEvaluationTypeRequest
| Field | Required | Description |
|-------|----------|-------------|
| `courseId` | ✅ | Course UUID |
| `code` | ✅ | Evaluation code |
| `name` | ❌ | Display name |
| `weight` | ❌ | Weight percentage (0-100) |

### UpdateEvaluationTypeRequest
All fields are optional.

## Validations

- **Code unique per course** - No duplicate codes within the same course (auto-uppercased)
- **Course required** - Must reference an existing course
- **Weight** - Must be between 0 and 100

## Existing Seed Data

Each course has pre-loaded evaluation types. Example for LEP101:

| Code | Name | Weight |
|------|------|--------|
| P1 | Primer Parcial | 25% |
| P2 | Segundo Parcial | 25% |
| P3 | Tercer Parcial | 25% |
| EX | Examen Final | 25% |

## Frontend Integration Guide

### 1. List All Evaluation Types
```javascript
const response = await fetch('/api/evaluation-types?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
```

### 2. Get By Course
```javascript
const response = await fetch(`/api/evaluation-types/by-course/${courseId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of EvaluationTypeDTO for that course
```

### 3. Create (Admin)
```javascript
const response = await fetch('/api/evaluation-types', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    courseId: "uuid-of-course",
    code: "TA",
    name: "Trabajo Académico",
    weight: 10
  })
});
```

### 4. Delete (Admin)
```javascript
const response = await fetch(`/api/evaluation-types/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un tipo de evaluación con ese código en el curso" }

// 404 - Not found
{ "success": false, "message": "Tipo de evaluación no encontrado" }
```

## Notes

- Physical delete (no soft delete - table has no is_deleted column)
- Uses `is_active = false` as "inactive" state instead of delete
- Code is automatically uppercased
- DTO includes resolved course code and name
- Extra endpoint: `GET /by-course/{courseId}` to get all types for a specific course
