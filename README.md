# Turnos Odontología

Backend multi-tenant para la administración de clínicas odontológicas. El backend usa Java 17,
Spring Boot 3, Spring Security, JPA, Flyway y H2.

Proyecto del Trabajo Global Integrador: Discovery → fundación con Active Stack → ciclo OPSX sobre un change.

## Stack

- **Backend:** Java 17 o superior, Spring Boot 3.3.5, Maven, H2 embebida en modo archivo y Flyway para migraciones.
- **Frontend:** React 18 + Vite + TypeScript con tipado estricto.
- **Tests:** JUnit 5 y Spring Boot Test.

## Estructura del repositorio

| Ruta | Contenido |
|---|---|
| `backend/` | API y dominio (`domain/`, `application/`, `infrastructure/`, `api/`) |
| `frontend/` | SPA (`features/`, `shared/`, `pages/`) |
| `knowledge-base/` | Los 10 archivos canónicos (visión, actores, datos, reglas, flujos, etc.) |
| `docs/` | Informe de Discovery |
| `openspec/changes/` | Changes en curso, con `proposal.md`, `design.md`, `specs/` y `tasks.md` |
| `openspec/changes/archive/` | Changes cerrados |
| `openspec/specs/` | Specs oficiales del proyecto |
| `CHANGES.md` | Roadmap de 14 changes, dependencias, gates y estado |
| `AGENTS.md` / `CLAUDE.md` | Reglas del agente |
| `TEAM_DISTRIBUTION.md` | Asignación de changes por integrante |

## Configuración

Copiar `.env.example` a `.env` y completar los valores que se necesiten. El repositorio **no contiene secretos reales**: todos los valores son placeholders o campos vacíos.

## C-04: agenda backend

Todas las rutas internas requieren un access token JWT:

```http
Authorization: Bearer <access-token>
```

El `tenantId` se obtiene del token. No se recibe en los cuerpos de las solicitudes y todas las
búsquedas se limitan a la clínica autenticada.

### Endpoints

| Recurso | Endpoints | Escritura |
|---|---|---|
| Profesionales | `POST /api/professionals`, `GET /api/professionals`, `GET /api/professionals/{id}`, `PUT /api/professionals/{id}`, `DELETE /api/professionals/{id}` | admin |
| Sillones | `POST /api/chairs`, `GET /api/chairs`, `GET /api/chairs/{id}`, `PUT /api/chairs/{id}`, `DELETE /api/chairs/{id}` | admin |
| Prestaciones | `POST /api/services`, `GET /api/services`, `GET /api/services/{id}`, `PUT /api/services/{id}`, `DELETE /api/services/{id}` | admin |
| Bloqueos | `POST /api/blocks`, `GET /api/blocks`, `GET /api/blocks/{id}`, `PUT /api/blocks/{id}`, `DELETE /api/blocks/{id}` | admin, receptionist; professional solo sobre su agenda |
| Turnos | `POST /api/appointments`, `GET /api/appointments`, `GET /api/appointments/{id}`, `PUT /api/appointments/{id}`, `PATCH /api/appointments/{id}/status`, `DELETE /api/appointments/{id}` | admin y receptionist; professional solo sobre su agenda; DELETE solo admin |

El listado de turnos admite `from`, `to`, `professionalId`, `chairId` y `status` como filtros
opcionales. `from` y `to` usan fecha y hora ISO-8601.

### Horario semanal del profesional

El campo `schedule` contiene `windows`, un mapa desde los días ISO en mayúsculas a una lista de
ventanas. Las ventanas de un día no pueden superponerse y se interpretan en la zona horaria de
la clínica.

```json
{
  "userId": 15,
  "specialty": "Ortodoncia",
  "schedule": {
    "windows": {
      "MONDAY": [
        { "start": "09:00:00", "end": "13:00:00" },
        { "start": "14:00:00", "end": "18:00:00" }
      ],
      "TUESDAY": [
        { "start": "09:00:00", "end": "13:00:00" }
      ]
    }
  },
  "active": true
}
```

### Crear y modificar un turno

```json
{
  "patientId": 30,
  "professionalId": 12,
  "chairId": 4,
  "serviceId": 8,
  "startAt": "2026-11-24T10:00:00",
  "allowOverbooking": false,
  "notes": "Control"
}
```

`endAt` se calcula con la duración de la prestación. Los intervalos son semiabiertos
`[startAt, endAt)`, por lo que dos turnos adyacentes están permitidos. Un turno `cancelled` no
bloquea disponibilidad. `allowOverbooking: true` solo permite ignorar conflictos con otros
turnos y registra al usuario autorizante; nunca ignora horarios, bloqueos o recursos inactivos.

Estados aceptados:

```text
scheduled, confirmed, cancelled, no_show, completed
```

Ejemplo de cambio de estado:

```json
{ "status": "confirmed" }
```

### Roles

- `admin`: configura recursos, administra turnos y puede eliminar turnos creados por error.
- `receptionist`: administra bloqueos y turnos, incluido un sobreturno explícito; no configura
  recursos ni elimina físicamente turnos.
- `professional`: consulta y opera únicamente sobre sus propios bloqueos y turnos.
- `patient`: no accede a los endpoints internos de agenda.

Al eliminar profesionales, sillones o prestaciones, los recursos sin referencias se borran. Si ya
están vinculados a turnos o bloqueos, permanecen registrados y se desactivan (`active: false`).

### Errores

Los errores usan un código estable y un mensaje genérico sin información de pacientes ni de
otras clínicas:

```json
{
  "code": "PROFESSIONAL_CONFLICT",
  "message": "El profesional no está disponible"
}
```

Los códigos HTTP principales son `403` para permisos, `404` para recursos no visibles,
`409` para conflictos de disponibilidad y `422` para validaciones de negocio.

## Ejecutar el backend y las pruebas

Este repositorio no incluye Maven Wrapper; se utiliza Maven instalado en el sistema.

```bash
cd backend
mvn spring-boot:run
```

Suite completa:

```bash
cd backend
mvn test
```

Pruebas de C-04:

```bash
cd backend
mvn -Dtest='Agenda*Test,Appointment*Test,ProfessionalScheduleCodecTest' test
```

Validación estricta del cambio OpenSpec, desde la raíz:

```bash
openspec validate agenda-backend --strict
```

## Frontend

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
npm run build      # typecheck + build de producción
```

## Ciclo OPSX

Cada funcionalidad se especifica antes de implementarse: `explore → propose → apply → verify → archive`. Los artefactos de cada change viven en `openspec/changes/` y, al cerrarse, se mueven a `openspec/changes/archive/` junto con la actualización de `openspec/specs/`.

El estado de cada change (pendiente o completado) se consulta en `CHANGES.md`.

## Reglas del proyecto

Ver `AGENTS.md`. Las principales: `tenant_id` en toda consulta, migraciones numeradas para cada cambio de esquema, datos de pacientes siempre ficticios y ningún secreto en el código.

## Video de defensa

[Ver video de defensa (8–12 min)](https://youtu.be/B-5p8ZLb5Ww)
