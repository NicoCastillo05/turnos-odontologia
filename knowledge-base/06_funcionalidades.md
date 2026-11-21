# Funcionalidades

## Épica 1: Agenda y turnos

### US-001 — Ver agenda del día/semana
**Como** recepcionista/odontólogo
**Quiero** ver la agenda diaria/semanal por profesional y sillón
**Para** coordinar turnos y evitar solapamientos

**Criterios de aceptación**:
- [ ] Vista día y semana.
- [ ] Filtro por profesional y sillón.
- [ ] Estados de turno con colores.

**Reglas relacionadas**: RN-AG-01, RN-AG-02, RN-AG-08

### US-002 — Crear turno
**Como** recepcionista
**Quiero** crear un turno eligiendo paciente, profesional, sillón, servicio y horario
**Para** agendar una atención

**Criterios de aceptación**:
- [ ] Duración automática por servicio.
- [ ] Validación anti-solapamiento.
- [ ] Confirmación visual.

**Reglas relacionadas**: RN-AG-01, RN-AG-02, RN-AG-03, RN-AG-07

### US-003 — Bloquear agenda
**Como** profesional/administrador
**Quiero** bloquear horarios (vacaciones, capacitaciones)
**Para** evitar asignaciones en esos períodos

**Criterios de aceptación**:
- [ ] Creación de bloqueo por rango.
- [ ] No asignable a turnos.

**Reglas relacionadas**: RN-AG-05

### US-004 — Sobreturno
**Como** recepcionista
**Quiero** agregar un sobreturno con confirmación explícita
**Para** atender urgencias sin romper la agenda

**Criterios de aceptación**:
- [ ] Confirmación explícita requerida.
- [ ] Marcado visual como sobreturno.

**Reglas relacionadas**: RN-AG-06

## Épica 2: Reserva online

### US-005 — Reservar turno online 24/7
**Como** paciente
**Quiero** reservar un turno eligiendo profesional y horario desde un link
**Para** no tener que llamar ni escribir por WhatsApp

**Criterios de aceptación**:
- [ ] Disponible 24/7.
- [ ] Confirmación automática.
- [ ] Link compartible (web/WhatsApp/redes).

**Reglas relacionadas**: RN-RO-01, RN-RO-02, RN-RO-05

### US-006 — Cancelar/reprogramar turno
**Como** paciente
**Quiero** cancelar o reprogramar mi turno con anticipación mínima
**Para** gestionar mi agenda

**Criterios de aceptación**:
- [ ] Política configurable (2-24h).
- [ ] Aviso de pérdida de seña si fuera de plazo.

**Reglas relacionadas**: RN-RO-03, RN-RO-04

## Épica 3: Pacientes

### US-007 — Ficha de paciente + odontograma
**Como** odontólogo
**Quiero** ver/editar ficha, anamnesis y odontograma FDI
**Para** registrar la atención clínica

**Criterios de aceptación**:
- [ ] Notación FDI.
- [ ] Adjuntos/imágenes.

**Reglas relacionadas**: RN-PA-01, RN-PA-04

## Épica 4: Notificaciones

### US-008 — Recordatorios automáticos
**Como** odontólogo
**Quiero** que el sistema envíe recordatorios por WhatsApp y email
**Para** reducir ausencias

**Criterios de aceptación**:
- [ ] Plantillas aprobadas por Meta.
- [ ] Configurable por clínica.

**Reglas relacionadas**: RN-NO-01, RN-NO-02

## Épica 5: Administración

### US-009 — Caja y cobros
**Como** recepcionista
**Quiero** registrar cobros, señas y saldos, integrar Mercado Pago
**Para** controlar ingresos

**Criterios de aceptación**:
- [ ] Múltiples métodos de pago.
- [ ] Señas vinculadas a turno.
- [ ] Reportes básicos.

**Reglas relacionadas**: RN-PG-01, RN-PG-02, RN-PG-03, RN-PG-04

### US-010 — Reportes
**Como** administrador
**Quiero** ver ocupación, ingresos y ausentismo
**Para** tomar decisiones

**Criterios de aceptación**:
- [ ] Dashboard básico.
- [ ] Exportación CSV/Excel.

## Épica 6: Multi-profesional y roles

### US-011 — Roles y permisos
**Como** administrador
**Quiero** asignar roles y permisos a usuarios
**Para** controlar el acceso

**Criterios de aceptación**:
- [ ] RBAC por rol.
- [ ] Aislamiento por tenant.

**Reglas relacionadas**: RN-SE-01, RN-SE-02, RN-SE-03