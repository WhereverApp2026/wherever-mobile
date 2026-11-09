# plan.md — NV-05 — Flujo general

> Deriva de `specs/nv-05/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `navigation/MainViewModel.kt`
- `navigation/Routes.kt`
- `navigation/WhereverNavHost.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../navigation/MainViewModelTest.kt` — destino según sesión, eliminación pendiente y onboarding

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — login 200 de la doc → perfil → destino Inicio
- `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — sin red Room conserva el perfil; limpiar la sesión deja Room vacío (instrumentada)

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../security/SourceSecurityTest.kt` — las rutas no llevan tokens ni contraseñas — MASVS PLATFORM
- `test/.../navigation/MainViewModelTest.kt` — sesión rechazada limpia y lleva a Login — MASVS AUTH

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
