# Checklist — nv-39 CI/CD: gates del push y del pull request

> Trazabilidad: `spec §2.1-§6 ↔ plan §3-§5 ↔ task T1-T3`. Estado: implementada y aprobada (Jorge, 2026-11-06). Evidencia local del 2026-11-05 (Windows, JDK 17).

- [x] `ci-pr.yml` en orden de constitución §8, en cada `push` y `pull_request`; YAML válido (`yaml.safe_load`)
- [x] `./gradlew assembleDebug` sin errores; paso de artefacto `wherever-debug-apk`
- [x] `ktlintCheck`, `detekt` (JDK 17) y `lintDebug` sin errores
- [x] Unit + Data (MockWebServer) + Security: `testDebugUnitTest` verde
- [ ] UI + Data instrumentada (Room real): `connectedDebugAndroidTest` compila (`assembleDebugAndroidTest`); sin correr en local: no hay emulador ni teléfono (ver `docs/revision-jorge.md`)
- [x] Kover: `koverVerifyDebug` con `minBound(45)` y `check_coverage.sh --min 45 → COVERAGE-OK` (71,99 %; 37,17 % sin excluir `@Composable`, ver spec §2.5)
- [x] Delta advisory informa sin bloquear
- [x] Security · secretos: `gitleaks git . --config .gitleaks.toml --redact → no leaks found`; sin `secrets.*` en el workflow
- [x] Security · dependencias: `check_deps_osv.py` → `DEPS-OK` (detecta log4j 2.14.1 con entrada sintética)
- [x] Security · lint: Android Lint (categoría Security, advertencias como error) + `semgrep --config=.semgrep/semgrep.yaml --error → 0 findings`
- [x] `critic-guardrails.sh → PASS`, `check_routes.sh → OK`, `check_strings.sh → OK`, `check_specs.sh → SPECS-OK`, `check_sdd.sh → SDD-OK`
- [x] Sin CD ni publicación; Dependabot semanal
- [ ] Primer run verde en GitHub Actions
- [x] Aprobación firmada: Revisor Jorge — 2026-11-06
