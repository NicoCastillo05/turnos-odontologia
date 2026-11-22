# Design — C-03 auth-y-rbac

## Context

Ver `proposal.md`. Backend Spring Boot 3, JPA, H2, entidades con tenant_id. Existe `DevSecurityConfig` permissivo de C-01.

## Goals / Non-Goals

**Goals:** JWT access+refresh, register/login/refresh, RBAC, tenant aislamiento, rate limit.
**Non-Goals:** OAuth social, MFA, gestión de usuarios avanzada (se deja para después).

## Decisions

- Usar `jjwt` 0.12 para tokens; secret por env `JWT_SECRET` (min 32 chars).
- Access token 15 min, refresh 7 días; refresh rotado en cada refresh.
- Roles como enum en User (ya existe) mapeados a Spring Security authorities `ROLE_*`.
- `TenantFilter` extrae clinic_id del JWT y lo pone en `TenantContext` (ThreadLocal).
- Rate limiting con bucket4j por IP+email en login.
- Register crea Clinic + User admin en transacción.

## Risks / Trade-offs

- [Secret débil] → validar longitud en startup.
- [Refresh reutilizado] → rotación + blacklist simple en memoria (v1).

## Migration Plan

No cambios de esquema. Solo código.

## Open Questions

- Ninguna.