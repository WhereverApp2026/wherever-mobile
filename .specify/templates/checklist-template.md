# PR Checklist Template — gates de PR

> El gate de fase va en `specs/nv-0X/checklists/gates.md`; este checklist es para el PR.
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [ ] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [ ] Unit + Data (Room real + MockWebServer) verdes
- [ ] Security + UI + Arquitectura verdes
- [ ] `docs/routes.md` actualizado y enlazado desde la spec
- [ ] Cobertura ≥45 % (`scripts/check_coverage.sh --min 45` `[planificado nv-03]`) — umbral provisional
- [ ] Puerta incremental advisory: delta ≥80 % + mutación enfocada sin break (no bloquea)
- [ ] Semgrep sin altos; Dependency-Check informativo (hallazgos altos revisados en el PR)
- [ ] Sin secretos, sin tokens en logs, solo HTTPS; debug solo confía en CA de desarrollo
- [ ] Estados cubiertos: cargando, vacío, error, sin red
- [ ] Referencias a artefactos futuros marcadas `[planificado nv-0X]`
- [ ] `Memoria.md` actualizada, sin secretos
- [ ] 1 revisor, `main` protegida; tag `vX.Y.Z-featN` si aplica
