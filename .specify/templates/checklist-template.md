# Checklist Template — Gates PR

> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [ ] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [ ] Unit + Data (Room real + MockWebServer) verdes
- [ ] Security + UI + Arquitectura verdes
- [ ] `docs/routes.md` actualizado y enlazado desde la spec
- [ ] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [ ] Sin secretos, sin tokens en logs, solo HTTPS
- [ ] Estados cubiertos: cargando, vacío, error, sin red
- [ ] `Memoria.md` actualizada, sin secretos
- [ ] 1 revisor, `main` protegida
