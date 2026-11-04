# 008 — Sesión y tokens

## T1 — Implementación
- [x] Cadena `RequestId → AuthHeader → RedactedLogging` + `TokenRefreshAuthenticator`, con lock y cliente aparte para el refresh.
- [x] `DataStore` cifrado con AES-256-GCM de Android Keystore; descifrado fallido equivale a sin sesión.
- [x] Las rutas públicas se marcan con `@PublicEndpoint`.
- [x] El log solo tiene método, ruta sin parámetros, estado e ids; nunca cabeceras ni cuerpos.
- [x] 429 llega como `AppError.RateLimited(retryAfterSeconds)`; cada pantalla hace la cuenta atrás. No se reintenta solo.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
