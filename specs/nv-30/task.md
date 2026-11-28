# NV-30 — El día del plan

> Parcial el 2026-11-22 (decisiones D-30a a D-30e en la spec; mapas esperan D25). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-30/spec.md` aprobada
- [ ] 2. (No aplica, D-30d) Entidad Room + DAO + esquema exportado + migración
- [x] 3. DTO de red + mapeos + `AppError`
- [x] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [x] 5. ViewModel con `UiState` y `StateFlow`
- [ ] 6. (Parcial: 26_CheckinOk; 13_Mapa y 14_MapaFull el 2026-11-26 en `feat/nv-30-mapas`; 15 y 18 esperan un proveedor de rutas, D-30f) Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [x] 7. Pruebas de los tres tipos (T2)
- [x] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [x] 9. APK + verificación en teléfono contra staging (13 y 14 con un plan real de QA B, 2026-11-26; `docs/pruebas-telefono.md` #27)
- [x] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [x] Unit: orden de paradas, totales, URL de Iniciar ruta y estados de 13 y 14 (`PlanMapViewModelTest`); los pasos de la ruta esperan un proveedor (D-30f).
- [ ] (No aplica, D-30d; `CheckinRepositoryImplTest` cubre el contrato) Integración: Data: el estado del check-in queda en Room y se lee sin red.
- [x] Seguridad: Security: no queda histórico de ubicación en disco ni en logs.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06