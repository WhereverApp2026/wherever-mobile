# Checklist — NV-23 — Crear plan con IA

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T3`. Estado: aprobada (Jorge, 2026-11-06), implementada el 2026-11-22 (Diego).
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [x] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [x] Unitarias verdes (previstas en `plan.md §3`)
- [ ] Integración: Room real + MockWebServer con el contrato publicado, sin staging
- [x] Seguridad verde y `docs/masvs-checklist.md` verificado
- [x] `docs/routes.md` actualizado y enlazado desde la spec
- [x] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [x] Sin secretos, sin tokens en logs, solo HTTPS
- [x] Estados cubiertos: cargando, vacío, error, sin red
- [x] `Memoria.md` actualizada, sin secretos
- [x] Aprobación: Revisor Jorge — 2026-11-06
