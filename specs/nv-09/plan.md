# plan.md — NV-09 — Registro con correo

> Deriva de `specs/nv-09/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/auth/ui/RegisterScreen.kt`
- `feature/auth/ui/RegisterViewModel.kt`
- `feature/auth/domain/RegistrationValidator.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` — validación en vivo, términos, correo disponible, fecha ISO, 409 y 422 por campo
- `test/.../core/common/ValidatorsTest.kt` — nombre, edad ≥ 18 y contraseña

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — `POST /usuarios` 201 de la doc deja sesión; 409 llega como conflicto
- `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — registro manda términos y su versión

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../core/common/ValidatorsTest.kt` — entradas validadas antes de enviar — MASVS CODE
- `test/.../integration/AuthIntegrationTest.kt` — la contraseña no aparece en logs — MASVS STORAGE
- `test/.../core/security/EncryptedTokensSerializerTest.kt` — los tokens de la sesión nueva se guardan cifrados — MASVS STORAGE

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
