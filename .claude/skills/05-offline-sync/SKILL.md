---
name: 05-offline-sync
description: Room fuente de verdad y WorkManager (nv-05+)
---
# 05-offline-sync

> Rector: `.specify/memory/constitution.md`. Fase: `nv-01` (Room base) / `nv-05+` (sync real).

## What I do
- `nv-01`: Room vacío con esquema exportado y política de migraciones; sin sync real.
- `nv-05+`: trabajos de sincronización con reintentos y WorkManager.
- Room como fuente de verdad: la red actualiza Room y la UI lee de Room.

## Cuando usarme
Datos locales y sincronización.

## No hacer
- `fallbackToDestructiveMigration`.
- Sync real o WorkManager en `nv-01` sin spec.
- Repositorios simulados en pruebas de datos.

## Verify
`MigrationTestHelper` verde; sin migración destructiva; sync real solo en `nv-05+`.
