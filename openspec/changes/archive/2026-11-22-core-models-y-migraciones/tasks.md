# Tasks — C-02 core-models-y-migraciones

> Orden de ejecución. Cada tarea se marca `[x]` al completarla.

## Preparación

- [x] 1. Leer `CHANGES.md` §C-02, `knowledge-base/04_modelo_de_datos.md` y `knowledge-base/08_arquitectura_propuesta.md`.

## Configuración y dependencias

- [x] 2. Agregar `org.flywaydb:flyway-core` a `backend/pom.xml`.
- [x] 3. Configurar Flyway en `application.yml`: `baseline-on-migrate: true`, `baseline-version: 0`.
- [x] 4. Cambiar `spring.jpa.hibernate.ddl-auto` a `validate` en `dev` y `test` (Flyway es el dueño del esquema).

## Dominio

- [x] 5. Crear `BaseEntity` con `id`, `tenantId`, `createdAt` y `updatedAt`.
- [x] 6. Crear entidades de identidad: `Clinic`, `User`.
- [x] 7. Crear entidades de agenda: `Professional`, `Chair`, `Service`, `Appointment`, `Block`.
- [x] 8. Crear entidades de pacientes: `Patient`, `Odontogram`, `Attachment`.
- [x] 9. Crear entidades de administración: `Payment`, `Deposit`.

## Migraciones y seed

- [x] 10. Crear `V001__base.sql`: tablas Clinic/User + seed de roles (datos ficticios).
- [x] 11. Crear `V002__agenda.sql`: tablas Professional/Chair/Service/Appointment/Block + servicios base y política de cancelación 24 h.
- [x] 12. Crear `V003__pacientes.sql`: tablas Patient/Odontogram/Attachment.
- [x] 13. Crear `V004__pagos.sql`: tablas Payment/Deposit.
- [x] 14. Verificar que las 12 tablas tengan `tenant_id NOT NULL`.

## Persistencia

- [x] 15. Crear repositorios por entidad con `findAllByTenantId` y `findByIdAndTenantId`.

## Tests y verificación

- [x] 16. Test: migraciones aplican en orden sobre H2 vacía.
- [x] 17. Test: repositorios filtran por tenant y bloquean acceso cruzado.
- [x] 18. Test: seed carga roles, servicios y política de cancelación; datos ficticios.
- [x] 19. Correr `mvn test` y dejar todo en verde (Verify).

## Cierre

- [x] 20. Actualizar `CHANGES.md`: C-02 → `[x]` completado.
- [x] 21. Ejecutar `/opsx:archive` para mover el change a `openspec/changes/archive/`.
