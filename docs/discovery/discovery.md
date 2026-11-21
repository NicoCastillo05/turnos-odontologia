# Discovery — Turnos Odontología

**Fecha**: 2026-11-21
**Fuentes investigadas**: 17+ sistemas odontológicos (ver `docs/discovery/informe-discovery.md`, sección A)
**Nota**: esta corrida se ejecutó en contexto de subagente; las respuestas del checklist se infirieron del contexto del proyecto (sistema de gestión de turnos odontológicos para el mercado argentino) y de la investigación de mercado. Los puntos se validaron con el usuario y se consideran definitivos.

## 1. Problema que resuelve

Los consultorios y clínicas odontológicas argentinas coordinan turnos por WhatsApp, teléfono y papel, lo que genera dobles reservas, ausentismo (no-shows), tiempo perdido confirmando manualmente cada turno y pérdida de pacientes por fricción en la reserva. A la vez, la mayoría de los sistemas existentes o bien son caros en dólares, o exigen instalación local, o están orientados a liquidación de obras sociales y dejan de lado la experiencia del paciente y la automatización moderna (WhatsApp, recordatorios, reserva 24/7). Oportunidad: un producto competitivo con UX moderna, precios en pesos, automatización real y foco en la experiencia del paciente, sin descuidar agenda y administración.

## 2. Usuarios / roles

- **Paciente**: reserva, cancela/reprograma y consulta sus turnos; recibe recordatorios.
- **Odontólogo/profesional**: ve su agenda, atiende, registra evolución clínica.
- **Recepcionista/secretaria**: gestiona agenda, confirma turnos, caja.
- **Administrador/owner de clínica**: reportes, permisos, configuración, multi-sede.
- **(Opcional) Laboratorio dental**: seguimiento de trabajos (etapa posterior).

## 3. Casos de uso

1. Como paciente, quiero reservar un turno online 24/7 eligiendo profesional y horario para no tener que llamar ni escribir por WhatsApp.
2. Como recepcionista, quiero ver la agenda del día/semana de todos los profesionales y sillones para evitar solapamientos.
3. Como odontólogo, quiero que el sistema envíe recordatorios automáticos por WhatsApp para reducir ausencias.
4. Como administrador, quiero registrar cobros, emitir comprobantes y ver reportes de ocupación e ingresos.
5. Como paciente, quiero cancelar o reprogramar mi turno con anticipación mínima configurable.

## 4. Competidores / soluciones existentes

Ver `docs/discovery/informe-discovery.md` sección A (17 sistemas). Resumen: mercado argentino maduro en gestión odontológica (Bilog, DentalTec, DentalCore, DentalSoft, Odonto.ar, Odonthia, Dentia, DentalHub, Dentiqa, Denteo, Órbita), con fuerte foco en obras sociales/ARCA. Regionales (Dentalink CL, AgendaPro CL/MX, Doctocliq PE, Dentidesk CL) e internacionales (Clinic Cloud ES, Dendoo ES, Dentrix US, Open Dental US) como referencia. Diferenciadores reales del mercado: validación de OS en tiempo real (DentalTec), IA en WhatsApp 24/7 (Dentia, Dentiqa, Bilog iAngela), gestión gratis (Odonthia), precio en pesos (DentalSoft, Dentia, DentalHub, Dentiqa).

## 5. Funcionalidades necesarias (MVP)

- Agenda diaria/semanal por profesional y sillón/box, con duración variable por prestación y prevención de solapamientos.
- Reserva online 24/7 con link compartible (web/WhatsApp/redes), confirmación/cancelación/reprogramación.
- Ficha de paciente + odontograma + anamnesis básica.
- Recordatorios automáticos por WhatsApp (y email) configurables.
- Caja: registro de cobros, señas, saldos, Mercado Pago.
- Multi-profesional y roles/permisos básicos.
- Reportes básicos (ocupación, ingresos, ausentismo).
- Exportación de datos.

## 6. Funcionalidades opcionales (post-MVP)

- Liquidación de obras sociales/prepagas y validación en tiempo real.
- Facturación electrónica ARCA.
- IA (chatbot WhatsApp 24/7, dictado, análisis de radiografías).
- Campañas de recaptación y marketing.
- Portal del paciente.
- Multi-sede avanzado y laboratorio dental.
- Integración Google Calendar, firma digital, radiología.

## 7. Reglas de negocio

- No se pueden solapar dos turnos del mismo profesional/sillón.
- Duración del turno depende de la prestación seleccionada.
- Cancelación con anticipación mínima configurable (ej. 2-24 hs); fuera de plazo puede aplicar pérdida de seña.
- Horarios de atención configurables por profesional y por día.
- Sobreturnos permitidos solo con confirmación explícita.
- Bloqueos de agenda (vacaciones, capacitaciones) no asignables.
- Datos de salud sensibles: acceso restringido por rol (Ley 25.326).

## 8. Integraciones

- WhatsApp Business API (recordatorios, confirmaciones, reserva).
- Mercado Pago (cobros/señas).
- Email (recordatorios, notificaciones).
- Google Calendar (sincronización, etapa posterior).
- ARCA/AFIP (facturación, etapa posterior).
- Exportación CSV/Excel.

## 9. Restricciones

- Mercado argentino: precios en ARS, idioma español, normativa local (Ley 26.529 historia clínica, Ley 25.326 datos personales, Ley 25.506 firma digital).
- Presupuesto acotado (proyecto académico/universitario, equipo pequeño): priorizar SaaS nube, sin instalación local.
- Debe funcionar bien en celular (mayoría reserva desde móvil).
- Sin dependencias de servicios pagos obligatorios para el MVP (WhatsApp vía API tiene costo; evaluar).
- Internet requerido (sin modo offline en v1).

## 10. Riesgos

- Supuesto sin probar: que los pacientes prefieran reservar online en vez de seguir por WhatsApp por costumbre.
- Riesgo: costo y complejidad de WhatsApp Business API (Meta factura por mensaje) puede erosionar margen.
- Riesgo: competencia consolidada con fuerte foco en obras sociales; diferenciarse solo por UX puede no bastar.
- Riesgo: cumplimiento normativo (Ley 26.529 inalterabilidad/conservación 10 años) si no se diseña desde el inicio.
- Riesgo: adopción por profesionales acostumbrados a papel/WhatsApp.

## 11. Preguntas abiertas

- ¿Soporte de obras sociales en MVP o etapa posterior? (recomendación: etapa posterior, salvo tarifario básico)
- ¿App nativa o PWA? (recomendación: PWA primero)
- ¿Monetización: freemium con límites o prueba gratuita? (competidores usan ambos)
- ¿Multi-tenant SaaS o instancia por clínica?
- ¿Incluir laboratorio dental en roadmap o descartar?
- ¿Política de no-show y señas obligatorias?

## Próximos pasos

- Pasar a `kb-creator` para armar la Knowledge Base.
- Usar `informe-discovery.md` (sección D) como base del MVP.
