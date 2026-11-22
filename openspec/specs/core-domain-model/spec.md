# core-domain-model Specification

## Purpose

Definir el modelo de dominio del core y los repositorios con aislamiento por clínica, de modo que los changes posteriores (auth, agenda, pacientes y pagos) puedan construirse sobre entidades estables y verificables.

## Requirements

### Requirement: Entidades del core con relaciones correctas
El dominio SHALL contener las 12 entidades definidas en la knowledge-base, con sus campos y relaciones.

#### Scenario: Todas las entidades están presentes
- **WHEN** se inspecciona el paquete `com.turnos.odontologia.domain`
- **THEN** existen las clases Clinic, User, Professional, Chair, Service, Patient, Appointment, Block, Payment, Deposit, Attachment y Odontogram con los campos de `knowledge-base/04_modelo_de_datos.md`

#### Scenario: Relaciones de un turno
- **WHEN** se construye un Appointment con Patient, Professional, Chair y Service
- **THEN** las relaciones son N:1 hacia esas entidades y el turno conserva `start_at`, `end_at` y `status`

#### Scenario: Estado inválido de turno (borde)
- **WHEN** se intenta persistir un Appointment con `status` fuera de scheduled/confirmed/cancelled/no_show/completed
- **THEN** la validación Bean Validation rechaza la entidad y no se persiste

### Requirement: Repositorios por entidad
El backend SHALL exponer repositorios Spring Data para las entidades del core.

#### Scenario: Repositorios disponibles
- **WHEN** se inspecciona `infrastructure/persistence`
- **THEN** existe un repositorio por entidad con métodos `findAllByTenantId` y `findByIdAndTenantId`
