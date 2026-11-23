# Proposal

## Why

El proyecto ya cuenta con autenticación, aislamiento por clínica y entidades persistentes, pero todavía no permite administrar la agenda ni crear turnos aplicando las reglas del dominio. C-04 habilita el núcleo funcional del MVP y desbloquea la reserva online, la agenda visual, las notificaciones y los reportes.

## What Changes

- Incorporar endpoints autenticados para administrar profesionales, sillones, prestaciones y bloqueos, siempre limitados a la clínica del usuario.
- Incorporar endpoints para crear, consultar y actualizar turnos mediante DTOs separados de las entidades JPA.
- Calcular automáticamente la finalización del turno con la duración de la prestación seleccionada.
- Validar ventanas de atención, bloqueos y solapamientos por profesional y por sillón.
- Permitir sobreturnos únicamente cuando un rol autorizado envía una confirmación explícita.
- Aplicar los estados `scheduled`, `confirmed`, `cancelled`, `no_show` y `completed`.
- Añadir pruebas automatizadas para CRUD, reglas de disponibilidad, casos de borde, permisos y aislamiento entre clínicas.
- Mantener fuera de alcance la interfaz de usuario, la reserva pública, la política de cancelación/reprogramación, las notificaciones y los pagos.

## Capabilities

### New Capabilities

- `agenda-resource-management`: Administración multi-tenant de profesionales, horarios semanales, sillones, prestaciones y bloqueos de agenda.
- `appointment-scheduling`: Creación, consulta y actualización de turnos con duración automática, disponibilidad, anti-solapamiento, sobreturnos explícitos, estados y permisos.

### Modified Capabilities

Ninguna. Las capacidades existentes de autenticación, modelo de dominio y aislamiento multi-tenant se reutilizan sin cambiar sus contratos vigentes.

## Impact

- **Backend:** nuevos DTOs, servicios de aplicación, controladores REST y manejo uniforme de errores para el dominio de agenda.
- **Persistencia:** ampliación de consultas multi-tenant para detectar turnos y bloqueos superpuestos; una migración numerada será necesaria si el diseño aprobado agrega datos persistentes para la confirmación de sobreturnos o normaliza horarios.
- **Seguridad:** autorización por rol en los endpoints de agenda y uso obligatorio del `tenantId` derivado del JWT.
- **Pruebas:** pruebas unitarias o de integración para cada escenario aprobado en las especificaciones.
- **Dependencias posteriores:** C-05, C-07, C-12 y C-13 podrán consumir el contrato de agenda una vez archivado C-04.
