# Guía de Testing - Módulo de Autenticación y Usuarios

Guía para probar los endpoints del sistema.

## Tabla de Contenidos

1. [Auth Endpoints](#auth-endpoints)
2. [Registration Endpoints](#registration-endpoints)
3. [User Management Endpoints](#user-management-endpoints)
4. [Flujos Completos](#flujos-completos)

---

## Auth Endpoints

**Base URL**: `http://localhost:8080/api/auth`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/login` | Inicio de sesión | No |
| POST | `/verify-2fa` | Verificar código 2FA | No |
| POST | `/refresh` | Renovar token | No |
| POST | `/logout` | Cerrar sesión | Sí |
| POST | `/recovery` | Solicitar código OTP de recuperación | No |
| POST | `/verify-otp` | Verificar código OTP | No |
| POST | `/reset-password` | Restablecer contraseña | No |
| POST | `/change-password` | Cambiar contraseña | Sí |
| POST | `/change-password-temp` | Cambiar password temporal (sin token) | No |
| POST | `/2fa/request-setup` | Solicitar setup 2FA | Sí |
| POST | `/2fa/enable` | Habilitar 2FA | Sí |
| POST | `/2fa/disable` | Deshabilitar 2FA | Sí |

### POST `/api/auth/login`

```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Respuesta exitosa:**
```json
{
    "success": true,
    "message": "Login exitoso",
    "data": {
        "userId": "uuid",
        "username": "admin",
        "email": "admin@enez.edu.mx",
        "roles": ["ADMIN"],
        "accessToken": "eyJhbG...",
        "refreshToken": "eyJhbG...",
        "expiresIn": 3600,
        "requiresTwoFactor": false
    }
}
```

**Respuesta - Cambio de contraseña requerido:**
```json
{
    "success": false,
    "message": "Debe cambiar su contraseña antes de continuar",
    "data": null,
    "requiresPasswordChange": true
}
```

---

### POST `/api/auth/change-password-temp`

**Descripción**: Permite cambiar la contraseña temporal sin necesidad de JWT token. Útil para usuarios nuevos que no pueden hacer login normal.

```json
{
    "email": "usuario@enez.edu.mx",
    "tempPassword": "ZMDeJc2y3xYY",
    "newPassword": "Test123456!",
    "confirmPassword": "Test123456!"
}
```

**Validaciones:**
- Password temporal debe coincidir con la asignada
- Email debe existir
- newPassword y confirmPassword deben ser iguales
- newPassword debe cumplir requisitos: 8+ chars, mayúscula, minúscula, número, símbolo

**Respuesta exitosa:**
```json
{
    "success": true,
    "message": "Contraseña actualizada",
    "data": {
        "userId": "uuid",
        "username": "usuario",
        "email": "usuario@enez.edu.mx",
        "roles": ["ADMIN"],
        "accessToken": "eyJhbG...",
        "refreshToken": "eyJhbG...",
        "expiresIn": 900000
    },
    "requiresPasswordChange": false
}
```

---

## Registration Endpoints

**Base URL**: `http://localhost:8080/api/auth/register`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/init` | Iniciar registro público | No |
| POST | `/verify` | Verificar registro con OTP | No |
| POST | `/verify-email` | Verificar email (opcional) | No |
| POST | `/resend-email-otp` | Reenviar código email | No |
| GET | `/status/{userId}` | Ver estado del usuario | No |

### POST `/api/auth/register/init` - Iniciar Registro

**Descripción**: Requiere CURP válido en student o teacher.

```json
{
    "curp": "XAXX010101HNEXXRA18",
    "email": "estudiante@enez.edu.mx"
}
```

**Respuesta:**
```json
{
    "success": true,
    "message": "Código de verificación enviado",
    "data": {
        "id": "uuid",
        "curp": "XAXX010101HNEXXRA18",
        "email": "estudiante@enez.edu.mx",
        "status": "PENDING"
    }
}
```

**Errores:**
- CURP no existe: "No se encontró registro académico activo con este CURP"
- Email ya registrado: "El email ya está registrado en el sistema"
- CURP ya usado: "Ya existe una solicitud de registro con este CURP"

---

### POST `/api/auth/register/verify` - Verificar Registro

```json
{
    "curp": "XAXX010101HNEXXRA18",
    "otp": "123456"
}
```

**Respuesta:**
```json
{
    "success": true,
    "message": "Registro completado",
    "data": {
        "id": "uuid",
        "curp": "XAXX010101HNEXXRA18",
        "email": "estudiante@enez.edu.mx",
        "status": "APPROVED"
    }
}
```

**Nota**: El usuario se crea con password temporal. Roles asignados según CURP (STUDENT o TEACHER).

---

### POST `/api/auth/register/verify-email` - Verificar Email (Opcional)

```json
{
    "code": "123456"
}
```

**Nota**: Esta verificación es opcional. El usuario puede hacer login sin verificar email.

---

## User Management Endpoints

**Base URL**: `http://localhost:8080/api/users`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar usuarios (paginados) | ADMIN |
| GET | `/{id}` | Ver usuario específico | ADMIN |
| POST | `/` | Crear usuario | ADMIN |
| PUT | `/{id}` | Editar usuario | ADMIN |
| DELETE | `/{id}` | Eliminar usuario (soft delete) | ADMIN |

### GET `/api/users` - Listar Usuarios

**Headers**: `Authorization: Bearer {accessToken}`

**Query Parameters**:
| Parámetro | Default | Descripción |
|-----------|---------|-------------|
| page | 0 | Número de página |
| size | 20 | Tamaño por página |
| sort | createdAt,desc | Ordenamiento |

---

### POST `/api/users` - Crear Usuario

**Validación de CURP por Rol:**

| Rol | Requiere CURP | Máximo Roles |
|-----|-------------|--------------|
| STUDENT | Sí | 2 |
| TEACHER | Sí | 2 |
| ADMIN | No | 1 |
| CONTROL_ESCOLAR | No | 1 |
| DIRECTOR | No | 1 |

**Crear ADMIN (sin CURP, 1 rol):**
```json
{
    "email": "admin@enez.edu.mx",
    "roles": ["ADMIN"]
}
```

**Crear STUDENT (con CURP, máx 2 roles):**
```json
{
    "email": "estudiante@enez.edu.mx",
    "curp": "XAXX010101HNEXXRA18",
    "roles": ["STUDENT"]
}
```

**Crear usuario con 2 roles (maestro que también es estudiante):**
```json
{
    "email": "profesor@enez.edu.mx",
    "curp": "XAXX010101HNEXXRA18",
    "roles": ["TEACHER", "STUDENT"]
}
```

**Errores:**
- Email ya existe: "El email ya está registrado"
- CURP requerido para STUDENT/TEACHER: "El CURP es requerido para los roles: [STUDENT, TEACHER]"
- CURP no válido: "El CURP no corresponde a un registro académico activo"
- Roles mixtos: "No se puede asignar roles que requieren CURP con roles que no lo requieren"
- Máximo roles excedido: "Los usuarios con registro académico pueden tener máximo 2 roles"

---

### PUT `/api/users/{id}` - Editar Usuario

```json
{
    "isActive": true,
    "roles": ["ADMIN", "CONTROL_ESCOLAR"],
    "mustChangePassword": true
}
```

**Errores:**
- Password temporal incorrecta: "Credenciales inválidas"
- Email no existe: "Usuario no encontrado"
- Contraseñas no coinciden: "Las contraseñas no coinciden"

---

## Flujo de Verificación de Email

El sistema maneja dos tipos de usuarios con diferente verificación:

| Tipo | `mustVerifyEmail` | `isVerified` | Puede hacer login? |
|------|------------------|--------------|-------------------|
| **Auto-registro público** | `true` | `false` | ❌ No - debe verificar email |
| **Creado por Admin** | `false` (default) | `false` | ✅ Sí - admin ya validó email |

**Auto-registro** (`/api/registration/**`):
- Usuario se crea con `mustVerifyEmail=true`
- Login requiere verificar email primero via `/api/registration/verify-email`

**Creado por Admin** (`/api/users`):
- Usuario se crea con `mustVerifyEmail=false` (default)
- Admin ya validó el email al crear la cuenta
- Puede hacer login directamente

---

## Flujos Completos

### Flujo 1: Registro Público de Estudiante

```
1. POST /api/auth/register/init
   Body: { "curp": "XAXX...", "email": "est@email.com" }
   → Recibe OTP por email

2. POST /api/auth/register/verify
   Body: { "curp": "XAXX...", "otp": "123456" }
   → Usuario creado con rol STUDENT
   → Password temporal enviado por email

3. POST /api/auth/login
   Body: { "username": "...", "password": "password_temp" }
   → Login exitoso (sin verificar email)

4. POST /api/auth/change-password (opcional)
   Headers: Authorization: Bearer {token}
   Body: { "currentPassword": "...", "newPassword": "...", "confirmPassword": "..." }
```

### Flujo 2: Admin Crear Usuario ADMIN

```
1. POST /api/auth/login
   Body: { "username": "admin", "password": "admin123" }
   → Guardar accessToken

2. POST /api/users
   Headers: Authorization: Bearer {accessToken}
   Body: { "email": "nuevo@enez.edu.mx", "roles": ["ADMIN"] }
   → Usuario creado con password temporal
   → Email enviado con credenciales

3. POST /api/auth/change-password-temp
   Body: { "email": "nuevo@enez.edu.mx", "tempPassword": "...", "newPassword": "...", "confirmPassword": "..." }
   → Devuelve tokens JWT completos

4. POST /api/auth/login
   Body: { "username": "...", "password": "nuevaPassword" }
   → Login normal exitoso
```

### Flujo 2b: Usuario Admin - Login Completo

```
1. Admin crea usuario → recibe password temporal por email

2. POST /api/auth/login
   Body: { "username": "...", "password": "tempPassword" }
   → { "requiresPasswordChange": true, "message": "Debe cambiar su contraseña" }

3. POST /api/auth/change-password-temp
   Body: { "email": "...", "tempPassword": "...", "newPassword": "...", "confirmPassword": "..." }
   → { "accessToken": "...", "refreshToken": "..." }

4. POST /api/auth/login (futuro)
   Body: { "username": "...", "password": "nuevaPassword" }
   → Login normal exitoso
```

### Flujo 3: Admin Crear STUDENT con 2 Roles

```
1. POST /api/users
   Headers: Authorization: Bearer {accessToken}
   Body: {
       "email": "maestro@enez.edu.mx",
       "curp": "XAXX010101HNEXXRA18",
       "roles": ["TEACHER", "STUDENT"]
   }
   → Usuario creado con 2 roles
```

### Flujo 4: Recuperación de Contraseña

```
1. POST /api/auth/recovery
   Body: { "email": "user@enez.edu.mx" }
   → OTP enviado por email

2. POST /api/auth/verify-otp
   Body: { "email": "user@enez.edu.mx", "code": "123456", "purpose": "PASSWORD_RECOVERY" }

3. POST /api/auth/reset-password
   Body: { "email": "user@enez.edu.mx", "token": "123456", "newPassword": " nueva..." }
```

### Flujo 5: Login con 2FA

```
1. POST /api/auth/login
   Body: { "username": "...", "password": "..." }
   → Recibe tempToken

2. POST /api/auth/verify-2fa
   Body: { "tempToken": "...", "code": "123456" }
   → Login completo
```

---

## Códigos de Error

| Código | Descripción |
|--------|-------------|
| 200 | Éxito |
| 400 | Validación fallida |
| 401 | No autenticado |
| 403 | Sin permisos (ADMIN requerido) |
| 429 | Rate limit excedido |

---

## Variables de Entorno Postman

```json
{
    "baseUrl": "http://localhost:8080/api",
    "authUrl": "http://localhost:8080/api/auth",
    "accessToken": "",
    "refreshToken": ""
}
```