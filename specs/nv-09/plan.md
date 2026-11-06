# plan.md — nv-09 — Registro con correo

1. Mapeo
- `feature/auth/ui/RegisterScreen.kt`
- `feature/auth/ui/RegisterViewModel.kt`
- `feature/auth/domain/RegistrationValidator.kt`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/auth/ui/AuthViewModelsTest.kt`
- `feature/auth/data/AuthRepositoryImplTest.kt`
- `core/common/ValidatorsTest.kt`

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
