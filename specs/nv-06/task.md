# NV-06 — Splash y bienvenida

## T1 — Implementación
- [x] SplashScreen API: se mantiene hasta decidir el destino. `MainViewModel` lee `TokenStore` y Room.
- [x] Con sesión entra de inmediato con la caché y valida detrás con `GET /usuarios/yo`. Si el servidor rechaza el refresh, `SessionEvents` lleva a Login.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../navigation/MainViewModelTest.kt` — sin sesión → Bienvenida; con sesión → Inicio con la caché
- [ ] Integración: `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — arranque sin red lee de Room (instrumentada)
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — refresh rechazado limpia la sesión
- [x] Seguridad: `test/.../navigation/MainViewModelTest.kt` — sesión rechazada por el servidor limpia y lleva a Login
- [x] Seguridad: `test/.../security/PlatformSecurityTest.kt` — sin backup de datos de sesión
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
