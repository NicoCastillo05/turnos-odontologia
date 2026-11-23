# Proposal

## Why

Los pacientes no tienen forma de reservar turnos por su cuenta; hoy toda la agenda depende de recepción. La reserva online 24/7 por link compartible (web/WhatsApp/redes) reduce carga administrativa y permite captar turnos fuera del horario de oficina, con confirmación inmediata (RN-RO-01, RN-RO-02).

## What Changes

- Nueva ruta pública `/booking/:slug` (sin autenticación) para pacientes.
- Flujo de reserva: selección de profesional → prestación (servicio) → fecha → horario disponible → datos mínimos del paciente (nombre, teléfono) → confirmación automática.
- Consumo del backend existente de `appointment-scheduling` (anti-solapamiento, duración por servicio, ventanas de atención y bloqueos) para validar disponibilidad y crear el turno.
- Confirmación automática al reservar (no solicitud a aprobar) y feedback claro de éxito/error.
- Link compartible por clínica (slug) para WhatsApp/redes.
- Componentes React/Vite con tipado estricto (sin `any`), siguiendo la estructura `features/`, `pages/`, `shared/`.

## Capabilities

### New Capabilities
- `online-booking`: Flujo público de reserva de turnos por link compartible, con selección de profesional/servicio/slot, validación de disponibilidad contra el backend y confirmación automática.

### Modified Capabilities
- (ninguno — el backend de `appointment-scheduling` ya provee las validaciones; este cambio es frontend consumiendo esos endpoints)

## Impact

- Frontend: nuevas páginas y componentes en `frontend/src/` (rutas, booking flow, API client).
- Backend: reutiliza endpoints existentes de `appointment-scheduling` (sin cambios de contrato; puede requerir un endpoint público de disponibilidad si no existe).
- Multi-tenant: el slug identifica la clínica; todas las consultas siguen scope por `tenant_id`.
- Dependencias: C-04 agenda-backend (archivada).