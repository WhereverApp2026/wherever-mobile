# plan.md — NV-04 — Arquitectura de Wherever Móvil

> Deriva de `specs/nv-04/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/network/*`
- `core/security/*`
- `core/database/*`
- `core/common/*`
- `core/di/*`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../core/network/SafeApiCallTest.kt` — cada `codigo` de la API → `AppError`, `Retry-After`, cuerpo ilegible
- `test/.../core/network/InterceptorsTest.kt` — Bearer solo en rutas con JWT y `request_id` en cada petición
- `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — 401 → un solo refresh, refresh rotado, dos 401 simultáneos
- `test/.../core/common/ValidatorsTest.kt` — validaciones comunes
- `test/.../arch/ArchitectureTest.kt` — Konsist: `ui → domain ← data`, sufijos de nombre (§ nombres), ViewModels con Hilt y estado mutable privado, `Dispatchers` solo en `core/common/DefaultDispatcherProvider`

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/AuthIntegrationTest.kt` — cadena de red real con respuestas de la doc: 401 con refresh y refresh rechazado
- `test/.../integration/ProfileIntegrationTest.kt` — `If-Match`, 409, 422 y 401 con refresh en `PATCH /usuarios/yo`
- `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — Room real + MockWebServer por HTTPS local (instrumentada)
- `androidTest/.../feature/profile/data/UserProfileDaoTest.kt` — DAO con Room real (instrumentada)

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../core/security/EncryptedTokensSerializerTest.kt` — el archivo cifrado no contiene los tokens en claro — MASVS STORAGE, CRYPTO
- `test/.../security/PlatformSecurityTest.kt` — sin backup, solo HTTPS, solo CA del sistema, componentes no exportados, permisos mínimos — MASVS NETWORK, PLATFORM
- `test/.../security/SourceSecurityTest.kt` — sin secretos ni URLs fijas; tokens solo cifrados; logs solo en el interceptor redactado — MASVS CODE, STORAGE
- `test/.../integration/AuthIntegrationTest.kt` — logs de red sin tokens, contraseñas ni correos — MASVS STORAGE
- `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — refresh rechazado limpia la sesión — MASVS AUTH

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
