# plan.md — NV-08 — Recuperar contraseña

> Deriva de `specs/nv-08/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/auth/ui/RecoverScreen.kt`
- `feature/auth/ui/RecoverViewModel.kt`
- `feature/auth/ui/CheckEmailScreen.kt`
- `feature/auth/ui/CheckEmailViewModel.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` — recuperar tras 202, 429 con `Retry-After`, cuenta atrás de 60 s y reenviar

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — `POST /auth/recuperar` 202 sin cuerpo y 429 con `Retry-After`

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` — el mensaje no revela si la cuenta existe — MASVS AUTH, PRIVACY
- `test/.../integration/AuthIntegrationTest.kt` — el correo no aparece en logs — MASVS STORAGE
- `test/.../core/common/ValidatorsTest.kt` — correo validado antes de llamar — MASVS CODE

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
