# NV-11 — Permisos en contexto

> Sin ejecutar. Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [ ] 1. Spec en `specs/nv-11/spec.md` aprobada
- [ ] 2. Entidad Room + DAO + esquema exportado + migración
- [ ] 3. DTO de red + mapeos + `AppError`
- [ ] 4. Repositorio (Room fuente de verdad) sin Dispatchers fijos
- [ ] 5. ViewModel con `UiState` y `StateFlow`
- [ ] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [ ] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos
- [ ] 9. APK + verificación en emulador + tag `vX.Y.Z-featN`
- [ ] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar lo marcado "Pendiente de backend" con datos simulados.

## T2 — Pruebas previstas
- [ ] Unit: Unit: flujo de decisión por permiso.
- [ ] Integración: Data: `PATCH /usuarios/yo/privacidad` con MockWebServer escribe Room tras la decisión, y la UI lee de Room.
- [ ] Seguridad: Security: el manifiesto declara solo los permisos de la matriz.

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06