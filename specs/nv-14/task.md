# NV-14 — Perfil, edición e insignias

## T1 — Implementación
- [x] Perfil lee de Room y actualiza con `GET /usuarios/yo`. Sin red conserva la caché y muestra el error.
- [x] Editar perfil une `82_EditarPerfil` y `84_DatosPers` (D09): nombre y ciudad, con `PATCH /usuarios/yo` e `If-Match`.
- [x] Ante 409 avisa "Tus datos cambiaron en otro dispositivo", recarga y deja al usuario revisar.
- [x] Aviso de correo sin verificar, que lleva a Revisa tu correo (D20).
- [x] Room: `UserProfileEntity` con esquema exportado en `app/schemas`.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — perfil, sin red con caché, edición parcial, 409 recarga, 422 bajo el campo
- [x] Unit: `test/.../feature/profile/data/ProfileRepositoryImplTest.kt` — If-Match con la versión guardada; campos desconocidos no rompen
- [x] Integración: `test/.../integration/ProfileIntegrationTest.kt` — perfil de la doc en caché; PATCH 200/409/422 y 401 con refresh
- [ ] Integración: `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — 409 no toca Room; la UI lee de Room (instrumentada)
- [ ] Integración: `androidTest/.../feature/profile/data/UserProfileDaoTest.kt` — listas y objetos anidados en Room (instrumentada)
- [x] Seguridad: `test/.../security/PlatformSecurityTest.kt` — la base de datos se excluye de respaldos
- [x] Seguridad: `test/.../integration/AuthIntegrationTest.kt` — sin datos personales en logs
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
