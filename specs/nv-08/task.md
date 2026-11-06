# NV-08 — Recuperar contraseña

## T1 — Implementación
- [x] `03_Recuperar` y `04_RevisaCorreo`, con el aviso "Si el correo existe, te llegará un enlace".
- [x] `CheckEmailRoute` tiene modo: `RECOVERY` reenvía con `POST /auth/recuperar` y arranca con 60 s; `VERIFICATION` reenvía con `POST /auth/reenviar-verificacion` y se abre desde el aviso de Perfil.
- [x] 429 usa `Retry-After` para la cuenta atrás. El texto dice 1 hora (D02).

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
