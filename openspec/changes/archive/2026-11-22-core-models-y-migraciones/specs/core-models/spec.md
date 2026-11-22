# core-models Specification

## Purpose

Definir el modelo de dominio del core y su esquema versionado, de modo que los changes posteriores (auth, agenda, pacientes y pagos) puedan construirse sobre entidades estables, aisladas por clínica y verificables mediante migraciones.

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

### Requirement: Esquema versionado con Flyway
El backend SHALL crear y versionar el esquema mediante migraciones numeradas, sin generarlo implícitamente.

#### Scenario: Arranque sobre base vacía
- **WHEN** se inicia el backend con una base H2 vacía
- **THEN** Flyway aplica V001, V002, V003 y V004 en orden y crea las tablas del core

#### Scenario: Reinicio sin cambios de esquema
- **WHEN** se reinicia el backend sin modificar las migraciones
- **THEN** no se vuelven a aplicar y los datos persistidos se conservan

#### Scenario: Migración inconsistente (fallo)
- **WHEN** una entidad no coincide con el esquema migrado
- **THEN** Hibernate falla el arranque en modo `validate` con un error que identifica la diferencia

### Requirement: Aislamiento multi-tenant
Todas las tablas SHALL incluir `tenant_id` y todo acceso SHALL poder filtrarse por ese campo.

#### Scenario: Consulta de una clínica
- **WHEN** se consultan turnos con `tenant_id` de la clínica A
- **THEN** se devuelven únicamente los turnos de la clínica A

#### Scenario: Acceso cruzado (error)
- **WHEN** se intenta obtener un registro pasando un `tenant_id` distinto al suyo
- **THEN** el repositorio no devuelve el registro

#### Scenario: Columna presente en todas las tablas (borde)
- **WHEN** se inspecciona el esquema generado por las migraciones
- **THEN** las 12 tablas del core contienen la columna `tenant_id NOT NULL`

### Requirement: Seed data ficticio
Las migraciones SHALL cargar datos iniciales ficticios y SHALL NOT incluir datos personales reales.

#### Scenario: Carga inicial
- **WHEN** se aplican las migraciones por primera vez
- **THEN** existen los roles admin, receptionist y professional, servicios base con duración (consulta 30', limpieza 45') y la política de cancelación de 24 h

#### Scenario: Sin datos reales
- **WHEN** se inspeccionan los scripts de seed y los fixtures de test
- **THEN** todos los nombres, DNI y teléfonos son claramente ficticios

### Requirement: Repositorios por entidad
El backend SHALL exponer repositorios Spring Data para las entidades del core.

#### Scenario: Repositorios disponibles
- **WHEN** se inspecciona `infrastructure/persistence`
- **THEN** existe un repositorio por entidad con métodos `findAllByTenantId` y `findByIdAndTenantId`
