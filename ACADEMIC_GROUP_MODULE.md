# Academic Group Module Documentation

## Overview

The Academic Group Module manages course sections (groups) within academic semesters. A group represents a specific section of a course taught in a given semester, optionally assigned to a teacher with a defined capacity.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 212-230).

### Key Fields

- `id` - UUID primary key
- `name` - Group name (e.g., "A", "B", "C")
- `academic_semester_id` - Reference to academic semester
- `course_id` - Reference to course
- `teacher_id` - Reference to teacher (optional, nullable)
- `capacity` - Maximum students (default: 30)
- `is_active` - Whether the group is active
- `is_deleted` - Soft delete flag

### Constraints

- `UNIQUE (name, academic_semester_id, course_id)` - No duplicate group names within the same semester and course

## API Endpoints

### Get All Academic Groups (Active)
```
GET /api/academic-groups?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<AcademicGroupDTO>`

### Get Academic Group by ID
```
GET /api/academic-groups/{id}
```
**Auth:** Any authenticated user  
**Response:** `AcademicGroupDTO`

### Create Academic Group (Admin)
```
POST /api/academic-groups
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateAcademicGroupRequest`
```json
{
  "name": "A",
  "academicSemesterId": "uuid-of-2025-1",
  "courseId": "uuid-of-LEP101",
  "teacherId": "uuid-of-teacher",
  "capacity": 30
}
```

### Update Academic Group (Admin)
```
PUT /api/academic-groups/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateAcademicGroupRequest` (all fields optional)

### Delete Academic Group (Admin)
```
DELETE /api/academic-groups/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Academic Groups (Admin)
```
GET /api/academic-groups/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<AcademicGroupDTO>` (paginated)

## DTOs

### AcademicGroupDTO (Response)
```json
{
  "id": "uuid",
  "name": "A",
  "capacity": 30,
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2025-01-10",
  "academicSemesterId": "uuid",
  "academicSemesterName": "2025-1",
  "courseId": "uuid",
  "courseCode": "LEP101",
  "courseName": "Introducción a la Educación",
  "teacherId": "uuid",
  "teacherFullName": "Juan Pérez López"
}
```

### CreateAcademicGroupRequest
| Field | Required | Description |
|-------|----------|-------------|
| `name` | ✅ | Group name (e.g., "A", "B") |
| `academicSemesterId` | ✅ | Academic semester UUID |
| `courseId` | ✅ | Course UUID |
| `teacherId` | ❌ | Teacher UUID |
| `capacity` | ❌ | Max students (default: 30) |

### UpdateAcademicGroupRequest
All fields are optional.

## Validations

- **Unique constraint** - No duplicate group names within the same semester and course
- **Name required** - Must not be blank
- **Semester required** - Must reference an existing academic semester
- **Course required** - Must reference an existing course
- **Teacher optional** - Must reference an existing teacher if provided
- **Capacity** - Defaults to 30 if not specified
- **Soft delete** - Records are marked, never physically deleted

## Existing Seed Data

The system comes with pre-loaded groups for the 2025-1 semester:

| Name | Semester | Course | Capacity |
|------|----------|--------|----------|
| A | 2025-1 | LEP101 - Introducción a la Educación | 30 |
| A | 2025-1 | LEP102 - Psicología del Desarrollo | 30 |
| A | 2025-1 | LEP103 - Didáctica General | 30 |
| A | 2025-1 | LES101 - Introducción a la Educación Secundaria | 25 |
| A | 2025-1 | LES102 - Álgebra Superior | 25 |

## Relationships (for future modules)

- `enrollment` → depends on `academic_group`
- `attendance` → depends on `academic_group` (via enrollment)
- `schedule` → depends on `academic_group`

## Frontend Integration Guide

### 1. List All Groups
```javascript
const response = await fetch('/api/academic-groups?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of AcademicGroupDTO with resolved semester, course, and teacher info
```

### 2. Create Group (Admin)
```javascript
const response = await fetch('/api/academic-groups', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: "B",
    academicSemesterId: "uuid-of-2025-1",
    courseId: "uuid-of-LEP101",
    teacherId: "uuid-of-teacher",
    capacity: 35
  })
});
```

### 3. Update Group (Admin)
```javascript
const response = await fetch(`/api/academic-groups/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ capacity: 40, teacherId: "new-teacher-uuid" })
});
```

### 4. Delete Group (Admin)
```javascript
const response = await fetch(`/api/academic-groups/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

### 5. View Deleted Groups (Admin)
```javascript
const response = await fetch('/api/academic-groups/deleted?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
const { data } = await response.json();
// data is array of deleted AcademicGroupDTO
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un grupo con ese nombre en el semestre y curso seleccionados" }

// 404 - Not found
{ "success": false, "message": "Grupo académico no encontrado" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- Unique constraint: (name, academic_semester_id, course_id) - you can have group "A" in different courses/semesters
- Teacher assignment is optional (ON DELETE SET NULL)
- DTO includes resolved semester name, course code/name, and teacher full name for frontend convenience
- Paginated responses for list endpoints
