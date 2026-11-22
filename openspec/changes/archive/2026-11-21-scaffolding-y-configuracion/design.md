# Design

## Context

Ver `proposal.md` para la motivación. El repositorio contiene documentación pero ningún proyecto ejecutable. La arquitectura objetivo está definida en `knowledge-base/08_arquitectura_propuesta.md`: Spring Boot 3 + Java 17 + H2 embebida en modo archivo, React + Vite, capas `domain` / `application` / `infrastructure` / `api`, y secretos exclusivamente por variables de entorno. Las reglas duras de `AGENTS.md` prohiben secretos en código, `System.out.println`, mezclar entidades con DTOs y omitir `tenant_id`; ninguna aplica todavía a código existente porque no lo hay.

## Goals / Non-Goals

**Goals:**
- Dejar un esqueleto backend y frontend que arranquen/compilen con cero servicios externos.
- Fijar el package base `com.turnos.odontologia` y los cuatro directorios de capas antes de que lleguen los siguientes changes.
- Establecer la convención de configuración por entorno (perfiles `dev` / `test`, placeholders y plantilla `.env.example`).

**Non-Goals:**
- No crear entidades, repositorios, migraciones ni endpoints (C-02 en adelante).
- No implementar autenticación, RBAC ni lógica de negocio (C-03 en adelante).
- No generar pantallas ni componentes visuales; el frontend queda en estructura mínima.
- No ejecutar builds ni commits: la regla dura 1 de `AGENTS.md` lo requiere bajo pedido explícito.

## Decisions

- **Maven antes que Gradle.** El stack de la KB admite ambos; Maven es el más usado en el contexto académico del proyecto y produce un `pom.xml` autocontenido sin wrapper binario. Alternativa descartada: Gradle (requiere wrapper JAR o Gradle instalado).
- **Starters mínimos, versiones gestionadas por Spring Boot.** `spring-boot-starter-web`, `-data-jpa`, `-security`, `-validation`, `h2` y `spring-boot-starter-test`. No se agrega Flyway todavía: C-02 introducirá las migraciones numeradas y la regla dura 7 exige que todo cambio de esquema llegue con una. Alternativa descartada: incluir Flyway ahora sin scripts (añade complejidad sin beneficio).
- **H2 en modo archivo con `ddl-auto=update` en dev**, según la KB. La consola se habilita sólo en el perfil `dev` mediante `spring.h2.console.enabled`; el perfil `test` usa H2 en memoria y sin consola. Alternativa descartada: H2 en memoria para todos los entornos (pierde datos entre reinicios, contradice la spec).
- **Configuración por placeholders `${VAR:default}`.** `.env.example` documenta las variables sin valores reales; `.gitignore` ya ignora `.env` y `*.env`. Los secretos (JWT, WhatsApp, MP, SMTP) quedan como placeholders vacíos o valores de desarrollo claramente-no-producción. Alternativa descartada: valores por defecto reales en `application.yml` (violaría la regla dura 3).
- **React + Vite + TypeScript estricto.** `strict: true` en `tsconfig.json`, sin `any` (regla dura 8), componentes en PascalCase y carpetas `features/`, `shared/`, `pages/`. Alternativa descartada: Thymeleaf (la KB lo menciona pero recomienda React para PWA).
- **Clase de arranque vacía y un test de contexto.** Una única `@SpringBootApplication` y un test `contextLoads` verifican que el esqueleto es válido sin anticipar lógica de negocio.
- **Seguridad temporal sólo en dev.** Un `SecurityFilterChain` con `@Profile("dev")` permite el acceso a `/h2-console` con `frameOptions` same-origin y CSRF eximido únicamente en esa ruta; sin él, el starter de Security dejaría la consola detrás de autenticación básica. C-03 lo reemplaza por la configuración RBAC definitiva.

## Risks / Trade-offs

- [H2 en modo archivo puede dejar el archivo abierto si el proceso se corta de forma abrupta] → El driver H2 gestiona el lock; `data/` ya está en `.gitignore`, y la consola habilitada sólo en dev evita acceso externo.
- [Sin Flyway en el scaffolding, `ddl-auto=update` puede generar esquema implícito] → Se acepta sólo en dev; C-02 incorpora migraciones numeradas antes de cualquier entidad de dominio.
- [El frontend queda sin estilos ni rutas reales] → Intencional: C-05 y C-07 construirán las pantallas sobre esta base.
- [El `SecurityFilterChain` de dev permite todo tipo de petición] → Está restringido al perfil `dev` y se elimina en C-03; en `test` y producción rige la configuración por defecto de Spring Security.

## UI Mockups

- Design system revision: no aplica (no existe `design-system/` en el proyecto).
- Decision: no (no UI) — cambio de infraestructura y estructura de directorios, sin pantallas, componentes ni estilos.
- Preview: no aplica.
- Visual verify: no aplica.

## Migration Plan

No hay datos ni esquema previo: no requiere migración. Para revertir, alcanza con eliminar los directorios `backend/`, `frontend/` y el archivo `.env.example` creados por este change.

## Open Questions

Ninguna que afecte specs, diseño o tareas. La elección de Maven sobre Gradle es reversible sin impacto en las specs (la spec describe comportamiento observable, no la herramienta de build).
