# Conduct Module Documentation

## Overview

The Conduct Module manages student conduct (behavior) records and incidents. Each student has a conduct record per enrollment and academic semester, tracking warnings, congratulations, and a conduct grade. Incidents are individual behavior events (warnings, congratulations, suspensions, etc.).

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 497-530).

### Conduct (Master)

- `id` - UUID primary key
- `enrollment_id` - Reference to enrollment (RESTRICT)
- `academic_semester_id` - Reference to academic semester (RESTRICT)
- `grade` - Conduct grade (e.g., A, B, C)
- `observations` - Notes
- `warnings` - Warning count (default: 0)
- `congratulations` - Congratulations count (default: 0)
- `recorded_by` - Reference to user who recorded

**Unique:** `(enrollment_id, academic_semester_id)`

### Conduct Incident (Detail)

- `id` - UUID primary key
- `enrollment_id` - Reference to enrollment (CASCADE)
- `incident_type` - WARNING, CONGRATULATION, CALL_ATTENTION, SUSPENSION, OTHER
- `description` - Incident description
- `incident_date` - Date of incident
- `severity` - MINOR, MODERATE, SERIOUS
- `actions_taken` - Actions taken for the incident

## API Endpoints

### Get All Conduct Records
```
GET /api/conduct?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<ConductDTO>` (paginated)

### Get Conduct by ID
```
GET /api/conduct/{id}
```
**Auth:** Any authenticated user  
**Response:** `ConductDTO`

### Get Conduct by Enrollment
```
GET /api/conduct/by-enrollment/{enrollmentId}
```
**Auth:** Any authenticated user  
**Response:** `List<ConductDTO>`

### Get Conduct by Semester
```
GET /api/conduct/by-semester/{semesterId}
```
**Auth:** Any authenticated user  
**Response:** `List<ConductDTO>`

### Create Conduct (Admin)
```
POST /api/conduct
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateConductRequest`
```json
{
  "enrollmentId": "uuid-of-enrollment",
  "academicSemesterId": "uuid-of-semester",
  "grade": "A",
  "observations": "Buena conducta durante el semestre",
  "warnings": 0,
  "congratulations": 3
}
```

### Update Conduct (Admin)
```
PUT /api/conduct/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateConductRequest` (all fields optional)

### Delete Conduct (Admin)
```
DELETE /api/conduct/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Conduct Records (Admin)
```
GET /api/conduct/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

### Get Incidents by Enrollment
```
GET /api/conduct/incidents/by-enrollment/{enrollmentId}
```
**Auth:** Any authenticated user  
**Response:** `List<ConductIncidentDTO>`

### Create Incident (Admin)
```
POST /api/conduct/incidents
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateConductIncidentRequest`
```json
{
  "enrollmentId": "uuid-of-enrollment",
  "incidentType": "WARNING",
  "description": "Llegó tarde 3 veces en la semana",
  "incidentDate": "2025-04-15",
  "severity": "MODERATE",
  "actionsTaken": "Citatorio a padres",
  "attentionDate": "2025-04-18"
}
```

### Update Incident (Admin)
```
PUT /api/conduct/incidents/{id}
```
**Auth:** `ADMIN` role

### Delete Incident (Admin)
```
DELETE /api/conduct/incidents/{id}
```
**Auth:** `ADMIN` role  
**Note:** Soft delete

### Get Deleted Incidents (Admin)
```
GET /api/conduct/incidents/deleted
```
**Auth:** `ADMIN` role

## Frontend Integration Guide

### 1. Get Student's Conduct Record
```javascript
const response = await fetch(`/api/conduct/by-enrollment/${enrollmentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();
// Returns conduct records with grade, warnings, congratulations
```

### 2. Create Conduct Record (Admin)
```javascript
const response = await fetch('/api/conduct', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    enrollmentId: "uuid-of-enrollment",
    academicSemesterId: "uuid-of-semester",
    grade: "A",
    observations: "Buena conducta",
    warnings: 0,
    congratulations: 2
  })
});
```

### 3. Register an Incident (Admin)
```javascript
const response = await fetch('/api/conduct/incidents', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    enrollmentId: "uuid-of-enrollment",
    incidentType: "WARNING",
    description: "Falta de respeto a compañeros",
    incidentDate: "2025-04-15",
    severity: "SERIOUS",
    actionsTaken: "Suspensión 1 día",
    attentionDate: "2025-04-16"
  })
});
```

### 4. Display Conduct in Student Profile (Vue.js example)
```javascript
const conduct = data; // from GET /api/conduct/by-enrollment/{id}
console.log(`Conducta: ${conduct.grade}`);
console.log(`Amonestaciones: ${conduct.warnings}`);
console.log(`Felicitaciones: ${conduct.congratulations}`);

// Get incidents
const incidentsResp = await fetch(`/api/conduct/incidents/by-enrollment/${enrollmentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data: incidents } = await incidentsResp.json();
incidents.forEach(inc => {
  console.log(`${inc.incidentType} - ${inc.description} (${inc.severity})`);
});
```

## Error Responses

```json
// 400 - Duplicate
{ "success": false, "message": "Ya existe un registro de conducta para esta inscripción y semestre" }

// 400 - Invalid incident type
{ "success": false, "message": "Tipo de incidente inválido. Valores: WARNING, CONGRATULATION, CALL_ATTENTION, SUSPENSION, OTHER" }

// 400 - Invalid severity
{ "success": false, "message": "Severidad inválida. Valores: MINOR, MODERATE, SERIOUS" }

// 404 - Not found
{ "success": false, "message": "Registro de conducta no encontrado" }
```

## Notes

- Soft delete only (no physical deletion)
- Unique constraint: one conduct record per enrollment + semester
- Incidents are linked directly to enrollment (not to conduct record)
- Conduct grade is a letter (A, B, C, D, F) - no numeric value
- Warnings and congratulations are auto-counted from incidents or manually set