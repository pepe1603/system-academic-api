# Academic Semester Module Documentation

## Overview

The Academic Semester Module manages academic periods in the system. A semester defines the time frame for academic activities including class periods, enrollment deadlines, and drop deadlines.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 136-153).

### Key Fields

- `id` - UUID primary key
- `name` - Semester name (e.g., "2025-1")
- `year` - Academic year (e.g., 2025)
- `period` - Period number (1 or 2)
- `start_date` - Semester start date
- `end_date` - Semester end date
- `classes_start_date` - Classes start date
- `classes_end_date` - Classes end date
- `enrollment_deadline` - Enrollment deadline
- `drop_deadline` - Drop deadline
- `status` - Semester status (DRAFT, OPEN, CLOSED, ARCHIVED)
- `is_current` - Whether this is the current active semester
- `is_deleted` - Soft delete flag

### Constraints

- `year >= 2000`
- `period IN (1, 2)`
- `status IN ('DRAFT', 'OPEN', 'CLOSED', 'ARCHIVED')`
- `end_date > start_date`
- `classes_end_date >= classes_start_date`

## API Endpoints

### Get All Academic Semesters (Active)
```
GET /api/academic-semesters?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<AcademicSemesterDTO>`

### Get Academic Semester by ID
```
GET /api/academic-semesters/{id}
```
**Auth:** Any authenticated user  
**Response:** `AcademicSemesterDTO`

### Create Academic Semester (Admin)
```
POST /api/academic-semesters
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateAcademicSemesterRequest`
```json
{
  "name": "2025-1",
  "year": 2025,
  "period": 1,
  "startDate": "2025-01-15",
  "endDate": "2025-06-30",
  "classesStartDate": "2025-01-20",
  "classesEndDate": "2025-06-15",
  "enrollmentDeadline": "2025-02-15",
  "dropDeadline": "2025-03-31",
  "status": "DRAFT",
  "isCurrent": false
}
```

### Update Academic Semester (Admin)
```
PUT /api/academic-semesters/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateAcademicSemesterRequest` (all fields optional)

### Delete Academic Semester (Admin)
```
DELETE /api/academic-semesters/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Academic Semesters (Admin)
```
GET /api/academic-semesters/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<AcademicSemesterDTO>` (paginated)

## DTOs

### AcademicSemesterDTO (Response)
```json
{
  "id": "uuid",
  "name": "2025-1",
  "year": 2025,
  "period": 1,
  "startDate": "2025-01-15",
  "endDate": "2025-06-30",
  "classesStartDate": "2025-01-20",
  "classesEndDate": "2025-06-15",
  "enrollmentDeadline": "2025-02-15",
  "dropDeadline": "2025-03-31",
  "status": "OPEN",
  "isCurrent": true,
  "isDeleted": false,
  "createdAt": "2025-01-10"
}
```

### CreateAcademicSemesterRequest
| Field | Required | Description |
|-------|----------|-------------|
| `name` | ✅ | Semester display name |
| `year` | ✅ | Academic year |
| `period` | ✅ | Period (1 or 2) |
| `startDate` | ✅ | Semester start date |
| `endDate` | ✅ | Semester end date |
| `classesStartDate` | ✅ | Classes start date |
| `classesEndDate` | ✅ | Classes end date |
| `enrollmentDeadline` | ❌ | Enrollment deadline |
| `dropDeadline` | ❌ | Drop deadline |
| `status` | ❌ | Status (default: DRAFT) |
| `isCurrent` | ❌ | Mark as current (default: false) |

### UpdateAcademicSemesterRequest
All fields are optional.

## Validations

- **Name unique** - No duplicate semester names
- **Name required** - Must not be blank
- **Year required** - Must not be null
- **Period required** - Must be 1 or 2
- **Date constraints** - end_date > start_date, classes_end_date >= classes_start_date
- **Soft delete** - Records are marked, never physically deleted
- **Status values** - Must be one of: DRAFT, OPEN, CLOSED, ARCHIVED

## Existing Seed Data

The system comes with 4 pre-loaded semesters:

| Name | Year | Period | Status | Current |
|------|------|--------|--------|---------|
| 2025-1 | 2025 | 1 | OPEN | ✅ |
| 2025-2 | 2025 | 2 | DRAFT | ❌ |
| 2024-2 | 2024 | 2 | CLOSED | ❌ |
| 2024-1 | 2024 | 1 | ARCHIVED | ❌ |

## Relationships (for future modules)

- `academic_group` → depends on `academic_semester` (ON DELETE CASCADE)
- `kardex` → depends on `academic_semester` (ON DELETE RESTRICT)
- `report_card` → depends on `academic_semester` (ON DELETE RESTRICT)
- `attendance` → depends on `academic_semester` (ON DELETE RESTRICT)
- `conduct` → depends on `academic_semester` (ON DELETE RESTRICT)
- `extraordinary_exam` → depends on `academic_semester` (ON DELETE SET NULL)

## Frontend Integration Guide

### 1. List All Semesters
```javascript
const response = await fetch('/api/academic-semesters?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of AcademicSemesterDTO
```

### 2. Create Semester (Admin)
```javascript
const response = await fetch('/api/academic-semesters', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: "2026-1",
    year: 2026,
    period: 1,
    startDate: "2026-01-15",
    endDate: "2026-06-30",
    classesStartDate: "2026-01-20",
    classesEndDate: "2026-06-15",
    enrollmentDeadline: "2026-02-15",
    dropDeadline: "2026-03-31",
    status: "DRAFT",
    isCurrent: false
  })
});
```

### 3. Update Semester (Admin)
```javascript
const response = await fetch(`/api/academic-semesters/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ status: "OPEN", isCurrent: true })
});
```

### 4. Delete Semester (Admin)
```javascript
const response = await fetch(`/api/academic-semesters/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

### 5. View Deleted Semesters (Admin)
```javascript
const response = await fetch('/api/academic-semesters/deleted?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
const { data } = await response.json();
// data is array of deleted AcademicSemesterDTO
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un semestre académico con ese nombre" }

// 404 - Not found
{ "success": false, "message": "Semestre académico no encontrado" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- Only one semester should be `is_current = true` at a time (enforced by DB trigger)
- Semesters with existing `kardex`, `report_card`, `attendance`, or `conduct` records cannot be deleted due to ON DELETE RESTRICT
- Paginated responses for list endpoints
- Already has seed data: 2025-1 (OPEN), 2025-2 (DRAFT), 2024-2 (CLOSED), 2024-1 (ARCHIVED)
