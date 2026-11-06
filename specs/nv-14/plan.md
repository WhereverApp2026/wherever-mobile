# plan.md — NV-14 — Perfil, edición e insignias

> Deriva de `specs/nv-14/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/profile/ui/ProfileScreen.kt`
- `feature/profile/ui/ProfileViewModel.kt`
- `feature/profile/ui/EditProfileScreen.kt`
- `feature/profile/ui/EditProfileViewModel.kt`
- `feature/profile/data/*`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — perfil, sin red con caché, edición parcial, 409 recarga, 422 bajo el campo
- `test/.../feature/profile/data/ProfileRepositoryImplTest.kt` — If-Match con la versión guardada; campos desconocidos no rompen

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/ProfileIntegrationTest.kt` — perfil de la doc en caché; PATCH 200/409/422 y 401 con refresh
- `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — 409 no toca Room; la UI lee de Room (instrumentada)
- `androidTest/.../feature/profile/data/UserProfileDaoTest.kt` — listas y objetos anidados en Room (instrumentada)

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../security/PlatformSecurityTest.kt` — la base de datos se excluye de respaldos — MASVS STORAGE, PLATFORM
- `test/.../integration/AuthIntegrationTest.kt` — sin datos personales en logs — MASVS PRIVACY

## 4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
- Pendiente: recorrido manual en emulador.
