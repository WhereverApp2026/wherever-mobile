---
name: 05-offline-sync
description: Room fuente de verdad y WorkManager
---
# 05-offline-sync

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- Esquema exportado, migraciones explícitas.
- Trabajos de sincronización con reintentos.

## Cuando usarme
Datos locales y sync.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
`MigrationTestHelper` verde; sin `fallbackToDestructiveMigration`.
