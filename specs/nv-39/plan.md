# plan.md — nv-39 CI/CD: gates del push y del pull request

> Deriva de `specs/nv-39/spec.md`. Respeta constitución §8 (orden de CI, sin CD) y §3 (stack).

## 1. Mapeo
- Crear: `.github/workflows/ci-pr.yml`, `.github/dependabot.yml`, `.gitleaks.toml`, `scripts/check_deps_osv.py`, `scripts/check_routes.sh`, `scripts/check_strings.sh`, `scripts/check_specs.sh`, `scripts/check_sdd.sh`, `scripts/critic-guardrails.sh`, `scripts/check_coverage.sh`, `scripts/check_delta_coverage.py`, `.semgrep/semgrep.yaml`.
- Modificar: `app/build.gradle.kts` (bloque `kover` con umbral y exclusiones de código generado), `AGENTS.md §1/§4`, `specs/README.md`, `Memoria.md`.

## 2. Guardarraíles
- Constitución §8: orden `deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan`; sin CD.
- Constitución §6: sin secretos en el repo ni en el workflow; Dependency-Check (aquí OSV) informativo.
- Constitución §7: cobertura ≥ 45 % como único bloqueo de cobertura; delta y mutación advisory.
- Constitución §3: sin librerías nuevas. Gitleaks y OSV son herramientas de CI fuera del APK; Semgrep y Kover ya están en el stack.
- `continue-on-error` solo en `delta-check`, `semgrep --config=auto` y `deps-scan`.

## 3. Pruebas
- Unit (scripts): `check_coverage.sh` con XML sintéticos (≥ umbral → `COVERAGE-OK`, < umbral → `COVERAGE-FAIL`); `check_deps_osv.py` con una salida de `:app:dependencies` real (resuelve `->` y `{strictly}`); `check_delta_coverage.py` sin reporte → sale `0` en advisory.
- Data (integración): el workflow corre las suites reales de la app (`testDebugUnitTest` con MockWebServer y `connectedDebugAndroidTest` con Room real). Ninguna usa staging.
- Security: `gitleaks git` sobre la historia completa sin hallazgos; `critic-guardrails.sh → PASS`; `semgrep --config=.semgrep/semgrep.yaml --error → 0`; `grep -n 'secrets\.' .github/workflows/ci-pr.yml → vacío`.
- Kover: umbral 45 % sobre líneas. Excluye solo código generado (Hilt, Room `_Impl`, `BuildConfig`, `ComposableSingletons`) y las funciones `@Composable`/`@Preview`, que se prueban con Compose Testing en el paso `ui` y no con pruebas JVM. Medición en `Memoria.md` y en `docs/revision-jorge.md`.

## 4. Secuencia
1. Scripts de puerta y `semgrep.yaml` (Diego, `abfcfae`).
2. Workflow base en orden §8 (Diego, `abfcfae`, `43cfedf`).
3. Disparo en todo push, JDK 17, artefacto APK, gitleaks, OSV, Kover con umbral, Dependabot.
4. Medir cobertura y subirla con pruebas hasta el umbral (specs con código, ver cada `plan.md §3`).
5. Correr todo en local y registrar evidencia en `checklists/gates.md`.

## 5. Verificación
- `python -c "import yaml;yaml.safe_load(open('.github/workflows/ci-pr.yml'))" → sin error`
- `./gradlew ktlintCheck detekt lintDebug → BUILD SUCCESSFUL` (detekt con JDK 17)
- `./gradlew testDebugUnitTest koverVerifyDebug → BUILD SUCCESSFUL`
- `./gradlew assembleDebug → app/build/outputs/apk/debug/app-debug.apk`
- `gitleaks git . --config .gitleaks.toml --redact → no leaks found`
- `bash scripts/critic-guardrails.sh → PASS`, `check_routes.sh → OK`, `check_strings.sh → OK`, `check_specs.sh → SPECS-OK`, `check_sdd.sh → SDD-OK`, `check_coverage.sh --min 45 → COVERAGE-OK`
- `python scripts/check_deps_osv.py build/deps.txt --advisory → DEPS-OK | DEPS-ADVISORY`
- Primer run en GitHub Actions tras el push: pendiente de ver (sin `gh` en este equipo).
