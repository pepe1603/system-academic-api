# Guía de Testing - Módulo de Administración de Usuarios

Guía para administrar usuarios en el sistema académico.

## Tabla de Contenidos

1. [User Management Endpoints](#user-management-endpoints)
2. [Flujo de Administración Completa](#flujo-de-administración-completa)

---

## User Management Endpoints

**Base URL**: `http://localhost:8080/api/users`  
**Auth requerida**: `ADMIN` (JWT Bearer token)

---

### 1. Listar Usuarios (Paginado)

```
GET /api/users?page=0&size=20&sort=createdAt,desc
```

**Parámetros (query):**
| Parámetro | Default | Descripción |
|-----------|---------|-------------|
| page | 0 | Número de página |
| size | 20 | Tamaño por página |
| sort | createdAt,desc | Ordenamiento |

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "username": "admin",
        "email": "admin@enez.edu.mx",
        "isActive": true,
        "isVerified": false,
        "mustChangePassword": true,
        "roles": ["ADMIN"],
        "createdAt": "2026-04-24T12:00:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  },
  "requiresPasswordChange": false
}
```

---

### 2. Ver Usuario Específico

```
GET /api/users/{id}
```

**Parámetros (path):**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID (string) | ID del usuario |

**Respuesta:** Mismo formato que elemento individual de la lista.

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"

---

### 3. Crear Usuario

```
POST /api/users
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:** `CreateUserRequest`
```json
{
  "email": "usuario@enez.edu.mx",
  "curp": "XAXX010101HNEXXRA18",
  "roles": ["ADMIN"]
}
```

**Validaciones:**
| Campo | Regla |
|-------|------|
| email | @NotBlank, @Email (válido) |
| curp | Requerido si el rol es STUDENT/TEACHER |
| roles | Máx 2 si tiene CURP, 1 si no tiene |

**Reglas por Rol:**
| Rol | Requiere CURP | Máximo Roles |
|-----|--------------|---------------|
| STUDENT | Sí | 2 |
| TEACHER | Sí | 2 |
| ADMIN | No | 1 |
| CONTROL_ESCOLAR | No | 1 |
| DIRECTOR | No | 1 |

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario creado",
  "data": {
    "id": "uuid",
    "username": "usuario",
    "email": "usuario@enez.edu.mx",
    "isActive": true,
    "isVerified": false,
    "mustChangePassword": true,
    "roles": ["ADMIN"],
    "createdAt": "2026-04-24T12:00:00"
  },
  "requiresPasswordChange": false
}
```

**Excepciones:**
- `400 Bad Request` - "El email ya está registrado"
- `400 Bad Request` - "El CURP es requerido para los roles: [STUDENT, TEACHER]"
- `400 Bad Request` - "El CURP no corresponde a un registro académico activo"
- `400 Bad Request` - "No se puede asignar roles que requieren CURP con roles que no lo requieren"

**Nota:** El usuario recibe un email con:
- Username generado automáticamente
- Password temporal (12 caracteres)
- Debe cambiar password en primer login

---

### 4. Actualizar Usuario

```
PUT /api/users/{id}
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:** `UpdateUserRequest` (todos los campos son opcionales)
```json
{
  "isActive": true,
  "roles": ["ADMIN", "CONTROL_ESCOLAR"],
  "mustChangePassword": true
}
```

**Respuesta:** Mismo formato que `GET /api/users/{id}`

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"
- `400 Bad Request` - Roles inválidos

---

### 5. Eliminar Usuario (Soft Delete)

```
DELETE /api/users/{id}
Authorization: Bearer {token}
```

**Efecto:** Marca `isActive=false` e `isDeleted=true`. No borra físicamente.

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario eliminado",
  "data": null,
  "requiresPasswordChange": false
}
```

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"

---

### 6. Revocar Todas las Sesiones

```
DELETE /api/users/{id}/sessions
Authorization: Bearer {token}
```

**Efecto:** Invalida todos los JWT tokens activos del usuario.

**Respuesta:**
```json
{
  "success": true,
  "message": "Sesiones invalidadas",
  "data": null,
  "requiresPasswordChange": false
}
```

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"

---

### 7. Desbloquear Usuario (Manual)

```
PUT /api/users/{id}/unlock
Authorization: Bearer {token}
```

**Efecto:**
- `isLocked = false`
- `failedAttempts = 0`
- Elimina key de Redis (si existe)

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario desbloqueado",
  "data": null,
  "requiresPasswordChange": false
}
```

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"

**Nota:** También se desbloquea automáticamente tras 30 min (Redis TTL).

---

### 8. Bloquear Usuario (Manual)

```
PUT /api/users/{id}/lock
Authorization: Bearer {token}
```

**Efecto:**
- `isLocked = true`
- `failedAttempts = maxLoginAttempts` (10)
- Crea key en Redis con TTL 30 min

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario bloqueado",
  "data": null,
  "requiresPasswordChange": false
}
```

**Excepciones:**
- `404 Not Found` - "Usuario no encontrado"

---

## Flujo de Administración Completa

```
1. ADMIN hace login
   POST /api/auth/login
   → Guardar accessToken

2. Listar usuarios (página 1)
   GET /api/users?page=0&size=20
   → Ver todos los usuarios

3. Crear nuevo usuario
   POST /api/users
   Body: { "email": "nuevo@enez.edu.mx", "roles": ["STUDENT"] }
   → Usuario creado, email enviado con credenciales temporales

4. Usuario hace cambio de password temporal
   POST /api/auth/change-password-temp
   Body: { "email": "...", "tempPassword": "...", "newPassword": "...", "confirmPassword": "..." }
   → Recibe JWT tokens

5. Si usuario se bloquea por intentos fallidos
   → Se bloquea automáticamente tras 10 intentos
   → Redis TTL: 30 min (auto-unlock)
   O
   → ADMIN desbloquea manualmente:
      PUT /api/users/{id}/unlock

6. Revocar sesiones (si cuenta comprometida)
   DELETE /api/users/{id}/sessions
   → Todas las sesiones invalidadas

7. Eliminar usuario
   DELETE /api/users/{id}
   → Soft delete (isActive=false, isDeleted=true)
```

---

## 📝 Notas Importantes

| Concepto | Descripción |
|----------|-------------|
| **Soft Delete** | No borra el usuario físicamente, solo lo marca |
| **Auto-Unlock** | Redis TTL 30 min, luego auto-unlock en DB |
| **Failed Attempts** | Se resetea en login exitoso |
| **Roles mixtos** | No se pueden mezclar roles con/sin CURP |
| **Email verification** | Solo para registro público, no para creados por ADMIN |
