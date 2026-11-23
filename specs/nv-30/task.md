# NV-30 — El día del plan

> Parcial el 2026-11-22 (decisiones D-30a a D-30e en la spec; mapas esperan D25). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-30/spec.md` aprobada
- [ ] 2. (No aplica, D-30d) Entidad Room + DAO + esquema exportado + migración
- [x] 3. DTO de red + mapeos + `AppError`
- [x] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [x] 5. ViewModel con `UiState` y `StateFlow`
- [ ] 6. (Parcial: 26_CheckinOk; 13, 14, 15 y 18 esperan D25) Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [x] 7. Pruebas de los tres tipos (T2)
- [x] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador + tag `vX.Y.Z-featN`
- [x] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [ ] (Espera D25; hay pruebas de la llegada en `ArrivalViewModelTest`) Unit: Unit: pasos de la ruta.
- [ ] (No aplica, D-30d; `CheckinRepositoryImplTest` cubre el contrato) Integración: Data: el estado del check-in queda en Room y se lee sin red.
- [x] Seguridad: Security: no queda histórico de ubicación en disco ni en logs.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06