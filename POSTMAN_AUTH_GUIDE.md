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
| POST | `/login` | Inicio de sesión | No |
| POST | `/verify-2fa` | Verificar código 2FA | No |
| POST | `/refresh` | Renovar token | No |
| POST | `/logout` | Cerrar sesión | Sí |
| POST | `/recovery` | Solicitar recuperación de contraseña | No |
| POST | `/reset-password` | Restablecer contraseña | No |
| POST | `/change-password` | Cambiar contraseña | Sí |
| POST | `/register` | Registro de usuario | No |
| POST | `/admin/register` | Registro por administrador | Sí (ADMIN) |
| POST | `/2fa/request-setup` | Solicitar setup 2FA | Sí |
| POST | `/2fa/enable` | Habilitar 2FA | Sí |
| POST | `/2fa/disable` | Deshabilitar 2FA | Sí |

---

## 1. Login

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

## 2. Verificar 2FA

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

## 3. Refrescar Token

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

## 4. Logout

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

## 5. Solicitar Recuperación de Contraseña

### POST `/api/auth/recovery`

**Descripción**: Envía un email con link para restablecer contraseña.

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
    "message": "Si el email existe, se envió un enlace de recuperación",
    "data": null
}
```

---

## 6. Restablecer Contraseña

### POST `/api/auth/reset-password`

**Descripción**: Restablece la contraseña usando el token del email.

**Request Body**:
```json
{
    "token": "token_recibido_en_email",
    "newPassword": "nuevaContraseña123"
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

---

## 7. Cambiar Contraseña (Usuario Logueado)

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

## 8. Registro de Usuario

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

## 9. Registro por Administrador

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

## 10. Solicitar Setup de 2FA

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

## 11. Habilitar 2FA

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

## 12. Deshabilitar 2FA

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

### Flujo 3: Recuperación de Contraseña

1. **POST** `/recovery` → Solicitar recuperación (ver email)
2. **POST** `/reset-password` → Restablecer con token

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
