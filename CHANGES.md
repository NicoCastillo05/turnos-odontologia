# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **Turnos Odontología**.
> Cada change es atómico: un agente puede implementarlo en una sesión (~4-6 horas).
> **Leer este archivo antes de empezar un item.**

---

## Cómo usar este documento

1. Identificá el item elegible (sin dependencias pendientes o ya completadas).
2. Verificá dependencias y leé los archivos de la KB indicados en "Leer antes".
3. Acordá alcance y criterios de verificación con el equipo.
4. Implementá y verificá el resultado.
5. Actualizá el estado del item (`[x]` completado).

---

## Árbol de dependencias

```
C-01 scaffolding-y-configuracion
└── C-02 core-models-y-migraciones
    └── C-03 auth-y-rbac
        ├── C-04 agenda-backend
        │   ├── C-05 reserva-online-frontend
        │   │   └── C-06 cancelacion-reprogramacion
        │   └── C-07 frontend-agenda
        ├── C-08 pacientes-y-odontograma
        │   └── C-09 adjuntos-clinicos
        ├── C-10 caja-y-pagos
        │   └── C-11 mercadopago-y-senas
        ├── C-12 notificaciones-whatsapp-email
        └── C-13 reportes-y-exportacion
C-14 pwa-y-responsive (transversal, al final)
```

### Paralelismo por fase

**GATE 0**: `C-01` scaffolding ✓ ← PRIMER FORK
  → C-02 core-models-y-migraciones [Agente A]

**GATE 1**: `C-02` core-models ✓
  → C-03 auth-y-rbac [Agente A]

**GATE 2**: `C-03` auth-y-rbac ✓ ← FORK
  → C-04 agenda-backend [Agente A]
  → C-08 pacientes-y-odontograma [Agente B]
  → C-10 caja-y-pagos [Agente C]
  → C-12 notificaciones-whatsapp-email [Agente C — si C-04 ✓]

**GATE 3**: `C-04` agenda-backend ✓ ← FORK
  → C-05 reserva-online-frontend [Agente A]
  → C-07 frontend-agenda [Agente B]
  → C-06 cancelacion-reprogramacion [Agente C — si C-05 ✓]

**GATE 4**: `C-08` pacientes-y-odontograma ✓
  → C-09 adjuntos-clinicos [Agente B]

**GATE 5**: `C-10` caja-y-pagos ✓
  → C-11 mercadopago-y-senas [Agente C]

**GATE 6**: cierre de MVP ✓
  → C-13 reportes-y-exportacion [Agente A]
  → C-14 pwa-y-responsive [Agente B]

### Camino crítico (9 changes — mínimo irreducible)

C-01 → C-02 → C-03 → C-04 → C-05 → C-06 → C-11 → C-13 → C-14*

* C-14 puede intercambiarse con C-13 en el cierre.

### Plan óptimo con 3 agentes

| Paso | Agente A (Backend Core) | Agente B (Backend Aux) | Agente C (Frontend) |
|------|------------------------|------------------------|---------------------|
| 1 | C-01 scaffolding | — | — |
| 2 | C-02 core-models | — | — |
| 3 | C-03 auth-y-rbac | — | — |
| 4 | C-04 agenda-backend | C-08 pacientes | C-10 caja |
| 5 | C-05 reserva-online | C-09 adjuntos | C-11 mercadopago |
| 6 | C-06 cancelacion | — | C-12 notificaciones |
| 7 | C-07 frontend-agenda | C-13 reportes | C-14 pwa |

---

## FASE 1 — Infraestructura y modelo core

### [C-01] `scaffolding-y-configuracion`
- **Estado**: `[x]` completado (2026-11-21, OPSX `2026-11-21-scaffolding-y-configuracion`)
- **Scope**:
  - Estructura de directorios backend/frontend según `08_arquitectura_propuesta.md`.
  - Configuración de variables de entorno (DATABASE_URL, JWT_SECRET, etc.).
  - Configuración de H2 embebida (modo archivo para persistencia entre reinicios; consola habilitada en dev).
  - Sin Docker requerido: la base de datos se crea automáticamente al iniciar.
- **Dependencias**: ninguna
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios
  - `knowledge-base/08_arquitectura_propuesta.md` §Variables de entorno

### [C-02] `core-models-y-migraciones`
- **Estado**: `[x]` completado (2026-11-22, OPSX `2026-11-22-core-models-y-migraciones`)
- **Scope**:
  - Modelos: Clinic, User, Professional, Chair, Service, Patient, Appointment, Block, Payment, Deposit, Attachment, Odontogram.
  - Migraciones numeradas (001 base, 002 agenda, 003 pacientes, 004 pagos).
  - Repositorios genéricos multi-tenant (tenant_id en todas las tablas).
  - Seed data: roles por defecto, servicios base, política de cancelación.
- **Dependencias**: `C-01`
- **Governance**: CRITICO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Entidades principales
  - `knowledge-base/04_modelo_de_datos.md` §Seed data inicial
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones aplicados

---

## FASE 2 — Autenticación y autorización

### [C-03] `auth-y-rbac`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - JWT access + refresh tokens.
  - Endpoints: POST /api/auth/login, /register (alta de clínica), /refresh.
  - RBAC por rol (admin, receptionist, professional, patient) en cada endpoint.
  - Aislamiento por tenant (clinic_id).
  - Rate limiting 5/60s por IP+email en login.
- **Dependencias**: `C-02`
- **Governance**: CRITICO
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md` §RBAC
  - `knowledge-base/05_reglas_de_negocio.md` §Acceso y seguridad
  - `knowledge-base/08_arquitectura_propuesta.md` §Seguridad

---

## FASE 3 — Agenda y turnos

### [C-04] `agenda-backend`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Endpoints CRUD de Appointment, Block, Professional, Chair, Service.
  - Validación anti-solapamiento (RN-AG-01, RN-AG-02, RN-AG-07).
  - Duración automática por servicio (RN-AG-03).
  - Ventanas de atención por profesional y día (RN-AG-04).
  - Sobreturnos con confirmación explícita (RN-AG-06).
  - Estados de turno (RN-AG-08).
  - Tests: CRUD, anti-solapamiento, aislamiento tenant.
- **Dependencias**: `C-03`
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Appointment, Block
  - `knowledge-base/05_reglas_de_negocio.md` §Agenda
  - `knowledge-base/07_flujos_principales.md` §Flujo 2

### [C-07] `frontend-agenda`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Vistas día y semana por profesional y sillón.
  - Filtros por profesional/sillón.
  - Estados de turno con colores.
  - Formulario de creación de turno con validación.
  - Marcado visual de sobreturnos y bloqueos.
- **Dependencias**: `C-04`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-001, US-002, US-003, US-004
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios

---

## FASE 4 — Reserva online y cancelación

### [C-05] `reserva-online-frontend`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Ruta pública `/booking/:slug`.
  - Selección de profesional, servicio, fecha/hora disponible.
  - Validación anti-solapamiento en backend.
  - Confirmación automática (RN-RO-02).
  - Requiere datos mínimos del paciente (RN-RO-05).
  - Link compartible.
- **Dependencias**: `C-04`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-005
  - `knowledge-base/07_flujos_principales.md` §Flujo 1
  - `knowledge-base/05_reglas_de_negocio.md` §Reservas online

### [C-06] `cancelacion-reprogramacion`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Endpoint cancelar/reprogramar con política de anticipación configurable (2-24h).
  - Aviso de pérdida de seña fuera de plazo.
  - Notificación a paciente y profesional.
  - Estados: cancelled.
- **Dependencias**: `C-05`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-006
  - `knowledge-base/07_flujos_principales.md` §Flujo 3
  - `knowledge-base/05_reglas_de_negocio.md` §RN-RO-03, RN-RO-04

---

## FASE 5 — Pacientes y ficha clínica

### [C-08] `pacientes-y-odontograma`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - CRUD de Patient.
  - Ficha, anamnesis, odontograma FDI (dientes permanentes y temporales).
  - Acceso restringido por rol (RN-PA-01).
  - Endpoints: /api/patients, /api/patients/:id/odontogram.
- **Dependencias**: `C-03`
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Patient, Odontogram
  - `knowledge-base/06_funcionalidades.md` §US-007
  - `knowledge-base/05_reglas_de_negocio.md` §Pacientes

### [C-09] `adjuntos-clinicos`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Upload de imágenes/radiografías (Attachment).
  - Almacenamiento local o S3-compatible.
  - Galería en ficha de paciente.
- **Dependencias**: `C-08`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Attachment
  - `knowledge-base/06_funcionalidades.md` §US-007

---

## FASE 6 — Caja y pagos

### [C-10] `caja-y-pagos`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Registro de Payment con método (efectivo, tarjeta, MP, transferencia).
  - Señas vinculadas a turno (Deposit).
  - Saldos pendientes por paciente.
  - Aplicación de seña al pago final.
  - Reportes básicos de caja.
- **Dependencias**: `C-03`
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Payment, Deposit
  - `knowledge-base/06_funcionalidades.md` §US-009
  - `knowledge-base/05_reglas_de_negocio.md` §Pagos
  - `knowledge-base/07_flujos_principales.md` §Flujo 4

### [C-11] `mercadopago-y-senas`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Integración Mercado Pago (adapter pattern) para cobros online/señas.
  - Generación de link de pago para seña.
  - Webhook de confirmación de pago.
  - Actualización de estado de Deposit/Payment.
- **Dependencias**: `C-10`
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §RN-PG-04
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones aplicados

---

## FASE 7 — Notificaciones

### [C-12] `notificaciones-whatsapp-email`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Adapter WhatsApp (Meta Business API) con plantillas aprobadas.
  - Adapter Email (SMTP).
  - Cron de recordatorios (24h/2h antes del turno).
  - Confirmación inmediata de reserva (RN-NO-02).
  - Aviso de cancelación/reprogramación (RN-NO-03).
  - Configurable por clínica.
- **Dependencias**: `C-04`
- **Governance**: ALTO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-008
  - `knowledge-base/05_reglas_de_negocio.md` §Notificaciones
  - `knowledge-base/07_flujos_principales.md` §Flujo 5
  - `knowledge-base/08_arquitectura_propuesta.md` §Variables de entorno

---

## FASE 8 — Reportes y cierre MVP

### [C-13] `reportes-y-exportacion`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - Dashboard básico: ocupación, ingresos, ausentismo.
  - Exportación CSV/Excel.
  - Filtros por rango de fechas y profesional.
- **Dependencias**: `C-10`, `C-04`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` §US-010
  - `knowledge-base/04_modelo_de_datos.md`

### [C-14] `pwa-y-responsive`
- **Estado**: `[ ]` pendiente
- **Scope**:
  - PWA: manifest, service worker, offline mínimo (solo lectura cacheada).
  - Responsive mobile-first.
  - Optimización de performance.
- **Dependencias**: `C-07`, `C-05`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones aplicados
  - `knowledge-base/01_vision_y_objetivos.md`

---

## Tabla resumen

| ID | Nombre | Fase | Governance | Dependencias |
|----|--------|------|------------|--------------|
| C-01 | scaffolding-y-configuracion | 1 | BAJO | — |
| C-02 | core-models-y-migraciones | 1 | CRITICO | C-01 |
| C-03 | auth-y-rbac | 2 | CRITICO | C-02 |
| C-04 | agenda-backend | 3 | ALTO | C-03 |
| C-07 | frontend-agenda | 3 | MEDIO | C-04 |
| C-05 | reserva-online-frontend | 4 | MEDIO | C-04 |
| C-06 | cancelacion-reprogramacion | 4 | MEDIO | C-05 |
| C-08 | pacientes-y-odontograma | 5 | ALTO | C-03 |
| C-09 | adjuntos-clinicos | 5 | MEDIO | C-08 |
| C-10 | caja-y-pagos | 6 | ALTO | C-03 |
| C-11 | mercadopago-y-senas | 6 | ALTO | C-10 |
| C-12 | notificaciones-whatsapp-email | 7 | ALTO | C-04 |
| C-13 | reportes-y-exportacion | 8 | MEDIO | C-10, C-04 |
| C-14 | pwa-y-responsive | 8 | MEDIO | C-07, C-05 |

**Primer item elegible**: `C-02` (core-models-y-migraciones).

> Nota: se excluyen del MVP las funcionalidades diferidas (portal del paciente, Google Calendar, firma electrónica, liquidación OS, facturación ARCA, IA, campañas, multi-sede avanzado, laboratorio dental, app nativa) según Discovery sección D. La normativa AR avanzada se deja para v1.1.
