# NV-44 — Presupuesto con barra y cantidad manual

## T1 — Implementación
- [x] `BudgetValidator`: limpieza del texto y errores vacío, fuera de rango ($0 a $50,000) y mínimo mayor que el máximo.
- [x] `BudgetForm`: barra libre con paso de $50 al soltar; campos que mueven la barra; cantidad mayor de $1000 deja la barra al tope sin perder lo escrito.
- [x] `BudgetSection`: barra y campos "Mínimo"/"Máximo" siempre visibles, con prefijo "$" y TalkBack.
- [x] ViewModel: Continuar/Guardar deshabilitado con error; 422 en `presupuesto_min`/`presupuesto_max` bajo el campo; carga desde Ajustes.
- [x] Textos en `strings.xml`.

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (2026-11-12): `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (230), `koverVerifyDebug` (75,44 %), `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep (0 hallazgos).
- [x] `BudgetSectionTest` en el teléfono de Jorge (2311DRK48G, Android 16): 6/6 (2026-11-12).
- [ ] CI del PR en verde.
- [ ] Contra staging: guardar $150 – $2,500 escrito a mano devuelve 200 y se conserva al volver a Gustos.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `BudgetValidatorTest` (8), `BudgetFormTest` (10), `TastesViewModelTest` (5 nuevas), `ProfileViewModelsTest`
- [x] Integración: `TastesBudgetIntegrationTest` (2)
- [x] UI: `BudgetSectionTest` (6, verde en dispositivo)
- [x] Seguridad: sin superficie nueva
- **No hacer**: redondear a $50 lo escrito a mano, recortar en silencio una cantidad mayor de $1000 o validar en el Composable.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
