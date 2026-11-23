# Evidencia académica — C-04 agenda-backend

## Identificación

- Change: `C-04 agenda-backend`.
- Rama de trabajo: `feature/C-04-agenda-backend`.
- Responsable delegado: Pedro.
- Governance: ALTO.
- Archivo OpenSpec: `openspec/changes/archive/2026-11-23-agenda-backend/`.
- Fecha de Archive: 2026-11-23.

## Ciclo OPSX realizado

1. **Explore**: relevamiento guardado en `docs/explore/C-04-agenda-backend.md`.
2. **Propose**: proposal, dos specs, design y 18 tareas aprobadas.
3. **Apply**: implementación mediante TDD estricto con ciclos RED, GREEN y triangulación.
4. **Verify**: revisión independiente de implementación, especificaciones, migración,
   seguridad multi-tenant, documentación y cobertura.
5. **Archive**: cambio archivado por el CLI de OpenSpec y requisitos incorporados a las specs
   principales `agenda-resource-management` y `appointment-scheduling`.

## Alcance entregado

- CRUD REST de profesionales, sillones, prestaciones, bloqueos y turnos mediante DTOs.
- Cálculo automático de duración y estados tipados de turno.
- Horarios semanales normalizados y validados.
- Detección de solapamientos con intervalos semiabiertos y turnos cancelados excluidos.
- Bloqueos por profesional, sillón o ambos.
- Sobreturnos explícitos con registro del usuario autorizante.
- Autorización por rol y propiedad profesional.
- Aislamiento de lecturas, escrituras, referencias y conflictos por `tenant_id`.
- Desactivación de recursos referenciados y borrado físico de recursos sin referencias.
- Errores HTTP estables sin datos personales o clínicos.
- Migración Flyway `V005__agenda_backend.sql` con auditoría e índices de disponibilidad.

## Evidencia de validación

### Suite final

```text
Command: mvn -f backend/pom.xml test
Result: BUILD SUCCESS
Tests: 52
Failures: 0
Errors: 0
Skipped: 0
```

Captura independiente:

- Run: `244b6fc876e6b653fe0b52c2447b9437`.
- SHA-256: `1356820a4de08d91df143ec7967ef757bcf35f7a71e5ea29d11ffc76da04f03e`.

### OpenSpec

```text
Command: openspec validate agenda-backend --strict
Result: Change 'agenda-backend' is valid
```

Tras Archive:

```text
Command: openspec validate --all --strict
Result: 6 specs passed, 0 failed
```

### Verify independiente

- Veredicto final: **PASS**.
- Hallazgos pendientes: ninguno.
- Fingerprint revisado:
  `sha256:0f48d6994353b80cfbe7ba14d9acb60d8929cad467b781281b2035f44535eeb0`.
- Se verificaron además los archivos tracked y untracked de texto sin problemas de whitespace.

## Resultado

C-04 completó el ciclo requerido `explore → propose → apply → verify → archive`. El Archive
actualizó las specs principales con 12 requisitos y dejó cero changes activos. El commit y el
push no forman parte de esta evidencia y requieren una acción posterior explícita del responsable.
