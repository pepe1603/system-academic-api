# Attendance Period Module Documentation

## Overview

The Attendance Period Module manages attendance summaries per enrollment per academic semester. It tracks total classes, present, absent, justified, and late records and automatically calculates the attendance percentage and status.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 472-491).

### Key Fields

- `id` - UUID primary key
- `enrollment_id` - Reference to enrollment
- `academic_semester_id` - Reference to academic semester
- `total_classes` - Total classes held
- `total_present` - Classes attended
- `total_absent` - Classes missed
- `total_justified` - Justified absences
- `total_late` - Late arrivals
- `attendance_percentage` - Auto-calculated percentage
- `attendance_status` - SATISFACTORY (>=80%), AT_RISK (>=60%), INSUFFICIENT (<60%)
- Unique constraint on (enrollment_id, academic_semester_id)

## API Endpoints

### Get All Attendance Periods (Paginated)
```
GET /api/attendance-periods?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `Page<AttendancePeriodDTO>`

### Get by Enrollment or Semester
```
GET /api/attendance-periods?enrollmentId={uuid}&page=0&size=10
GET /api/attendance-periods?academicSemesterId={uuid}&page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `Page<AttendancePeriodDTO>`

### Get by Enrollment and Semester
```
GET /api/attendance-periods/by-enrollment-semester?enrollmentId={uuid}&academicSemesterId={uuid}
```
**Auth:** Any authenticated user  
**Response:** `AttendancePeriodDTO`

### Get by ID
```
GET /api/attendance-periods/{id}
```
**Auth:** Any authenticated user  
**Response:** `AttendancePeriodDTO`

### Create (Admin only)
```
POST /api/attendance-periods
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateAttendancePeriodRequest`
```json
{
  "enrollmentId": "uuid",
  "academicSemesterId": "uuid",
  "totalClasses": 30,
  "totalPresent": 25,
  "totalAbsent": 3,
  "totalJustified": 1,
  "totalLate": 1,
  "observations": "Regular attendance"
}
```

### Update (Admin only)
```
PUT /api/attendance-periods/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateAttendancePeriodRequest`
```json
{
  "totalClasses": 32,
  "totalPresent": 28,
  "totalAbsent": 2
}
```

### Delete (Admin only)
```
DELETE /api/attendance-periods/{id}
```
**Auth:** `ADMIN` role  
**Response:** Success message

## DTOs

### AttendancePeriodDTO (Response)
```json
{
  "id": "uuid",
  "enrollmentId": "uuid",
  "academicSemesterId": "uuid",
  "totalClasses": 30,
  "totalPresent": 25,
  "totalAbsent": 3,
  "totalJustified": 1,
  "totalLate": 1,
  "attendancePercentage": 83.33,
  "attendanceStatus": "SATISFACTORY",
  "observations": "Regular attendance",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": null,
  "studentName": "Juan Perez",
  "enrollmentNumber": "ENE-2025-0001",
  "courseCode": "MAT101",
  "courseName": "Matemáticas I",
  "semesterName": "2025-1"
}
```

### CreateAttendancePeriodRequest
- `enrollmentId` (required) - Enrollment UUID
- `academicSemesterId` (required) - Academic semester UUID
- `totalClasses` (optional) - Default 0
- `totalPresent` (optional) - Default 0
- `totalAbsent` (optional) - Default 0
- `totalJustified` (optional) - Default 0
- `totalLate` (optional) - Default 0
- `observations` (optional)

### UpdateAttendancePeriodRequest
All fields are optional:
- `totalClasses`
- `totalPresent`
- `totalAbsent`
- `totalJustified`
- `totalLate`
- `observations`

## Auto-Calculation

The system automatically calculates:

- **Attendance Percentage**: `(totalPresent / totalClasses) * 100`
- **Attendance Status**:
  - `>= 80%` → SATISFACTORY
  - `>= 60%` → AT_RISK
  - `< 60%` → INSUFFICIENT

These calculations run on every create and update via `@PrePersist` and `@PreUpdate`.

## Validations

- **Unique** - Only one period per enrollment + semester combination
- **Enrollment exists** - Validated on create
- **Auto-calculated** - Percentage and status are computed automatically

## Service Methods

### AttendancePeriodService

- `getAllAttendancePeriods(Pageable)` - Get all periods (paginated)
- `getAttendancePeriodById(String id)` - Get by ID
- `getByEnrollmentAndSemester(String enrollmentId, String semesterId)` - Get by composite key
- `getByEnrollment(String enrollmentId, Pageable)` - Get by enrollment
- `getByAcademicSemester(String semesterId, Pageable)` - Get by semester
- `createAttendancePeriod(CreateAttendancePeriodRequest)` - Create new period
- `updateAttendancePeriod(String id, UpdateAttendancePeriodRequest)` - Update period
- `deleteAttendancePeriod(String id)` - Hard delete period

## Frontend Integration Guide

### 1. Get All Attendance Periods
```javascript
const response = await fetch(`/api/attendance-periods?page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
// data.content is an array of AttendancePeriodDTO
```

### 2. Get by Enrollment
```javascript
const response = await fetch(`/api/attendance-periods?enrollmentId=${enrollmentId}&page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 3. Get by Enrollment and Semester
```javascript
const response = await fetch(`/api/attendance-periods/by-enrollment-semester?enrollmentId=${enrollmentId}&academicSemesterId=${semesterId}`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
```

### 4. Create Attendance Period (Admin)
```javascript
const response = await fetch('/api/attendance-periods', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    enrollmentId: enrollmentId,
    academicSemesterId: semesterId,
    totalClasses: 30,
    totalPresent: 25,
    totalAbsent: 3,
    totalJustified: 1,
    totalLate: 1
  })
});
const { data } = await response.json();
```

### 5. Update Attendance Period (Admin)
```javascript
const response = await fetch(`/api/attendance-periods/${periodId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    totalClasses: 32,
    totalPresent: 28
  })
});
```

### 6. Delete Attendance Period (Admin)
```javascript
const response = await fetch(`/api/attendance-periods/${periodId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

## Notes

- No soft delete; records are hard deleted (no `is_deleted` column in the table)
- Percentage and status are auto-calculated on every save
- Useful for generating attendance reports and statistics
- The summary data can be aggregated from individual attendance records

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler`:

- `IllegalArgumentException` → 400 Bad Request
- Generic exceptions → 500 Internal Server Error

Example error response:
```json
{
  "success": false,
  "message": "Ya existe un período de asistencia para esta inscripción y semestre"
}
```
