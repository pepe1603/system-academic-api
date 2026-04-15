# Portal Público - Módulo Portal

## Descripción
Desarrollo del módulo del portal público académico que incluye:
- Información institucional
- Noticias del portal
- Eventos públicos
- Banners/publicidad
- Formulario de contacto

## Tecnologías
- Java 17
- Spring Boot 3.5.11
- MySQL 8.0 (Aiven)
- Hibernate 6.x

## Estado de MySQL (Portal)

### Schema (01_schema_portal.sql)
- ✅ institution
- ✅ news
- ✅ event
- ✅ portal_advertisement
- ✅ portal_contact

### Seed (02_seed_portal.sql)
- ✅ Datos institucionales
- ✅ Noticias iniciales
- ✅ Eventos iniciales
- ✅ Anuncios/banners

### Vistas/Functions
- ❌ No existen en MySQL (no es necesario para este módulo)

---

## Plan de Acción

### Fase 1: Estructura Base (Local: fase-1) ✅ COMPLETADO
- [x] 1.1 Verificar entities y repositories existentes
- [x] 1.2 Crear DTOs para el portal (5 DTOs creados)
- [x] 1.3 Crear Services del portal (PortalService)
- [x] 1.4 Crear Controllers del portal (PortalController)
- [x] 1.5 Configurar seguridad para endpoints públicos
- [x] 1.6 Corregir errores de conexión Aiven (límite pool)
- [x] 1.7 Corregir columna `values` reservada en MySQL

### Fase 2: Endpoints Públicos (sin auth) (Local: fase-2) ✅ COMPLETADO
- [x] 2.1 GET /api/portal/institution - Info de institución
- [x] 2.2 GET /api/portal/news - Listar noticias
- [x] 2.3 GET /api/portal/news/paged - Noticias paginadas
- [x] 2.4 GET /api/portal/events - Listar eventos
- [x] 2.5 GET /api/portal/events/paged - Eventos paginados
- [x] 2.6 GET /api/portal/ads - Listar anuncios/banners
- [x] 2.7 POST /api/portal/contact - Enviar mensaje de contacto

### Fase 3: Endpoints Privados (con auth - admin) (Local: fase-3)
- [ ] 3.1 CRUD News (crear, editar, eliminar noticias)
- [ ] 3.2 CRUD Events (crear, editar, eliminar eventos)
- [ ] 3.3 CRUD Advertisements (gestionar banners)
- [ ] 3.4 CRUD Institution (editar info institucional)
- [ ] 3.5 Ver/leer mensajes de contacto
- [ ] 3.6 Responder mensajes de contacto

---

## Recomendaciones de Implementación

### Seguridad
- Endpoints de lectura (GET) → públicos (`permitAll`)
- Endpoints de escritura (POST/PUT/DELETE) → autenticados (`authenticated`)
- Solo rol ADMIN puede gestionar contenido

### Base de Datos
- Usar `ddl-auto: validate` para MySQL (no modify schema)
- Las entities ya mapean correctamente las tablas

### Rendimiento
- Implementar paginación para news y events
- Agregar índices si es necesario

### Estructura de Paquetes
```
controller/portal/
  └── PortalController.java
service/portal/
  └── PortalService.java
dto/portal/
  ├── InstitutionDTO.java
  ├── NewsDTO.java
  ├── EventDTO.java
  ├── AdvertisementDTO.java
  └── ContactDTO.java
```

---

## Dependencias Actuales
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- MySQL Connector
- Lombok

---

## Notas
- El portal público lee de MySQL (Aiven)
- El sistema académico usa PostgreSQL (Aiven)
- Ambos están configurados en la API con dual datasource