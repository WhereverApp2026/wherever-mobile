---
name: 01-foundation
description: Crea el proyecto base Android sin pantallas del producto
---
# 01-foundation

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- Gradle Kotlin DSL, catálogo de versiones, módulo `app`, paquetes `core/*`.
- Hilt, Navigation, tema Wherever, `strings.xml`.
- `detekt`, `ktlint`, `.editorconfig`, `LocalPropertiesExampleTest` y scripts de gobernanza.

## Cuando usarme
Ejecutar `specs/nv-01`.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
`./gradlew assembleDebug` OK y la app abre la pantalla `home` vacía.
