# Reglas de Negocio

## Dominio: Agenda (RN-AG)

- **RN-AG-01**: No se pueden solapar dos turnos del mismo profesional. — Justificación: evita dobles asignaciones.
- **RN-AG-02**: No se pueden solapar dos turnos en el mismo sillón/box.
- **RN-AG-03**: La duración del turno depende de la prestación seleccionada (Service.duration_minutes).
- **RN-AG-04**: Horarios de atención configurables por profesional y por día (ventanas de trabajo).
- **RN-AG-05**: Bloqueos de agenda (vacaciones, capacitaciones) no asignables a turnos.
- **RN-AG-06**: Sobreturnos permitidos solo con confirmación explícita del profesional/recepcionista.
- **RN-AG-07**: Turno debe caer dentro de la ventana de atención del profesional y no colisionar con bloqueos.
- **RN-AG-08**: Estados de turno: programado, confirmado, cancelado, no presentado, completado.

## Dominio: Reservas online (RN-RO)

- **RN-RO-01**: Reserva online 24/7 por link compartible (web/WhatsApp/redes).
- **RN-RO-02**: Confirmación automática al reservar (no solicitud a aprobar).
- **RN-RO-03**: Cancelación/reprogramación con anticipación mínima configurable (2-24h).
- **RN-RO-04**: Fuera de plazo puede aplicar pérdida de seña (RN-PA-03).
- **RN-RO-05**: Requiere datos mínimos del paciente (nombre, teléfono).

## Dominio: Pacientes (RN-PA)

- **RN-PA-01**: Datos de salud sensibles: acceso restringido por rol (Ley 25.326).
- **RN-PA-02**: Historia clínica conforme Ley 26.529 (integridad, inalterabilidad, conservación 10 años) — v1.1.
- **RN-PA-03**: Seña obligatoria configurable por clínica para confirmar turno.
- **RN-PA-04**: Odontograma en notación FDI (dientes permanentes y temporales).

## Dominio: Pagos (RN-PG)

- **RN-PG-01**: Cobros registrados en caja con método (efectivo, tarjeta, MP, transferencia).
- **RN-PG-02**: Señas vinculadas a turno; aplicables a pago final.
- **RN-PG-03**: Saldos pendientes por paciente.
- **RN-PG-04**: Integración Mercado Pago para cobros online/señas.

## Dominio: Notificaciones (RN-NO)

- **RN-NO-01**: Recordatorios automáticos por WhatsApp (plantillas aprobadas) y email.
- **RN-NO-02**: Confirmación de reserva inmediata.
- **RN-NO-03**: Aviso de cancelación/reprogramación.

## Dominio: Acceso y seguridad (RN-SE)

- **RN-SE-01**: Autenticación JWT con refresh tokens.
- **RN-SE-02**: Autorización por rol (RBAC) en cada endpoint.
- **RN-SE-03**: Aislamiento de datos por tenant (clinic_id).
- **RN-SE-04**: Sin modo offline en v1; internet requerido.

## Excepciones globales

- Un administrador puede reasignar turnos entre profesionales si no hay solapamiento.
- Sobreturnos requieren confirmación explícita (no automática).