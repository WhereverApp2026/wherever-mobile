# NV-08 — Recuperar contraseña

## T1 — Implementación
- [x] `03_Recuperar` y `04_RevisaCorreo`, con el aviso "Si el correo existe, te llegará un enlace".
- [x] `CheckEmailRoute` tiene modo: `RECOVERY` reenvía con `POST /auth/recuperar` y arranca con 60 s; `VERIFICATION` reenvía con `POST /auth/reenviar-verificacion` y se abre desde el aviso de Perfil.
- [x] 429 usa `Retry-After` para la cuenta atrás. El texto dice 1 hora (D02).

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/auth/ui/AuthViewModelsTest.kt` — recuperar tras 202, 429 con `Retry-After`, cuenta atrás de 60 s y reenviar
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — `POST /auth/recuperar` 202 sin cuerpo y 429 con `Retry-After`
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — el 202 de la doc no trae datos: la app no puede distinguir si la cuenta existe
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — el correo no aparece en logs
- [x] Seguridad: `test/.../core/common/ValidatorsTest.kt` — correo validado antes de llamar
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06