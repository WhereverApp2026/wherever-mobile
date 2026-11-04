---
name: 02-feature-mold
description: Molde de funcionalidad Repository → ViewModel → UiState (nv-02)
---
# 02-feature-mold

> Rector: `.specify/memory/constitution.md`. Fase: `nv-02`. Agente: `foundation-builder` o agente principal.

## What I do
- Flujo: `DTO → Mapper → Entity (Room) → Domain → UiState`.
- `core/common` (`Result`, `PageResult`), `core/data` (`BaseRepository`), `core/ui` (`UiState`, `BaseViewModel`).
- `DemoRepository`/`DemoViewModel` solo en `app/src/test`; sin pantalla ni release.
- `PageResult` con límite máximo 50.

## Cuando usarme
Crear el molde `nv-02` y luego cada feature `nv-05+` sin tocar `core/*`.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-04`.
- Meter lógica de negocio en Composables o demo en `main`.
- Referenciar artefactos futuros sin `[planificado nv-0X]`.

## Verify
Konsist 5/5; Unit 12; `DemoRepository`/`DemoViewModel` fuera de release.
