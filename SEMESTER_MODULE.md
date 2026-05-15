# Semester Module Documentation

## Overview

The Semester Module manages the semester catalog within study plans. Each study plan has semesters (1st to 10th).

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 124-131).

### Key Fields

- `id` - UUID primary key
- `study_plan_id` - Reference to study plan (CASCADE)
- `semester_number` - Semester number (1-10)
- `name` - Semester name
- `is_active` - Active status

## API Endpoints

### Get All Semesters
```
GET /api/semesters?page=0&size=10
```
**Auth:** Any authenticated user

### Get Semester by ID
```
GET /api/semesters/{id}
```
**Auth:** Any authenticated user

### Get by Study Plan
```
GET /api/semesters/by-study-plan/{studyPlanId}
```
**Auth:** Any authenticated user

### Create (Admin)
```
POST /api/semesters
Content-Type: application/json
```
**Auth:** `ADMIN` role
```json
{
  "studyPlanId": "uuid-of-study-plan",
  "semesterNumber": 1,
  "name": "Primer Semestre"
}
```

### Update (Admin)
```
PUT /api/semesters/{id}
```
**Auth:** `ADMIN` role

### Delete (Admin)
```
DELETE /api/semesters/{id}
```
**Auth:** `ADMIN` role (soft delete)

### Get Deleted (Admin)
```
GET /api/semesters/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration

```javascript
// Get semesters by study plan
const response = await fetch(`/api/semesters/by-study-plan/${studyPlanId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
```

## Notes

- Soft delete only
- semester_number must be between 1 and 10
- Unique per study plan + semester number