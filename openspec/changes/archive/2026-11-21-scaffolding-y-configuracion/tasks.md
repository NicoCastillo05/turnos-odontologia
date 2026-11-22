# Tasks

## 1. Estructura y configuración raíz

- [x] 1.1 Verificar que la raíz del repositorio conserva `knowledge-base/`, `openspec/`, `CHANGES.md` y `.gitignore` intactos, y crear `.env.example` con todas las variables de la KB (`DATABASE_URL`, `JWT_SECRET`, `WHATSAPP_TOKEN`, `WHATSAPP_PHONE_ID`, `MP_ACCESS_TOKEN`, `SMTP_HOST`, `SMTP_USER`, `SMTP_PASS`, `FRONTEND_URL`) sin valores secretos reales; verificar que `.gitignore` ya ignora `.env`, `*.env` y `data/`.

## 2. Backend Spring Boot

- [x] 2.1 Crear `backend/pom.xml` con Spring Boot 3 (Java 17) y los starters `web`, `data-jpa`, `security`, `validation`, `h2` y `test`, y verificar que el archivo declara todas las dependencias y la versión de Java correctas.
- [x] 2.2 Crear `backend/src/main/java/com/turnos/odontologia/TurnosOdontologiaApplication.java` con `@SpringBootApplication` y los paquetes vacíos `domain`, `application`, `infrastructure` y `api` (cada uno con `.gitkeep`), y verificar que la clase principal existe en el package base de la arquitectura.
- [x] 2.3 Crear `backend/src/main/resources/application.yml` con perfil `dev` por defecto, datasource H2 en modo archivo `jdbc:h2:file:./data/turnosdb`, `ddl-auto=update`, consola H2 habilitada, y todos los valores sensibles como placeholders `${VAR:default}`; crear `application-test.yml` con H2 en memoria y consola deshabilitada, y verificar que ningún archivo versionado contiene un secreto real.
- [x] 2.4 Crear `backend/src/test/java/com/turnos/odontologia/TurnosOdontologiaApplicationTests.java` con el test `contextLoads` usando el perfil `test`, y verificar que el test existe y no imprime por `System.out.println`.

## 3. Frontend Vite + React + TypeScript

- [x] 3.1 Crear `frontend/package.json`, `frontend/tsconfig.json`, `frontend/tsconfig.node.json`, `frontend/vite.config.ts`, `frontend/index.html` y `frontend/src/vite-env.d.ts` con `typescript.strict` activado y las dependencias de React + Vite; verificar que los archivos existen y que `tsconfig.json` declara `strict: true`.
- [x] 3.2 Crear `frontend/src/main.tsx`, `frontend/src/App.tsx` (componente en PascalCase) y `frontend/src/index.css` mínimos, más los directorios `features/`, `shared/` y `pages/` con `.gitkeep`; verificar que no aparece la palabra `any` en archivos TypeScript y que la estructura coincide con la arquitectura propuesta.

## 4. Verificación del change

- [x] 4.1 Ejecutar `openspec validate scaffolding-y-configuracion` y verificar que el change pasa la validación sin errores.
- [x] 4.2 Revisar que el estado del change indica planning completo y tareas listas para apply mediante `openspec status --change scaffolding-y-configuracion`, y verificar que `applyRequires` queda satisfecho.

## Workflow follow-up

- Ejecutar `/opsx:apply` para implementar las tareas marcadas.
- Ejecutar `/opsx:archive` para sincronizar la spec `application-bootstrap` y cerrar el change.
- No ejecutar builds (`mvn`/`npm`) ni commits sin pedido explícito, según la regla dura 1 de `AGENTS.md`.
