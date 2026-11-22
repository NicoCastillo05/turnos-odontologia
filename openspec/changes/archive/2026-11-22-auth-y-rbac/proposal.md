# Proposal — C-03 auth-y-rbac

## Why

C-02 dejó los modelos y migraciones, pero no hay forma de autenticar usuarios ni de aislar por clínica. Sin JWT y RBAC, los endpoints de agenda/pacientes/caja (C-04 en adelante) no pueden protegerse ni verificar tenant. Es la puerta de entrada a toda la funcionalidad del MVP.

## What Changes

- Implementar JWT access + refresh tokens con firma segura por env.
- Endpoints `POST /api/auth/login`, `/register` (alta de clínica) y `/refresh`.
- RBAC por rol (admin, receptionist, professional, patient) en cada endpoint mediante Spring Security.
- Aislamiento por tenant (clinic_id) derivado del token.
- Rate limiting 5/60s por IP+email en login.
- Nuevo filtro de seguridad y configuración de CORS/CSRF.

## Capabilities

### New Capabilities
- `auth-rbac`: autenticación JWT con refresh tokens, registro de clínica, RBAC por rol y aislamiento multi-tenant.

### Modified Capabilities
- (ninguna)

## Impact

- Nuevos paquetes `security`, `auth` (controller/service/dto) en backend.
- Modificación de `DevSecurityConfig` / nueva `SecurityConfig`.
- Dependencias: jjwt, spring-boot-starter-security, bucket4j (rate limit).
- Tests de integración de login/register/refresh/RBAC.
- `CHANGES.md` estado C-03.