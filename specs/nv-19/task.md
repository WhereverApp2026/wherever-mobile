# NV-19 — Búsqueda

> Ejecutada en parte (2026-11-19). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-19/spec.md` aprobada
- [x] 2. Entidad Room + DAO + esquema exportado + migración — no aplica: reutiliza los repositorios de lugares, amigos y planes, que ya guardan en Room
- [x] 3. DTO de red + mapeos + `AppError`
- [x] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [x] 5. ViewModel con `UiState` y `StateFlow`
- [x] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [ ] 7. Pruebas de los tres tipos (T2)
- [x] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador (en el teléfono: Buscar, ideas y Sin resultados ✅; Personas y "solicitud enviada" pendientes, sin teléfono conectado) + tag `vX.Y.Z-featN`
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [x] Unit: `SearchViewModelTest` (9 pruebas): espera y cancelación con dispatcher inyectado.
- [ ] Integración: Data: los resultados con cursor se guardan en Room y la UI lee de Room sin red.
- [ ] Seguridad: Security: la búsqueda de personas respeta `buscable_por_telefono` y no registra correos ni teléfonos en logs.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06