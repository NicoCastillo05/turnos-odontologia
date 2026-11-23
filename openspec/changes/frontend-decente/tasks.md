# Tasks

## 1. Infraestructura frontend (auth + HTTP)

- [x] 1.1 Crear `shared/api/http.ts` con fetch wrapper que inyecte `Authorization: Bearer` y reintente una vez tras 401 usando refresh; verificar llamada protegida incluye header.
- [x] 1.2 Crear `features/auth/types.ts` y `features/auth/api.ts` (login, refresh) y `features/auth/AuthContext.tsx` con estado de tokens en localStorage; verificar login exitoso almacena tokens.
- [x] 1.3 Crear `shared/ui/RequireAuth.tsx` que redirija a `/login` sin token; verificar acceso a `/agenda` sin token redirige.

## 2. Routing y páginas base

- [x] 2.1 Actualizar `App.tsx`/`main.tsx` con rutas `/login`, `/agenda` (protegida) y mantener `/booking/:slug`; verificar navegación entre rutas.
- [x] 2.2 Crear `pages/LoginPage.tsx` con formulario email/contraseña y manejo de error; verificar login fallido muestra mensaje.

## 3. Vista de agenda

- [x] 3.1 Crear `features/agenda/types.ts` y `features/agenda/api.ts` para listar/crear/actualizar turnos, profesionales, sillones, servicios; verificar listado por fecha carga datos.
- [x] 3.2 Crear `pages/AgendaPage.tsx` con filtro por fecha y profesional, listado con colores por estado y formulario de creación; verificar creación exitosa agrega turno al listado.
- [x] 3.3 Implementar cambio de estado de turno y reflejo visual; verificar actualización persiste y cambia color.

## 4. Pulido visual y responsive

- [x] 4.1 Reescribir `index.css` con tokens (color, tipografía, espaciado, radios, sombras) y clases base; verificar build Vite sin errores.
- [x] 4.2 Aplicar estilos a `LoginPage`, `AgendaPage` y `BookingPage`; verificar responsive a 390px y 1440px sin desbordamiento.

## 5. Integración y verificación

- [x] 5.1 Ejecutar `tsc --noEmit` y `vite build`; verificar sin errores de tipo ni build.
- [x] 5.2 Verificar flujo end-to-end: login → agenda → crear turno → cambiar estado → logout; verificar redirección a `/login`.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
