# NV-55 — Diseño normativo: tokens, componentes únicos y capturas

> Pedido de Jorge (2026-11-19): que la app se vea profesional, igual a la maqueta de Figma y sin irregularidades.
> Norma: `docs/diseno.md`. Referencia visual: `maqueta/figma-frames/` y `docs/figma-inventory.md`.

## 1. Objetivo
Que todas las pantallas, las hechas y las nuevas, usen solo los tokens y los componentes de `core/designsystem`, y que
un cambio de diseño no se cuele sin que una compuerta lo detecte.

## 2. Alcance
1. Tokens en `core/designsystem/Dimens.kt`: `Spacing` (escala 4/8), `Radius`, `WhereverShapes`, `Strokes`,
   `Elevations` y `Sizes`. Colores (`Color.kt`) y tipografía (`Type.kt`) ya existían y se conservan.
2. Regla de detekt `HardcodedDesignValue` (módulo `:detekt-rules`): falla con `N.dp`, `N.sp` o `Color(0x…)` fuera de
   `core/designsystem` y de las pruebas.
3. Componentes únicos nuevos: `WhereverDialog`, `WhereverTextButton`, `WhereverIconButton`, `WhereverSnackbarHost`,
   `InlineProgress`, `LargeProgress`, `WhereverProgressBar`, `ListSkeleton` y `RouteIllustration`. Una regla de Konsist
   prohíbe importar sus equivalentes de Material fuera de `core/designsystem`.
4. Pruebas de captura con Roborazzi: 8 pantallas principales en claro, oscuro y pantalla chica con letra grande.
5. **Excepción de stack (constitución §3, regla de `nv-00`):** Robolectric 4.17 y Roborazzi 1.77.0, solo en
   `testImplementation`. Licencia Apache 2.0 las dos; mantenidas (Robolectric por Google, Roborazzi con versión
   mensual); no entran en la APK. Motivo: Jorge pidió pruebas de captura con Paparazzi o Roborazzi; Roborazzi corre
   en `testDebugUnitTest`, sin emulador. Versiones con más de dos semanas al 2026-11-19.
6. Fuera: rediseñar. Los valores fuera de escala se ajustan al paso de 4/8 más cercano (hacia arriba en espaciados).

## 3. Contratos
Sin endpoints. API de Compose: los componentes únicos tienen la misma firma que su equivalente de Material para que
cambiar de uno a otro no cambie el comportamiento.

## 4. Modelo de datos
Sin cambios en Room ni en la red.

## 5. Seguridad
Sin datos nuevos. Las capturas usan los datos de ejemplo de las `@Preview` (sin datos personales reales). Las librerías
nuevas son solo de prueba y no se empaquetan.

## 6. Tests
- Unit: `HardcodedDesignValueTest` (3 pruebas de la regla) y `ScreenshotTest` (24 capturas comparadas).
- Data: sin cambios de datos; las suites de datos existentes siguen verdes.
- Security: sin cambios; `ArchitectureTest` agrega "las pantallas usan los componentes únicos del sistema de diseño".
- UI: las pruebas de Compose existentes siguen encontrando `common_loading` en `ListSkeleton`.

## 7. Plan de entrega
Un commit en `main` con tokens, regla, componentes, capturas y norma. Después, una revisión por pantalla contra la
maqueta con capturas del teléfono en `docs/pruebas/` (necesita el teléfono conectado).

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución (excepción de stack documentada en §2.5)
