# Checklist — NV-05 — Flujo general

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada y aprobada (Jorge, 2026-11-06). Evidencia local del 2026-11-05.
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [x] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [x] Unitarias verdes (ver `plan.md §3`)
- [x] Integración JVM verde: MockWebServer con respuestas de `docs/contexto_api_sprint1.md`, sin staging
- [ ] Integración instrumentada (Room real): compila; sin correr en local por falta de emulador o teléfono (`docs/revision-jorge.md`)
- [x] Seguridad verde; MASVS: AUTH, PLATFORM
- [x] `docs/routes.md` actualizado y enlazado desde la spec
- [x] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [x] Puerta incremental advisory: delta informa sin bloquear
- [x] Sin secretos (gitleaks), sin tokens en logs, solo HTTPS
- [ ] Estados cargando, vacío, error y sin red revisados en emulador
- [x] `Memoria.md` actualizada, sin secretos
- [x] Aprobación: Revisor Jorge — 2026-11-06