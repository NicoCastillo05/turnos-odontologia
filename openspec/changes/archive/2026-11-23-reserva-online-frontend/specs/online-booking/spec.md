# Spec Delta

## Purpose

Permitir a pacientes reservar turnos de forma pública y autónoma a través de un link compartible, con selección de profesional, prestación y horario disponible, validación de disponibilidad contra el backend y confirmación automática.

## ADDED Requirements

### Requirement: Acceso público por link compartible
El sistema SHALL exponer una ruta pública `/booking/:slug` que identifique la clínica por su slug y permita reservar sin autenticación previa.

#### Scenario: Acceso a la página de reserva
- **GIVEN** una clínica con slug válido
- **WHEN** un paciente abre `/booking/:slug`
- **THEN** el sistema muestra la página de reserva con los profesionales y prestaciones disponibles de esa clínica

#### Scenario: Slug inválido o clínica inexistente
- **GIVEN** un slug que no corresponde a ninguna clínica
- **WHEN** un paciente intenta acceder a `/booking/:slug`
- **THEN** el sistema muestra un mensaje de "clínica no encontrada" sin exponer datos internos

### Requirement: Selección de profesional, prestación y horario
El sistema SHALL permitir al paciente seleccionar un profesional, una prestación (servicio) y un horario disponible, calculando la duración del turno desde `Service.durationMinutes`.

#### Scenario: Turno reservado con éxito (happy path)
- **GIVEN** un paciente con nombre y teléfono, un profesional disponible, una prestación activa y un horario libre dentro de la ventana de atención
- **WHEN** el paciente confirma la reserva
- **THEN** el sistema crea el turno con estado `confirmed` y muestra confirmación automática con los datos del turno

#### Scenario: Datos mínimos faltantes
- **GIVEN** un paciente que omite nombre o teléfono
- **WHEN** intenta confirmar la reserva
- **THEN** el sistema rechaza la reserva y solicita los datos mínimos requeridos (RN-RO-05)

### Requirement: Validación de disponibilidad contra solapamiento
El sistema SHALL validar que el slot seleccionado no se solape con turnos activos ni bloqueos del profesional/sillón, tratando los turnos como intervalos semiabiertos `[startAt, endAt)`.

#### Scenario: Intento de reservar slot ocupado (business error)
- **GIVEN** un turno activo de 10:00 a 10:30 para un profesional
- **WHEN** el paciente intenta reservar otro turno del mismo profesional que se superpone con ese intervalo
- **THEN** el sistema rechaza la reserva con un mensaje claro de horario no disponible

#### Scenario: Turnos adyacentes (edge case)
- **GIVEN** un turno que finaliza a las 10:30
- **WHEN** el paciente reserva un turno del mismo profesional que comienza exactamente a las 10:30
- **THEN** el sistema considera el horario disponible y crea la reserva correctamente

### Requirement: Confirmación automática
El sistema SHALL confirmar la reserva automáticamente al crear el turno, sin requerir aprobación manual (RN-RO-02).

#### Scenario: Confirmación inmediata
- **GIVEN** una reserva válida creada exitosamente
- **WHEN** el sistema responde al paciente
- **THEN** el turno queda en estado `confirmed` y se muestra la confirmación con fecha, hora, profesional y prestación

### Requirement: Aislamiento por clínica
El sistema SHALL resolver el `tenant_id` a partir del slug y aplicar aislamiento multi-tenant en todas las consultas y creaciones de la reserva pública.

#### Scenario: Reserva aislada por clínica
- **GIVEN** dos clínicas con profesionales cuyos identificadores lógicos coinciden
- **WHEN** un paciente reserva en la clínica A
- **THEN** la disponibilidad y el turno creado corresponden únicamente a la clínica A