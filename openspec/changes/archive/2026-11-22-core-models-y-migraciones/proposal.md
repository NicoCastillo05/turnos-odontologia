# Proposal — C-02 core-models-y-migraciones

## Why

El change C-01 dejó un esqueleto ejecutable (backend Maven/Spring Boot 3 + H2, frontend Vite) pero **sin entidades de dominio, sin esquema de base de datos y sin migraciones**. Los changes que vienen después (C-03 auth, C-04 agenda, C-08 pacientes, C-10 caja) dependen de C-02 y no pueden empezar hasta que existan los modelos y las tablas.

Además, `CHANGES.md` marca este change como **Governance: CRITICO**: un error en los modelos o en el aislamiento por tenant se propaga a todo el proyecto. Por eso se implementa primero, apenas terminado C-01.

## What Changes

- Crear las **12 entidades JPA** en `backend/src/main/java/com/turnos/odontologia/domain/`: Clinic, User, Professional, Chair, Service, Patient, Appointment, Block, Payment, Deposit, Attachment y Odontogram, con los campos definidos en `knowledge-base/04_modelo_de_datos.md`.
- Crear una clase base con `id`, `tenant_id`, `created_at` y `updated_at` para que **todas** las tablas queden aisladas por clínica (multi-tenant).
- Crear repositorios Spring Data JPA por entidad, con métodos que filtran por `tenant_id` (`findAllByTenantId`, `findByIdAndTenantId`) para que C-03 solo tenga que inyectar el tenant del token.
- Agregar **Flyway** (`flyway-core`) y migraciones numeradas `V001__base.sql`, `V002__agenda.sql`, `V003__pacientes.sql`, `V004__pagos.sql`.
- Cargar **seed data 100% ficticio**: roles por defecto, servicios base con duraciones y política de cancelación de 24 h.
- Ajustar la configuración: Flyway pasa a ser el dueño del esquema y Hibernate queda en modo `validate` (se mantiene `baseline-on-migrate` para el archivo H2 ya creado por C-01).
- Agregar tests automatizados con JUnit 5 sobre H2 en memoria.

## Capabilities

### New Capabilities
- `core-domain-model`: modelo de dominio del core con las 12 entidades, sus relaciones y el campo `tenant_id` en todas ellas.
- `tenant-migrations`: esquema versionado con migraciones numeradas, repositorios con filtro por tenant y datos iniciales ficticios.

### Modified Capabilities
- `application-bootstrap`: el arranque ya no genera el esquema con `ddl-auto=update`; ahora aplica migraciones Flyway y valida que el esquema coincida con las entidades.

## Impact

- Nuevos archivos bajo `backend/src/main/java/com/turnos/odontologia/domain/` e `infrastructure/persistence/`.
- Nuevos scripts bajo `backend/src/main/resources/db/migration/`.
- Modificados: `backend/pom.xml`, `application.yml`, `application-dev.yml`, `application-test.yml` y `CHANGES.md` (estado del item al archivar).
- **Sin datos personales reales** (regla 9 de `AGENTS.md`). Todos los fixtures son ficticios.
- No se crean endpoints, lógica de negocio de agenda ni pantallas: eso corresponde a C-03 en adelante.
