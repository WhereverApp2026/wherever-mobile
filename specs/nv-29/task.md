# NV-29 — Cupones

> Ejecutada el 2026-11-25 en `feat/nv-29-cupones` (decisiones D-29a a D-29e en la spec). Plantilla: `.specify/templates/tasks-template.md`.

## T1 — Slice liberable
- [x] 1. Spec en `specs/nv-29/spec.md` aprobada (contrato conciliado el 2026-11-25)
- [ ] 2. (No aplica, D-29b) Entidad Room + DAO + esquema exportado + migración
- [x] 3. DTO de red + mapeos + `AppError`
- [x] 4. Repositorio sin Dispatchers fijos (sin Room, D-29b)
- [x] 5. ViewModel con `UiState` y `StateFlow`
- [x] 6. Pantalla Compose sin lógica; textos en `strings.xml`; ruta en `docs/routes.md`
- [x] 7. Pruebas de los tres tipos (T2)
- [ ] 8. Gates: cobertura ≥45% + `routes.md` + `permissions-matrix.md` + `masvs-checklist.md` vivos (detekt lo corre la CI: no hay JDK 17-22 en el equipo)
- [x] 9. APK + verificación en teléfono contra staging (29_Cupones con la cartera vacía; el detalle espera un cupón real, `docs/pruebas-telefono.md` #26)
- [x] 10. Actualizar `Memoria.md` + `CHECKLIST_PR.md`
- **No hacer**: implementar con datos simulados lo que no tiene endpoint.

## T2 — Pruebas
- [x] Unit: textos de vigencia, pestañas y código de canje (`CouponsViewModelsTest`).
- [x] Integración: Data: `CouponRepositoryImplTest` con MockWebServer (el uso del cupón no se guarda en Room, D-29b).
- [x] Seguridad: Security: el código del cupón no aparece en logs (`CouponsSecurityTest`).

## T3 — Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-25): Osmar — fecha:
