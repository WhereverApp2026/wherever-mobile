---
name: 03-api-vertical-slice
description: Plantilla de funcionalidad vertical (molde nv-02)
---
# 03-api-vertical-slice

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- DTO → Mapper → Repository → ViewModel → UiState.
- Sin pantallas.

## Cuando usarme
Crear el molde y luego cada feature.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
Test de arquitectura Konsist: capas respetadas.
