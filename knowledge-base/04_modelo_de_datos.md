# Modelo de Datos

## Dominios

- Identidad y acceso (usuarios, roles, tenants/clínicas).
- Agenda (profesionales, sillones/boxes, servicios, turnos, bloqueos).
- Pacientes (ficha, anamnesis, odontograma, adjuntos).
- Administración (cobros, señas, saldos, reportes).

## Entidades principales

### Clinic (Tenant)
- id, name, slug, timezone, settings
- Relación 1:N con Professional, Chair, Patient, Service

### User
- id, email, password_hash, role, clinic_id
- Roles: admin, receptionist, professional

### Professional
- id, user_id, clinic_id, specialty, schedule (por día), active

### Chair (sillón/box)
- id, clinic_id, name, active

### Service (prestación)
- id, clinic_id, name, duration_minutes, color, active

### Patient
- id, clinic_id, full_name, dni, phone, email, birth_date, notes
- Relación 1:N con Appointment, Payment, Attachment

### Appointment (turno)
- id, clinic_id, patient_id, professional_id, chair_id, service_id
- start_at, end_at, status (scheduled/confirmed/cancelled/no_show/completed)
- deposit_id (opcional), notes

### Block (bloqueo)
- id, clinic_id, professional_id?, chair_id?, start_at, end_at, reason

### Payment
- id, clinic_id, patient_id, appointment_id?, amount, method, status, paid_at

### Deposit (seña)
- id, patient_id, appointment_id, amount, status

### Attachment
- id, patient_id, type (image/xray), url, uploaded_at

### Odontogram
- id, patient_id, tooth_code (FDI), status, notes, updated_at

## Seed data inicial

- Roles por defecto (admin, receptionist, professional).
- Servicios base con duraciones (consulta 30', limpieza 45', etc.).
- Configuración de política de cancelación por defecto (24h).

## ERD (resumen)

Clinic 1:N Professional, Chair, Patient, Service, Appointment, Block, Payment
Patient 1:N Appointment, Payment, Attachment, Odontogram
Appointment N:1 Professional, Chair, Service, Patient
Appointment 1:0..1 Deposit, 1:N Payment