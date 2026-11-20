# Diseño de Wherever Móvil — norma

> Obligatoria para todas las pantallas, las hechas y las nuevas. Spec: `specs/nv-55`.
> Referencia: la maqueta de Figma (`maqueta/figma-frames/`, inventario en `docs/figma-inventory.md`) y la constitución §4.
> Si la maqueta no define algo, se sigue la pantalla más parecida y se anota en `docs/revision-jorge.md`.

## 1. Regla de oro

Mejorar el diseño es acercarlo a la maqueta: alineación, espaciado, estados y transiciones. Colores, tipografía, íconos,
orden de los elementos, textos y flujo **no se cambian** por gusto.

## 2. Prohibido en las pantallas

Fuera de `core/designsystem` no se escribe ningún `N.dp`, `N.sp` ni `Color(0x…)`. La regla de detekt
`HardcodedDesignValue` (módulo `detekt-rules/`) lo bloquea en `./gradlew detekt`, en local y en la CI.

Tampoco se usan directamente los componentes de Material que tienen versión propia (tabla §4): `ArchitectureTest` lo bloquea.

## 3. Tokens

### Color (`Color.kt`, constitución §4)

| Token | Hex | Uso |
|---|---|---|
| `RouteBlue` | `#2455E8` | primario, enlaces, íconos activos |
| `Ink` | `#10243B` | texto; fondo en modo oscuro |
| `Sunset` | `#FF7A3D` | botón principal (uno por pantalla) |
| `Mint` | `#12A37F` | éxito |
| `Gold` | `#F2B531` | Sello de Excelencia, estrellas |
| `Mist` | `#F4F7FC` | fondo claro |
| `InkMuted`, `OutlineSoft`, tintes | — | texto secundario, bordes, fondos de ícono |

| `RouteBlueLight` | `#9BB4FF` | primario en modo oscuro (enlaces, pestaña activa) |
| `InkRaised` | `#274A8C` | contenedor primario en modo oscuro (indicador de la barra inferior) |
| `InkTrack` | `#0A1828` | riel del selector en modo oscuro (la píldora usa `surface`) |

En pantalla se usan los roles del tema (`MaterialTheme.colorScheme.primary`, `onSurfaceVariant`…); el modo oscuro
sale de `Theme.kt`. Un texto o ícono sobre una superficie del tema nunca usa `RouteBlue` fijo: usa
`colorScheme.primary`, que cambia en oscuro. `RouteBlue` fijo solo va sobre blanco fijo o sobre fotos/degradados.
En oscuro, `primaryContainer` y `surfaceVariant` deben ser distintos de `surface` para que se vea la selección.

### Tipografía (`Type.kt`)

Sora para títulos (`headline*`, `title*`) y DM Sans para cuerpo y controles (`body*`, `label*`). Solo estilos de
`MaterialTheme.typography`; los tamaños son en `sp` y escalan con la letra grande del sistema.

### Espaciado (`Spacing`, escala de 4/8 dp)

| Token | dp | Uso típico |
|---|---|---|
| `XS` | 4 | entre ícono y texto pegados, chips |
| `S` | 8 | entre elementos de una fila |
| `M` | 12 | dentro de tarjetas, entre filas de una lista |
| `L` | 16 | relleno de tarjetas, entre secciones cortas |
| `XL` | 20 | margen lateral de la pantalla (`ScreenPadding`) |
| `XXL` | 24 | entre secciones |
| `XXXL` | 32 | separaciones grandes, pie de listas |
| `Huge` / `Giant` | 48 / 64 | estados vacíos e ilustraciones |

Los valores fuera de escala que había (6, 10, 14, 18, 22) se llevaron al paso de 4/8 siguiente.

### Formas y radios (`Radius`, `WhereverShapes`)

`Radius`: XS 4 · S 8 · M 12 · L 16 · XL 20 · XXL 24 · XXXL 32. Formas: `Small` (8), `Medium` (12), `Field` (16, campos
y avisos), `Card` (20, tarjetas y diálogos), `Sheet` (24 arriba), `Pill` (botones, chips, avatares).

### Trazos y elevación (`Strokes`, `Elevations`)

Trazos: `Hairline` 1 (bordes, divisores), `Thin` 2 (líneas de ruta, progreso), `Thick` 4. Elevación: `None` 0, `Card` 2,
`Raised` 6, `Overlay` 8 (aviso flotante). La maqueta usa sombras suaves: nada por encima de `Overlay`.

### Tamaños (`Sizes`)

Área táctil mínima `TouchTarget` 48 · botón `ButtonHeight` 56 · fila `RowMinHeight` 52 · íconos 12/16/20/24/32/40 ·
avatares 40/44/56/88/96 · imágenes `CardImage` 132 y `HeroImage` 240 · ancho máximo del contenido `ContentMaxWidth` 520.

## 4. Componentes únicos (`core/designsystem`)

| Necesidad | Componente | En vez de |
|---|---|---|
| Botón principal / secundario / peligro | `WhereverPrimaryButton`, `WhereverOutlinedButton`, `WhereverDangerButton` | `Button`, `OutlinedButton` |
| Enlace o botón de texto | `WhereverTextLink`, `WhereverTextButton` | `TextButton` |
| Botón de ícono | `WhereverIconButton`, `BackCircleButton`, `CircleIconButton` | `IconButton` |
| Campo de texto | `LabeledTextField` | `TextField`, `OutlinedTextField` |
| Chips y segmentos | `SelectChip`, `ChipGroup`, `SegmentedControl` | `FilterChip` |
| Tarjeta | `SurfaceCard`, `NavRow`, `ToggleRow` | `Card` |
| Encabezado | `TopTitleRow`, `TitleBlock`, `StepHeader` | `TopAppBar` |
| Carga | `ListSkeleton` (listas), `LoadingState` (pantalla), `InlineProgress`, `LargeProgress` | `CircularProgressIndicator` |
| Error y vacío | `ErrorState`, `ErrorBanner`, `InfoBanner` | — |
| Diálogo | `WhereverDialog`, `ConfirmDialog` | `AlertDialog` |
| Aviso breve | `WhereverSnackbarHost` | `SnackbarHost` |
| Progreso | `WhereverProgressBar` | `LinearProgressIndicator` |

## 5. Movimiento

- Navegación: `NavMotion` en `WhereverNavHost` define `enter`, `exit`, `popEnter` y `popExit` para todo el grafo; las
  pantallas de resultado usan su propio par. Ninguna ruta define transiciones sueltas.
- Cambios de estado con `PhaseCrossfade` / `LoadingSwap`; errores de campo con `AnimatedFieldError`.
- Con las animaciones del sistema apagadas (`LocalMotionEnabled`), todo cambia sin animar, incluido el pulso de `ListSkeleton`.
- Al cargar, las listas muestran `ListSkeleton` para que no salte el diseño; Atrás vuelve siempre a la pantalla anterior del grafo.

## 6. Accesibilidad

- Todo lo tocable mide al menos 48 dp (`Sizes.TouchTarget`); los componentes únicos ya lo aplican.
- Ícono con acción → `contentDescription` desde `strings.xml`; ícono decorativo → `null`.
- Contraste: texto sobre fondo con los pares del tema (`onSurface` sobre `surface`, `onPrimary` sobre `primary`).
- Letra grande: solo estilos de tipografía (escalan); nada de alturas fijas en contenedores de texto.

## 7. Cómo se verifica

| Qué | Dónde |
|---|---|
| Sin valores sueltos | `./gradlew detekt` (`HardcodedDesignValue`) |
| Sin componentes duplicados | `ArchitectureTest` en `testDebugUnitTest` |
| El diseño no se rompe | `ScreenshotTest` (Roborazzi) compara 24 capturas con `app/src/test/screenshots`; grabar a propósito con `-Proborazzi.record=true` |
| Igual a la maqueta | captura del teléfono junto a la de Figma en `docs/pruebas/` por pantalla terminada |
