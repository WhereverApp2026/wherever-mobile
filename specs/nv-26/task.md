# NV-26 — Vaca virtual y gastos

> Ejecutada el 2026-11-22 (decisiones D-26a a D-26e en la spec). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-26/spec.md` aprobada
- [ ] 2. (No aplica, D-26b) Entidad Room + DAO + esquema exportado + migración
- [x] 3. DTO de red + mapeos + `AppError`
- [x] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [x] 5. ViewModel con `UiState` y `StateFlow`
- [x] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [x] 7. Pruebas de los tres tipos (T2)
- [x] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador + tag `vX.Y.Z-featN`
- [x] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [x] Unit: Unit: el ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70).
- [x] Unit: Unit: redondeo sin centavos perdidos.
- [ ] (No aplica, D-26b; `VacaRepositoryImplTest` cubre el contrato) Integración: Data: gastos y saldos se guardan en Room y se recalculan tras reiniciar sin red.
- [x] Seguridad: Security: no hay datos bancarios ni de tarjeta en los gastos y su cuerpo no se registra en logs.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06