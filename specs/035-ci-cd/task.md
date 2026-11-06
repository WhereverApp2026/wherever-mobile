# 035 — CI/CD: gates del push y del pull request

## T1 — Scripts de puerta y workflow base
- [x] **Crear**: `scripts/critic-guardrails.sh`, `check_routes.sh`, `check_strings.sh`, `check_specs.sh`, `check_sdd.sh`, `check_coverage.sh`, `check_delta_coverage.py`, `.semgrep/semgrep.yaml`, `ci-pr.yml` en orden §8 (Diego, 2026-11-05).
- **Verificar**: cada script devuelve su salida esperada sobre la rama.

## T2 — Pedido del equipo (2026-11-05)
- [x] **Modificar** `ci-pr.yml`: `push` en toda rama + `pull_request`; JDK 17; artefacto `wherever-debug-apk`; `koverVerifyDebug`; emulador `google_apis` con `reactivecircus/android-emulator-runner`; `Reports` siempre.
- [x] **Crear**: paso gitleaks (binario verificado por checksum) + `.gitleaks.toml`; `scripts/check_deps_osv.py` (deps-scan informativo); `.github/dependabot.yml`.
- [x] **Modificar** `app/build.gradle.kts`: `kover.reports.verify` con `minBound(45)` y exclusiones de código generado.
- **No hacer**: CD, publicación, secretos, Pitest o Dependency-Check sin acuerdo.

## T3 — Pruebas y evidencia
- [ ] **Unit**: scripts con entradas sintéticas (ver `plan.md §3`).
- [ ] **Data (integración)**: suites reales de la app en `testDebugUnitTest` y `connectedDebugAndroidTest`, sin staging.
- [ ] **Security**: gitleaks, critic, Semgrep propio y workflow sin `secrets.*`.
- [ ] **Verificar**: todos los comandos de `plan.md §5` en local; evidencia en `checklists/gates.md`.
- [ ] Primer run verde en GitHub Actions.
- [ ] Revisor: Jorge.
