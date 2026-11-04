# 011 — Privacidad, mis datos y eliminar cuenta

## T1 — Implementación
- [x] Privacidad usa los 5 interruptores de la API, en el estilo de `28_PrivSocial` (D04).
- [x] Eliminar cuenta sigue a la API: se escribe ELIMINAR y hay 30 días para cancelar (D03). Al aceptar limpia la sesión local y va a Login.
- [x] Pantalla de eliminación pendiente (sin frame, D13): se detecta con `eliminar_en`, y permite cancelar con `POST /usuarios/yo/cancelar-eliminacion` o cerrar sesión.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging
- [ ] Revisor
