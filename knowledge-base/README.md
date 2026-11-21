# Turnos Odontología — Base de Conocimiento

Base de conocimiento generada a partir de los documentos de discovery del proyecto.

## Índice de Archivos

| Archivo | Contenido |
|---|---|
| [01_vision_y_objetivos.md](01_vision_y_objetivos.md) | Propósito, actores, alcance MVP, fuera de alcance |
| [02_descripcion_general.md](02_descripcion_general.md) | Stack, arquitectura, integraciones, API |
| [03_actores_y_roles.md](03_actores_y_roles.md) | Actores, RBAC, rutas públicas |
| [04_modelo_de_datos.md](04_modelo_de_datos.md) | Dominios, entidades, ERD, seed data |
| [05_reglas_de_negocio.md](05_reglas_de_negocio.md) | Reglas por dominio (RN-XX-NN) |
| [06_funcionalidades.md](06_funcionalidades.md) | Historias de usuario por épica |
| [07_flujos_principales.md](07_flujos_principales.md) | Flujos extremo a extremo |
| [08_arquitectura_propuesta.md](08_arquitectura_propuesta.md) | Patrones, estructura, seguridad, env vars |
| [09_decisiones_y_supuestos.md](09_decisiones_y_supuestos.md) | Decisiones y supuestos |
| [10_preguntas_abiertas.md](10_preguntas_abiertas.md) | Inconsistencias y preguntas abiertas |

## Quick Start para Desarrolladores

1. Entender el dominio → [01](01_vision_y_objetivos.md), [03](03_actores_y_roles.md)
2. Entender los datos → [04](04_modelo_de_datos.md)
3. Entender las reglas → [05](05_reglas_de_negocio.md)
4. Entender la arquitectura → [02](02_descripcion_general.md), [08](08_arquitectura_propuesta.md)
5. Implementar → [07](07_flujos_principales.md), [06](06_funcionalidades.md)
6. Antes de codificar → [10](10_preguntas_abiertas.md)

## Resumen Ejecutivo

Sistema SaaS web de gestión de turnos odontológicos para el mercado argentino, con foco en agenda inteligente (anti-solapamiento, duración variable, bloqueos, sobreturnos controlados), reserva online 24/7 con confirmación automática, recordatorios WhatsApp automáticos, ficha clínica con odontograma FDI, caja con Mercado Pago y multi-profesional con roles. Precios en pesos, PWA mobile-first, plan gratis/prueba sin tarjeta. Obras sociales, ARCA e IA quedan para etapas posteriores.