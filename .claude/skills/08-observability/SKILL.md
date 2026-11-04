---
name: 08-observability
description: Logs redactados y request_id por llamada (nv-01)
---
# 08-observability

> Rector: `.specify/memory/constitution.md` §9. Fase: `nv-01`. Agente: `foundation-builder`.

## What I do
- `RedactedLogging` sin tokens, credenciales ni datos personales.
- `request_id` en cada llamada de red, propagado a logs.
- Sin Crashlytics ni analítica en esta etapa `[planificado nv-05+]`.

## Cuando usarme
Tocar `core/network`, interceptores o cualquier log de producción.

## No hacer
- Loguear tokens, cuerpos sensibles o datos personales.
- Añadir analítica o Crashlytics sin spec.

## Verify
Test de redacción de logs y presencia de `request_id`; `grep` de tokens en logcat.
