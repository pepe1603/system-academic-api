# PostgreSQL - Sistema Académico

Este directorio contiene el schema y scripts para la base de datos PostgreSQL, que es la base de datos principal del Sistema Académico.

---

## Contenido

| Archivo | Descripción |
|---------|-------------|
| `01_schema_academic.sql` | Schema completo con todas las tablas académicas |
| `02_logic.sql` | Funciones, triggers y views |
| `03_seed.sql` | Datos iniciales (seed) |
| `04_security.sql` | Configuraciones adicionales de seguridad |

---

## Requisitos

- PostgreSQL 14+ (recomendado 16+)
- Extensión `uuid-ossp` habilitada

---

## Instalación

### 1. Crear base de datos

```sql
CREATE DATABASE academic_system;
```

### 2. Conectar y ejecutar scripts

```bash
# Conectar a la base de datos
psql -U postgres -d academic_system -f 01_schema_academic.sql
psql -U postgres -d academic_system -f 02_logic.sql
psql -U postgres -d academic_system -f 03_seed.sql
psql -U postgres -d academic_system -f 04_security.sql
```

O ejecutar todos los scripts en orden:

```bash
psql -U postgres -d academic_system -f 01_schema_academic.sql && \
psql -U postgres -d academic_system -f 02_logic.sql && \
psql -U postgres -d academic_system -f 03_seed.sql && \
psql -U postgres -d academic_system -f 04_security.sql
```

---

## Tablas Incluidas

### Módulo de Seguridad
- `app_user` - Usuarios
- `role` - Roles
- `permission` - Permisos
- `user_session` - Sesiones
- `password_recovery` - Recuperación de contraseñas
- `user_role` - Relación usuario-rol
- `role_permission` - Relación rol-permiso

### Módulo Académico
- `generation` - Generaciones
- `study_plan` - Planes de estudio
- `semester` - Semestres del plan
- `academic_semester` - Semestres académicos
- `academic_period` - Períodos académicos
- `course` - Cursos/Materias
- `academic_group` - Grupos académicos
- `student` - Estudiantes
- `teacher` - Profesores
- `enrollment` - Inscripciones
- `evaluation_type` - Tipos de evaluación
- `grade` - Calificaciones

### Kardex y Reportas
- `kardex` - Kardex académico
- `report_card` - Boletas de calificaciones
- `report_card_detail` - Detalle de boletas

### Asistencia y Conducta
- `attendance` - Asistencia diaria
- `attendance_period` - Resumen de asistencia
- `conduct` - Conducta
- `conduct_incident` - Incidentes de conducta

### Otros Módulos
- `extraordinary_exam` - Exámenes extraordinarios
- `retake_exam` - Exámenes de repetición
- `certificate` - Certificados
- `guardian` - Tutores
- `student_document` - Documentos de estudiantes
- `system_configuration` - Configuración del sistema
- `access_audit` - Auditoría de accesos

---

## Configuración de Producción

### Conexión desde API (Spring Boot)

```yaml
spring:
  datasource:
    postgres-primary:
      jdbc-url: jdbc:postgresql://host:port/academic_system
      username: academic_user
      password: ${DB_PASSWORD}
      driver-class-name: org.postgresql.Driver
```

### Parámetros Recomendados postgresql.conf

```ini
# Memoria
shared_buffers = 256MB
work_mem = 16MB
maintenance_work_mem = 128MB

# Conexiones
max_connections = 100

# WAL
wal_level = replica
max_wal_size = 1GB
min_wal_size = 80MB

# Logging
log_destination = 'stderr'
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
```

---

## Consideraciones

- **UUID**: Usa UUIDs como IDs primarios
- **Soft Delete**: Tablas usan `is_deleted` para eliminación lógica
- **Auditoría**: Campos `created_at`, `updated_at`, `created_by`, `updated_by`
- **Constraints**: Validaciones a nivel de base de datos

---

## Recursos

- [Documentación PostgreSQL](https://www.postgresql.org/docs/)
- [Tipos UUID](https://www.postgresql.org/docs/current/datatype-uuid.html)
