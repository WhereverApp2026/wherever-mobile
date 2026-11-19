# NV-08 — Recuperar contraseña

## T1 — Implementación
- [x] `03_Recuperar` y `04_RevisaCorreo`, con el aviso "Si el correo existe, te llegará un enlace".
- [x] `CheckEmailRoute` tiene modo: `RECOVERY` reenvía con `POST /auth/recuperar` y arranca con 60 s; `VERIFICATION` reenvía con `POST /auth/reenviar-verificacion` y se abre desde el aviso de Perfil.
- [x] 429 usa `Retry-After` para la cuenta atrás. El texto dice 1 hora (D02).
- [x] `NuevaContrasena` (2026-11-19): `NewPasswordRoute` con `POST /auth/restablecer {token, password_nuevo, dispositivo}`; guarda la sesión y entra. Enlace vencido o usado (400/410/422) → "Pedir otro enlace".
- [x] Enlaces del correo (D11): `wherever://restablecer#token=…` y `wherever://verificar#token=…` en el Manifest; el enlace `https://{APP_LINK_BASE}/…#token=…` se pega en Revisa tu correo. El token vive en `PendingEmailLink` (memoria), nunca en la ruta.
- [x] Verificar correo desde el enlace: `VerifyEmailRoute` con `POST /auth/verificar-correo`.
- [x] Registro: "Siguiente" en el correo pasa a la contraseña (antes el teclado mostraba ✓ y no avanzaba).

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [x] En el teléfono (Xiaomi, 2026-11-19) contra staging: enlace falso → `restablecer` 400 → "Pedir otro enlace" → Recuperar 202 → pegar texto inválido (error) y enlace válido (abre Nueva contraseña). Capturas en `docs/pruebas/nv08_*`.
- [ ] Restablecer con un token real: staging no manda correos (sin SMTP).

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/auth/ui/AuthViewModelsTest.kt` — recuperar tras 202, 429 con `Retry-After`, cuenta atrás de 60 s y reenviar
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — `POST /auth/recuperar` 202 sin cuerpo y 429 con `Retry-After`
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — el 202 de la doc no trae datos: la app no puede distinguir si la cuenta existe
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — el correo no aparece en logs
- [x] Seguridad: `test/.../core/common/ValidatorsTest.kt` — correo validado antes de llamar
- [x] Unit: `test/.../feature/auth/ui/EmailLinkViewModelsTest.kt` — enlace https, esquema propio y pegado; rechazos; nueva contraseña (coinciden, vencido, campo, sin red, se olvida el token); verificar correo
- [x] Unit: `AuthViewModelsTest` (pegar enlace) y `MainViewModelTest` (enlace abierto)
- [x] Integración: `test/.../integration/EmailLinksIntegrationTest.kt` — `restablecer` 200 guarda sesión sin `Authorization`, 422 no guarda; `verificar-correo` sin sesión no pide perfil
- [x] Seguridad: `SourceSecurityTest` — ninguna ruta lleva el token
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06