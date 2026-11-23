# Checklist — NV-56 — Planes sin conexión (Room)

> Trazabilidad: `spec §1-§7 ↔ plan §1-§6 ↔ task T0-T3`. Estado: borrador revisado (Borre, 2026-11-22), sin código.

- [ ] Issue a Jorge por `core/di/AppModule.kt`, `app/build.gradle.kts` y `MainActivity.kt` atendido
> Orden CI-PR: deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan.

- [ ] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [ ] Unitarias verdes (`plan.md §3`)
- [ ] Integración: Room real + MockWebServer con el contrato publicado, sin staging
- [ ] `MigrationTest` 5 → 6 verde y esquema 6 exportado
- [ ] Seguridad verde y `docs/masvs-checklist.md` verificado (almacenamiento local, limpieza al cerrar sesión)
- [ ] `docs/routes.md` sin cambios (sin rutas nuevas)
- [ ] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [ ] Puerta incremental advisory: delta ≥80% + mutación enfocada sin break (no bloquea)
- [ ] Sin secretos, sin tokens en logs, solo HTTPS; `ConnectionSecurityTest` verde
- [ ] Estados cubiertos: cargando, vacío, error, sin red
- [ ] Probado en teléfono en modo avión
- [ ] `Memoria.md` actualizada, sin secretos
- [ ] Aprobación: Revisor Osmar — fecha:
