# Decisiones y Supuestos

## Decisiones documentadas

### DD-01 — PWA en lugar de app nativa
**Decisión**: Usar PWA mobile-first en v1.
**Contexto**: Presupuesto acotado, equipo pequeño.
**Alternativas**: App nativa iOS/Android.
**Justificación**: Menor costo, mayor alcance, sin instalación.
**Trade-offs**: Sin notificaciones push nativas avanzadas, sin modo offline.

### DD-02 — Precios en pesos argentinos
**Decisión**: Monetizar en ARS.
**Contexto**: Competidores en dólares generan fricción.
**Justificación**: Accesibilidad y adopción local.
**Trade-offs**: Riesgo cambiario.

### DD-03 — Plan gratis o prueba sin tarjeta
**Decisión**: Freemium o prueba sin tarjeta.
**Contexto**: Odonthia y Doctocliq usan modelo gratis.
**Justificación**: Reducir fricción de adopción.
**Trade-offs**: Costo de infraestructura para usuarios gratuitos.

### DD-04 — Obras sociales y ARCA en etapa posterior
**Decisión**: Postergar liquidación OS y facturación ARCA.
**Contexto**: Complejidad y costo.
**Justificación**: Priorizar MVP de agenda/automatización.
**Trade-offs**: Competidores maduros (Bilog, DentalTec) tienen ventaja en OS.

### DD-05 — WhatsApp Business API oficial
**Decisión**: Usar API oficial de Meta con plantillas aprobadas.
**Contexto**: Mercado usa envío manual/packs.
**Justificación**: Diferencial de automatización real.
**Trade-offs**: Costo por mensaje (Meta factura).

## Supuestos inferidos

### SU-01 — Preferencia de reserva online
**Supuesto**: Los pacientes preferirán reservar online.
**Origen**: Discovery, sección 10 (riesgo no probado).
**Riesgo si es falso**: Baja adopción del canal online.
**Cómo validar**: Piloto con clínica real, métricas de uso.

### SU-02 — Multi-tenant SaaS
**Supuesto**: Arquitectura multi-tenant por clínica.
**Origen**: Contexto del proyecto (SaaS web).
**Riesgo si es falso**: Costos de infraestructura.
**Cómo validar**: Estimación de costos por tenant.

### SU-03 — PWA suficiente para móvil
**Supuesto**: PWA cubre la mayoría de los casos móviles.
**Origen**: Discovery, sección 9.
**Riesgo si es falso**: UX inferior a app nativa.
**Cómo validar**: Testing en dispositivos reales.