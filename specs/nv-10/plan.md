# plan.md — nv-10 — Onboarding de gustos

1. Mapeo
- `feature/tastes/ui/TastesScreen.kt`
- `feature/tastes/ui/TastesViewModel.kt`
- `feature/profile/ui/CitySearch.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/profile/ui/ProfileViewModelsTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
