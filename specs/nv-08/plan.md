# plan.md — nv-08 — Recuperar contraseña

1. Mapeo
- `feature/auth/ui/RecoverScreen.kt`
- `feature/auth/ui/RecoverViewModel.kt`
- `feature/auth/ui/CheckEmailScreen.kt`
- `feature/auth/ui/CheckEmailViewModel.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/auth/ui/AuthViewModelsTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
