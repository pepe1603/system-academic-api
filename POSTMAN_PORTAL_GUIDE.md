# Guía de Testing - Módulo Portal Público

Guía para probar los endpoints del módulo de portal público usando **Postman**.

## Información General

- **Base URL**: `http://localhost:8080/api/portal`
- **Content-Type**: `application/json`

---

## Tabla de Endpoints

| Método | Endpoint | Descripción | Auth Requerida |
|--------|----------|-------------|----------------|
| GET | `/institution` | Ver información institucional | No |
| PUT | `/institution` | Actualizar información | ✅ ADMIN |
| GET | `/news` | Listar noticias publicadas | No |
| GET | `/news/{id}` | Ver noticia por ID | No |
| POST | `/news` | Crear noticia | ✅ ADMIN |
| PUT | `/news/{id}` | Actualizar noticia | ✅ ADMIN |
| DELETE | `/news/{id}` | Eliminar noticia | ✅ ADMIN |
| GET | `/events` | Listar eventos publicados | No |
| GET | `/events/{id}` | Ver evento por ID | No |
| POST | `/events` | Crear evento | ✅ ADMIN |
| PUT | `/events/{id}` | Actualizar evento | ✅ ADMIN |
| DELETE | `/events/{id}` | Eliminar evento | ✅ ADMIN |
| GET | `/ads` | Listar anuncios publicados | No |
| GET | `/ads/{position}` | Anuncios por posición | No |
| POST | `/ads` | Crear anuncio | ✅ ADMIN |
| PUT | `/ads/{id}` | Actualizar anuncio | ✅ ADMIN |
| DELETE | `/ads/{id}` | Eliminar anuncio | ✅ ADMIN |
| POST | `/contact` | Enviar mensaje de contacto | No |
| GET | `/contact` | Ver todos los mensajes | ✅ ADMIN |
| GET | `/contact/unread` | Ver mensajes sin leer | ✅ ADMIN |
| PUT | `/contact/{id}/read` | Marcar como leído | ✅ ADMIN |
| POST | `/contact/{id}/respond` | Responder mensaje | ✅ ADMIN |

---

## 1. Endpoints Públicos (Sin Auth)

### GET `/institution`

**Descripción**: Obtiene la información institucional (nombre, misión, visión, etc.)

**Request**:
```
GET http://localhost:8080/api/portal/institution
```

**Respuesta (200)**:
```json
{
    "success": true,
    "message": null,
    "data": {
        "id": "a3b31a98-3233-11f1-837c-4a158e121aa6",
        "name": "Escuela Normal Emiliano Zapata",
        "address": "Av. principal S/N, Col. Centro",
        "phone": "+52 999 999 9999",
        "email": "contacto@enez.edu.mx",
        "website": "https://www.enez.edu.mx",
        "mission": "Formar profesionales de la educación...",
        "vision": "Ser una institución líder...",
        "history": "Institución de educación superior...",
        "values": "Integridad, Excelencia, Compromiso...",
        "logoUrl": null,
        "isActive": true,
        "createdAt": "2026-04-06T21:41:19"
    }
}
```

---

### GET `/news`

**Descripción**: Lista todas las noticias publicadas

**Request**:
```
GET http://localhost:8080/api/portal/news
```

**Respuesta (200)**:
```json
{
    "success": true,
    "message": null,
    "data": [
        {
            "id": "...",
            "title": "Inicio del Semestre 2025-1",
            "content": "Se da inicio oficial al nuevo semestre académico...",
            "imageUrl": null,
            "isPublished": true,
            "isDeleted": false,
            "createdAt": "2026-04-06T21:41:20",
            "updatedAt": "2026-04-06T21:41:20"
        }
    ]
}
```

---

### GET `/news/paged`

**Descripción**: Lista noticias con paginación

**Request**:
```
GET http://localhost:8080/api/portal/news/paged?page=0&size=10
```

**Parámetros**:
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| page | int | 0 | Número de página (0-indexed) |
| size | int | 10 | Elementos por página |

**Respuesta (200)**:
```json
{
    "success": true,
    "message": null,
    "data": {
        "content": [
            {
                "id": "a4545c17-3233-11f1-837c-4a158e121aa6",
                "title": "Inicio del Semestre 2025-1",
                "content": "Se da inicio oficial al nuevo semestre académico. Bienvenidos estudiantes.",
                "imageUrl": null,
                "isPublished": true,
                "isDeleted": false,
                "createdAt": "2026-04-06T21:41:20",
                "updatedAt": "2026-04-06T21:41:20"
            },
            {
                "id": "a45462dc-3233-11f1-837c-4a158e121aa6",
                "title": "Convocatoria de Inscripción 2025",
                "content": "Inscripciones abiertas para nuevo ingreso. Consulta los requisitos.",
                "imageUrl": null,
                "isPublished": true,
                "isDeleted": false,
                "createdAt": "2026-04-06T21:41:20",
                "updatedAt": "2026-04-06T21:41:20"
            },
            {
                "id": "a4547c44-3233-11f1-837c-4a158e121aa6",
                "title": "Premio a la Excelencia Académica",
                "content": "Convocatoria para estudiantes con promedio mayor a 90.",
                "imageUrl": null,
                "isPublished": true,
                "isDeleted": false,
                "createdAt": "2026-04-06T21:41:20",
                "updatedAt": "2026-04-06T21:41:20"
            },
            {
                "id": "a4547d78-3233-11f1-837c-4a158e121aa6",
                "title": "Semana de la Educación 2025",
                "content": "Evento académico con conferencias y talleres pedagógicos.",
                "imageUrl": null,
                "isPublished": true,
                "isDeleted": false,
                "createdAt": "2026-04-06T21:41:20",
                "updatedAt": "2026-04-06T21:41:20"
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 10,
            "sort": {
                "empty": false,
                "sorted": true,
                "unsorted": false
            },
            "offset": 0,
            "paged": true,
            "unpaged": false
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 4,
        "first": true,
        "size": 10,
        "number": 0,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "numberOfElements": 4,
        "empty": false
    }
}
```

---

### GET `/events`

**Descripción**: Lista todos los eventos publicados

**Request**:
```
GET http://localhost:8080/api/portal/events
```

---

### GET `/events/paged`

**Descripción**: Lista eventos con paginación

**Request**:
```
GET http://localhost:8080/api/portal/events/paged?page=0&size=10
```

**Parámetros**:
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| page | int | 0 | Número de página (0-indexed) |
| size | int | 10 | Elementos por página |

**Respuesta (200)**:
```json
{
    "success": true,
    "message": null,
    "data": {
        "content": [
            {
                "id": "...",
                "title": "Evento 1",
                "description": "...",
                "eventDate": "2025-06-10",
                "location": "Auditorio Principal"
            }
        ],
        "totalElements": 4,
        "totalPages": 1,
        "size": 10,
        "number": 0
    }
}
```

---

### GET `/ads`

**Descripción**: Lista todos los anuncios/banners publicados

**Request**:
```
GET http://localhost:8080/api/portal/ads
```

---

### GET `/ads/{position}`

**Descripción**: Lista anuncios por posición (BANNER, SIDEBAR, FOOTER)

**Request**:
```
GET http://localhost:8080/api/portal/ads/BANNER
```

---

### POST `/contact`

**Descripción**: Envía un mensaje de contacto

**Request Body**:
```json
{
    "fullName": "Juan Pérez",
    "email": "juan@email.com",
    "phone": "+52 999 123 4567",
    "subject": "Consulta sobre admisiones",
    "message": "Me gustaría saber los requisitos de inscripción..."
}
```

**Respuesta (200)**:
```json
{
    "success": true,
    "message": "Mensaje enviado correctamente",
    "data": {
        "id": "...",
        "fullName": "Juan Pérez",
        "email": "juan@email.com",
        "phone": "+52 999 123 4567",
        "subject": "Consulta sobre admisiones",
        "message": "Me gustaría saber los requisitos de inscripción...",
        "isRead": false,
        "isResponded": false,
        "response": null,
        "responseDate": null,
        "createdAt": "2026-04-06T22:00:00"
    }
}
```

---

## 2. Endpoints Privados (Requiere Auth - ADMIN)

### Autenticación

Para acceder a los endpoints privados, necesitas:

1. **Hacer login** para obtener el access token:
```
POST http://localhost:8080/api/auth/login
```

**Body**:
```json
{
    "username": "admin",
    "password": "admin123"
}
```

2. **Usar el token** en los headers:
```
Authorization: Bearer {{accessToken}}
```

---

### PUT `/institution`

**Descripción**: Actualiza la información institucional

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body**:
```json
{
    "id": "a3b31a98-3233-11f1-837c-4a158e121aa6",
    "name": "Escuela Normal Emiliano Zapata",
    "address": "Nueva dirección...",
    "mission": "Nueva misión..."
}
```

---

### POST `/news` - Crear Noticia

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body**:
```json
{
    "title": "Nueva Noticia",
    "content": "Contenido de la noticia...",
    "imageUrl": "/images/news/imagen.jpg",
    "isPublished": true
}
```

---

### DELETE `/news/{id}` - Eliminar Noticia

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request**:
```
DELETE http://localhost:8080/api/portal/news/{id}
```

---

### POST `/events` - Crear Evento

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body**:
```json
{
    "title": "Conferencia de Matemáticas",
    "description": "Conferencia sobre metodologías de enseñanza...",
    "eventDate": "2025-04-20",
    "location": "Auditorio Principal",
    "isPublished": true
}
```

---

### POST `/ads` - Crear Anuncio

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body**:
```json
{
    "title": "Nuevo Programa",
    "description": "Descripción del programa...",
    "imageUrl": "/images/ads/banner.jpg",
    "linkUrl": "/programa",
    "position": "BANNER",
    "displayOrder": 1,
    "isPublished": true,
    "startDate": "2025-04-01",
    "endDate": "2025-04-30"
}
```

---

### GET `/contact` - Ver Todos los Mensajes

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request**:
```
GET http://localhost:8080/api/portal/contact
```

---

### PUT `/contact/{id}/read` - Marcar como Leído

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request**:
```
PUT http://localhost:8080/api/portal/contact/{id}/read
```

---

### POST `/contact/{id}/respond` - Responder Mensaje

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body** (plain text):
```
Gracias por contactarnos. Su inscripción está abierta...
```

**Nota**: Al responder, automáticamente se marca el mensaje como leído.

---

## 3. Request Bodies Completos

### Auth - Login
```json
{
    "username": "admin",
    "password": "admin123"
}
```

### PUT /api/portal/institution - Actualizar Institución
```json
{
    "id": "a3b31a98-3233-11f1-837c-4a158e121aa6",
    "name": "Escuela Normal Emiliano Zapata",
    "address": "Av. Principal 123, Col. Centro",
    "phone": "+52 999 123 4567",
    "email": "contacto@enez.edu.mx",
    "website": "https://www.enez.edu.mx",
    "mission": "Formar profesionales de la educación con calidad y compromiso social",
    "vision": "Ser una institución líder en formación docente",
    "history": "Historia de la institución...",
    "values": "Integridad, Excelencia, Compromiso, Innovación",
    "logoUrl": "/images/logo.png"
}
```

### POST /api/portal/news - Crear Noticia
```json
{
    "title": "Nueva Noticia Importante",
    "content": "Contenido completo de la noticia. Puede contener múltiples párrafos y detalles relevantes sobre el evento o announcement.",
    "imageUrl": "/images/news/2025-noticia.jpg",
    "isPublished": true
}
```

### PUT /api/portal/news/{id} - Actualizar Noticia
```json
{
    "title": "Título Actualizado",
    "content": "Nuevo contenido de la noticia...",
    "imageUrl": "/images/news/nueva-imagen.jpg",
    "isPublished": true
}
```

### POST /api/portal/events - Crear Evento
```json
{
    "title": "Conferencia de Matemáticas 2025",
    "description": "Conferencia sobre metodologías de enseñanza innovadoras en matemáticas para nivel básico y medio superior.",
    "eventDate": "2025-06-15",
    "location": "Auditorio Principal",
    "isPublished": true
}
```

### PUT /api/portal/events/{id} - Actualizar Evento
```json
{
    "title": "Conferencia de Matemáticas - Fecha Cambiada",
    "description": "Conferencia actualizada...",
    "eventDate": "2025-06-20",
    "location": "Salón de Eventos A",
    "isPublished": true
}
```

### POST /api/portal/ads - Crear Anuncio
```json
{
    "title": "Programa de Becas 2025",
    "description": "Convocatoria para estudiantes de nuevo ingreso. Consulta los requisitos completos en nuestra sección de admisiones.",
    "imageUrl": "/images/ads/becas-2025.jpg",
    "linkUrl": "/admisiones/becas",
    "position": "BANNER",
    "displayOrder": 1,
    "isPublished": true,
    "startDate": "2025-04-01",
    "endDate": "2025-06-30"
}
```

### PUT /api/portal/ads/{id} - Actualizar Anuncio
```json
{
    "title": "Programa de Becas - Actualizado",
    "description": "Nueva descripción...",
    "imageUrl": "/images/ads/becas-updated.jpg",
    "linkUrl": "/admisiones/becas",
    "position": "BANNER",
    "displayOrder": 1,
    "isPublished": true,
    "startDate": "2025-04-01",
    "endDate": "2025-07-31"
}
```

### POST /api/portal/contact - Enviar Mensaje de Contacto
```json
{
    "fullName": "Juan Pérez López",
    "email": "juan.perez@email.com",
    "phone": "+52 999 123 4567",
    "subject": "Consulta sobre programas académicos",
    "message": "Hola, me gustaría obtener información sobre los programas de licenciaturas disponibles para el ciclo 2025-1. ¿Qué documentos necesito para el proceso de inscripción?"
}
```

### POST /api/portal/contact/{id}/respond - Responder Mensaje
**Content-Type: text/plain**

Body (plain text, sin JSON):
```
Gracias por contactarnos. Le informamos que nuestro proceso de inscripción está abierto. Puede consultar los requisitos en nuestra página web o visitarnos directamente en oficina de admisiones.
```

### Environment Variables Recomendadas

```json
{
    "key": "baseUrl",
    "value": "http://localhost:8080/api/portal",
    "enabled": true
},
{
    "key": "accessToken",
    "value": "",
    "enabled": true
}
```

---

## Códigos de Respuesta HTTP

| Código | Descripción |
|--------|-------------|
| 200 | Éxito |
| 400 | Solicitud inválida |
| 401 | No autenticado |
| 403 | Sin permisos (no es admin) |
| 404 | Recurso no encontrado |
| 500 | Error interno del servidor |

---

## Errores Comunes

### Error 401 - No autenticado
```json
{
    "success": false,
    "message": "Token no proporcionado",
    "data": null
}
```
**Solución**: Asegúrate de incluir el header `Authorization: Bearer {{token}}`

### Error 403 - Sin permisos
```json
{
    "success": false,
    "message": "Access Denied",
    "data": null
}
```
**Solución**: El usuario debe tener rol ADMIN para acceder a estos endpoints