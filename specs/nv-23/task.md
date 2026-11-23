# NV-23 — Crear plan con IA

> Ejecutada el 2026-11-22 sobre nv-48 (ver decisiones D-23a a D-23e en la spec). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-23/spec.md` aprobada
- [ ] 2. (No aplica, D-23b) Entidad Room + DAO + esquema exportado + migración
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
- [x] Unit: Unit: transiciones y cancelación con dispatcher de prueba.
- [ ] (No aplica, D-23b; el contrato de generar y guardar lo cubre `PlansIntegrationTest`) Integración: Data: el borrador del plan generado persiste en Room y sobrevive a un reinicio sin red.
- [x] Seguridad: Security: los gustos usados por la IA no aparecen en logs y viaja `privacidad.aprendizaje_personalizado`.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
