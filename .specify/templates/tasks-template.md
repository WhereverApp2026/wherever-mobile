# Feature Tasks Template — nv-05+ (funcionalidad liberable)

> Cada funcionalidad liberable: spec→datos→dominio→ViewModel→UI→tests→gates→tag `vX.Y.Z-featN`.
> Para fases de arquitectura (`nv-00…nv-04`) usar `phase-task-template.md`.

- [ ] 1. Spec en `specs/nv-0X/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración
- [ ] 3. DTO de red + mapeos + `AppError`
- [ ] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [ ] 5. ViewModel con `UiState` y `StateFlow`
- [ ] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [ ] 7. Tests: Unit + Data + Security + UI + Arquitectura
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador + tag `vX.Y.Z-featN`
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
