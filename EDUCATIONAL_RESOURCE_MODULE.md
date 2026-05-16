# Educational Resource Module Documentation

## Overview

The Educational Resource Module manages learning resources (PDFs, videos, links, documents, presentations) associated with courses.

## Database Schema

Defined in `db/postgresql/01_schema_academic.sql` (lines 694-706).

### Key Fields

- `id` - UUID primary key
- `title` - Resource title
- `resource_type` - PDF, VIDEO, LINK, DOCUMENT, PRESENTATION
- `resource_url` - URL or path to resource
- `course_id` - Reference to course (SET NULL)
- `is_published` - Publish status

## API Endpoints

```
GET    /api/educational-resources?page=0&size=10
GET    /api/educational-resources/{id}
GET    /api/educational-resources/by-course/{courseId}
POST   /api/educational-resources  (ADMIN)
PUT    /api/educational-resources/{id}  (ADMIN)
DELETE /api/educational-resources/{id}  (ADMIN)
GET    /api/educational-resources/deleted  (ADMIN)
```

## Notes

- Resource types: PDF, VIDEO, LINK, DOCUMENT, PRESENTATION
- Can be linked to a course or standalone
- Soft delete only