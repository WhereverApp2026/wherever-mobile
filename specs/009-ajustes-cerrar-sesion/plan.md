# plan.md — 009 — Ajustes y cerrar sesión

1. Mapeo
- `feature/settings/ui/SettingsScreen.kt`
- `feature/settings/ui/SettingsViewModel.kt`
- `feature/settings/ui/SessionsScreen.kt`
- `feature/settings/ui/SessionsViewModel.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/settings/ui/SettingsViewModelsTest.kt`
- `feature/auth/data/AuthRepositoryImplTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
