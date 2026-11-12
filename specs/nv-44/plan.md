# plan.md — NV-44 — Presupuesto con barra y cantidad manual

> Deriva de `specs/nv-44/spec.md`. Respeta constitución §5 (sin lógica de negocio en Composables, textos en `strings.xml`) y §7 (pruebas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/tastes/domain/BudgetValidator.kt`: limpieza del texto y errores `EMPTY`, `OUT_OF_RANGE`, `MIN_GREATER`
- `feature/tastes/ui/BudgetForm.kt`: estado Kotlin puro de la barra y los campos (paso de $50 al soltar, tope visual de $1000) y `BudgetActions`
- `feature/tastes/ui/BudgetSection.kt`: `RangeSlider` y los dos campos con prefijo "$"
- `feature/tastes/ui/TastesViewModel.kt`: `budget: BudgetForm`, `budgetMinError`/`budgetMaxError` del 422, Continuar deshabilitado con error
- `feature/tastes/ui/TastesScreen.kt`: usa `BudgetSection`; se quita el slider privado
- `core/designsystem/Inputs.kt`: `LabeledTextField` con `prefix` y etiqueta de accesibilidad
- `res/values/strings.xml`: `tastes_budget_*`

## 2. Guardarraíles
- La validación vive en `BudgetValidator` y el ViewModel, no en el Composable.
- Textos y prefijo de moneda solo en `strings.xml`.
- Presupuesto fuera de los logs; sin cambios en Room ni en la API.

## 3. Pruebas
### Unitarias
- `test/.../feature/tastes/domain/BudgetValidatorTest.kt` (8)
- `test/.../feature/tastes/ui/BudgetFormTest.kt` (10)
- `test/.../feature/tastes/ui/TastesViewModelTest.kt` (5 nuevas)
- `test/.../feature/profile/ui/ProfileViewModelsTest.kt`: ajustada a la nueva API de presupuesto

### Integración
- `test/.../integration/TastesBudgetIntegrationTest.kt` (2): 200 con los enteros escritos y 422 en `presupuesto_max`

### UI
- `androidTest/.../feature/tastes/ui/BudgetSectionTest.kt` (6)

### Seguridad
- Sin superficie nueva: `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes

## 4. Secuencia
1. `BudgetValidator` y sus pruebas.
2. `BudgetForm` y sus pruebas.
3. ViewModel, `BudgetSection` y pantalla.
4. Integración, UI y compuertas una por una.

## 5. Verificación
- Con JDK 17, una por una: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep
- `BudgetSectionTest` en dispositivo físico (`connectedDebugAndroidTest` filtrado)
