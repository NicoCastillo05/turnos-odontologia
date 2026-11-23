# Tasks

## 1. Persistencia y tipos del dominio

- [x] 1.1 Crear la migración V005 con auditoría de sobreturnos e índices multi-tenant de disponibilidad, y verificar con una prueba de migración que las columnas, índices y valores por defecto existen.
- [x] 1.2 Actualizar `Appointment` con estado tipado y datos de sobreturno sin exponer entidades en la API, y verificar con pruebas de persistencia los estados válidos y el autorizante opcional.
- [x] 1.3 Implementar el contrato tipado del horario semanal y su conversión JSON, y verificar con pruebas unitarias la normalización, ventanas múltiples, horarios inválidos y superposiciones internas.

## 2. Recursos de agenda

- [x] 2.1 Implementar DTOs y servicios multi-tenant para profesionales, sillones y prestaciones, y verificar CRUD, recursos inactivos, referencias inexistentes y acceso cruzado entre clínicas.
- [x] 2.2 Implementar DTOs y servicio de bloqueos con validación temporal y de pertenencia, y verificar bloqueos por profesional, por sillón, combinados, inválidos y pertenecientes a otra clínica.
- [x] 2.3 Exponer controladores REST para recursos y bloqueos con autorización por rol, y verificar mediante pruebas HTTP los códigos 201, 200, 204, 403, 404 y 422 definidos en la especificación.

## 3. Disponibilidad y programación de turnos

- [x] 3.1 Añadir consultas multi-tenant de solapamiento para turnos con intervalos semiabiertos, exclusión de `cancelled` y exclusión por id durante actualización, y verificar profesional, sillón, adyacencia, cancelados y separación entre clínicas.
- [x] 3.2 Añadir consultas multi-tenant de bloqueos y verificar conflictos por profesional, sillón y ambos recursos usando la misma semántica de intervalos.
- [x] 3.3 Implementar `AppointmentService` para resolver recursos, calcular `endAt`, validar actividad, horario y bloqueos, y verificar el caso feliz, fuera de horario, duración automática y referencias inválidas.
- [x] 3.4 Implementar sobreturnos explícitos y auditables, y verificar autorización de admin/recepcionista, profesional propio, ausencia de confirmación y prohibición de ignorar bloqueos u horarios.
- [x] 3.5 Implementar actualización, cambio de estado y eliminación administrativa de turnos, y verificar la revalidación al cambiar horario o recursos, el conjunto de estados y los permisos de eliminación.

## 4. Contrato HTTP de turnos

- [x] 4.1 Crear DTOs y controlador de `/api/appointments` con creación, consulta individual, listado filtrado, actualización, cambio de estado y eliminación, y verificar cada operación mediante pruebas HTTP.
- [x] 4.2 Implementar respuestas de error estables para validación, conflicto, permisos y recursos ausentes, y verificar que no incluyan datos personales, clínicos ni detalles de otra clínica.
- [x] 4.3 Verificar la matriz de roles y propiedad mediante pruebas HTTP: admin y recepcionista administran la clínica, profesional opera solo sobre lo propio y patient recibe 403.
- [x] 4.4 Verificar aislamiento multi-tenant de extremo a extremo para listados, consultas, escrituras, conflictos y referencias cruzadas.

## 5. Integración y documentación

- [x] 5.1 Documentar en el README los endpoints de agenda, formato del horario semanal, roles y comandos de prueba, y comprobar que los ejemplos coinciden con los DTOs implementados.
- [x] 5.2 Ejecutar la suite completa del backend y verificar que pasan los escenarios de C-04 y las regresiones de C-01 a C-03.
- [x] 5.3 Ejecutar la validación estricta de OpenSpec y corregir cualquier divergencia entre implementación, diseño y especificaciones antes de Verify.

## Workflow follow-up

- Ejecutar Verify con evidencia independiente después de completar todas las tareas.
- Archivar `agenda-backend` únicamente cuando las pruebas y health checks estén en verde.
- Actualizar `CHANGES.md` y la evidencia académica con el resultado archivado.
