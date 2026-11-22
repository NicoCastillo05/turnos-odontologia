# auth-rbac Specification

## Purpose

Permite autenticar usuarios de una clínica, emitir y renovar tokens JWT, y autorizar cada endpoint según el rol del usuario, manteniendo el aislamiento de datos por tenant (clinic_id).

## Requirements

### Requirement: Login con JWT
El sistema SHALL autenticar un usuario por email y contraseña y devolver access token y refresh token.

#### Scenario: Login exitoso
- **WHEN** el usuario envía credenciales válidas
- **THEN** el sistema devuelve 200 con access token y refresh token

#### Scenario: Credenciales inválidas
- **WHEN** email o contraseña no coinciden
- **THEN** el sistema devuelve 401 sin revelar cuál falló

### Requirement: Alta de clínica (register)
El sistema SHALL permitir crear una nueva clínica con su usuario administrador.

#### Scenario: Registro exitoso
- **WHEN** se envían datos válidos de clínica y admin
- **THEN** se crea la clínica y el usuario admin, devolviendo tokens

### Requirement: Refresh token
El sistema SHALL renovar el access token a partir de un refresh token válido.

#### Scenario: Refresh válido
- **WHEN** se envía un refresh token no expirado
- **THEN** el sistema devuelve un nuevo access token

#### Scenario: Refresh inválido/expirado
- **WHEN** el refresh token es inválido o expirado
- **THEN** el sistema devuelve 401

### Requirement: RBAC por rol
El sistema SHALL autorizar cada endpoint según el rol del usuario (admin, receptionist, professional, patient).

#### Scenario: Acceso denegado por rol
- **WHEN** un usuario con rol insuficiente intenta acceder a un endpoint restringido
- **THEN** el sistema devuelve 403

### Requirement: Aislamiento por tenant
El sistema SHALL filtrar toda consulta por clinic_id derivado del token.

#### Scenario: Cross-tenant bloqueado
- **WHEN** un usuario intenta acceder a datos de otra clínica
- **THEN** el sistema devuelve 404 o 403

### Requirement: Rate limiting en login
El sistema SHALL limitar a 5 intentos por 60 segundos por IP+email en /api/auth/login.

#### Scenario: Límite excedido
- **WHEN** se superan 5 intentos en 60s
- **THEN** el sistema devuelve 429
