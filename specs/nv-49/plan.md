# plan.md — NV-49 — Autenticación en el teléfono

> Deriva de `specs/nv-49/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/designsystem/Layouts.kt`: `FormScreen` con `BoxWithConstraints` y `COMPACT_FORM_HEIGHT`; `rememberFormSubmit`
- `core/designsystem/Inputs.kt`: `keyboardActions` en `LabeledTextField`
- `core/network/RetryingDns.kt` y `core/di/NetworkModule.kt` (`baseConfig().dns(...)`)
- `core/common/Validators.kt`: `withoutSpaces()`
- `feature/auth/ui/{Login,Register,Recover}Screen.kt` y sus ViewModels
- `app/build.gradle.kts`: versión 2 (0.1.1)

## 2. Guardarraíles
- La cadena de interceptores no cambia (constitución §4).
- Sin pruebas desactivadas; las de UI existentes siguen igual.

## 3. Pruebas
### Unitarias
- `RetryingDnsTest` (3), `ValidatorsTest` (+1)

### Integración
- Sin superficie nueva

### UI
- `FormScreenCompactTest` (2)

### Seguridad
- Sin superficie nueva

## 4. Secuencia
1. Reproducir en el teléfono con logcat.
2. Arreglos y pruebas.
3. Reinstalar y confirmar en el teléfono; compuertas; PR.

## 5. Riesgos
- MIUI no gira la pantalla por `adb`: el caso horizontal se cubre con `FormScreenCompactTest` y con el teclado abierto en vertical, que también deja menos de 520 dp.
