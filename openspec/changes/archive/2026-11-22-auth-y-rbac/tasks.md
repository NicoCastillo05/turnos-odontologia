# Tasks — C-03 auth-y-rbac

## 1. Dependencias y configuración

- [x] 1.1 Agregar jjwt, spring-security, bucket4j al pom y verificar build
- [x] 1.2 Reemplazar DevSecurityConfig por SecurityConfig con JWT filter y verificar app arranca

## 2. Autenticación

- [x] 2.1 Implementar JwtService (generar/validar access+refresh) y verificar unit test
- [x] 2.2 Implementar AuthService.login/register/refresh y verificar tests de integración
- [x] 2.3 Crear AuthController con endpoints /login /register /refresh y verificar requests

## 3. Autorización y tenant

- [x] 3.1 Implementar TenantContext + JwtAuthFilter que setea tenant y roles, verificar filtro
- [x] 3.2 Configurar @PreAuthorize por rol en endpoints base y verificar 403
- [x] 3.3 Implementar rate limiting en login y verificar 429

## 4. Verificación

- [x] 4.1 Tests de integración: login/register/refresh/RBAC/tenant/rate-limit y verificar todos pasan

## Workflow follow-up

- Archive the change after review.