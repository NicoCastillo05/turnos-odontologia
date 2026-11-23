# Spec Delta

## Purpose

Permite al personal de la clínica autenticarse, acceder a rutas protegidas y operar la agenda (consultar, crear y actualizar turnos) mediante un frontend con estilos y comportamiento responsive consistentes.

## ADDED Requirements

### Requirement: Login de personal
El sistema SHALL autenticar al personal mediante email y contraseña, almacenar los tokens JWT y redirigir a la vista protegida tras un login exitoso.

#### Scenario: Login exitoso
- **WHEN** un usuario ingresa credenciales válidas
- **THEN** el sistema almacena el access token y el refresh token y redirige a `/agenda`

#### Scenario: Login fallido
- **WHEN** un usuario ingresa credenciales inválidas
- **THEN** el sistema muestra un mensaje de error y no almacena tokens

### Requirement: Rutas protegidas
El sistema SHALL impedir el acceso a las vistas del personal sin un token válido, redirigiendo a `/login`.

#### Scenario: Acceso sin token
- **WHEN** un usuario sin token intenta acceder a `/agenda`
- **THEN** el sistema redirige a `/login`

#### Scenario: Token expirado con refresh válido
- **WHEN** el access token expira pero el refresh token sigue válido
- **THEN** el sistema renueva el access token automáticamente y continúa la navegación

### Requirement: Listado de turnos por fecha y profesional
El sistema SHALL mostrar los turnos de la clínica filtrados por rango de fecha y profesional opcional, indicando el estado con color.

#### Scenario: Listado por fecha
- **WHEN** el personal selecciona una fecha
- **THEN** el sistema muestra los turnos del día ordenados por hora con color según estado

#### Scenario: Filtro por profesional
- **WHEN** el personal selecciona un profesional
- **THEN** el sistema restringe el listado a los turnos de ese profesional

### Requirement: Creación de turno desde la agenda
El sistema SHALL permitir crear un turno indicando paciente, profesional, sillón, servicio y fecha/hora de inicio, validando campos obligatorios.

#### Scenario: Creación exitosa
- **WHEN** el personal completa todos los campos obligatorios con datos válidos
- **THEN** el sistema crea el turno y lo muestra en el listado con estado correspondiente

#### Scenario: Campos obligatorios faltantes
- **WHEN** el personal intenta crear un turno sin paciente, profesional, sillón, servicio o fecha/hora
- **THEN** el sistema rechaza la creación y solicita los campos faltantes

### Requirement: Actualización de estado de turno
El sistema SHALL permitir cambiar el estado de un turno (confirmado, cancelado, completado, no-show) y reflejar el cambio visualmente.

#### Scenario: Cambio de estado
- **WHEN** el personal cambia el estado de un turno
- **THEN** el sistema persiste el nuevo estado y actualiza el color del turno en la vista

### Requirement: Pulido visual y responsive
El sistema SHALL presentar un estilo global consistente y adaptarse a pantallas mobile y desktop en login, agenda y reserva pública.

#### Scenario: Vista mobile
- **WHEN** la aplicación se visualiza en ancho de 390px
- **THEN** los formularios y listados se adaptan sin desbordamiento horizontal

#### Scenario: Vista desktop
- **WHEN** la aplicación se visualiza en ancho de 1440px
- **THEN** el contenido se centra y aprovecha el espacio disponible

### Requirement: Cliente HTTP con autenticación
El sistema SHALL enviar el header `Authorization: Bearer <token>` en todas las llamadas protegidas y renovar el token ante expiración.

#### Scenario: Llamada autenticada
- **WHEN** el frontend realiza una llamada a un endpoint protegido
- **THEN** incluye el header `Authorization: Bearer` con el access token vigente

#### Scenario: Renovación automática
- **WHEN** una llamada protegida retorna 401 por token expirado
- **THEN** el frontend intenta renovar el token usando el refresh token y reintenta la llamada una vez
