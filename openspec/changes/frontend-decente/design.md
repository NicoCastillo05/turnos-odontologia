# Design

## Context

Ver proposal.md — Why. Frontend React/Vite existente solo tiene `BookingPage` y un esqueleto de `App`. Backend ya expone endpoints protegidos de auth y agenda. Este cambio es frontend que consume esos endpoints, sin cambios de contrato backend.

## Goals / Non-Goals

**Goals:**
- Login + guard de rutas + cliente HTTP autenticado con refresh.
- Vista de agenda operativa: listado filtrable, creación de turno, cambio de estado.
- Pulido visual y responsive compartido (CSS global) aplicado a login, agenda y reserva pública.

**Non-Goals:**
- Gestión de usuarios/clínicas desde el frontend (solo login).
- Reportes, pagos, notificaciones.
- Rediseño del backend o nuevos endpoints.

## Decisions

- **Estado de autenticación**: Context + hook `useAuth` con tokens en `localStorage` (access/refresh). Alternativa considerada: cookies httpOnly — rechazada por simplicidad del stack sin BFF.
- **Cliente HTTP**: módulo `shared/api/http.ts` con fetch wrapper que inyecta `Authorization` y reintenta una vez tras 401 usando refresh. Alternativa: axios — rechazada para no añadir dependencia.
- **Routing**: `react-router-dom` v6 con rutas públicas (`/`, `/login`, `/booking/:slug`) y protegidas (`/agenda`) envueltas en `<RequireAuth>`.
- **Estructura**: `features/auth/`, `features/agenda/`, `shared/ui/`, `shared/api/`, siguiendo la convención existente de `features/booking/`.
- **Estilos**: un único `index.css` global con variables CSS (tokens) y clases utilitarias mínimas (layout, botones, inputs, cards). Sin librería de UI.
- **Datos de agenda**: estados de turno con colores mapeados por `AppointmentStatus` (confirmed, cancelled, completed, no_show).
- **Profesional en listado**: se muestra `specialty` o `#id` ya que `ProfessionalResponse` no incluye nombre; no se modifica backend.

## Risks / Trade-offs

- [Tokens en localStorage expuestos a XSS] → Mitigar con escape adecuado y no renderizar HTML no confiable; aceptable para demo.
- [Refresh concurrente] → Serializar renovación con promesa compartida para evitar múltiples refresh.
- [Sin nombre de profesional en respuesta] → Mostrar specialty/id; documentado como limitación.

## Migration Plan

Build estático Vite apuntando al backend existente vía `VITE_API_BASE`. Sin migraciones. Rollback: revertir build del frontend.

## Open Questions

- Ninguno bloqueante.
