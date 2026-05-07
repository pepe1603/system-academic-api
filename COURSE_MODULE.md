# Course Module Documentation

## Overview

The Course Module manages academic courses within study plans. A course belongs to a study plan and a semester (position within the plan), with attributes like credits, hours, and mandatory status.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 191-207).

### Key Fields

- `id` - UUID primary key
- `study_plan_id` - Reference to study plan
- `semester_id` - Reference to semester (position within plan)
- `course_code` - Unique course code (e.g., "LEP101", "LES102")
- `name` - Course display name
- `credits` - Number of credits (> 0)
- `hours_theory` - Theory hours per week
- `hours_practice` - Practice hours per week
- `description` - Course description
- `is_mandatory` - Whether the course is mandatory
- `is_active` - Whether the course is active
- `is_deleted` - Soft delete flag

## API Endpoints

### Get All Courses (Active)
```
GET /api/courses?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<CourseDTO>`

### Get Course by ID
```
GET /api/courses/{id}
```
**Auth:** Any authenticated user  
**Response:** `CourseDTO`

### Create Course (Admin)
```
POST /api/courses
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateCourseRequest`
```json
{
  "studyPlanId": "uuid-of-study-plan",
  "semesterId": "uuid-of-semester",
  "courseCode": "LEP106",
  "name": "Historia de la Educación",
  "credits": 6,
  "hoursTheory": 3,
  "hoursPractice": 2,
  "description": "Historia y evolución de los sistemas educativos",
  "isMandatory": true,
  "isActive": true
}
```

### Update Course (Admin)
```
PUT /api/courses/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateCourseRequest` (all fields optional)

### Delete Course (Admin)
```
DELETE /api/courses/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete (marks `is_deleted = true`)

### Get Deleted Courses (Admin)
```
GET /api/courses/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<CourseDTO>` (paginated)

## DTOs

### CourseDTO (Response)
```json
{
  "id": "uuid",
  "courseCode": "LEP101",
  "name": "Introducción a la Educación",
  "credits": 8,
  "hoursTheory": 4,
  "hoursPractice": 2,
  "description": "Fundamentos de la educación y la pedagogía",
  "isMandatory": true,
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2024-01-15",
  "studyPlanId": "uuid",
  "studyPlanCode": "LEP",
  "studyPlanName": "Licenciatura en Educación Primaria",
  "semesterId": "uuid",
  "semesterName": "Primer Semestre",
  "semesterNumber": 1
}
```

### CreateCourseRequest
| Field | Required | Description |
|-------|----------|-------------|
| `studyPlanId` | ✅ | Study plan UUID |
| `semesterId` | ✅ | Semester UUID |
| `courseCode` | ✅ | Unique course code |
| `name` | ✅ | Course display name |
| `credits` | ✅ | Number of credits (> 0) |
| `hoursTheory` | ❌ | Theory hours (default: 0) |
| `hoursPractice` | ❌ | Practice hours (default: 0) |
| `description` | ❌ | Course description |
| `isMandatory` | ❌ | Mandatory flag (default: true) |
| `isActive` | ❌ | Active flag (default: true) |

### UpdateCourseRequest
All fields are optional.

## Validations

- **Course code unique** - No duplicate course codes (auto-uppercased)
- **Course code required** - Must not be blank
- **Name required** - Must not be blank
- **Credits required** - Must be > 0
- **Study plan required** - Must reference an existing study plan
- **Semester required** - Must reference an existing semester
- **Soft delete** - Records are marked, never physically deleted

## Existing Seed Data

The system comes with pre-loaded courses for LEP and LES study plans:

**LEP - Semester 1:**
| Code | Name | Credits |
|------|------|---------|
| LEP101 | Introducción a la Educación | 8 |
| LEP102 | Psicología del Desarrollo | 6 |
| LEP103 | Didáctica General | 8 |
| LEP104 | Matemáticas para Maestros I | 6 |
| LEP105 | Lenguaje y Comunicación | 6 |

**LEP - Semester 2:**
| Code | Name | Credits |
|------|------|---------|
| LEP201 | Psicología Educativa | 6 |
| LEP202 | Diseño Curricular | 8 |
| LEP203 | Matemáticas para Maestros II | 6 |
| LEP204 | Expresión Oral y Escrita | 6 |

**LES - Semester 1:**
| Code | Name | Credits |
|------|------|---------|
| LES101 | Introducción a la Educación Secundaria | 8 |
| LES102 | Álgebra Superior | 8 |
| LES103 | Geometría y Trigonometría | 8 |
| LES104 | Didáctica de las Matemáticas | 8 |

## Relationships (for future modules)

- `academic_group` → depends on `course` (ON DELETE CASCADE)
- `kardex` → depends on `course` (ON DELETE RESTRICT)
- `evaluation_type` → depends on `course` (ON DELETE CASCADE)
- `extraordinary_exam` → depends on `course` (ON DELETE SET NULL)

## Frontend Integration Guide

### 1. List All Courses
```javascript
const response = await fetch('/api/courses?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// data is array of CourseDTO with study plan and semester info
```

### 2. Create Course (Admin)
```javascript
const response = await fetch('/api/courses', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studyPlanId: "uuid-of-lep",
    semesterId: "uuid-of-semester-1",
    courseCode: "LEP106",
    name: "Historia de la Educación",
    credits: 6,
    hoursTheory: 3,
    hoursPractice: 2,
    description: "Historia y evolución de los sistemas educativos",
    isMandatory: true
  })
});
```

### 3. Update Course (Admin)
```javascript
const response = await fetch(`/api/courses/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ credits: 8, hoursPractice: 3 })
});
```

### 4. Delete Course (Admin)
```javascript
const response = await fetch(`/api/courses/${id}`, {
  method: 'DELETE',
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
```

### 5. View Deleted Courses (Admin)
```javascript
const response = await fetch('/api/courses/deleted?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${adminToken}` }
});
const { data } = await response.json();
// data is array of deleted CourseDTO
```

## Error Responses

```json
// 400 - Validation error
{ "success": false, "message": "Ya existe un curso con ese código" }

// 404 - Not found
{ "success": false, "message": "Curso no encontrado" }

// 200 - Success
{ "success": true, "data": { ... } }
```

## Notes

- Soft delete only (no physical deletion)
- Course code is automatically uppercased
- Requires existing `study_plan` and `semester` records (both have seed data)
- Courses with existing `kardex` or `evaluation_type` records cannot be deleted due to ON DELETE RESTRICT
- Paginated responses for list endpoints
- DTO includes resolved study plan and semester names for frontend convenience
