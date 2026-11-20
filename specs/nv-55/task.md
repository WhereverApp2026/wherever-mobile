# NV-55 — Diseño normativo

## T1 — Tokens y regla
- [x] 1. `Dimens.kt` con Spacing, Radius, WhereverShapes, Strokes, Elevations y Sizes
- [x] 2. Sin `dp`, `sp` ni colores hex fuera de `core/designsystem`
- [x] 3. Regla `HardcodedDesignValue` con pruebas; probada con una violación a propósito

## T2 — Componentes únicos
- [x] 1. Diálogo, botón de texto, botón de ícono, snackbar, progreso y barra
- [x] 2. `ListSkeleton` en las 7 pantallas de lista
- [x] 3. Regla de Konsist

## T3 — Capturas
- [x] 1. `ScreenshotTest`: 8 pantallas × claro, oscuro y chica con letra grande

## T4 — Revisión contra la maqueta
- [x] 1. Capturas del teléfono (Xiaomi, modo oscuro, letra grande) en `docs/pruebas/nv55/telefono_*.png`: Inicio, Perfil (arriba y abajo) y Mis planes, el 2026-11-19
- [x] 2. Bugs encontrados en el teléfono y corregidos: paleta oscura (indicador de barra y selector invisibles, enlaces blancos), enlaces corridos 12 dp, Perfil cortado sobre la barra inferior, Bienvenida en horizontal (captura `01_bienvenida_horizontal`)
- [ ] 3. Comparación lado a lado con Figma del resto de pantallas (solo las 8 de `docs/pruebas/comparacion/`)
