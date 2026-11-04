---
name: 04-auth-refresh
description: Flujo completo de refresh (nv-05+); en nv-01 solo interfaz y Authenticator de un reintento
---
# 04-auth-refresh

> Rector: `.specify/memory/constitution.md`. Fase: `nv-01` (interfaz) / `nv-05+` (flujo real).

## What I do
- `nv-01`: interfaz `TokenRefresher`, `Authenticator` con un solo reintento y doble controlado; si falla, sesión limpia.
- `nv-05+`: contrato real de refresh con el backend y flujo de sesión completo.
- Sin tokens en logs ni almacenamiento sin cifrar.

## Cuando usarme
Diseñar autenticación o tocar la sesión.

## No hacer
- Implementar refresh real en `nv-01` sin spec ni contrato.
- Más de un reintento automático.
- Referenciar el endpoint de refresh sin `[planificado nv-05]`.

## Verify
401 → un solo intento; si falla, sesión limpia. Spec aprobada antes de codificar el flujo real.
