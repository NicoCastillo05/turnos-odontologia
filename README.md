# Turnos Odontología

Backend multi-tenant para la administración de clínicas odontológicas. El backend usa Java 17,
Spring Boot 3, Spring Security, JPA, Flyway y H2.

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
