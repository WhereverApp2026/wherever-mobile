# 002 — Splash y bienvenida

## T1 — Implementación
- [x] SplashScreen API: se mantiene hasta decidir el destino. `MainViewModel` lee `TokenStore` y Room.
- [x] Con sesión entra de inmediato con la caché y valida detrás con `GET /usuarios/yo`. Si el servidor rechaza el refresh, `SessionEvents` lleva a Login.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
