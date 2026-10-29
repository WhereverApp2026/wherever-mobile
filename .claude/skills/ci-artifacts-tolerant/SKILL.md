---
name: ci-artifacts-tolerant
description: Artefactos de CI tolerantes a fallo
---
# ci-artifacts-tolerant

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- APK debug, reportes Kover/detekt/Lint subidos aunque un paso informativo falle.

## Cuando usarme
Editar CI.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
`if: always()` en subida de artefactos.
