# Checklist — NV-18 — Notificaciones

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T3`. Estado: aprobada (Jorge, 2026-11-06), sin código.
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [ ] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [ ] Unitarias verdes (previstas en `plan.md §3`)
- [ ] Integración: Room real + MockWebServer con el contrato publicado, sin staging
- [ ] Seguridad verde y `docs/masvs-checklist.md` verificado
- [ ] `docs/routes.md` actualizado y enlazado desde la spec
- [ ] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [ ] Sin secretos, sin tokens en logs, solo HTTPS
- [ ] Estados cubiertos: cargando, vacío, error, sin red
- [ ] `Memoria.md` actualizada, sin secretos
- [x] Aprobación: Revisor Jorge — 2026-11-06