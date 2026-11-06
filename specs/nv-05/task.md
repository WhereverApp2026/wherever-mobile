# NV-05 — Flujo general

## T1 — Implementación
- [x] Ver `spec.md`, sección "Implementado".

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../navigation/MainViewModelTest.kt` — destino según sesión, eliminación pendiente y onboarding
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — login 200 de la doc → perfil → destino Inicio
- [ ] Integración: `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — sin red Room conserva el perfil; limpiar la sesión deja Room vacío (instrumentada)
- [x] Seguridad: `test/.../security/SourceSecurityTest.kt` — las rutas no llevan tokens ni contraseñas
- [x] Seguridad: `test/.../navigation/MainViewModelTest.kt` — sesión rechazada limpia y lleva a Login
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
