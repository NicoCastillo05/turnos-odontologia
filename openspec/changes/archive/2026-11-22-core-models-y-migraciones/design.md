# Design — C-02 core-models-y-migraciones

## Context

C-01 entregó un proyecto ejecutable con Spring Boot 3.3.5 / Java 17, H2 en modo archivo y perfiles `dev` / `test`, pero sin entidades ni esquema. La arquitectura objetivo está en `knowledge-base/08_arquitectura_propuesta.md`: capas `domain` / `application` / `infrastructure` / `api`, multi-tenant por `tenant_id`, Repository pattern con Spring Data JPA y migraciones numeradas. Las reglas duras de `AGENTS.md` exigen `tenant_id` en toda consulta (6), migración numerada para cada cambio de esquema (7), datos ficticios (9), sin secretos en código (3), sin `System.out.println` (4) y separar entidades de DTOs (5).

Ver `proposal.md` para la motivación.

## Goals / Non-Goals

**Goals:**
- Definir las 12 entidades del core con los campos y relaciones de la KB.
- Versionar el esquema con migraciones Flyway numeradas y repetibles en entornos limpios.
- Garantizar el aislamiento por clínica desde la propia estructura de datos.
- Dejar seed data ficticio y tests que verifiquen migraciones, tenant y seed.

**Non-Goals:**
- No implementar autenticación, JWT ni RBAC (C-03).
- No implementar endpoints, anti-solapamiento ni reglas de agenda (C-04).
- No crear pantallas ni componentes frontend (C-05, C-07).
- No almacenar datos reales de pacientes ni escribir secretos en el repositorio.

## Decisions

- **Flyway como dueño del esquema.** Se agrega `org.flywaydb:flyway-core` (versión gestionada por Spring Boot 3.3.5). Hibernate pasa a `ddl-auto=validate` en `dev` y `test`, de modo que cualquier diferencia entre entidades y migraciones falle el arranque. Alternativa descartada: seguir con `update`, porque genera esquema implícito sin trazabilidad y rompe la regla 7.
- **`baseline-on-migrate: true` y `baseline-version: 0`.** C-01 ya creó `./data/turnosdb` con `ddl-auto=update`. Sin baseline, Flyway rechazaría una base no vacía sin tabla de historial. Alternativa descartada: borrar el archivo H2 (perdería datos y rompería la spec de persistencia de C-01).
- **Clase base `BaseEntity`.** Con `id` (Long, generado), `tenantId` (Long, calculado desde el contexto en C-03), `createdAt` y `updatedAt`. La columna física se llama `tenant_id` y equivale a `Clinic.id`. Alternativa descartada: repetir el campo en cada entidad (duplicación y riesgo de olvidarlo).
- **Nomenclatura de migraciones.** `V001__base.sql` (Clinic, User + roles), `V002__agenda.sql` (Professional, Chair, Service, Appointment, Block + servicios base y política de cancelación), `V003__pacientes.sql` (Patient, Odontogram, Attachment), `V004__pagos.sql` (Payment, Deposit). Cumple los "001 a 004" de `CHANGES.md` con el prefijo `V` que exige Flyway.
- **Seed dentro de las migraciones.** Se inserta con `INSERT ... WHERE NOT EXISTS` para que sea idempotente y no duplique al reiniciar. Datos claramente ficticios ("Clínica Demo", "Paciente de prueba", DNI de ejemplo). Regla 9.
- **Repositorios con filtro explícito.** Cada repositorio extiende `JpaRepository` y expone `findAllByTenantId(Long tenantId)` y `findByIdAndTenantId(Long id, Long tenantId)`. Así C-03 solo necesita pasar el tenant del JWT sin reescribir consultas. Regla 6.
- **Sin DTOs ni controllers.** Las entidades no se exponen en API (regla 5); este change solo prepara la capa de dominio y persistencia.
- **H2 se mantiene como base de datos.** Según `knowledge-base/08_arquitectura_propuesta.md` y lo ya implementado en C-01 (`application.yml` con `jdbc:h2:file:./data/turnosdb`), H2 en modo archivo es la BD definitiva del MVP. No se evalúa migrar a PostgreSQL en este change (la KB lo deja para una etapa futura de producción).

## Risks / Trade-offs

- [Cambiar `ddl-auto` a `validate` puede fallar si el archivo H2 tiene un esquema viejo generado por `update`] → `baseline-on-migrate: true` congela el esquema actual como versión 0; las migraciones posteriores sí se validan. Si hay incompatibilidad, se documenta y se ajusta la migración, nunca se desactiva `validate`.
- [Los ids numéricos pueden colisionar entre clínicas] → se usa `tenant_id` en todas las claves y consultas; el id es local a la tabla pero siempre se accede filtrando por tenant.
- [El seed se ejecuta en todos los entornos] → se acepta porque son datos ficticios; en producción futura se puede mover a un perfil dedicado sin cambiar el esquema.

## Migration Plan

No hay datos reales: la migración es puramente aditiva. `baseline-on-migrate` permite arrancar sobre el H2 ya existente. Para revertir, se eliminan los scripts `V00*` y las entidades; `application-bootstrap` vuelve a `ddl-auto=update`.


