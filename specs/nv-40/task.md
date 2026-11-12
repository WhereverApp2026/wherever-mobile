# NV-40 — Conexión con la API en wifi y datos móviles

## T1 — Implementación
- [x] `AppError.SecureConnection` y `AppError.InvalidResponse`, con su texto.
- [x] `IOException.toAppError()` con causas y suprimidas; "Sin conexión" solo para fallas de red reales.
- [x] Log `FAILED <clases>` en `RedactedLoggingInterceptor`.
- [x] Timeouts connect 15 s, read/write 30 s.
- [x] `release` con la URL de staging por defecto.

## T2 — Verificación
- [x] `curl` a `/health`, `/ready` y `/api/v1/ping` desde la PC: 200 los tres (2026-11-11 13:23 CST).
- [ ] Versión instalada en el teléfono (`dumpsys package`) y excepción real en Logcat.
- [ ] Prueba manual con datos móviles: registro, login, cerrar sesión, eliminar cuenta.
- [ ] Prueba manual con wifi: lo mismo.
- [ ] Gates: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`.

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../core/network/SafeApiCallTest.kt`
- [x] Integración: `test/.../integration/ConnectionIntegrationTest.kt`
- [x] Seguridad: `test/.../security/ConnectionSecurityTest.kt`
- **No hacer**: usar el servidor de staging en las pruebas automáticas.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
