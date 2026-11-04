---
name: 10-testing-strategy
description: Estrategia de pruebas y umbrales
---
# 10-testing-strategy

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- Unit, Data, Security, UI, Arquitectura. Cobertura ≥45 % provisional hasta la primera medición.
- Room real en memoria + MockWebServer; sin repositorios simulados en pruebas de datos.

## Cuando usarme
Planear pruebas de una spec.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
`check_coverage.sh --min 45` OK.
