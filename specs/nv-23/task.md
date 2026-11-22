# NV-23 — Crear plan con IA

> Parcial: lo hecho entró con `specs/nv-48` (conciliado el 2026-11-22). Lo que falta: Fase 3 de `docs/itinerario-planes.md`. Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-23/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración (en `nv-56`)
- [x] 3. DTO de red + mapeos + `AppError` (nv-48: `PlansApi.kt`)
- [x] 4. Repositorio sin Dispatchers fijos (nv-48); Room como fuente de verdad en `nv-56`
- [x] 5. ViewModel con `UiState` y `StateFlow` (`CreatePlanViewModel`)
- [ ] 6. Pantallas: `IAPalabra` y `Generando` 1-3 hechas; faltan `17_AjustaPlan` y el 4.º paso
- [ ] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en teléfono con catálogo en staging
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar con datos simulados lo que no tiene endpoint.

## T2 — Pruebas
- [x] Unit: asistente en `PlansViewModelsTest` (nv-48).
- [ ] Unit: Cancelar con dispatcher de prueba, 4 pasos y `17_AjustaPlan`.
- [x] Integración: generar y otra idea en `PlansIntegrationTest` (nv-48).
- [ ] Integración: Data: el borrador del plan generado persiste en Room y sobrevive a un reinicio sin red (nv-56).
- [ ] Seguridad: Security: ni la frase ni los gustos aparecen en logs; `aprendizaje_personalizado` lo aplica el servidor.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
