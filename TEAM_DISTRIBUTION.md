# Distribución de Changes por Equipo

> Asignación de los 14 changes del roadmap (`CHANGES.md`) entre los 5 integrantes del equipo.
> El líder (Nico Castillo) coordina, revisa PRs y mergea a `master`. No implementa changes directamente.
> Cada integrante trabaja en su rama (`feature/C-XX-nombre`) y abre PR hacia `master`.

## Equipo

| Integrante | Rol |
|---|---|
| Nico Castillo | Líder — coordinación, revisión de PRs, merge a master |
| Nico Rodríguez | Implementador |
| Joaquín Conti | Implementador |
| Yunuen Obrador | Implementador |
| Pedro Navajas | Implementador |

## Distribución por olas (waves)

Las olas respetan las dependencias del `CHANGES.md`. No se avanza a la siguiente ola hasta que todas las dependencias estén mergeadas en `master`.

### Wave 0 — Scaffolding (bloqueante)
| Integrante | Change | Depende de |
|---|---|---|
| Nico Rodríguez | C-01 scaffolding-y-configuracion | — |

### Wave 1 — Core models (bloqueante)
| Integrante | Change | Depende de |
|---|---|---|
| Nico Rodríguez | C-02 core-models-y-migraciones | C-01 |

### Wave 2 — Auth (bloqueante)
| Integrante | Change | Depende de |
|---|---|---|
| Nico Rodríguez | C-03 auth-y-rbac | C-02 |

### Wave 3 — Fork (3 en paralelo)
| Integrante | Change | Depende de |
|---|---|---|
| Nico Castillo (o delegado) | C-04 agenda-backend | C-03 |
| Nico Rodríguez | C-08 pacientes-y-odontograma | C-03 |
| Joaquín Conti | C-10 caja-y-pagos | C-03 |

### Wave 4 — Fork grande (5 en paralelo) ⭐
| Integrante | Change | Depende de |
|---|---|---|
| Yunuen Obrador | C-05 reserva-online-frontend | C-04 |
| Nico Rodríguez | C-07 frontend-agenda | C-04 |
| Joaquín Conti | C-09 adjuntos-clinicos | C-08 |
| Pedro Navajas | C-11 mercadopago-y-senas | C-10 |
| Nico Castillo (o delegado) | C-12 notificaciones-whatsapp-email | C-04 |

### Wave 5 — Cierre MVP (3 en paralelo)
| Integrante | Change | Depende de |
|---|---|---|
| Yunuen Obrador | C-06 cancelacion-reprogramacion | C-05 |
| Joaquín Conti | C-13 reportes-y-exportacion | C-10 + C-04 |
| Pedro Navajas | C-14 pwa-y-responsive | C-07 + C-05 |

## Resumen por integrante

| Integrante | Changes | Total |
|---|---|---|
| Nico Rodríguez | C-01, C-02, C-03, C-08, C-07 | 5 |
| Joaquín Conti | C-10, C-09, C-13 | 3 |
| Yunuen Obrador | C-05, C-06 | 2 |
| Pedro Navajas | C-11, C-14 | 2 |
| Nico Castillo (líder) | C-04, C-12 (o delegados) | 2 |

## Reglas de coordinación

1. **Ramas:** cada integrante trabaja en `feature/C-XX-nombre`. El líder revisa y mergea PRs a `master`.
2. **Gates:** no avanzar a la siguiente Wave hasta que todas las dependencias estén mergeadas en `master`.
3. **Leer antes:** cada integrante lee los archivos de `knowledge-base/` indicados en "Leer antes" de su change en `CHANGES.md`.
4. **Commits:** conventional commits (`feat:`, `fix:`), sin co-autoría IA. Cada persona con sus propios commits.
5. **Para la entrega académica (Etapa 6):** solo se necesita un change completo con ciclo OPSX (explore → propose → apply → verify → archive). El recomendado es C-04 (agenda-backend). Los demás changes son para el proyecto real.
6. **Datos de pacientes:** nunca datos reales en el repo. Todos los datos de test deben ser ficticios (regla 9 de AGENTS.md).

## Nota sobre C-01, C-02, C-03 (infraestructura)

Los primeros 3 changes (C-01, C-02, C-03) son infraestructura/pre-requisitos. Se asignan a **Nico Rodríguez** que los implementará en secuencia. El líder (Nico Castillo) no implementa changes directamente, solo coordina y revisa.

Si algún integrante no puede hacer su change, el líder reasigna. La distribución es flexible.