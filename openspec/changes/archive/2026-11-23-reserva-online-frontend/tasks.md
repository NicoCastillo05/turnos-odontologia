# Tasks

## 1. Verificación backend (API de disponibilidad)

- [x] 1.1 Revisar endpoints existentes de appointment-scheduling y confirmar si existe endpoint público de slots por slug; documentar en design.md Open Questions si falta
- [x] 1.2 Si falta, agregar endpoint mínimo de disponibilidad por slug en backend y verificar que responde slots disponibles

## 2. Estructura frontend y routing

- [x] 2.1 Configurar router (react-router-dom) con ruta `/booking/:slug` y verificar navegación
- [x] 2.2 Crear estructura `features/booking/`, `pages/BookingPage.tsx`, `shared/` y verificar build Vite

## 3. Componentes de reserva

- [x] 3.1 Implementar selección de profesional y prestación con tipado estricto y verificar render
- [x] 3.2 Implementar calendario/selector de fecha y slots disponibles consumiendo backend y verificar carga de slots
- [x] 3.3 Implementar formulario de datos mínimos (nombre, teléfono) con validación y verificar rechazo si faltan
- [x] 3.4 Implementar confirmación de reserva y feedback de éxito/error y verificar estado `confirmed`

## 4. Integración y verificación

- [x] 4.1 Integrar flujo completo end-to-end y verificar reserva exitosa (happy path)
- [x] 4.2 Verificar rechazo de slot ocupado con mensaje claro (business error)
- [x] 4.3 Verificar reserva en slot adyacente (edge case) crea turno correctamente

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.