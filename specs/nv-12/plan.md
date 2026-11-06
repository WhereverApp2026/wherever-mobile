# plan.md — NV-12 — Sesión y tokens

> Deriva de `specs/nv-12/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/network/TokenRefreshAuthenticator.kt`
- `core/network/AuthHeaderInterceptor.kt`
- `core/security/*`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — renueva una vez, guarda el refresh rotado, dos 401 → un refresh, 503 conserva la sesión
- `test/.../core/network/InterceptorsTest.kt` — Bearer solo con JWT
- `test/.../core/security/EncryptedTokensSerializerTest.kt` — cifrado y descifrado de `SessionTokens`

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — 401 → `POST /auth/refresh` (doc) → repite con el token nuevo y guarda el refresh rotado
- `test/.../integration/AuthIntegrationTest.kt` — refresh rechazado limpia la sesión; logout 204 limpia tokens, Room y onboarding

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../core/security/EncryptedTokensSerializerTest.kt` — tokens nunca en texto plano en disco — MASVS STORAGE, CRYPTO
- `test/.../integration/AuthIntegrationTest.kt` — tokens fuera de los logs — MASVS STORAGE
- `test/.../security/PlatformSecurityTest.kt` — sin backup del archivo de sesión — MASVS PLATFORM
- `test/.../security/SourceSecurityTest.kt` — tokens solo en el `TokenStore` cifrado con Keystore — MASVS STORAGE

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
