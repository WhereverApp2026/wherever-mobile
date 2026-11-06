# plan.md — NV-13 — Ajustes y cerrar sesión

> Deriva de `specs/nv-13/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/settings/ui/SettingsScreen.kt`
- `feature/settings/ui/SettingsViewModel.kt`
- `feature/settings/ui/SessionsScreen.kt`
- `feature/settings/ui/SessionsViewModel.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — tema, notificaciones, cerrar sesión, sesiones, 404 al cerrar una, cerrar todas

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — `GET /auth/sesiones` (doc) con 401 y refresh; logout 204
- `test/.../integration/ProfileIntegrationTest.kt` — preferencias 422 sin tocar la caché

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../integration/AuthIntegrationTest.kt` — logout limpia tokens, Room y onboarding — MASVS AUTH, STORAGE
- `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — logout-todos solo limpia si el servidor confirma — MASVS AUTH

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
