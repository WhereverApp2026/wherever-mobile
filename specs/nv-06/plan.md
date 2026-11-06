# plan.md — NV-06 — Splash y bienvenida

> Deriva de `specs/nv-06/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/auth/ui/WelcomeScreen.kt`
- `navigation/MainViewModel.kt`
- `MainActivity.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../navigation/MainViewModelTest.kt` — sin sesión → Bienvenida; con sesión → Inicio con la caché

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — arranque sin red lee de Room (instrumentada)
- `test/.../integration/AuthIntegrationTest.kt` — refresh rechazado limpia la sesión

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../navigation/MainViewModelTest.kt` — sesión rechazada por el servidor limpia y lleva a Login — MASVS AUTH
- `test/.../security/PlatformSecurityTest.kt` — sin backup de datos de sesión — MASVS STORAGE, PLATFORM

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
