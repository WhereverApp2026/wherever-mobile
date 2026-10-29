---
name: emulator-hygiene
description: Emulador y pruebas instrumentadas estables
---
# emulator-hygiene

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- Un solo emulador, animaciones desactivadas, espera por idling.

## Cuando usarme
Pruebas UI/migraciones.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
Sin `Thread.sleep`.
