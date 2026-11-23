# appointment-scheduling Specification

## Purpose

Permitir crear y administrar turnos verificando duración, horarios, bloqueos y disponibilidad de profesionales y sillones, sin mezclar información entre clínicas.

## Requirements

### Requirement: Creación de turno con duración automática
El sistema SHALL crear turnos con estado inicial `scheduled` y SHALL calcular `endAt` sumando a `startAt` la duración vigente de la prestación seleccionada.

#### Scenario: Turno creado correctamente
- **GIVEN** un paciente, profesional, sillón y prestación activos de la misma clínica, con una ventana de atención disponible
- **WHEN** un rol autorizado solicita un turno indicando `startAt` y los identificadores correspondientes
- **THEN** el sistema crea el turno con estado `scheduled` y calcula `endAt` desde `Service.durationMinutes`

#### Scenario: Recurso inactivo o inexistente
- **GIVEN** un paciente inexistente, o un profesional, sillón o prestación inexistente, inactivo o perteneciente a otra clínica
- **WHEN** se solicita crear un turno que lo referencia
- **THEN** el sistema rechaza la creación sin exponer datos de otra clínica

### Requirement: Disponibilidad por intervalo
El sistema SHALL tratar los turnos como intervalos semiabiertos `[startAt, endAt)` y SHALL impedir solapamientos activos del mismo profesional o sillón.

#### Scenario: Solapamiento de profesional
- **GIVEN** un turno activo de 10:00 a 10:30 para un profesional
- **WHEN** se solicita otro turno del mismo profesional que comienza antes de las 10:30 y termina después de las 10:00
- **THEN** el sistema rechaza la creación con estado 409 e identifica el conflicto de profesional

#### Scenario: Solapamiento de sillón
- **GIVEN** un turno activo de 10:00 a 10:30 en un sillón
- **WHEN** se solicita otro turno en el mismo sillón que se superpone con ese intervalo
- **THEN** el sistema rechaza la creación con estado 409 e identifica el conflicto de sillón

#### Scenario: Turnos adyacentes
- **GIVEN** un turno que finaliza a las 10:30
- **WHEN** se solicita otro turno del mismo profesional y sillón que comienza exactamente a las 10:30
- **THEN** el sistema considera el horario disponible y permite crear el nuevo turno

#### Scenario: Turno cancelado no ocupa disponibilidad
- **GIVEN** un turno con estado `cancelled`
- **WHEN** se solicita un nuevo turno en el mismo intervalo, profesional y sillón
- **THEN** el sistema no considera al turno cancelado como conflicto de disponibilidad

### Requirement: Ventana de atención y bloqueos
El sistema SHALL crear un turno únicamente cuando todo su intervalo esté contenido en una ventana de atención del profesional y no colisione con un bloqueo aplicable.

#### Scenario: Fuera del horario profesional
- **GIVEN** un profesional con una ventana de atención de 09:00 a 13:00 en la fecha solicitada
- **WHEN** un turno comienza antes de las 09:00 o termina después de las 13:00
- **THEN** el sistema rechaza la creación con estado 422

#### Scenario: Colisión con bloqueo profesional
- **GIVEN** un bloqueo del profesional que intersecta el intervalo solicitado
- **WHEN** se solicita crear el turno
- **THEN** el sistema rechaza la creación con estado 409

#### Scenario: Colisión con bloqueo de sillón
- **GIVEN** un bloqueo del sillón que intersecta el intervalo solicitado
- **WHEN** se solicita crear el turno
- **THEN** el sistema rechaza la creación con estado 409

### Requirement: Sobreturno con confirmación explícita
El sistema SHALL permitir ignorar conflictos con otros turnos únicamente mediante `allowOverbooking=true` enviado por un rol autorizado, y SHALL conservar quién autorizó el sobreturno. Esta excepción MUST NOT ignorar bloqueos, recursos inactivos ni ventanas de atención.

#### Scenario: Sobreturno confirmado por recepcionista
- **GIVEN** un intervalo ocupado por otro turno y un usuario con rol `admin` o `receptionist`
- **WHEN** solicita el nuevo turno con `allowOverbooking=true`
- **THEN** el sistema crea el turno marcado como sobreturno y registra al usuario autorizante

#### Scenario: Profesional confirma en su propia agenda
- **GIVEN** un intervalo ocupado en la agenda del profesional autenticado
- **WHEN** ese profesional solicita el nuevo turno con `allowOverbooking=true`
- **THEN** el sistema crea el sobreturno y registra al profesional autorizante

#### Scenario: Confirmación ausente
- **GIVEN** un intervalo ocupado por otro turno
- **WHEN** la solicitud omite `allowOverbooking=true`
- **THEN** el sistema rechaza el conflicto con estado 409

#### Scenario: Sobreturno no puede ignorar un bloqueo
- **GIVEN** un intervalo afectado por un bloqueo o fuera de la ventana de atención
- **WHEN** se solicita un turno con `allowOverbooking=true`
- **THEN** el sistema rechaza la creación

### Requirement: Consulta y modificación de turnos
El sistema SHALL permitir listar, consultar y actualizar turnos de la clínica autenticada, recalculando y revalidando la disponibilidad cuando cambien el horario, profesional, sillón o prestación.

#### Scenario: Listado de turnos
- **GIVEN** un usuario autenticado con acceso a la agenda
- **WHEN** consulta turnos por rango de fechas y filtros opcionales de profesional, sillón o estado
- **THEN** el sistema devuelve únicamente los turnos coincidentes de su clínica

#### Scenario: Revalidación al modificar agenda
- **GIVEN** un turno existente
- **WHEN** un rol autorizado modifica su inicio, profesional, sillón o prestación
- **THEN** el sistema recalcula `endAt` y aplica las mismas validaciones usadas durante la creación

#### Scenario: Eliminación administrativa
- **GIVEN** un turno creado por error y un usuario con rol `admin`
- **WHEN** solicita eliminarlo
- **THEN** el sistema elimina el turno de su clínica y responde 204

### Requirement: Estados de turno
El sistema SHALL aceptar únicamente los estados `scheduled`, `confirmed`, `cancelled`, `no_show` y `completed`, aplicados por un rol autorizado.

#### Scenario: Cambio de estado válido
- **GIVEN** un turno de la clínica autenticada
- **WHEN** un rol autorizado solicita uno de los estados permitidos
- **THEN** el sistema actualiza el estado y devuelve el turno actualizado

#### Scenario: Estado desconocido
- **WHEN** se solicita un estado fuera del conjunto permitido
- **THEN** el sistema rechaza la solicitud con estado 422

### Requirement: Roles de acceso a turnos
El sistema SHALL permitir a `admin` y `receptionist` administrar los turnos de su clínica; un `professional` SHALL acceder y operar solo sobre sus propios turnos; un `patient` MUST NOT acceder a estos endpoints internos.

#### Scenario: Profesional consulta turno ajeno
- **GIVEN** un usuario con rol `professional`
- **WHEN** consulta o modifica un turno asignado a otro profesional
- **THEN** el sistema rechaza la operación con estado 403 o 404

#### Scenario: Paciente accede a agenda interna
- **GIVEN** un usuario con rol `patient`
- **WHEN** intenta acceder a un endpoint interno de turnos
- **THEN** el sistema rechaza la operación con estado 403

### Requirement: Aislamiento de turnos por clínica
El sistema MUST obtener el `tenant_id` del token y MUST incluirlo en todas las búsquedas, validaciones de conflictos y escrituras de turnos.

#### Scenario: Acceso cruzado a turno
- **GIVEN** un turno perteneciente a la clínica B
- **WHEN** un usuario de la clínica A intenta consultarlo, modificarlo o eliminarlo por identificador
- **THEN** el sistema responde 404 sin revelar su existencia

#### Scenario: Conflictos aislados por clínica
- **GIVEN** dos clínicas con profesionales y sillones cuyos identificadores lógicos coinciden
- **WHEN** ambas crean turnos en el mismo intervalo usando sus propios recursos
- **THEN** el sistema evalúa la disponibilidad de cada clínica por separado
