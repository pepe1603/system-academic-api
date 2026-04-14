# MySQL - Portal Público

Este directorio contiene el schema y scripts para la base de datos MySQL, utilizada exclusivamente para el Portal Público del Sistema Académico.

---

## Contenido

| Archivo | Descripción |
|---------|-------------|
| `01_schema_portal.sql` | Schema completo del portal público |
| `02_seed_portal.sql` | Datos iniciales (opcional) |

---

## Requisitos

- MySQL 8.0+

---

## Instalación

### 1. Crear base de datos

```sql
CREATE DATABASE portal_public;
```

### 2. Ejecutar scripts

```bash
mysql -u root -p portal_public < 01_schema_portal.sql
```

O si tienes datos seed:

```bash
mysql -u root -p portal_public < 01_schema_portal.sql
mysql -u root -p portal_public < 02_seed_portal.sql
```

---

## Tablas Incluidas

| Tabla | Descripción |
|-------|-------------|
| `institution` | Información de la institución |
| `news` | Noticias públicas |
| `event` | Eventos públicos |
| `portal_advertisement` | Banners y publicidad |
| `portal_contact` | Mensajes de contacto |

**Nota:** Estas tablas NO tienen relaciones con las tablas académicas de PostgreSQL.

---

## Diferencias con PostgreSQL

| Aspecto | PostgreSQL | MySQL |
|---------|------------|-------|
| UUID | `UUID` tipo nativo | `CHAR(36)` o `BINARY(16)` |
| Boolean | `BOOLEAN` | `BOOLEAN` (1 byte) |
| Timestamps | `TIMESTAMPTZ` | `TIMESTAMP` |
| JSON | `JSONB` | `JSON` |
| Auto increment | `GENERATED ALWAYS` | `AUTO_INCREMENT` |

---

## Conexión desde API (Spring Boot)

```yaml
spring:
  datasource:
    mysql-portal:
      jdbc-url: jdbc:mysql://host:3306/portal_public?useSSL=false&allowPublicKeyRetrieval=true
      username: portal_user
      password: ${MYSQL_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## Consideraciones

- **Sin dependencias académicas**: Estas tablas no référencian a PostgreSQL
- **Datos públicos**: Información visible sin autenticación
- **Rendimiento**: Índices optimizados para consultas read-heavy

---

## Recursos

- [Documentación MySQL](https://dev.mysql.com/doc/)
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)
