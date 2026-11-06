# plan.md — NV-07 — Iniciar sesión

> Deriva de `specs/nv-07/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/auth/ui/LoginScreen.kt`
- `feature/auth/ui/LoginViewModel.kt`
- `feature/auth/data/AuthRepositoryImpl.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` — `LoginViewModel` con 200, 401, 422, 423, 429 y sin red
- `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — login guarda tokens y manda el dispositivo; el fallo no guarda
- `test/.../core/common/ValidatorsTest.kt` — correo válido antes de enviar

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — login 200, 401, 422 (campos), 423 y 429 (`Retry-After`) con las respuestas de la doc

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../integration/AuthIntegrationTest.kt` — 401 de login no dispara refresh ni guarda tokens — MASVS AUTH
- `test/.../integration/AuthIntegrationTest.kt` — la contraseña y el correo no aparecen en logs — MASVS STORAGE
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` — no envía con correo inválido; borra la contraseña tras entrar — MASVS AUTH

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
