# Checklist — NV-40 — Conexión con la API en wifi y datos móviles

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, pendiente de revisión.
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [ ] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [ ] Unitarias verdes (ver `plan.md §3`)
- [ ] Integración JVM verde: MockWebServer, sin staging
- [ ] Seguridad verde; MASVS: NETWORK
- [x] `docs/routes.md`: sin rutas nuevas
- [ ] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [x] Sin secretos, sin tokens en logs, solo HTTPS
- [ ] Prueba manual en el teléfono con datos móviles y con wifi
- [ ] `Memoria.md` actualizada, sin secretos
- [ ] Aprobación: Revisor Jorge — fecha:
