# NV-53 — Amigos

## T1 — Implementación
- [x] `FriendsRepository` con personas y amistades.
- [x] Amigos, Agregar amigos y Perfil de la persona; fila en Perfil.
- [x] Nombre de usuario, código de amigo y "Buscable por nombre de usuario"; Room 4.
- [x] Push y avisos sociales abren el perfil; rutas en `docs/routes.md`; versión 8 (0.6.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] `MigrationTest` en la CI.
- [ ] En el teléfono contra staging con dos cuentas de prueba.

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `FriendsViewModelsTest` (11)
- [x] Integración: `FriendsIntegrationTest` (5)
- [x] UI: `MigrationTest` (+1)
- [x] Seguridad: `SourceSecurityTest`, `ArchitectureTest`
- **No hacer**: mostrar o pedir correo o teléfono de otra persona, ni avisarle que la rechazaron o bloquearon.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
