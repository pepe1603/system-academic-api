# Guía de Testing - Módulo de Autenticación

Guía para probar los endpoints del módulo de autenticación usando **Postman**.

## Información General

- **Base URL**: `http://localhost:8080/api/auth`
- **Content-Type**: `application/json`
- **Branch**: `feature/auth-module`

---

## Tabla de Endpoints

| Método | Endpoint | Descripción | Auth Requerida |
|--------|----------|-------------|----------------|
| GET | `/health` | Verificar estado del servidor (simple) | No |
| GET | `/monitor` | Verificar estado con detalles de servicios | Sí |
| POST | `/login` | Inicio de sesión | No |
| POST | `/verify-2fa` | Verificar código 2FA | No |
| POST | `/refresh` | Renovar token | No |
| POST | `/logout` | Cerrar sesión | Sí |
| POST | `/recovery` | Solicitar código OTP de recuperación | No |
| POST | `/verify-otp` | Verificar código OTP | No |
| POST | `/reset-password` | Restablecer contraseña | No |
| POST | `/change-password` | Cambiar contraseña | Sí |
| POST | `/register` | Registro de usuario | No |
| POST | `/admin/register` | Registro por administrador | Sí (ADMIN) |
| POST | `/2fa/request-setup` | Solicitar setup 2FA | Sí |
| POST | `/2fa/enable` | Habilitar 2FA | Sí |
| POST | `/2fa/disable` | Deshabilitar 2FA | Sí |
| POST | `/registration/init` | Iniciar registro público | No |
| POST | `/registration/verify` | Verificar registro con OTP | No |
| POST | `/registration/verify-email` | Verificar email | No |
| GET | `/admin/users` | Listar usuarios | Sí (ADMIN) |
| POST | `/admin/users` | Crear usuario | Sí (ADMIN) |
| GET | `/admin/users/registrations` | Ver solicitudes de registro | Sí (ADMIN) |
| GET | `/admin/users/registrations/pending` | Ver solicitudes pendientes | Sí (ADMIN) |

---

## 1. Verificar Estado del Servidor

### GET `/api/health` (Sin Auth)

**Descripción**: Verifica que el servidor esté funcionando. Endpoint público sin autenticación.

**Request**: No requiere body ni headers.

**Respuesta Exitosa (200)**:
```json
{
    "status": "UP",
    "timestamp": "2026-04-05T14:30:00Z"
}
```

---

### GET `/api/monitor` (Con Auth)

**Descripción**: Verifica el estado del servidor y sus servicios (DB, Redis). Requiere autenticación.

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "status": "UP",
    "timestamp": "2026-04-05T14:30:00Z",
    "services": {
        "database": "UP",
        "redis": "UP"
    }
}
```

**Respuesta Degradada (503)** - Cuando algún servicio está caído:
```json
{
    "status": "DEGRADED",
    "timestamp": "2026-04-05T14:30:00Z",
    "services": {
        "database": "UP",
        "redis": "DOWN"
    }
}
```

---

## 2. Login

### POST `/api/auth/login`

**Descripción**: Autentica un usuario y retorna tokens de acceso.

**Request Body**:
```json
{
    "username": "string",
    "password": "string"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Login exitoso",
    "data": {
        "userId": "uuid",
        "username": "string",
        "email": "string",
        "roles": ["ROLE_USER", "ROLE_STUDENT"],
        "permissions": ["READ", "WRITE"],
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 3600,
        "requiresTwoFactor": false,
        "tempToken": null,
        "message": "Login exitoso"
    }
}
```

**Respuesta con 2FA Habilitado (200)**:
```json
{
    "success": true,
    "message": "Verificación 2FA requerida",
    "data": {
        "requiresTwoFactor": true,
        "tempToken": "temporal_token_para_verificacion",
        "message": "Ingrese el código de verificación"
    }
}
```

**Configuración en Postman**:
1. Método: `POST`
2. URL: `{{baseUrl}}/login`
3. Body: `raw` → `JSON`

---

## 3. Verificar 2FA

### POST `/api/auth/verify-2fa`

**Descripción**: Verifica el código OTP para completar el login.

**Request Body**:
```json
{
    "tempToken": "temporal_token_recibido_en_login",
    "code": "123456",
    "backupCode": null
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Verificación exitosa",
    "data": {
        "userId": "uuid",
        "username": "string",
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "roles": ["ROLE_USER"],
        "permissions": ["READ"]
    }
}
```

---

## 4. Refrescar Token

### POST `/api/auth/refresh`

**Descripción**: Renueva el token de acceso usando el refresh token.

**Request Body**:
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Token renovado exitosamente",
    "data": "nuevo_access_token"
}
```

---

## 5. Logout

### POST `/api/auth/logout`

**Descripción**: Invalida la sesión actual.

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Sesión cerrada exitosamente",
    "data": null
}
```

---

## 6. Recuperación de Contraseña (Flujo de 3 Pasos)

### Paso 1: Solicitar Código OTP

#### POST `/api/auth/recovery`

**Descripción**: Envía un código OTP de 6 dígitos al email del usuario.

**Request Body**:
```json
{
    "email": "usuario@example.com"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Si el email existe, se enviará un código de recuperación",
    "data": null
}
```

---

### Paso 2: Verificar Código OTP

#### POST `/api/auth/verify-otp`

**Descripción**: Verifica el código OTP recibido. Si es válido, permite continuar al siguiente paso.

**Request Body**:
```json
{
    "email": "usuario@example.com",
    "code": "123456",
    "purpose": "PASSWORD_RECOVERY"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Código verificado correctamente",
    "data": null
}
```

**Error - Código Inválido (400)**:
```json
{
    "success": false,
    "message": "Código inválido. Intentos restantes: 5",
    "data": null
}
```

**Error - Cuenta Bloqueada (429)**:
```json
{
    "success": false,
    "message": "Cuenta bloqueada por demasiados intentos fallidos. Intenta en 15 minutos",
    "data": null
}
```

---

### Paso 3: Restablecer Contraseña

#### POST `/api/auth/reset-password`

**Descripción**: Restablece la contraseña después de verificar el código OTP.

**Request Body**:
```json
{
    "email": "usuario@example.com",
    "token": "123456",
    "newPassword": "nuevaContraseña123"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Contraseña restablecida exitosamente",
    "data": null
}
```

**Notas**:
- El código OTP expira en 5 minutos (configurable).
- Máximo 6 intentos fallidos antes de bloquear por 15 minutos.
- Al solicitar un nuevo código, el anterior se invalida y los intentos se reinician.
- El bloqueo es automático y se libera después del tiempo configurado.

---

## 9. Cambiar Contraseña (Usuario Logueado)

### POST `/api/auth/change-password`

**Descripción**: Cambia la contraseña de un usuario autenticado.

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Request Body**:
```json
{
    "currentPassword": "contraseñaActual123",
    "newPassword": "nuevaContraseña456",
    "confirmPassword": "nuevaContraseña456"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Contraseña actualizada exitosamente",
    "data": null
}
```

**Error (400)** - Contraseñas no coinciden:
```json
{
    "success": false,
    "message": "Las contraseñas no coinciden",
    "data": null
}
```

---

## 10. Registro de Usuario

### POST `/api/auth/register`

**Descripción**: Permite auto-registro como estudiante o profesor.

**Request Body (Estudiante)**:
```json
{
    "username": "nuevoUsuario",
    "email": "nuevo@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "type": "STUDENT",
    "curp": "XAXX010101HNEXXXX18",
    "enrollmentNumber": "2021001234"
}
```

**Request Body (Profesor)**:
```json
{
    "username": "nuevoProfesor",
    "email": "profesor@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "type": "TEACHER",
    "rfc": "XAXX010101XXX",
    "employeeNumber": "EMP001234"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Usuario registrado exitosamente. Puede iniciar sesión.",
    "data": null
}
```

**Nota**: El tipo `GENERAL` no es válido para auto-registro (retorna error 400).

---

## 11. Registro por Administrador

### POST `/api/auth/admin/register`

**Descripción**: Permite a un ADMIN crear usuarios directamente.

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

**Parámetros de Query**:
| Parámetro | Tipo | Requerido | Descripción |
|------------|------|-----------|-------------|
| username | string | Sí | Nombre de usuario |
| email | string | Sí | Email del usuario |
| temporaryPassword | string | Sí | Contraseña temporal |
| type | enum | Sí | STUDENT, TEACHER, GENERAL |
| identifier | string | No | CURP (estudiante) o RFC (profesor) |

**Ejemplo de Request**:
```
POST {{baseUrl}}/admin/register?username=adminuser&email=admin@edu.com&temporaryPassword=temp123456&type=GENERAL
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Usuario creado exitosamente. Debe cambiar su contraseña.",
    "data": null
}
```

---

## 12. Solicitar Setup de 2FA

### POST `/api/auth/2fa/request-setup`

**Descripción**: Genera el secreto y códigos de backup para configurar 2FA.

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Configuración 2FA iniciada",
    "data": {
        "secret": "BASE32SECRET123",
        "backupCodes": ["ABC123", "DEF456", "GHI789"],
        "qrCodeUrl": "otpauth://totp/AcademicSystem:usuario?secret=BASE32SECRET123&issuer=AcademicSystem"
    }
}
```

---

## 13. Habilitar 2FA

### POST `/api/auth/2fa/enable`

**Descripción**: Activa el 2FA después de verificar el código.

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Parámetros de Query**:
| Parámetro | Tipo | Requerido | Descripción |
|------------|------|-----------|-------------|
| code | string | Sí | Código OTP de 6 dígitos |

**Ejemplo**:
```
POST {{baseUrl}}/2fa/enable?code=123456
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "2FA habilitado exitosamente",
    "data": null
}
```

---

## 14. Deshabilitar 2FA

### POST `/api/auth/2fa/disable`

**Descripción**: Desactiva el 2FA (requiere password actual).

**Headers Requeridos**:
```
Authorization: Bearer {{accessToken}}
```

**Parámetros de Query**:
| Parámetro | Tipo | Requerido | Descripción |
|------------|------|-----------|-------------|
| password | string | Sí | Contraseña actual del usuario |

**Ejemplo**:
```
POST {{baseUrl}}/2fa/disable?password=miPassword123
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "2FA deshabilitado exitosamente",
    "data": null
}
```

---

## Colección Postman - Variables

### Environment Variables Recomendadas

```json
{
    "id": "academic-api",
    "name": "Academic API",
    "values": [
        {
            "key": "baseUrl",
            "value": "http://localhost:8080/api/auth",
            "enabled": true
        },
        {
            "key": "accessToken",
            "value": "",
            "enabled": true
        },
        {
            "key": "refreshToken",
            "value": "",
            "enabled": true
        },
        {
            "key": "tempToken",
            "value": "",
            "enabled": true
        }
    ]
}
```

### Scripts de Tests para Auto-actualizar Tokens

**Login - Post-Response Script**:
```javascript
if (pm.response.code === 200 && pm.response.json().data.accessToken) {
    pm.collectionVariables.set("accessToken", pm.response.json().data.accessToken);
    pm.collectionVariables.set("refreshToken", pm.response.json().data.refreshToken);
    
    if (pm.response.json().data.tempToken) {
        pm.collectionVariables.set("tempToken", pm.response.json().data.tempToken);
    }
}
```

**Refresh Token - Pre-Request Script**:
```javascript
// No se requiere script adicional, solo pasar el refresh token en el body
```

---

## Flujo Completo de Testing

### Flujo 1: Login Simple (sin 2FA)

1. **POST** `/login` → Guardar `accessToken`
2. **POST** `/refresh` → Renovar token
3. **POST** `/logout` → Cerrar sesión

### Flujo 2: Login con 2FA

1. **POST** `/login` → Obtener `tempToken`
2. **POST** `/verify-2fa` → Completar verificación → Guardar `accessToken`
3. **POST** `/2fa/request-setup` → Obtener secretos
4. **POST** `/2fa/enable?code=XXXXXX` → Habilitar 2FA
5. **POST** `/logout`

### Flujo 3: Recuperación de Contraseña (3 Pasos)

1. **POST** `/recovery` → Solicitar código OTP
2. **POST** `/verify-otp` → Verificar código OTP
3. **POST** `/reset-password` → Establecer nueva contraseña

**Notas del flujo**:
- Código OTP: 6 dígitos, expira en 5 minutos
- Máximo 6 intentos fallidos → bloqueo por 15 minutos
- Cada solicitud de código nuevo invalida el anterior y reinicia los intentos

### Flujo 4: Registro y Cambio de Contraseña

1. **POST** `/register` → Registrar estudiante/profesor
2. **POST** `/login` → Login con nuevas credenciales
3. **POST** `/change-password` → Cambiar contraseña
4. **POST** `/logout`

### Flujo 5: Admin - Crear Usuarios

1. **POST** `/login` → Admin hace login
2. **POST** `/admin/register` → Crear nuevo usuario
3. **POST** `/logout`

---

## Códigos de Respuesta HTTP

| Código | Descripción |
|--------|-------------|
| 200 | Éxito |
| 400 | Solicitud inválida (validación fallida) |
| 401 | No autenticado / Token inválido |
| 403 | Sin permisos (ej: rol requerido) |
| 404 | Recurso no encontrado |
| 429 | Rate limit excedido |

---

## Notas Importantes

1. **Rate Limiting**: El API usa Redis para rate limiting. Si recibe 429, espere antes de reintentar.
2. **Refresh Tokens**: Los refresh tokens tienen mayor duración. Use `/refresh` antes de que expire el access token.
3. **2FA Backup Codes**: Guarde los códigos de backup en un lugar seguro al configurar 2FA.
4. **Tokens en Headers**: Para endpoints protegidos, use: `Authorization: Bearer {{accessToken}}`

---

## Errores Comunes

### Error 401 - Token Expirado
```json
{
    "success": false,
    "message": "Token ha expirado",
    "data": null
}
```
**Solución**: Use `/refresh` con el refresh token para obtener uno nuevo.

### Error 400 - Validación
```json
{
    "success": false,
    "message": "El username es requerido",
    "data": null
}
```
**Solución**: Revise el body de la request y asegúrese de enviar todos los campos requeridos.

### Error 403 - Sin Permisos
```json
{
    "success": false,
    "message": "Acceso denegado",
    "data": null
}
```
**Solución**: El endpoint requiere rol ADMIN. Asegúrese de usar un token de administrador.

---

## 15. Registro Público (Nuevo Flujo)

### POST `/api/registration/init`

**Descripción**: Inicia el proceso de registro público. Requiere CURP válido en el sistema académico.

**Request Body**:
```json
{
    "curp": "XAXX010101HNEXXXX18",
    "email": "usuario@email.com"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Código de verificación enviado",
    "data": {
        "id": "uuid",
        "curp": "XAXX010101HNEXXXX18",
        "email": "usuario@email.com",
        "status": "PENDING",
        "requestedAt": "2026-04-19T10:00:00"
    }
}
```

**Error (400) - CURP no válido**:
```json
{
    "success": false,
    "message": "No se encontró registro académico activo con este CURP",
    "data": null
}
```

---

### POST `/api/registration/verify`

**Descripción**: Verifica el código OTP y crea la cuenta de usuario.

**Request Body**:
```json
{
    "curp": "XAXX010101HNEXXXX18",
    "otp": "123456"
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Registro completado",
    "data": {
        "id": "uuid",
        "curp": "XAXX010101HNEXXXX18",
        "email": "usuario@email.com",
        "status": "APPROVED",
        "requestedAt": "2026-04-19T10:00:00",
        "processedAt": "2026-04-19T10:05:00"
    }
}
```

**Nota**: Al verificar, se crea el usuario con password temporal. Debe verificar su email antes de hacer login.

---

### POST `/api/registration/verify-email`

**Descripción**: Verifica el email del usuario creado.

**Parámetros de Query**:
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| userId | string | UUID del usuario |
| code | string | Código OTP de 6 dígitos |

**Ejemplo**:
```
POST {{baseUrl}}/registration/verify-email?userId=uuid&code=123456
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Email verificado",
    "data": null
}
```

---

## 16. Gestión de Usuarios (Admin)

### GET `/api/admin/users`

**Descripción**: Lista todos los usuarios del sistema.

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": null,
    "data": [
        {
            "id": "uuid",
            "username": "admin",
            "email": "admin@enez.edu.mx",
            "isActive": true,
            "isVerified": true,
            "mustVerifyEmail": false,
            "roles": ["ADMIN"],
            "createdAt": "2026-04-19T10:00:00"
        }
    ]
}
```

---

### POST `/api/admin/users`

**Descripción**: Crea un usuario sin necesidad de registro académico.

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

**Request Body**:
```json
{
    "email": "nuevo@email.com",
    "curp": "XAXX010101HNEXXXX18",
    "roles": ["STUDENT", "TEACHER"]
}
```

**Request Body (sin relación académica)**:
```json
{
    "email": "soporte@enez.edu.mx",
    "roles": ["ADMIN"]
}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Usuario creado",
    "data": {
        "id": "uuid",
        "username": "nuevo",
        "email": "nuevo@email.com",
        "isActive": true,
        "isVerified": false,
        "mustVerifyEmail": true,
        "roles": ["STUDENT"],
        "createdAt": "2026-04-19T10:00:00"
    }
}
```

**Nota**: El usuario creado recibe password temporal y debe verificar su email.

---

### GET `/api/admin/users/registrations`

**Descripción**: Lista todas las solicitudes de registro.

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": null,
    "data": [
        {
            "id": "uuid",
            "curp": "XAXX010101HNEXXXX18",
            "email": "usuario@email.com",
            "status": "PENDING",
            "requestedAt": "2026-04-19T10:00:00"
        }
    ]
}
```

---

### GET `/api/admin/users/registrations/pending`

**Descripción**: Lista solo solicitudes pendientes.

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

---

### DELETE `/api/admin/users/{id}` (Soft Delete)

**Descripción**: Desactiva un usuario (soft delete).

**Headers Requeridos**:
```
Authorization: Bearer {{adminAccessToken}}
```

**Respuesta Exitosa (200)**:
```json
{
    "success": true,
    "message": "Usuario eliminado",
    "data": null
}
```

---

## 17. Flujo Completo de Registro Público

### Paso 1: Iniciar Registro
```
POST /api/registration/init
Body: { "curp": "XAXX010101HNEXXXX18", "email": "correo@ejemplo.com" }
```
→ Recibes código OTP por email

### Paso 2: Verificar Registro
```
POST /api/registration/verify
Body: { "curp": "XAXX010101HNEXXXX18", "otp": "123456" }
```
→ Se crea usuario con password temporal

### Paso 3: Verificar Email
```
POST /api/registration/verify-email?userId={uuid}&code={codigo}
```
→ Email verificado, usuario puede hacer login

### Paso 4: Login
```
POST /api/auth/login
Body: { "username": "username", "password": "password_temporal" }
```

### Paso 5: Cambiar Contraseña
```
POST /api/auth/change-password
Headers: Authorization: Bearer {accessToken}
Body: { "currentPassword": "password_temporal", "newPassword": "nuevaPass123", "confirmPassword": "nuevaPass123" }
```

---

## 18. Flujo Completo - Admin Crear Usuario

### Paso 1: Admin Login
```
POST /api/auth/login
Body: { "username": "admin", "password": "admin123" }
```
Guardar `accessToken`

### Paso 2: Crear Usuario
```
POST /api/admin/users
Headers: Authorization: Bearer {{accessToken}}
Body: { "email": "nuevo@email.com", "curp": "XAXX010101HNEXXXX18", "roles": ["STUDENT"] }
```

### Paso 3: Notificar al Usuario
El sistema envía email con username y password temporal al nuevo usuario.

### Paso 4: Usuario-Verificar Email (como arriba)

### Paso 5: Usuario-Login y Cambio de Contraseña (como arriba)
