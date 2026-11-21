# Actores y Roles

## Actores del sistema

| Actor | Descripción | Interacción |
|---|---|---|
| Paciente | Cliente del consultorio | Reserva/cancela online, recibe recordatorios |
| Odontólogo/profesional | Prestador de servicios | Agenda, evolución clínica |
| Recepcionista/secretaria | Gestión operativa | Agenda, confirmaciones, caja |
| Administrador/owner | Dueño/gestor de clínica | Reportes, permisos, configuración |
| Laboratorio dental (post) | Seguimiento de trabajos | Etapa posterior |

## RBAC — Matriz de permisos

| Rol | Pacientes | Agenda | Caja | Reportes | Config |
|---|---|---|---|---|---|
| Paciente | Ver propio | Reservar/cancelar propio | Ver propio | — | — |
| Odontólogo | Ver ficha/odontograma | Ver/editar propia | — | Propios | — |
| Recepcionista | CRUD | CRUD todas | CRUD | Básicos | — |
| Administrador | CRUD | CRUD todas | CRUD | Todos | CRUD |

## Rutas públicas

- `/booking/:slug` — reserva online 24/7
- `/login`, `/register` (alta de clínica)
- `/health`