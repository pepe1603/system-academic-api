# Report Card Module Documentation

## Overview

The Report Card Module manages academic report cards (boletas) for students. Each report card represents a student's academic performance during a specific semester, with detailed subject-by-subject grades.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 369-442).

### Report Card (Header)

- `id` - UUID primary key
- `student_id` - Reference to student (RESTRICT)
- `academic_semester_id` - Reference to academic semester (RESTRICT)
- `generation_id` - Reference to generation (SET NULL)
- `report_card_type` - ORDINARY, EXTRAORDINARY, SPECIAL, PARTIAL_CERTIFICATE, FINAL_CERTIFICATE
- `generation_mode` - ONLINE, OFFICIAL
- `overall_average` - Numeric average (0-100)
- `average_letter` - Letter grade (A, B, C, D, F)
- `attendance_average` - Average attendance percentage
- `total_credits_enrolled` / `total_credits_approved`
- `total_subjects` / `total_subjects_approved`
- `status` - PENDING, ISSUED, DELIVERED, ARCHIVED, CANCELLED
- `folio` - Unique folio number
- `is_signed` - Whether signed by authority

### Report Card Detail (Lines)

- `id` - UUID primary key
- `report_card_id` - Reference to report card (CASCADE)
- `kardex_id` - Reference to kardex (SET NULL)
- `course_id` - Reference to course (RESTRICT)
- `subject_name` / `subject_code` - Course info
- `credits` - Course credits
- `grade` / `grade_letter` - Numeric and letter grade
- `subject_status` - Status of the subject
- `attendance_percentage` / `total_attendances` / `classes_attended`

## API Endpoints

### Get All Report Cards
```
GET /api/report-cards?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<ReportCardDTO>` (paginated, includes details)

### Get Report Card by ID
```
GET /api/report-cards/{id}
```
**Auth:** Any authenticated user  
**Response:** `ReportCardDTO` with full details

### Get Report Cards by Student
```
GET /api/report-cards/by-student/{studentId}
```
**Auth:** Any authenticated user  
**Response:** `List<ReportCardDTO>`

### Create Report Card (Admin)
```
POST /api/report-cards
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateReportCardRequest`
```json
{
  "studentId": "uuid-of-student",
  "academicSemesterId": "uuid-of-semester",
  "generationId": "uuid-of-generation",
  "reportCardType": "ORDINARY",
  "generationMode": "ONLINE",
  "folio": "BOL-2025-001",
  "observations": "",
  "details": [
    {
      "courseId": "uuid-of-course",
      "kardexId": "uuid-of-kardex",
      "subjectName": "Introducción a la Educación",
      "subjectCode": "LEP101",
      "credits": 8,
      "grade": 92.5,
      "gradeLetter": "A",
      "subjectStatus": "APPROVED",
      "attendancePercentage": 95.0
    }
  ]
}
```

### Update Report Card (Admin)
```
PUT /api/report-cards/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateReportCardRequest` (all fields optional)
```json
{
  "status": "DELIVERED",
  "deliveryDate": "2025-06-20",
  "isSigned": true,
  "signedAt": "2025-06-20T10:00:00Z",
  "signedSealUrl": "https://..."
}
```

### Delete Report Card (Admin)
```
DELETE /api/report-cards/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Report Cards (Admin)
```
GET /api/report-cards/deleted?page=0&size=10
```
**Auth:** `ADMIN` role  
**Response:** `List<ReportCardDTO>` (paginated)

## Frontend Integration Guide

### 1. Get Student's Report Card History
```javascript
const response = await fetch(`/api/report-cards/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns all report cards for that student with detail lines
```

### 2. Create Report Card (Admin)
```javascript
const response = await fetch('/api/report-cards', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    academicSemesterId: "uuid-of-semester",
    generationId: "uuid-of-generation",
    reportCardType: "ORDINARY",
    folio: "BOL-2025-001",
    details: [
      {
        courseId: "uuid-of-course",
        subjectName: "Introducción a la Educación",
        subjectCode: "LEP101",
        credits: 8,
        grade: 92.5,
        gradeLetter: "A",
        subjectStatus: "APPROVED"
      },
      {
        courseId: "uuid-of-course-2",
        subjectName: "Psicología Educativa",
        subjectCode: "LEP102",
        credits: 6,
        grade: 88.0,
        gradeLetter: "B",
        subjectStatus: "APPROVED"
      }
    ]
  })
});
```

### 3. Sign and Deliver Report Card (Admin)
```javascript
const response = await fetch(`/api/report-cards/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: "DELIVERED",
    deliveryDate: "2025-06-20",
    isSigned: true,
    signedAt: "2025-06-20T10:00:00Z",
    signedSealUrl: "https://storage.example.com/seals/seal-001.png"
  })
});
```

### 4. Display Report Card (Vue.js example)
```javascript
const reportCard = data; // from GET /api/report-cards/{id}
// Header info
console.log(`Estudiante: ${reportCard.studentName}`);
console.log(`Semestre: ${reportCard.academicSemesterName}`);
console.log(`Promedio: ${reportCard.overallAverage} (${reportCard.averageLetter})`);
console.log(`Folio: ${reportCard.folio}`);

// Detail table
reportCard.details.forEach(detail => {
  console.log(`${detail.subjectCode} - ${detail.subjectName}: ${detail.grade} (${detail.gradeLetter})`);
});
```

## Error Responses

```json
// 400 - Validation errors
{ "success": false, "message": "El estudiante es requerido" }

// 400 - Invalid type
{ "success": false, "message": "Tipo de boleta inválido. Valores: ORDINARY, EXTRAORDINARY, SPECIAL, PARTIAL_CERTIFICATE, FINAL_CERTIFICATE" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores: PENDING, ISSUED, DELIVERED, ARCHIVED, CANCELLED" }

// 400 - Duplicate folio
{ "success": false, "message": "El folio ya existe" }

// 404 - Not found
{ "success": false, "message": "Boleta no encontrada" }
```

## Notes

- Soft delete only (no physical deletion)
- Master-detail: create includes array of `details`
- Auto-calculates: `overallAverage`, `averageLetter`, `totalCredits`, `totalSubjects` from detail lines
- Report card types: ORDINARY (regular semester), EXTRAORDINARY (extraordinary exam), SPECIAL, PARTIAL_CERTIFICATE, FINAL_CERTIFICATE
- Generation modes: ONLINE (portal view), OFFICIAL (signed/sealed document)
- Signature flow: update `isSigned=true` + `signedBy` + `signedAt` for officialization
- Extra endpoint: `GET /by-student/{studentId}` for student history