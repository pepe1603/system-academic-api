# Generations Module Documentation

## Overview

The Generations Module manages academic generations (cohorts) in the system. A generation represents a group of students that entered the institution in the same year.

## Database Schema

The generation table is defined in `db/postgresql/01_schema_academic.sql` (lines 92-103).

### Key Fields

- `id` - UUID primary key
- `name` - Generation name (e.g., "Generación 2024")
- `entry_year` - Year students entered
- `graduation_year` - Expected graduation year
- `status` - ACTIVE, GRADUATED, ARCHIVED
- `start_date` - Generation start date
- `end_date` - Generation end date
- `is_active` - Active status flag
- `is_deleted` - Soft delete flag

## API Endpoints

### Get All Generations (Paginated)
```
GET /api/generations?page=0&size=10
```
**Auth:** Any authenticated user  
**Response:** `List<GenerationDTO>`

### Get Generation by ID
```
GET /api/generations/{id}
```
**Auth:** Any authenticated user  
**Response:** `GenerationDTO`

### Create Generation (Admin only)
```
POST /api/generations
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `CreateGenerationRequest`
```json
{
  "name": "Generación 2024",
  "entryYear": 2024,
  "graduationYear": 2028,
  "status": "ACTIVE",
  "startDate": "2024-01-15",
  "endDate": "2028-12-15"
}
```

### Update Generation (Admin only)
```
PUT /api/generations/{id}
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateGenerationRequest`
```json
{
  "name": "Generación 2024 - Actualizada",
  "status": "GRADUATED"
}
```

### Delete Generation (Admin only)
```
DELETE /api/generations/{id}
```
**Auth:** `ADMIN` role  
**Response:** Success message

## DTOs

### GenerationDTO (Response)
```json
{
  "id": "uuid",
  "name": "Generación 2024",
  "entryYear": 2024,
  "graduationYear": 2028,
  "status": "ACTIVE",
  "startDate": "2024-01-15",
  "endDate": "2028-12-15",
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2024-01-15"
}
```

### CreateGenerationRequest
- `name` (required) - Generation name
- `entryYear` (required) - Entry year
- `graduationYear` (optional) - Graduation year
- `status` (required) - ACTIVE, GRADUATED, ARCHIVED
- `startDate` (required) - Start date
- `endDate` (optional) - End date

### UpdateGenerationRequest
All fields are optional:
- `name`
- `entryYear`
- `graduationYear`
- `status`
- `startDate`
- `endDate`

## Validations

- **Name unique** - No duplicate generation names
- **Entry year** - Must be >= 2000
- **Status** - Must be: ACTIVE, GRADUATED, or ARCHIVED
- **Dates** - `endDate` must be after `startDate` (validated in DB)

## Service Methods

### GenerationService

- `getAllGenerations(Pageable)` - Get all active generations (paginated)
- `getGenerationById(String id)` - Get generation by ID
- `createGeneration(CreateGenerationRequest)` - Create new generation
- `updateGeneration(String id, UpdateGenerationRequest)` - Update generation
- `deleteGeneration(String id)` - Soft delete generation

## Frontend Integration Guide

### 1. Get All Generations
```javascript
const response = await fetch(`/api/generations?page=0&size=10`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const { data } = await response.json();
// data is an array of GenerationDTO
```

### 2. Create Generation (Admin)
```javascript
const response = await fetch('/api/generations', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: "Generación 2026",
    entryYear: 2026,
    graduationYear: 2030,
    status: "ACTIVE",
    startDate: "2026-01-15",
    endDate: "2030-12-15"
  })
});
const { data } = await response.json();
```

### 3. Update Generation (Admin)
```javascript
const response = await fetch(`/api/generations/${generationId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: "Generación 2026 - Actualizada",
    status: "ACTIVE"
  })
});
```

### 4. Delete Generation (Admin)
```javascript
const response = await fetch(`/api/generations/${generationId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

## Notes

- Generations are soft-deleted (`is_deleted` flag)
- Unique constraint on `name` (case-insensitive)
- `entry_year` must be >= 2000 (validated in DB)
- `end_date` must be after `start_date` (validated in DB)
- When deleting, associated `student` records have `generation_id` set to NULL
- Paginated responses use Spring Data's `Page` interface

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler`:

- `IllegalArgumentException` → 400 Bad Request
- `MultipartException` → 400 Bad Request
- Generic exceptions → 500 Internal Server Error

Example error response:
```json
{
  "success": false,
  "message": "Ya existe una generación con ese nombre"
}
```
