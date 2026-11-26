# Checklist — NV-29 — Cupones

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T3`. Estado: aprobada (Jorge, 2026-11-06); implementada el 2026-11-25 en `feat/nv-29-cupones` (contrato conciliado, D-29a a D-29e).
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [x] `./gradlew assembleDebug`, ktlint y Android Lint sin errores (detekt: CI)
- [x] Unitarias verdes (previstas en `plan.md §3`)
- [x] Integración: MockWebServer con el contrato publicado, sin staging (sin Room, D-29b)
- [x] Seguridad verde (`CouponsSecurityTest`)
- [x] `docs/routes.md` actualizado y enlazado desde la spec
- [x] Cobertura ≥45% (`koverVerifyDebug` en verde con la suite completa, 2026-11-25)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [x] Sin secretos, sin tokens en logs, solo HTTPS
- [x] Estados cubiertos: cargando, vacío, error, sin red
- [x] Probado en teléfono contra staging (cuenta QA B, 2026-11-26)
- [x] `Memoria.md` actualizada, sin secretos
- [x] Aprobación: Revisor Jorge — 2026-11-06
- [ ] Conciliación (2026-11-25): Revisor Osmar — fecha:
