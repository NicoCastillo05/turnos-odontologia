# Preguntas Abiertas

## Inconsistencias detectadas

### IN-01 — Duración variable por prestación
**Documento A dice**: Discovery lista "duración variable por prestación" como MVP.
**Documento B dice**: Informe Discovery marca "duración variable No evidenciado" en Bilog.
**Impacto**: Diseño de Service.duration_minutes sin referencia de competidores.
**Resolución propuesta**: Implementar duración configurable por prestación.

### IN-02 — Sillones/boxes en agenda
**Documento A dice**: Discovery menciona "profesional y sillón/box".
**Documento B dice**: Informe Discovery marca sillones "No evidenciado" en la mayoría.
**Impacto**: Modelo de datos incluye Chair; validar necesidad real.
**Resolución propuesta**: Mantener Chair como entidad opcional por clínica.

## Preguntas abiertas (priorizadas)

| Prioridad | Pregunta | Bloquea | Decisor |
|---|---|---|---|
| Alta | ¿Soporte de obras sociales en MVP o etapa posterior? | Sprint 1 | Product Owner |
| Alta | ¿Monetización: freemium con límites o prueba gratuita? | Sprint 1 | Product Owner |
| Alta | ¿Multi-tenant SaaS o instancia por clínica? | Arquitectura | Tech Lead |
| Media | ¿App nativa o PWA? | Sprint 2 | Tech Lead |
| Media | ¿Incluir laboratorio dental en roadmap? | Roadmap | Product Owner |
| Media | ¿Política de no-show y señas obligatorias? | Sprint 2 | Product Owner |
| Baja | ¿Integración Google Calendar en v1 o v1.1? | Lanzamiento | Equipo técnico |
| Baja | ¿Firma electrónica de consentimientos en v1? | Lanzamiento | Product Owner |

## Puntos "No evidenciado" del Discovery (sin evidencia pública)

- Anti-solapamiento comprobado en Bilog/Odonto.ar (solo afirmado).
- Duración variable por prestación en competidores (no evidenciado).
- Bloqueos de agenda en competidores (no evidenciado).
- Sobreturnos en competidores (no evidenciado).
- Sillones/boxes en agenda (no evidenciado en la mayoría).
- Confirmación automática de turnos online (varios usan solicitud a aprobar).
- WhatsApp Business API oficial (pocos evidencian integración oficial).
- Modo offline (ninguno evidencia).
- Normativa AR explícita (solo Odonthia la menciona).
- Precios públicos (muchos no publican).