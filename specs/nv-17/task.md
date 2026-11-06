# NV-17 — Inicio y feed

## T1 — Implementación
- [x] Ver `spec.md`, sección "Implementado".

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/home/ui/HomeViewModelTest.kt` — Inicio provisional lee el perfil de Room
- [ ] Integración: `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — el perfil de la API queda en Room y la UI lee de Room (instrumentada)
- [x] Seguridad: `test/.../security/SourceSecurityTest.kt` — sin logs ni datos personales fuera del interceptor redactado
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
