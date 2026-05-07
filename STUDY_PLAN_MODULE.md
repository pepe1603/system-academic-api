# Study Plan Module Documentation

## Overview

The Study Plan Module manages academic study plans (curricula) in the system. A study plan defines the structure of a degree program, including code, name, credits, duration, and title awarded.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 107-121).

### Key Fields

- `id` - UUID primary key
- `code` - Unique plan code (e.g., "LEP", "LES")
- `name` - Full plan name
- `version` - Plan version (e.g., "2024")
- `description` - Plan description
- `title_degree` - Degree title awarded
- `total_credits` - Total credits required
- `duration_semesters` - Duration in semesters
- `is_deleted` - Soft delete flag

## API Endpoints

### Get All Study Plans (Active)
```
GET /api/study-plans?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<StudyPlanDTO>`

### Get Study Plan by ID
```
GET /api/study-plans/{id}
```
**Auth:** Any authenticated user  
**Response:** `StudyPlanDTO`

### Create Study Plan (Admin)
```
POST /api/study-plans
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateStudyPlanRequest`
```json
{
  "code": "LENF",
  "name": "Licenciatura en Enfermería",
  "version": "2024",
  "description": "Formación de profesionales en enfermería",
  "titleDegree": "Licenciado/a en Enfermería",
  "totalCredits": 280,
  "durationSemesters": 9
}
```

### Update Study Plan (Admin)
```
PUT /api/study-plans/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateStudyPlanRequest` (all fields optional)

### Delete Study Plan (Admin)
```
DELETE /api/study-plans/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Study Plans (Admin)
```
GET /api/study-plans/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<StudyPlanDTO>` (paginated)

## DTOs

### StudyPlanDTO (Response)
```json
{
  "id": "uuid",
  "code": "LEP",
  "name": "Licenciatura en Educación Primaria",
  "version": "2024",
  "description": "Plan de estudios para formación de docentes",
  "titleDegree": "Licenciado/a en Educación Primaria",
  "totalCredits": 240,
  "durationSemesters": 8,
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2024-01-15"
}
```

### CreateStudyPlanRequest
| Field | Required | Description |
|-------|----------|-------------|
| `code` | ✅ | Unique plan code |
| `name` | ✅ | Plan display name |
| `version` | ❌ | Plan version |
| `description` | ❌ | Plan description |
| `titleDegree` | ❌ | Degree title awarded |
| `totalCredits` | ❌ | Total credits |
| `durationSemesters` | ❌ | Duration in semesters |

### UpdateStudyPlanRequest
All fields are optional.

## Validations

- **Code unique** - No duplicate plan codes (case-insensitive, auto-uppercased)
- **Code required** - Must not be blank
- **Name required** - Must not be blank
- **Soft delete** - Records are marked, never physically deleted

## Existing Seed Data

The system comes with 4 pre-loaded plans:
| Code | Name | Duration |
|------|------|----------|
| LEP | Licenciatura en Educación Primaria | 8 semestres |
| LES | Licenciatura en Educación Secundaria | 8 semestres |
| LEI | Licenciatura en Educación Inicial | 8 semestres |
| LENG | Licenciatura en Inglés | 8 semestres |

## Relationships (for future modules)

- `semester` → depends on `study_plan` (ON DELETE CASCADE)
- `course` → depends on `study_plan` (ON DELETE SET NULL)

## Frontend Integration Guide

### 1. List All Plans
```javascript
const response = await fetch('/api/study-plans?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of StudyPlanDTO
```

### 2. Create Plan (Admin)
```javascript
const response = await fetch('/api/study-plans', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    code: "LENF",
    name: "Licenciatura en Enfermería",
    totalCredits: 280,
    durationSemesters: 9
  })
});
```

### 3. Update Plan (Admin)
```javascript
const response = await fetch(`/api/study-plans/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ name: "Licenciatura en Enfermería - Actualizada" })
});
```

### 4. Delete Plan (Admin)
```javascript
const response = await fetch(`/api/study-plans/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

### 5. View Deleted Plans (Admin)
```javascript
const response = await fetch('/api/study-plans/deleted?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
const { data } = await response.json();
// data is array of deleted StudyPlanDTO
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un plan de estudio con ese código" }

// 404 - Not found
{ "success": false, "message": "Plan de estudio no encontrado" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- Code is automatically uppercased
- Plans with existing `semester` or `course` records cannot be deleted if CASCADE restriction is added
- Paginated responses for list endpoints
- Already has seed data: LEP, LES, LEI, LENG
