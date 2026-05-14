# Certificate Module Documentation

## Overview

The Certificate Module manages official academic certificates issued to students, including partial certificates, total certificates, titles, diplomas, and constancies (certifications).

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 594-626).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (RESTRICT)
- `generation_id` - Reference to generation (SET NULL)
- `certificate_type` - PARTIAL, TOTAL, TITLE, DIPLOMA, CONSTANCIA
- `official_folio` - Official folio number (UNIQUE)
- `internal_folio` - Internal control folio
- `final_average` - Final grade average
- `total_credits` / `total_subjects` - Academic summary
- `issue_date` / `delivery_date` - Key dates
- `status` - REQUESTED, IN_PROCESS, ISSUED, DELIVERED, CANCELLED
- `director_signer` / `secretary_signer` - Signing authorities (Teacher ref)

## API Endpoints

### Get All Certificates
```
GET /api/certificates?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<CertificateDTO>` (paginated)

### Get Certificate by ID
```
GET /api/certificates/{id}
```
**Auth:** Any authenticated user  
**Response:** `CertificateDTO`

### Get Certificates by Student
```
GET /api/certificates/by-student/{studentId}
```
**Auth:** Any authenticated user  
**Response:** `List<CertificateDTO>`

### Create Certificate (Admin)
```
POST /api/certificates
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateCertificateRequest`
```json
{
  "studentId": "uuid-of-student",
  "generationId": "uuid-of-generation",
  "certificateType": "TOTAL",
  "officialFolio": "CERT-2025-001",
  "series": "A",
  "finalAverage": 89.5,
  "totalCredits": 350,
  "totalSubjects": 45,
  "directorSigner": "uuid-of-director-teacher",
  "secretarySigner": "uuid-of-secretary-teacher",
  "recordNumber": "001",
  "recordBook": "LIBRO-1",
  "recordPage": "10"
}
```

### Update Certificate (Admin)
```
PUT /api/certificates/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateCertificateRequest` (all fields optional)
```json
{
  "status": "DELIVERED",
  "deliveryDate": "2025-07-15"
}
```

### Delete Certificate (Admin)
```
DELETE /api/certificates/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Certificates (Admin)
```
GET /api/certificates/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration Guide

### 1. Get Student's Certificates
```javascript
const response = await fetch(`/api/certificates/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns all certificates for that student
```

### 2. Create Certificate (Admin)
```javascript
const response = await fetch('/api/certificates', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    generationId: "uuid-of-generation",
    certificateType: "TOTAL",
    officialFolio: "CERT-2025-001",
    finalAverage: 89.5,
    totalCredits: 350,
    totalSubjects: 45,
    directorSigner: "uuid-of-director",
    secretarySigner: "uuid-of-secretary"
  })
});
```

### 3. Update Certificate Status (Admin)
```javascript
const response = await fetch(`/api/certificates/${id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: "DELIVERED",
    deliveryDate: "2025-07-15"
  })
});
```

### 4. Display Certificate Info (Vue.js example)
```javascript
const cert = data; // from GET /api/certificates/{id}
console.log(`Tipo: ${cert.certificateType}`);
console.log(`Folio: ${cert.officialFolio}`);
console.log(`Promedio: ${cert.finalAverage}`);
console.log(`Estado: ${cert.status}`);
console.log(`Director: ${cert.directorName}`);
console.log(`Secretario: ${cert.secretaryName}`);
```

## Error Responses

```json
// 400 - Invalid certificate type
{ "success": false, "message": "Tipo de certificado inválido. Valores: PARTIAL, TOTAL, TITLE, DIPLOMA, CONSTANCIA" }

// 400 - Invalid status
{ "success": false, "message": "Estado inválido. Valores: REQUESTED, IN_PROCESS, ISSUED, DELIVERED, CANCELLED" }

// 400 - Duplicate folio
{ "success": false, "message": "El folio oficial ya existe" }

// 404 - Not found
{ "success": false, "message": "Certificado no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- official_folio is unique
- Certificate types: PARTIAL (partial studies), TOTAL (complete studies), TITLE (professional title), DIPLOMA, CONSTANCIA (certification)
- Status workflow: REQUESTED → IN_PROCESS → ISSUED → DELIVERED, or CANCELLED at any point
- Signers reference Teacher records (director and secretary)
- Record book info for official registry tracking