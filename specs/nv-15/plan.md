# plan.md — nv-15 — Privacidad, mis datos y eliminar cuenta

1. Mapeo
- `feature/settings/ui/PrivacyScreen.kt`
- `feature/settings/ui/PrivacyViewModel.kt`
- `feature/settings/ui/DeleteAccountScreen.kt`
- `feature/settings/ui/DeleteAccountViewModel.kt`
- `feature/settings/ui/DeletionPendingScreen.kt`
- `feature/settings/ui/DeletionPendingViewModel.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/settings/ui/SettingsViewModelsTest.kt`
- `feature/profile/data/ProfileRepositoryImplTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
