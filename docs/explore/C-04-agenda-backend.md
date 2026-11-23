# Explore C-04 Agenda Backend

## Objetivo del relevamiento

Determinar el estado real del backend antes de proponer C-04 y delimitar el trabajo necesario para implementar la agenda sin agregar decisiones improvisadas durante Apply.

## Alcance confirmado

C-04 debe entregar endpoints CRUD para turnos, bloqueos, profesionales, sillones y prestaciones. La creación de turnos debe calcular su duración desde la prestación, respetar la ventana de atención, evitar colisiones por profesional y sillón, considerar bloqueos, exigir confirmación explícita para un sobreturno y preservar el aislamiento por clínica.

Fuentes: `CHANGES.md` C-04; `knowledge-base/05_reglas_de_negocio.md` RN-AG-01 a RN-AG-08; `knowledge-base/07_flujos_principales.md` Flujo 2.

## Estado actual del código

- Ya existen las entidades `Appointment`, `Block`, `Professional`, `Chair` y `Service`.
- La migración V002 ya crea las tablas correspondientes y los servicios iniciales con duraciones de 20, 30 y 45 minutos.
- Los repositorios actuales solo permiten listar y buscar por `tenant_id`; todavía no tienen consultas de solapamiento ni de bloqueos.
- `Professional.schedule` existe como texto JSON, pero el proyecto todavía no define el contrato de ese JSON ni su validación.
- `Appointment.status` admite `scheduled`, `confirmed`, `cancelled`, `no_show` y `completed`.
- No existen controladores, DTOs ni servicios de aplicación para el dominio de agenda.
- La autenticación JWT coloca el `tenantId` en `TenantContext` y exige autenticación para todos los endpoints fuera de `/api/auth/**`.
- No hay autorización por rol aplicada todavía a endpoints de agenda.
- No existen tests específicos de agenda.

## Brechas que debe resolver Propose

1. Definir rutas, DTOs, respuestas HTTP y roles autorizados para cada recurso.
2. Definir el formato del horario semanal de `Professional` y cómo se interpretan sus zonas horarias.
3. Definir la semántica exacta de los intervalos para permitir el caso borde donde un turno comienza cuando termina otro.
4. Definir si los turnos `cancelled` dejan de bloquear disponibilidad.
5. Definir cómo se solicita y registra la confirmación explícita de un sobreturno.
6. Definir las consultas de solapamiento siempre limitadas por `tenant_id`.
7. Determinar si hace falta una migración numerada adicional para guardar decisiones nuevas del dominio.
8. Separar entidades JPA de DTOs y mantener controladores delgados, según `AGENTS.md`.

## Escenarios mínimos para la especificación

- Caso feliz: crear un turno dentro del horario disponible y calcular `endAt` desde la duración del servicio.
- Error de negocio: rechazar un turno que se solapa con otro del mismo profesional o sillón.
- Caso de borde: aceptar un turno que comienza exactamente cuando termina el anterior.
- Bloqueo: rechazar un turno que colisiona con un bloqueo aplicable.
- Sobreturno: permitir la colisión únicamente con confirmación explícita y un rol autorizado.
- Aislamiento: impedir leer o modificar recursos pertenecientes a otra clínica.

## Fuera de alcance de C-04

- Interfaz de agenda.
- Reserva pública online.
- Cancelación y reprogramación con política de anticipación.
- Notificaciones por WhatsApp o correo.
- Pagos y señas.

## Resultado de Explore

C-04 está listo para pasar a Propose porque C-03 ya está integrado. La implementación debe apoyarse en las entidades y migraciones existentes, pero necesita especificar antes los contratos HTTP, el formato de horarios, la regla de intervalos, los permisos y la persistencia de la confirmación de sobreturnos.
