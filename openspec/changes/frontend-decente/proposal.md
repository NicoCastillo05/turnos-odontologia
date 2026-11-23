# Proposal

## Why

El frontend actual es solo un esqueleto con la página pública de reserva (`BookingPage`) y no ofrece autenticación ni vistas para el personal de la clínica. Para la demo del producto falta una experiencia mínima pero usable: login, agenda operativa (ver/crear turnos) y pulido visual/responsive de la reserva pública. Sin esto el flujo de recepción/administración no puede mostrarse.

## What Changes

- Nueva página de login (`/login`) que consume `POST /api/auth/login`, almacena el JWT (access + refresh) y protege las rutas del personal.
- Rutas protegidas con guard de autenticación (redirección a `/login` si no hay token válido).
- Nueva vista de agenda (`/agenda`) que lista turnos por fecha/profesional, muestra colores por estado, permite crear un turno (formulario con paciente, profesional, sillón, servicio, fecha/hora) y actualizar estado.
- Pulido de `BookingPage` (estilos, responsive) y CSS global compartido (tokens, layout, componentes base) reutilizable por todas las páginas.
- Cliente HTTP centralizado que agrega `Authorization: Bearer <token>` y maneja refresh.

## Capabilities

### New Capabilities
- `staff-portal`: Autenticación de personal (login/logout, tokens), rutas protegidas y vistas operativas mínimas (agenda: listar/crear/actualizar turnos), más estilo global y pulido responsive compartido.

### Modified Capabilities
- (ninguno — se reutilizan los endpoints existentes de `auth-rbac`, `appointment-scheduling` y `online-booking`; no cambian requisitos de backend)

## Impact

- Frontend: nuevas páginas/componentes en `frontend/src/` (`pages/LoginPage.tsx`, `pages/AgendaPage.tsx`, `features/auth/`, `features/agenda/`, `shared/ui/`), router actualizado, CSS global.
- Backend: sin cambios de contrato; se consumen endpoints existentes (`/api/auth/*`, `/api/appointments`, `/api/professionals`, `/api/chairs`, `/api/services`).
- Multi-tenant: el JWT contiene `clinicId`; todas las llamadas envían `Authorization: Bearer`.
- Dependencias: C-04 agenda-backend y C-05 reserva-online (archivadas).
