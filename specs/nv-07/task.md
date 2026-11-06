# NV-07 — Iniciar sesión

## T1 — Implementación
- [x] 401 muestra el `mensaje` de la API. 423 usa texto propio sobre el bloqueo de 15 min. 429 desactiva Entrar con cuenta atrás según `Retry-After`.
- [x] Tras entrar pide `GET /usuarios/yo` y decide el destino: eliminación pendiente, Gustos (si `gustos` es null y no se omitieron) o Inicio.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
