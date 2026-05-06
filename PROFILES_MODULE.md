# Profiles Module Documentation

## Overview

The Profiles Module consolidates personal information (firstName, lastName, CURP, RFC, phone, address, etc.) into a unified `UserProfile` entity linked to the `User` entity. The profile is enriched with academic data from `Student` and `Teacher` entities based on the user's roles and CURP.

## Database Schema

The profile is stored in PostgreSQL in the `user_profile` table (migration `db/postgresql/06_user_profile.sql`).

### Key Fields

- `id` - UUID primary key
- `user_id` - Foreign key to `app_user` (one-to-one relationship)
- `first_name`, `last_name` - Personal names
- `curp`, `rfc` - Mexican identity tax codes (unique)
- `phone`, `secondary_phone` - Contact numbers
- `birth_date`, `gender` - Personal details
- `employee_number`, `enrollment_number` - Academic identifiers (unique)
- `institutional_email`, `secondary_email` - Email addresses
- `address`, `city`, `state`, `postal_code` - Address fields
- `profile_picture_url` - Profile picture path
- `is_active`, `is_deleted` - Status flags

## API Endpoints

### Get Current User's Enriched Profile
```
GET /api/profile/me
```
**Auth:** Any authenticated user  
**Response:** `EnrichedProfileDTO` with roles and academic info (Student/Teacher) based on CURP

### Update Current User's Profile
```
PUT /api/profile/me
Content-Type: application/json
```
**Auth:** Any authenticated user (can only update their own profile)  
**Body:** `UpdateProfileRequest`
```json
{
  "firstName": "Juan",
  "lastName": "Perez",
  "curp": "PERJ800101HDFXXX",
  "rfc": "PERJ800101XXX",
  "phone": "5551234567",
  "secondaryPhone": "5557654321",
  "birthDate": "1980-01-01",
  "gender": "M",
  "employeeNumber": "EMP001",
  "enrollmentNumber": "ENR001",
  "institutionalEmail": "juan.perez@academic.com",
  "secondaryEmail": "juan@gmail.com",
  "address": "Calle Principal 123",
  "city": "Mexico City",
  "state": "CDMX",
  "postalCode": "01000",
  "profilePictureUrl": "/uploads/profile-pictures/uuid_file.jpg"
}
```

### Upload Profile Picture
```
POST /api/profile/me/picture
Content-Type: multipart/form-data
```
**Auth:** Any authenticated user (can only upload their own picture)  
**Param:** `file` (MultipartFile)  
**Response:** Updated `UserProfileDTO` with new `profilePictureUrl`

### Get Any User's Enriched Profile (Admin)
```
GET /api/users/{id}/profile
```
**Auth:** `ADMIN` role  
**Response:** `EnrichedProfileDTO` with roles and academic info

### Update Any User's Profile (Admin)
```
PUT /api/users/{id}/profile
Content-Type: application/json
```
**Auth:** `ADMIN` role  
**Body:** `UpdateProfileRequest`  
**Response:** Updated `UserProfileDTO`

## Enriched Profile Response

The `GET /api/profile/me` and `GET /api/users/{id}/profile` endpoints return an `EnrichedProfileDTO` that includes:

- All basic profile fields
- `roles` - Set of role names (e.g., ["STUDENT", "TEACHER"])
- `studentInfo` - Academic data from Student entity (if user has STUDENT role and CURP matches)
- `teacherInfo` - Academic data from Teacher entity (if user has TEACHER role and CURP matches)

### Multiple Roles Support

A user can have multiple roles (e.g., both `STUDENT` and `TEACHER`). In this case, the enriched profile will include both `studentInfo` and `teacherInfo` if the CURP matches records in both tables.

## Service Methods

### UserProfileService

- `getEnrichedProfileByUserId(String userId)` - Get enriched profile with academic data based on roles and CURP
- `getEnrichedProfileByCurp(String curp)` - Get enriched profile by CURP (for admins)
- `getProfileByUserId(String userId)` - Get basic profile by user ID
- `getProfileByCurp(String curp)` - Get basic profile by CURP
- `createOrUpdateProfile(String userId, UpdateProfileRequest)` - Create or update profile
- `deleteProfile(String userId)` - Soft-delete profile
- `getAcademicHistory(String userId)` - Get user's academic history

### ProfileMigrationService

- `migrateExistingProfiles()` - Migrates data from `student`/`teacher` to `user_profile`

## Profile Migration

### Automatic Migration
The system automatically migrates existing `student` and `teacher` data to `user_profile` on application startup. This ensures data consistency across the system.

**What happens during migration:**
- Reads all active students and teachers with `user_id` set
- Creates `UserProfile` records linked to the corresponding `app_user`
- Copies personal data: `first_name`, `last_name`, `curp`, `rfc`, `phone`, `email`, etc.
- Skips users who already have a profile (idempotent operation)

**Logs to watch for:**
```
INFO  c.a.s.ProfileMigrationService - Starting profile migration...
INFO  c.a.s.ProfileMigrationService - Migrated student profile for user_id: xxx
INFO  c.a.s.ProfileMigrationService - Profile migration completed. Students migrated: X, Teachers migrated: Y
```

### Manual Migration (Admin)
Admins can trigger migration manually via:

```
POST /api/admin/migrate-profiles
Header: Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Migración de perfiles completada exitosamente"
}
```

## Data Synchronization

When a `UserProfile` is updated, the system automatically syncs the changes to the corresponding `Student` or `Teacher` entity based on the user's roles:

- **STUDENT role**: Syncs `first_name`, `last_name`, `phone`, `institutional_email`, `birth_date`, `gender`
- **TEACHER role**: Syncs `first_name`, `last_name`, `rfc`, `phone`, `institutional_email`

This ensures backward compatibility with existing code that reads from `student`/`teacher` tables.

## Validations

### CURP Validation
- Format: 18 characters
- Pattern: `AAAA######AAAAAAXX` (4 letters + 6 digits + 6 letters/digits + 2 digits/letters)
- Validated on profile update

### RFC Validation
- Format: 13 characters
- Pattern: `AAA######AAA` (3-4 letters + 6 digits + 3 alphanumeric)
- Validated on profile update

## Notes

- Profiles are soft-deleted (`is_deleted` flag)
- Unique constraints: `curp`, `rfc`, `employee_number`, `enrollment_number`, `institutional_email`
- Profile picture uploads are saved to `uploads/profile-pictures/` directory
- The `user_id` relationship is unique (one-to-one)
- Academic data enrichment works by matching the profile's CURP with Student/Teacher records
- Users can only update their own profile via `/api/profile/me` endpoints
- Admin users can update any profile via `/api/users/{id}/profile` endpoints
- Migration is idempotent (can run multiple times without duplicating data)

## Frontend Integration Guide

### 1. Get Current User's Profile
```javascript
// Fetch enriched profile with academic data
const response = await fetch('/api/profile/me', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const data = await response.json();
// data.data contains EnrichedProfileDTO
```

### 2. Update Profile
```javascript
const response = await fetch('/api/profile/me', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    firstName: "Juan",
    lastName: "Perez",
    curp: "PERJ800101HDFXXX",
    rfc: "PERJ800101XXX",
    phone: "5551234567"
  })
});
```

### 3. Upload Profile Picture
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch('/api/profile/me/picture', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});
```

### 4. Search Profile by CURP (Admin)
```javascript
const response = await fetch(`/api/profile/search?curp=PERJ800101HDFXXX`, {
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

### 5. Sample Response (EnrichedProfileDTO)
```json
{
  "success": true,
  "data": {
    "firstName": "Juan",
    "lastName": "Perez",
    "curp": "PERJ800101HDFXXX",
    "roles": ["STUDENT", "TEACHER"],
    "studentInfo": {
      "enrollmentNumber": "ENR001",
      "enrollmentDate": "2024-01-15",
      "generationId": "uuid-here"
    },
    "teacherInfo": {
      "employeeNumber": "EMP001",
      "rfc": "PERJ800101XXX"
    }
  }
}
```
