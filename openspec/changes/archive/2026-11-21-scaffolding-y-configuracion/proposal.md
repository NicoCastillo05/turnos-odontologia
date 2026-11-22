# Proposal

## Why

El repositorio hoy contiene sólo documentación (base de conocimiento, roadmap y reglas) y todavía no existe código ejecutable. Sin una base común de directorios, dependencias y configuración, los siguientes 13 changes no pueden empezar ni verificarse de forma consistente.

## What Changes

- Crear la estructura de directorios del backend (`domain`, `application`, `infrastructure`, `api`) y del frontend (`features`, `shared`, `pages`) según `knowledge-base/08_arquitectura_propuesta.md`.
- Agregar el proyecto Maven de Spring Boot 3 / Java 17 con los starters mínimos (Web, Data JPA, Security, Validation, H2, Test) y la clase principal de arranque.
- Agregar el proyecto Vite + React + TypeScript con tipado estricto y los directorios de features compartidas y páginas.
- Externalizar toda la configuración (URL de H2, JWT, WhatsApp, Mercado Pago, SMTP, URL del frontend) mediante plantilla `.env.example` y placeholders en `application.yml`, sin valores secretos reales.
- Configurar H2 en modo archivo (`jdbc:h2:file:./data/turnosdb`) con consola habilitada sólo en desarrollo; la base se crea automáticamente al iniciar, sin Docker ni servicios externos.

## Capabilities

### New Capabilities
- `application-bootstrap`: Arranque local de la aplicación sin infraestructura externa, con persistencia H2 en modo archivo, consola de desarrollo y configuración sensible externalizada.

### Modified Capabilities
(ninguna — no hay specs previas en el proyecto)

## Impact

- Nuevos directorios `backend/` y `frontend/` en la raíz del repositorio.
- Nuevo archivo `.env.example` en la raíz (plantilla sin secretos reales).
- `openspec/specs/application-bootstrap/spec.md` se crea al archivar este change.
- No se modifica ninguna API, dato de paciente ni esquema de base de datos.
