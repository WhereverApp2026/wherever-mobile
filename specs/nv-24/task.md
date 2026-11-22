# NV-24 — Plan: detalle y edición

> Parcial: lo hecho entró con `specs/nv-48` (conciliado el 2026-11-22). Lo que falta: Fase 2 de `docs/itinerario-planes.md`. Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-24/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración (en `nv-56`)
- [x] 3. DTO de red + mapeos + `AppError` (nv-48: `PlansApi.kt`)
- [x] 4. Repositorio sin Dispatchers fijos (nv-48); Room como fuente de verdad en `nv-56`
- [x] 5. ViewModel con `UiState` y `StateFlow` (`PlanDetailViewModel`)
- [ ] 6. Pantallas: `39_Plan`, `Plan · *` y `40_Cambiar` hechas; faltan `41_AgregarParada` completa y `CrearPlan`
- [ ] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en teléfono con catálogo en staging
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar con datos simulados lo que no tiene endpoint.

## T2 — Pruebas
- [x] Unit: edición de paradas en `PlansViewModelsTest` (nv-48).
- [ ] Unit: Unit: reordenar y límites de `CrearPlan` y `41_AgregarParada`.
- [x] Integración: `If-Match`, 409 y nombres de parada en `PlansIntegrationTest` (nv-48).
- [ ] Integración: Data: guardar el plan con Room y leerlo sin red (nv-56).
- [ ] Seguridad: Security: editar un plan ajeno recibe 403/404 del servidor y la app no aplica el cambio localmente.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
