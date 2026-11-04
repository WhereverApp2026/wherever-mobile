# plan.md — 010 — Perfil, edición e insignias

1. Mapeo
- `feature/profile/ui/ProfileScreen.kt`
- `feature/profile/ui/ProfileViewModel.kt`
- `feature/profile/ui/EditProfileScreen.kt`
- `feature/profile/ui/EditProfileViewModel.kt`
- `feature/profile/data/*`

2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

3. Pruebas
- `feature/profile/ui/ProfileViewModelsTest.kt`
- `feature/profile/data/ProfileRepositoryImplTest.kt`
- `androidTest/.../UserProfileDaoTest.kt` (Room real; sin correr, requiere emulador)

4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
- `gradlew.bat testDebugUnitTest → verdes`
- `gradlew.bat ktlintCheck lintDebug → sin errores`
- Pendiente: recorrido manual en emulador contra staging.
