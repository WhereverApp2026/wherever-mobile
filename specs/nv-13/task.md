# NV-13 — Ajustes y cerrar sesión

## T1 — Implementación
- [x] Preferencias con `PATCH /usuarios/yo/preferencias`: tema, votaciones, cupones, reseñas y promociones. El tema se aplica a toda la app.
- [x] Usar mi ubicación va a `PATCH /usuarios/yo/privacidad`.
- [x] Pantalla Sesiones abiertas (sin frame, D07): `GET /auth/sesiones`, `DELETE /auth/sesiones/{id}` y `POST /auth/logout-todos` con confirmación.
- [x] Cerrar sesión: `POST /auth/logout` y limpieza local aunque la llamada falle. Cerrar todas solo limpia si el servidor confirma.
- [x] La sesión actual no se cierra desde la lista: se usa Cerrar sesión, que también limpia el teléfono.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/settings/ui/SettingsViewModelsTest.kt` — tema, notificaciones, cerrar sesión, sesiones, 404 al cerrar una, cerrar todas
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — `GET /auth/sesiones` (doc) con 401 y refresh; logout 204
- [x] Integración: `test/.../integration/ProfileIntegrationTest.kt` — preferencias 422 sin tocar la caché
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — logout limpia tokens, Room y onboarding
- [x] Seguridad: `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — logout-todos solo limpia si el servidor confirma
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
