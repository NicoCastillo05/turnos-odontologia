# tenant-migrations Specification

## Purpose

Definir el esquema versionado, el aislamiento por clínica y los datos iniciales ficticios del sistema, de modo que el esquema sea trazable y las consultas no mezclen información entre clínicas.

## Requirements

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
