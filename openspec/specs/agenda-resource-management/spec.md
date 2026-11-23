# agenda-resource-management Specification

## Purpose

Permitir que cada clínica administre de forma segura los profesionales, horarios, sillones, prestaciones y bloqueos que determinan la disponibilidad de su agenda.

## Requirements

### Requirement: Administración de recursos de agenda
El sistema SHALL exponer operaciones autenticadas para crear, listar, consultar, actualizar y eliminar profesionales, sillones y prestaciones dentro de la clínica del usuario.

#### Scenario: Administrador crea un recurso
- **GIVEN** un usuario autenticado con rol `admin`
- **WHEN** crea un profesional, sillón o prestación con datos válidos
- **THEN** el sistema devuelve el recurso creado con el `tenant_id` derivado del token

#### Scenario: Recepcionista consulta recursos
- **GIVEN** un usuario autenticado con rol `receptionist`
- **WHEN** lista profesionales, sillones o prestaciones
- **THEN** el sistema devuelve únicamente los recursos de su clínica

#### Scenario: Rol sin permiso intenta modificar configuración
- **GIVEN** un usuario autenticado sin rol `admin`
- **WHEN** intenta crear, actualizar o eliminar un profesional, sillón o prestación
- **THEN** el sistema rechaza la operación con estado 403

### Requirement: Horario semanal del profesional
El sistema SHALL representar el horario semanal como días con cero o más ventanas locales `start` y `end`, interpretadas en la zona horaria de la clínica, y SHALL rechazar ventanas inválidas o superpuestas.

#### Scenario: Horario semanal válido
- **GIVEN** un administrador de la clínica
- **WHEN** configura para un profesional ventanas con `start` anterior a `end` y sin superposición dentro de cada día
- **THEN** el sistema guarda el horario y lo devuelve con la misma estructura normalizada

#### Scenario: Ventanas del mismo día superpuestas
- **GIVEN** un administrador de la clínica
- **WHEN** configura dos ventanas horarias que se superponen en el mismo día
- **THEN** el sistema rechaza la operación con estado 422 y no modifica el horario anterior

### Requirement: Administración de bloqueos
El sistema SHALL permitir crear, consultar, actualizar y eliminar bloqueos asociados a un profesional, a un sillón o a ambos, siempre con inicio anterior al fin.

#### Scenario: Recepcionista crea un bloqueo
- **GIVEN** un usuario autenticado con rol `admin` o `receptionist`
- **WHEN** crea un bloqueo válido para un profesional o sillón de su clínica
- **THEN** el sistema devuelve el bloqueo creado y este afecta la disponibilidad del intervalo

#### Scenario: Profesional bloquea su propia agenda
- **GIVEN** un usuario autenticado con rol `professional` vinculado al profesional objetivo
- **WHEN** crea un bloqueo válido para sí mismo
- **THEN** el sistema crea el bloqueo dentro de su clínica

#### Scenario: Profesional intenta bloquear otra agenda
- **GIVEN** un usuario autenticado con rol `professional`
- **WHEN** intenta crear o modificar un bloqueo de otro profesional
- **THEN** el sistema rechaza la operación con estado 403

#### Scenario: Bloqueo sin recurso
- **WHEN** se solicita un bloqueo sin profesional ni sillón, o con inicio igual o posterior al fin
- **THEN** el sistema rechaza la operación con estado 422

### Requirement: Aislamiento de recursos por clínica
El sistema MUST resolver y persistir cada recurso de agenda usando exclusivamente el `tenant_id` derivado del token autenticado.

#### Scenario: Consulta cruzada de recurso
- **GIVEN** un recurso perteneciente a la clínica B
- **WHEN** un usuario de la clínica A intenta consultarlo, modificarlo o eliminarlo por identificador
- **THEN** el sistema responde 404 sin revelar que el recurso existe en otra clínica

#### Scenario: Referencia cruzada al crear un bloqueo
- **GIVEN** un profesional o sillón perteneciente a la clínica B
- **WHEN** un usuario de la clínica A intenta referenciarlo en un bloqueo
- **THEN** el sistema rechaza la operación sin crear datos cruzados
