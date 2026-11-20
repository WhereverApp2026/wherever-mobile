# Plan — NV-55 — Diseño normativo

1. Tokens en `Dimens.kt` y reemplazo de los 207 `dp` sueltos de 34 archivos (script; los valores fuera de escala van al paso de 4/8 más cercano).
2. La ilustración de Bienvenida pasa a `core/designsystem/Illustrations.kt`.
3. Módulo `:detekt-rules` con `HardcodedDesignValue`, cargado con `detektPlugins`; `detekt` depende de sus pruebas.
4. Componentes únicos en `Feedback.kt` y `Skeletons.kt`; regla de Konsist en `ArchitectureTest`.
5. Roborazzi y Robolectric en pruebas; capturas en `app/src/test/screenshots`.
6. Norma en `docs/diseno.md`.
