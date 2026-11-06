# NV-12 — Sesión y tokens

## T1 — Implementación
- [x] Cadena `RequestId → AuthHeader → RedactedLogging` + `TokenRefreshAuthenticator`, con lock y cliente aparte para el refresh.
- [x] `DataStore` cifrado con AES-256-GCM de Android Keystore; descifrado fallido equivale a sin sesión.
- [x] Las rutas públicas se marcan con `@PublicEndpoint`.
- [x] El log solo tiene método, ruta sin parámetros, estado e ids; nunca cabeceras ni cuerpos.
- [x] 429 llega como `AppError.RateLimited(retryAfterSeconds)`; cada pantalla hace la cuenta atrás. No se reintenta solo.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../core/network/TokenRefreshAuthenticatorTest.kt` — renueva una vez, guarda el refresh rotado, dos 401 → un refresh, 503 conserva la sesión
- [x] Unit: `test/.../core/network/InterceptorsTest.kt` — Bearer solo con JWT
- [x] Unit: `test/.../core/security/EncryptedTokensSerializerTest.kt` — cifrado y descifrado de `SessionTokens`
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — 401 → `POST /auth/refresh` (doc) → repite con el token nuevo y guarda el refresh rotado
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — refresh rechazado limpia la sesión; logout 204 limpia tokens, Room y onboarding
- [x] Seguridad: `test/.../core/security/EncryptedTokensSerializerTest.kt` — tokens nunca en texto plano en disco
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — tokens fuera de los logs
- [x] Seguridad: `test/.../security/PlatformSecurityTest.kt` — sin backup del archivo de sesión
- [x] Seguridad: `test/.../security/SourceSecurityTest.kt` — tokens solo en el `TokenStore` cifrado con Keystore
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
