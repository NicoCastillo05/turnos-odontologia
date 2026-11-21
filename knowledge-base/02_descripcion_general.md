# Descripción General

## Stack tecnológico (propuesto)

| Capa | Tecnología | Notas |
|---|---|---|
| Frontend | React + Vite (PWA) | Mobile-first, responsive |
| Backend | Java 17+ / Spring Boot 3 | API REST, Spring Web + Data JPA + Security |
| Base de datos | H2 (embebida, modo archivo) | Multi-tenant por clínica; persistencia entre reinicios; migrable a PostgreSQL |
| Auth | JWT (jjwt) + refresh tokens | RBAC por rol |
| Mensajería | WhatsApp Business API (Meta) | Plantillas aprobadas |
| Pagos | Mercado Pago SDK | Cobros/señas |
| Email | SMTP / SendGrid | Recordatorios |
| Hosting | Cloud (Vercel/Railway/Render) | SaaS nube |

## Arquitectura general

SaaS multi-tenant: cada clínica (tenant) aislada lógicamente. Frontend PWA consume API REST. Backend stateless con JWT (Spring Security). Base de datos H2 embebida (modo archivo) con tenant_id en tablas. Integraciones externas vía adaptadores (WhatsApp, Mercado Pago, email).

## Integraciones externas

| Servicio | Propósito | Tipo |
|---|---|---|
| WhatsApp Business API | Recordatorios, confirmaciones, reserva | REST/webhook |
| Mercado Pago | Cobros, señas, links de pago | SDK/REST |
| Email (SMTP) | Notificaciones | SMTP |
| Google Calendar | Sincronización (post-MVP) | OAuth/REST |
| ARCA/AFIP | Facturación electrónica (post-MVP) | Web services |

## API REST (resumen)

- `/auth` — login, refresh, logout
- `/clinics`, `/professionals`, `/chairs` — configuración
- `/patients` — ficha, odontograma, anamnesis
- `/appointments` — CRUD, bloqueos, sobreturnos
- `/services` — prestaciones con duración
- `/payments`, `/deposits` — caja
- `/reports` — ocupación, ingresos, ausentismo
- `/public/booking/:slug` — reserva online 24/7