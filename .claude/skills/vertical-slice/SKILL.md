---
name: vertical-slice
description: Una funcionalidad por PR, de spec a tag
---
# vertical-slice

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- `nv-05+`: spec→datos→dominio→ViewModel→UI→tests→gates→tag `vX.Y.Z-featN`.
- Una funcionalidad por PR; no aplica a `nv-00…nv-04`.

## Cuando usarme
Planear una funcionalidad del producto.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-04`.

## Verify
Todas las casillas de `task.md` con evidencia.
