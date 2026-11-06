# plan.md — NV-15 — Privacidad, mis datos y eliminar cuenta

> Deriva de `specs/nv-15/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/settings/ui/PrivacyScreen.kt`
- `feature/settings/ui/PrivacyViewModel.kt`
- `feature/settings/ui/DeleteAccountScreen.kt`
- `feature/settings/ui/DeleteAccountViewModel.kt`
- `feature/settings/ui/DeletionPendingScreen.kt`
- `feature/settings/ui/DeletionPendingViewModel.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — privacidad parcial, ELIMINAR exacto, eliminar limpia la sesión, cancelar fuera de plazo

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/ProfileIntegrationTest.kt` — privacidad 200, `DELETE /usuarios/yo` 202 y 422, cancelar 409 con respuestas de la doc

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — eliminar limpia la sesión local; el error la conserva — MASVS AUTH, PRIVACY
- `test/.../integration/ProfileIntegrationTest.kt` — confirmación validada (`ELIMINAR`) — MASVS CODE

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
