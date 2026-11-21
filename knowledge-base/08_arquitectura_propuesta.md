# Arquitectura Propuesta

## Stack tecnológico

- **Backend**: Java 17+ / Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security, Spring Validation).
- **Base de datos**: H2 (embebida, modo archivo para persistencia entre reinicios; consola H2 habilitada en dev).
- **Frontend**: React + Vite (o Thymeleaf si se prefiere server-side; recomendado React para PWA).
- **Autenticación**: JWT (jjwt) + refresh tokens.
- **Build**: Maven o Gradle.
- **Testing**: JUnit 5 + Mockito + Spring Boot Test.

## Patrones aplicados

| Patrón | Dónde | Por qué |
|---|---|---|
| Multi-tenant (tenant_id) | Todas las tablas | Aislamiento por clínica |
| RBAC | Endpoints (@PreAuthorize) | Control de acceso por rol |
| Adapter pattern | WhatsApp, MP, Email | Desacoplar integraciones |
| Repository pattern | Spring Data JPA | Testabilidad |
| DTO/MapStruct | API layer | Separar dominio de transporte |
| PWA | Frontend | Mobile-first, sin app nativa |

## Estructura de directorios

```
turnos-odontologia/
├── backend/
│   └── src/main/java/com/turnos/odontologia/
│       ├── domain/        (entidades JPA, value objects)
│       ├── application/   (servicios de caso de uso)
│       ├── infrastructure/(repositorios, adapters, config)
│       └── api/           (controllers, DTOs, mappers)
├── frontend/
│   └── src/
│       ├── features/
│       ├── shared/
│       └── pages/
├── knowledge-base/
└── docs/
```

## Seguridad

- Autenticación: JWT (jjwt) + refresh tokens.
- Autorización: RBAC por rol y tenant (@PreAuthorize).
- Validación de input: Bean Validation (@Valid, @NotNull, etc.) en cada DTO.
- Secrets management: application.yml con variables de entorno, nunca en código.

## Variables de entorno / configuración (application.yml)

| Variable | Descripción | Ejemplo | Sensible |
|---|---|---|---|
| spring.datasource.url | Conexión H2 (archivo) | jdbc:h2:file:./data/turnosdb | N |
| JWT_SECRET | Firma de tokens | (random) | Y |
| WHATSAPP_TOKEN | Token Meta | ... | Y |
| WHATSAPP_PHONE_ID | ID del número | ... | N |
| MP_ACCESS_TOKEN | Mercado Pago | ... | Y |
| SMTP_HOST/USER/PASS | Email | ... | Y |
| FRONTEND_URL | URL del frontend | https://... | N |

## Notas sobre H2

- Modo archivo (`jdbc:h2:file:./data/turnosdb`) para persistencia entre reinicios.
- Consola H2 habilitada en dev (`/h2-console`).
- En producción se puede migrar a PostgreSQL sin cambios en el código (solo cambiar datasource URL y driver).
- JPA/Hibernate genera el esquema automáticamente en dev (`ddl-auto=update`); en prod usar migraciones (Flyway).