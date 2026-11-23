# NV-28 — Mis planes

> Frames hechos con `specs/nv-48`; criterios pendientes (conciliado el 2026-11-22): estado de cada plan y uso sin red (`nv-56`). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-28/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración (en `nv-56`)
- [x] 3. DTO de red + mapeos + `AppError` (nv-48)
- [x] 4. Repositorio sin Dispatchers fijos (nv-48); Room como fuente de verdad en `nv-56`
- [x] 5. ViewModel con `UiState` y `StateFlow` (`MyPlansViewModel`)
- [ ] 6. Pantallas `24_MisPlanes` y `41_PlanesVacio` hechas; falta la etiqueta de estado
- [ ] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en teléfono con planes reales en staging
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar con datos simulados lo que no tiene endpoint.

## T2 — Pruebas
- [x] Unit: cambio de pestaña en `PlansViewModelsTest` (nv-48).
- [ ] Unit: Unit: `PlansViewModel` muestra carga, vacío y error según el resultado del repositorio por pestaña.
- [x] Integración: lista por pestaña en `PlansIntegrationTest` (nv-48).
- [ ] Integración: Data: lectura por pestaña desde Room (nv-56).
- [ ] Seguridad: Security: los planes se borran de Room al cerrar sesión (nv-56).

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
