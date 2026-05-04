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
- `getProfileByUserId(String userId)` - Get basic profile by user ID
- `getProfileByCurp(String curp)` - Get basic profile by CURP
- `createOrUpdateProfile(String userId, UpdateProfileRequest)` - Create or update profile
- `deleteProfile(String userId)` - Soft-delete profile

## Notes

- Profiles are soft-deleted (`is_deleted` flag)
- Unique constraints: `curp`, `rfc`, `employee_number`, `enrollment_number`, `institutional_email`
- Profile picture uploads are saved to `uploads/profile-pictures/` directory
- The `user_id` relationship is unique (one-to-one)
- Academic data enrichment works by matching the profile's CURP with Student/Teacher records
- Users can only update their own profile via `/api/profile/me` endpoints
- Admin users can update any profile via `/api/users/{id}/profile` endpoints
