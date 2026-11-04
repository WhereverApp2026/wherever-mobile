# 010 — Perfil, edición e insignias

## T1 — Implementación
- [x] Perfil lee de Room y actualiza con `GET /usuarios/yo`. Sin red conserva la caché y muestra el error.
- [x] Editar perfil une `82_EditarPerfil` y `84_DatosPers` (D09): nombre y ciudad, con `PATCH /usuarios/yo` e `If-Match`.
- [x] Ante 409 avisa "Tus datos cambiaron en otro dispositivo", recarga y deja al usuario revisar.
- [x] Aviso de correo sin verificar, que lleva a Revisa tu correo (D20).
- [x] Room: `UserProfileEntity` con esquema exportado en `app/schemas`.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
