---
name: 09-ci-matrix
description: Orden de CI-PR
---
# 09-ci-matrix

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

## Cuando usarme
Editar `ci-pr.yml`.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
YAML válido con `yaml.safe_load`; orden respetado.
