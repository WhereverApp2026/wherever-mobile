# plan.md — NV-45 — Transiciones entre pantallas y pulido de UI

> Deriva de `specs/nv-45/spec.md`. Respeta constitución §5 (colores, tipografías y tiempos solo desde `core/designsystem`) y §7 (pruebas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/designsystem/Motion.kt`: `WhereverMotion` (duraciones y curvas), `LocalMotionEnabled`, `NavMotion`, `PhaseCrossfade`, `LoadingSwap`, `AnimatedFieldError`
- `core/designsystem/Theme.kt`: provee `LocalMotionEnabled` desde la escala del animador del sistema
- `core/designsystem/Buttons.kt`: `LoadingSwap` en los botones, `color` en `WhereverTextLink`, `CircleIconButton` con borde
- `core/designsystem/Inputs.kt`: `FieldErrorText` animado en `LabeledTextField`
- `core/designsystem/Type.kt` y `res/font/*`: fuentes estáticas por peso; `values-night/themes.xml` apunta a `dm_sans_regular`
- `navigation/SafeNavigation.kt`: `navigateFrom`/`popFrom` (toques) y `navigateOnResult`/`popOnResult` (resultados)
- `navigation/WhereverNavHost.kt`: transiciones por defecto y de resultado; cada navegación pasa por la guarda
- `feature/{profile,settings}/ui/*Screen.kt`: `PhaseCrossfade`; `feature/tastes/ui/TastesScreen.kt`: `FieldErrorText`
- `AndroidManifest.xml`: atrás predictivo

## 2. Guardarraíles
- Ningún archivo fuera de `Motion.kt` fija duraciones de animación.
- Sin lógica de negocio en los Composables: la fase sale del `UiState`.
- Con las animaciones del sistema apagadas, todo cambia de inmediato.
- Sin librerías nuevas: `animation` ya viene con Compose.

## 3. Pruebas
### Unitarias
- `test/.../core/designsystem/MotionTest.kt` (5)
- `test/.../navigation/SafeNavigationTest.kt` (4)

### Integración
- Sin superficie nueva; `integration/*` siguen verdes

### UI
- `androidTest/.../navigation/SafeNavigationUiTest.kt` (3)
- `androidTest/.../core/designsystem/MotionComponentsTest.kt` (3)
- `androidTest/.../core/designsystem/TypographyWeightTest.kt` (2)

### Seguridad
- Sin superficie nueva: `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes

## 4. Secuencia
1. Fuentes estáticas y prueba de pesos en el teléfono.
2. `Motion.kt` y sus pruebas.
3. Botones, campos y fases de pantalla.
4. Guarda de navegación, transiciones del `NavHost` y atrás predictivo.
5. Pulido contra 81_Perfil y 85_Ajustes; compuertas una por una.

## 5. Verificación
- Con JDK 17, una por una: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep
- Pruebas de UI en la CI del PR (emulador) o en el teléfono de Jorge
