# NV-24 — Plan: detalle y edición

> Ejecutada el 2026-11-22 sobre nv-48 (decisiones D-24a a D-24f en la spec). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-24/spec.md` aprobada
- [ ] 2. (No aplica, D-24b) Entidad Room + DAO + esquema exportado + migración
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
- [x] Unit: Unit: reordenar y límites.
- [ ] (No aplica, D-24b; `PlansIntegrationTest` cubre crear a mano, visibilidad y alternativas) Integración: Data: guardar borrador con Room.
- [x] Seguridad: Security: editar un plan ajeno recibe 403 del servidor y la app no aplica el cambio localmente.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
