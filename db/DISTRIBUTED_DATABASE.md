# Base de Datos Distribuida - Sistema Académico

Este documento describe la arquitectura de base de datos distribuida del Sistema Académico, incluyendo configuraciones para diferentes entornos de despliegue.

---

## Tabla de Contenidos

1. [Arquitectura General](#1-arquitectura-general)
2. [PostgreSQL (Base de Datos Principal)](#2-postgresql-base-de-datos-principal)
3. [MySQL (Portal Público)](#3-mysql-portal-público)
4. [Configuración por Entorno](#4-configuración-por-entorno)
   - [Aiven.io](#41-aivenio)
   - [Docker](#42-docker)
   - [VPS](#43-vps-o-servidor-dedicado)
5. [Consideraciones de Migración](#5-consideraciones-de-migración)
6. [ Troubleshooting](#6-troubleshooting)

---

## 1. Arquitectura General

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           SISTEMA ACADÉMICO                             │
│                                                                         │
│   ┌──────────────────────────┐      ┌──────────────────────────┐      │
│   │   PostgreSQL (Principal) │      │   MySQL (Portal Público) │      │
│   │                          │      │                          │      │
│   │  - app_user             │      │  - institution           │      │
│   │  - role, permission     │      │  - news                  │      │
│   │  - student, teacher     │      │  - event                 │      │
│   │  - course, enrollment   │      │  - portal_advertisement  │      │
│   │  - kardex, grades       │      │  - portal_contact        │      │
│   │  - attendance           │      │                          │      │
│   │  - certificate          │      │  SIN dependencias        │      │
│   │  - TODA lógica academia │      │  académicas              │      │
│   │                          │      │                          │      │
│   │  ✗ Contiene FK a tablas│      │  ✓ Tablas independientes │      │
│   │    académicas           │      │                          │      │
│   └────────────┬─────────────┘      └────────────┬─────────────┘      │
│                │                                │                     │
│                │        ┌───────────────────────┘                     │
│                │        │                                              │
│                ▼        ▼                                              │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │                        API (Java/Spring)                     │    │
│   │  ┌─────────────────────┐  ┌─────────────────────────────┐    │    │
│   │  │ PostgreSQL Datasource│  │ MySQL Datasource (Portal)  │    │    │
│   │  └─────────────────────┘  └─────────────────────────────┘    │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │                    Frontend / Cliente                          │    │
│   └──────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Por qué separar?

| Beneficio | Descripción |
|-----------|-------------|
| **Escalabilidad** | Cada base de datos puede escalar independientemente |
| **Aislamiento de fallos** | Problemas en el portal no afectan el sistema académico |
| **Costo** | MySQL es más económico para datos simples del portal |
| **Mantenimiento** | Actualizaciones independientes sin riesgo |
| **Performance** | Consultas académicas no compiten con tráfico del portal |

---

## 2. PostgreSQL (Base de Datos Principal)

### Tablas Incluidas

**Módulo de Seguridad:**
- `app_user` - Usuarios del sistema
- `role` - Roles
- `permission` - Permisos
- `user_session` - Sesiones activas
- `password_recovery` - Recuperación de contraseñas

**Módulo Académico:**
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

**Módulos Adicionales:**
- Kardex
- Report Cards (Boletas)
- Attendance (Asistencia)
- Conduct (Conducta)
- Extraordinary Exams
- Certificates
- Guardians
- Student Documents
- System Configuration
- Audit

### Ubicación de Archivos

```
db/postgresql/
├── 01_schema_academic.sql   # Schema completo
├── 02_logic.sql            # Funciones, triggers, views
├── 03_seed.sql             # Datos iniciales
└── 04_security.sql         # Columnas de seguridad
```

### Requisitos del Servidor

| Recurso | Mínimo | Recomendado |
|---------|--------|-------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4 GB | 8+ GB |
| Disk | 20 GB | 50+ GB SSD |
| PostgreSQL | 14+ | 16+ |

---

## 3. MySQL (Portal Público)

### Tablas Incluidas

- `institution` - Información de la institución
- `news` - Noticias públicas
- `event` - Eventos públicos
- `portal_advertisement` - Banners/publicidad
- `portal_contact` - Mensajes de contacto

**Nota:** Estas tablas NO tienen relaciones con las tablas académicas de PostgreSQL.

### Ubicación de Archivos

```
db/mysql/
├── 01_schema_portal.sql    # Schema del portal
└── 02_seed_portal.sql      # Datos iniciales (opcional)
```

### Requisitos del Servidor

| Recurso | Mínimo | Recomendado |
|---------|--------|-------------|
| CPU | 1 core | 2+ cores |
| RAM | 1 GB | 2+ GB |
| Disk | 5 GB | 10+ GB |
| MySQL | 8.0+ | 8.0+ |

---

## 4. Configuración por Entorno

### 4.1 Aiven.io

#### PostgreSQL (Aiven)

1. **Crear servicio en Aiven:**
   - Ir a https://console.aiven.io/
   - Seleccionar "Create a new service"
   - Elegir "PostgreSQL"
   - Seleccionar región y plan

2. **Obtener credenciales:**
   - ir a "Overview" del servicio
   - Copiar "Service URI" o usar los parámetros individuales:
     - Host, Port, Database, User, Password

3. **Configuración en application.yml:**

```yaml
spring:
  datasource:
    postgres-primary:
      jdbc-url: jdbc:postgresql://HOST:PORT/DATABASE
      username: USERNAME
      password: PASSWORD
      driver-class-name: org.postgresql.Driver
```

4. **Extensiones necesarias:**

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

5. **Configuración de red:**
   - En Aiven: Service Settings → Network
   - Agregar IP/API key permitted network
   - Usar "Aiven IP" si es otro servicio Aiven

#### MySQL (Aiven)

1. **Crear servicio en Aiven:**
   - Seleccionar "MySQL" en lugar de PostgreSQL
   - Elegir plan según necesidades

2. **Configuración en application.yml:**

```yaml
spring:
  datasource:
    mysql-portal:
      jdbc-url: jdbc:mysql://HOST:PORT/DATABASE?useSSL=false&allowPublicKeyRetrieval=true
      username: USERNAME
      password: PASSWORD
      driver-class-name: com.mysql.cj.jdbc.Driver
```

#### Ventajas de Aiven

| Característica | Beneficio |
|----------------|-----------|
| **Managed** | Sin administración de servidores |
| **Backups** | Backups automáticos incluidos |
| **Alta disponibilidad** | Opciones de replica automática |
| **Escalabilidad** | Escalado con un click |
| **Monitoreo** | Métricas integradas |

---

### 4.2 Docker

#### docker-compose.yml

```yaml
version: '3.8'

services:
  # PostgreSQL - Base de datos principal
  postgres-academic:
    image: postgres:16
    container_name: postgres-academic
    environment:
      POSTGRES_DB: academic_system
      POSTGRES_USER: academic_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./db/postgresql:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U academic_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  # MySQL - Portal Público
  mysql-portal:
    image: mysql:8.0
    container_name: mysql-portal
    environment:
      MYSQL_DATABASE: portal_public
      MYSQL_USER: portal_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./db/mysql:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres-data:
  mysql-data:
```

#### Configuración application.yml (Docker)

```yaml
spring:
  datasource:
    postgres-primary:
      jdbc-url: jdbc:postgresql://localhost:5432/academic_system
      username: academic_user
      password: ${POSTGRES_PASSWORD}
    mysql-portal:
      jdbc-url: jdbc:mysql://localhost:3306/portal_public?useSSL=false
      username: portal_user
      password: ${MYSQL_PASSWORD}
```

#### Iniciar servicios

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

#### Consideraciones Docker

| Aspecto | Recomendación |
|---------|---------------|
| **Persistencia** | Usar volúmenes nombrados para datos |
| **Backups** | Crear script de backup externo |
| **Producción** | No usar en producción sin configuraciones adicionales |
| **Recursos** | Limitar memoria CPU en docker-compose |

---

### 4.3 VPS (Servidor Dedicado)

#### Arquitectura con 2 VPS

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              VPS 1 (Principal)                          │
│                                                                         │
│   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐  │
│   │   PostgreSQL    │  │      API       │  │       Nginx            │  │
│   │   (Puerto 5432)│  │  (Puerto 8080) │  │     (Puerto 80/443)    │  │
│   └─────────────────┘  └─────────────────┘  └─────────────────────────┘  │
│                                                                         │
│   CPU: 4 cores, RAM: 8GB, Disk: 100GB SSD                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ (Permitir conexión en firewall)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              VPS 2 (Portal)                             │
│                                                                         │
│   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐  │
│   │     MySQL       │  │   API Portal    │  │       Nginx            │  │
│   │   (Puerto 3306) │  │  (Puerto 8081)  │  │     (Puerto 80/443)    │  │
│   └─────────────────┘  └─────────────────┘  └─────────────────────────┘  │
│                                                                         │
│   CPU: 2 cores, RAM: 4GB, Disk: 50GB SSD                               │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Instalación en VPS (Ubuntu 22.04)

**VPS 1 - PostgreSQL + API Principal:**

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar PostgreSQL
sudo apt install -y postgresql-16

# Configurar PostgreSQL
sudo nano /etc/postgresql/16/main/postgresql.conf
# Cambiar: listen_addresses = '*'

# Crear usuario y base de datos
sudo -u postgres psql
CREATE USER academic_user WITH PASSWORD 'tu_password_seguro';
CREATE DATABASE academic_system OWNER academic_user;
GRANT ALL PRIVILEGES ON DATABASE academic_system TO academic_user;

# Habilitar extensión UUID
\c academic_system
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

# Configurar firewall
sudo ufw allow 5432/tcp
sudo ufw enable

# Importar schema
sudo -u postgres psql -d academic_system -f /ruta/a/01_schema_academic.sql
```

**VPS 2 - MySQL + Portal:**

```bash
# Instalar MySQL
sudo apt install -y mysql-server

# Configurar MySQL
sudo mysql_secure_installation
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
# bind-address = 0.0.0.0

# Crear usuario y base de datos
sudo mysql
CREATE DATABASE portal_public;
CREATE USER 'portal_user'@'%' IDENTIFIED BY 'tu_password_seguro';
GRANT ALL PRIVILEGES ON portal_public.* TO 'portal_user'@'%';
FLUSH PRIVILEGES;

# Importar schema
mysql -u portal_user -p portal_public < /ruta/a/01_schema_portal.sql

# Configurar firewall
sudo ufw allow 3306/tcp
sudo ufw allow 8081/tcp
```

#### Configuración de Red entre VPS

```bash
# En VPS 1 (PostgreSQL)
sudo ufw allow from IPS_VPS2 to any port 5432

# En VPS 2 (MySQL)
sudo ufw allow from IPS_VPS1 to any port 3306
```

#### application.yml (VPS)

```yaml
spring:
  datasource:
    postgres-primary:
      jdbc-url: jdbc:postgresql://VPS1_IP:5432/academic_system
      username: academic_user
      password: ${POSTGRES_PASSWORD}
    mysql-portal:
      jdbc-url: jdbc:mysql://VPS2_IP:3306/portal_public?useSSL=false
      username: portal_user
      password: ${MYSQL_PASSWORD}
```

#### Scripts de Backup (VPS)

```bash
#!/bin/bash
# backup-postgres.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/postgres"
mkdir -p $BACKUP_DIR

pg_dump -U academic_user -h localhost academic_system > $BACKUP_DIR/backup_$DATE.sql

# Eliminar backups mayores a 7 días
find $BACKUP_DIR -type f -mtime +7 -delete

echo "Backup completado: $DATE"
```

```bash
#!/bin/bash
# backup-mysql.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/mysql"
mkdir -p $BACKUP_DIR

mysqldump -u portal_user -p tu_password portal_public > $BACKUP_DIR/backup_$DATE.sql

# Eliminar backups mayores a 7 días
find $BACKUP_DIR -type f -mtime +7 -delete

echo "Backup completado: $DATE"
```

#### Programar Backups con Cron

```bash
# Editar crontab
crontab -e

# Agregar líneas:
0 2 * * * /ruta/backup-postgres.sh  # Diario a las 2 AM
0 3 * * * /ruta/backup-mysql.sh      # Diario a las 3 AM
```

---

## 5. Consideraciones de Migración

### Migrar de PostgreSQL único a distribuido

1. **Exportar tablas del portal:**
```sql
-- En PostgreSQL original
CREATE DATABASE portal_export;
\c portal_export

-- Exportar solo tablas del portal
CREATE TABLE institution AS SELECT * FROM institution;
CREATE TABLE news AS SELECT * FROM news;
-- ... demás tablas
```

2. **Importar a MySQL:**
```bash
mysql -u portal_user -p portal_public < institution_data.sql
```

3. **Actualizar API:**
   - Agregar segundo datasource
   - Actualizar entities para usar la nueva conexión

### Migrar entre proveedores

| De → A | Proceso |
|--------|---------|
| Docker → Aiven | Exportar datos, importar en Aiven, cambiar connection string |
| Aiven → VPS | Dump/Restore, instalar PostgreSQL en VPS |
| VPS → Aiven | Same que anterior, inverso |

---

## 6. Troubleshooting

### Problemas comunes PostgreSQL

| Error | Solución |
|-------|----------|
| `connection refused` | Verificar firewall, puerto 5432 |
| `UUID not found` | Ejecutar `CREATE EXTENSION "uuid-ossp"` |
| `role does not exist` | Crear usuario y dar permisos |

### Problemas comunes MySQL

| Error | Solución |
|-------|----------|
| `access denied` | Verificar usuario y contraseña |
| `can't connect` | Verificar `bind-address` |
| `table not found` | Ejecutar schema completo |

### Problemas de Conexión API

| Error | Solución |
|-------|----------|
| `No suitable driver` | Agregar dependencia MySQL driver |
| `Too many connections` | Revisar pool de conexiones |
| `Connection timeout` | Verificar red entre servicios |

---

## Referencias

- [Documentación PostgreSQL](https://www.postgresql.org/docs/)
- [Documentación MySQL](https://dev.mysql.com/doc/)
- [Aiven Console](https://console.aiven.io/)
- [Spring Boot Data Sources](https://spring.io/projects/spring-boot)
