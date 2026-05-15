# Guardian Module Documentation

## Overview

The Guardian Module manages student guardians/tutors. Each student can have multiple guardians (parents, guardians, siblings, etc.).

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 634-651).

### Key Fields

- `id` - UUID primary key
- `student_id` - Reference to student (CASCADE)
- `full_name` - Guardian full name
- `relationship` - FATHER, MOTHER, GUARDIAN, SIBLING, OTHER
- `curp` - CURP
- `primary_phone` / `secondary_phone` - Contact phones
- `email` - Email
- `occupation` / `company` - Work info
- `address` - Address
- `is_emergency_contact` - Emergency contact flag

## API Endpoints

### Get All Guardians
```
GET /api/guardians?page=0&size=10
```
**Auth:** Any authenticated user

### Get Guardian by ID
```
GET /api/guardians/{id}
```
**Auth:** Any authenticated user

### Get Guardians by Student
```
GET /api/guardians/by-student/{studentId}
```
**Auth:** Any authenticated user

### Create Guardian (Admin)
```
POST /api/guardians
Content-Type: application/json
```
**Auth:** `ADMIN` role
```json
{
  "studentId": "uuid-of-student",
  "fullName": "Juan Pérez López",
  "relationship": "FATHER",
  "curp": "PELJ850101HDFRRN00",
  "primaryPhone": "555-1234-5678",
  "email": "juan.perez@email.com",
  "occupation": "Ingeniero",
  "isEmergencyContact": true
}
```

### Update Guardian (Admin)
```
PUT /api/guardians/{id}
```
**Auth:** `ADMIN` role

### Delete Guardian (Admin)
```
DELETE /api/guardians/{id}
```
**Auth:** `ADMIN` role (soft delete)

### Get Deleted Guardians (Admin)
```
GET /api/guardians/deleted?page=0&size=10
```
**Auth:** `ADMIN` role

## Frontend Integration

```javascript
// Get guardians for a student
const response = await fetch(`/api/guardians/by-student/${studentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data } = await response.json();

// Create guardian (Admin)
const response = await fetch('/api/guardians', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    studentId: "uuid-of-student",
    fullName: "María López",
    relationship: "MOTHER",
    primaryPhone: "555-8765-4321",
    isEmergencyContact: true
  })
});
```

## Error Responses

```json
// 400 - Invalid relationship
{ "success": false, "message": "Parentesco inválido. Valores: FATHER, MOTHER, GUARDIAN, SIBLING, OTHER" }

// 404 - Not found
{ "success": false, "message": "Tutor no encontrado" }
```

## Notes

- Soft delete only
- Multiple guardians per student allowed
- Use `isEmergencyContact` to mark who to contact in emergencies