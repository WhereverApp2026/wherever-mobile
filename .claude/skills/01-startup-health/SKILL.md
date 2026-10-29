---
name: 01-startup-health
description: Ping de conexión con la API y estado de arranque
---
# 01-startup-health

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- Llamada a `/health` y `/ready` con timeout corto.
- Pantalla solo-debug `debug/conexion`.

## Cuando usarme
Verificar conectividad con el backend.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
MockWebServer: 200, 503 y timeout se muestran con su estado.
