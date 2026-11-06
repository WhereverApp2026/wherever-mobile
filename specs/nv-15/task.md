# NV-15 — Privacidad, mis datos y eliminar cuenta

## T1 — Implementación
- [x] Privacidad usa los 5 interruptores de la API, en el estilo de `28_PrivSocial` (D04).
- [x] Eliminar cuenta sigue a la API: se escribe ELIMINAR y hay 30 días para cancelar (D03). Al aceptar limpia la sesión local y va a Login.
- [x] Pantalla de eliminación pendiente (sin frame, D13): se detecta con `eliminar_en`, y permite cancelar con `POST /usuarios/yo/cancelar-eliminacion` o cerrar sesión.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — privacidad parcial, ELIMINAR exacto, eliminar limpia la sesión, cancelar fuera de plazo
- [x] Integración: `test/.../integration/ProfileIntegrationTest.kt` — privacidad 200, `DELETE /usuarios/yo` 202 y 422, cancelar 409 con respuestas de la doc
- [x] Seguridad: `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — eliminar limpia la sesión local; el error la conserva
- [x] Seguridad: `test/.../integration/ProfileIntegrationTest.kt` — confirmación validada (`ELIMINAR`)
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06