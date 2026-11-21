# Flujos Principales

## Flujo 1: Reserva online 24/7
**Disparador**: Paciente abre link de reserva
**Actor**: Paciente

**Pasos**:
1. Frontend muestra profesionales y horarios disponibles.
2. Paciente selecciona profesional, servicio, fecha/hora.
3. Backend valida anti-solapamiento (RN-AG-01, RN-AG-02, RN-AG-07).
4. Se crea Appointment (estado scheduled).
5. Se envía confirmación por WhatsApp/email (RN-NO-02).
6. Si hay seña, se genera link de pago MP.

**Casos de error**:
- Horario ya ocupado → sugerir alternativas.
- Paciente sin datos mínimos → solicitar.

## Flujo 2: Creación de turno manual
**Disparador**: Recepcionista crea turno
**Actor**: Recepcionista

**Pasos**:
1. Selecciona paciente (o crea nuevo).
2. Selecciona profesional, sillón, servicio, horario.
3. Backend valida ventanas de atención y anti-solapamiento.
4. Se crea Appointment.
5. Se envía confirmación.

## Flujo 3: Cancelación/reprogramación
**Disparador**: Paciente/recepcionista cancela
**Actor**: Paciente o recepcionista

**Pasos**:
1. Se verifica política de anticipación (RN-RO-03).
2. Si fuera de plazo, se marca seña como perdida (RN-RO-04).
3. Appointment pasa a cancelled.
4. Se notifica al paciente y profesional.

## Flujo 4: Cobro en caja
**Disparador**: Atención finalizada
**Actor**: Recepcionista

**Pasos**:
1. Se registra Payment con método.
2. Se aplica seña si existía.
3. Se actualiza saldo del paciente.
4. Se genera comprobante (post-MVP ARCA).

## Flujo 5: Recordatorios automáticos
**Disparador**: Cron (24h/2h antes del turno)
**Actor**: Sistema

**Pasos**:
1. Selecciona turnos programados/confirmados.
2. Envía plantilla WhatsApp aprobada.
3. Envía email de respaldo.
4. Registra envío.