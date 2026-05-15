# Student Document Module Documentation

## Overview

The Student Document Module manages student documents (CURP, birth certificates, photos, academic records, etc.).

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 659-686).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (CASCADE)
- `document_type` - CURP, BIRTH_CERTIFICATE, PHOTO, HIGH_SCHOOL_CERTIFICATE, HIGH_SCHOOL_KARDEX, IDENTIFICATION, PROOF_OF_ADDRESS, PAYMENT, OTHER
- `original_name` / `file_name` / `file_path` - File metadata
- `file_size_bytes` / `mime_type` - File info
- `document_number` - Official document number
- `is_verified` / `verified_by` / `verification_date` - Verification flow

## API Endpoints

### Get All Documents
```
GET /api/student-documents?page=0&size=10
```
**Auth:** Any authenticated user

### Get Document by ID
```
GET /api/student-documents/{id}
```
**Auth:** Any authenticated user

### Get by Student
```
GET /api/student-documents/by-student/{studentId}
```
**Auth:** Any authenticated user

### Create (Admin)
```
POST /api/student-documents
```
**Auth:** `ADMIN` role

### Update (Admin)
```
PUT /api/student-documents/{id}
```
**Auth:** `ADMIN` role

### Delete (Admin)
```
DELETE /api/student-documents/{id}
```
**Auth:** `ADMIN` role (soft delete)

### Get Deleted (Admin)
```
GET /api/student-documents/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration

```javascript
const response = await fetch('/api/student-documents', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

## Notes

- Soft delete only
- File paths stored, actual file upload not handled here