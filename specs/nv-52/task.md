# NV-52 — Avisos y horario silencioso

## T1 — Implementación
- [x] `NotificationsRepository` con los cinco endpoints.
- [x] Pantalla Avisos con grupos por día, filtros, leídas y borrar; campana en Inicio.
- [x] Horario silencioso en Ajustes.
- [x] Push con destino inválido abre Avisos; ruta en `docs/routes.md`; versión 7 (0.5.0).

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (ver `checklists/gates.md`).
- [ ] En el teléfono contra staging con avisos reales (cuando haya amigos o grupos).

## T3 — Pruebas (unitarias, integración, UI y seguridad)
- [x] Unit: `NotificationsViewModelTest` (11)
- [x] Integración: `NotificationsIntegrationTest` (5)
- [x] UI: sin pruebas nuevas
- [x] Seguridad: `SourceSecurityTest`, `ArchitectureTest`
- **No hacer**: mostrar texto que venga en `datos` ni navegar con ids sin validar.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
