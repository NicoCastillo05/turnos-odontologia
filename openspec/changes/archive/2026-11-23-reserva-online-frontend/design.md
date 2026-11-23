# Design

## Context

Ver proposal.md — Why. Frontend React/Vite base sin features; backend agenda ya provee anti-solapamiento, duración por servicio, ventanas y bloqueos. Este cambio es frontend que consume esos endpoints públicamente por slug de clínica.

## Goals / Non-Goals

**Goals:**
- Ruta pública `/booking/:slug` con flujo de reserva completo.
- Reutilizar validaciones del backend (no duplicar lógica de negocio en frontend).
- Confirmación automática y feedback claro.

**Non-Goals:**
- Cancelación/reprogramación (C-06).
- Notificaciones WhatsApp/email (C-07).
- Pagos/señas (C-11).
- Autenticación del paciente (reserva anónima).

## Decisions

- **Slug → tenant_id**: el backend resuelve el slug a clínica; el frontend no maneja tenant_id directamente. Alternativa considerada: pasar tenant_id en query — rechazada por seguridad/UX.
- **Disponibilidad**: consultar endpoint backend de slots disponibles por profesional/servicio/fecha; no calcular en frontend.
- **Estado confirmado**: la reserva se crea con estado `confirmed` directamente (RN-RO-02), sin flujo de aprobación.
- **Tipado estricto**: interfaces TypeScript para todas las respuestas; sin `any`.
- **Estructura**: `pages/BookingPage.tsx`, `features/booking/` (componentes, hooks, api client), `shared/` (UI primitives).

## Risks / Trade-offs

- [Endpoint público de disponibilidad puede no existir] → verificar backend; si falta, agregar endpoint mínimo (sin cambios de contrato del core).
- [Abuso/spam de reservas] → rate limiting backend (fuera de scope v1; registrar como riesgo).
- [Paciente ingresa teléfono inválido] → validación básica en frontend + backend.

## Migration Plan

Deploy frontend estático (Vite build) apuntando al backend existente. Sin migraciones de BD. Rollback: revertir build del frontend.

## Open Questions

- ¿El backend expone un endpoint público de slots disponibles por slug? (verificar en apply). **Respuesta: no existe endpoint público.** Se requiere agregar endpoint mínimo `/api/public/booking/{slug}` (pendiente backend).