# Academic Period Module Documentation

## Overview

The Academic Period Module manages academic periods (e.g., "2025-1", "2025-2") that define enrollment timeframes. This is a simple, dependency-free module used by the Enrollment system.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 158-169).

### Key Fields

- `id` - UUID primary key
- `name` - Unique period name (e.g., "2025-1")
- `start_date` - Period start date
- `end_date` - Period end date
- `is_active` - Active status
- `is_deleted` - Soft delete flag

### Constraints

- `UNIQUE (name)` - No duplicate period names
- `CHECK (end_date > start_date)`

## API Endpoints

### Get All Academic Periods (Active)
```
GET /api/academic-periods?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<AcademicPeriodDTO>`

### Get Academic Period by ID
```
GET /api/academic-periods/{id}
```
**Auth:** Any authenticated user  
**Response:** `AcademicPeriodDTO`

### Create Academic Period (Admin)
```
POST /api/academic-periods
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateAcademicPeriodRequest`
```json
{
  "name": "2025-1",
  "startDate": "2025-01-15",
  "endDate": "2025-06-30"
}
```

### Update Academic Period (Admin)
```
PUT /api/academic-periods/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateAcademicPeriodRequest` (all fields optional)

### Delete Academic Period (Admin)
```
DELETE /api/academic-periods/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Academic Periods (Admin)
```
GET /api/academic-periods/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<AcademicPeriodDTO>` (paginated)

## DTOs

### AcademicPeriodDTO (Response)
```json
{
  "id": "uuid",
  "name": "2025-1",
  "startDate": "2025-01-15",
  "endDate": "2025-06-30",
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2025-01-10"
}
```

### CreateAcademicPeriodRequest
| Field | Required | Description |
|-------|----------|-------------|
| `name` | ✅ | Unique period name |
| `startDate` | ✅ | Period start date |
| `endDate` | ✅ | Period end date |

### UpdateAcademicPeriodRequest
All fields are optional.

## Validations

- **Name unique** - No duplicate period names (auto-uppercased)
- **Name required** - Must not be blank
- **Date constraint** - end_date must be after start_date
- **Soft delete** - Records are marked, never physically deleted

## Existing Seed Data

| Name | Start | End |
|------|-------|-----|
| 2025-2 | 2025-08-15 | 2025-12-20 |
| 2024-2 | 2024-08-15 | 2024-12-20 |
| 2024-1 | 2024-01-15 | 2024-06-30 |

## Frontend Integration Guide

### 1. List All Periods
```javascript
const response = await fetch('/api/academic-periods?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
```

### 2. Create Period (Admin)
```javascript
const response = await fetch('/api/academic-periods', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: "2026-1",
    startDate: "2026-01-15",
    endDate: "2026-06-30"
  })
});
```

### 3. Delete Period (Admin)
```javascript
const response = await fetch(`/api/academic-periods/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un período académico con ese nombre" }

// 404 - Not found
{ "success": false, "message": "Período académico no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- Name is automatically uppercased
- Periods with existing `enrollment` records cannot be deleted due to ON DELETE RESTRICT
- This module is a prerequisite for the Enrollment module
- Paginated responses for list endpoints
