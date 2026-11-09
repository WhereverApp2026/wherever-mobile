# NV-07 — Iniciar sesión

## T1 — Implementación
- [x] 401 muestra el `mensaje` de la API. 423 usa texto propio sobre el bloqueo de 15 min. 429 desactiva Entrar con cuenta atrás según `Retry-After`.
- [x] Tras entrar pide `GET /usuarios/yo` y decide el destino: eliminación pendiente, Gustos (si `gustos` es null y no se omitieron) o Inicio.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/auth/ui/AuthViewModelsTest.kt` — `LoginViewModel` con 200, 401, 422, 423, 429 y sin red
- [x] Unit: `test/.../feature/auth/data/AuthRepositoryImplTest.kt` — login guarda tokens y manda el dispositivo; el fallo no guarda
- [x] Unit: `test/.../core/common/ValidatorsTest.kt` — correo válido antes de enviar
- [x] Integración: `test/.../integration/AuthIntegrationTest.kt` — login 200, 401, 422 (campos), 423 y 429 (`Retry-After`) con las respuestas de la doc
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — 401 de login no dispara refresh ni guarda tokens
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — la contraseña y el correo no aparecen en logs
- [x] Seguridad: `test/.../feature/auth/ui/AuthViewModelsTest.kt` — no envía con correo inválido; borra la contraseña tras entrar
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06