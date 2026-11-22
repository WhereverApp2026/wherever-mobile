# Checklist — NV-23 — Crear plan con IA

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T3`. Estado: aprobada (Jorge, 2026-11-06); **parcial**, lo hecho entró con nv-48 (conciliado el 2026-11-22).
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [x] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores (nv-48)
- [ ] Unitarias verdes (hechas las de nv-48; faltan las de `plan.md §3`)
- [ ] Integración: Room real + MockWebServer con el contrato publicado, sin staging (falta Room, nv-56)
- [ ] Seguridad verde y `docs/masvs-checklist.md` verificado
- [x] `docs/routes.md` actualizado: `CreatePlanRoute(phrase)` (nv-48)
- [x] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`) (nv-48)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [x] Sin secretos, sin tokens en logs, solo HTTPS
- [ ] Estados cubiertos: cargando, vacío, error, sin red (sin red depende de nv-56)
- [ ] `Memoria.md` actualizada, sin secretos
- [x] Aprobación: Revisor Jorge — 2026-11-06
- [ ] Conciliación (2026-11-22): Revisor Osmar — fecha:
