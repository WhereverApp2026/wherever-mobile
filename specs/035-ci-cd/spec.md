# 035 — CI/CD: gates del pull request

> Rector: `.specify/memory/constitution.md` (manda ante conflicto). Plantilla: `.specify/templates/spec-template.md`.
> Convención de esta spec: **[Constitución §n]** = no negociable. **[Decisión propuesta]** = la constitución no lo define; se propone con su porqué.

## 1. Objetivo

Que cada pull request corra en GitHub Actions, **en el orden de [Constitución §8]**, las mismas puertas que `AGENTS.md §4` exige en local, sin CD ([Constitución §9]). El PR es integrable solo con el workflow en verde.

Traslada a CI lo que esta rama ya tenía escrito pero no ejecutable: los scripts de puerta y el registro de rutas, suites y cobertura.

## 2. Alcance / No-alcance

**In:** `.github/workflows/ci-pr.yml`, los scripts de puerta (`critic-guardrails`, `check_routes`, `check_strings`, `check_specs`, `check_sdd`, `check_coverage`, `check_delta_coverage`), `.semgrep/semgrep.yaml`, este spec y su fila en `specs/README.md`.

**Out:** despliegue o publicación ([Constitución §9]); mutación (Pitest no está en el build); Dependency-Check (informativo, no instalado); MobSF/Frida y firma automática (diferidos, [Constitución §6]); reintento de tests flaky.

### 2.1 Historias y criterios de aceptación

- **Como revisor**, quiero ver en el PR el estado de todas las puertas para saber si se puede fusionar.
- **Como desarrollador**, quiero el mismo orden en CI que en local para que un verde local sea significativo.
- Criterios:
  - [ ] `ci-pr.yml` ejecuta los pasos en el orden §8 y el YAML parsea con `yaml.safe_load`.
  - [ ] Los scripts de puerta pasan sobre esta rama: `PASS`, `OK`, `SPECS-OK`, `SDD-OK`, `COVERAGE-OK`.
  - [ ] `semgrep` con las reglas propias da 0 hallazgos; el registry corre como advisory.
  - [ ] El workflow no contiene secretos ni despliega nada.

### 2.2 Orden de pasos (constitución §8)

| # | Paso en `ci-pr.yml` | Comando | Bloqueante |
|---|---|---|---|
| 1 | `deps` | `./gradlew :app:dependencies --configuration debugRuntimeClasspath` | sí |
| 2 | `build` | `./gradlew assembleDebug` | sí |
| 3 | `lint` | `./gradlew ktlintCheck detekt lintDebug` | sí |
| 4 | `unit · data · security · arquitectura` | `./gradlew testDebugUnitTest` | sí |
| 5 | `ui` | `./gradlew connectedDebugAndroidTest` (emulador API 35) | sí |
| 6 | `critic` | `scripts/critic-guardrails.sh` | sí |
| 7 | `routes` | `scripts/check_routes.sh` | sí |
| — | `strings` | `scripts/check_strings.sh` | sí (AGENTS §4) |
| — | `specs` | `scripts/check_specs.sh` | sí (AGENTS §4) |
| 8 | `sdd` | `scripts/check_sdd.sh` | sí |
| 9 | `coverage` | `scripts/check_coverage.sh --min 45` | sí |
| 10 | `delta-check` | `scripts/check_delta_coverage.py --min 80 --advisory` | no (advisory) |
| 11 | `mutation` | — | no configurado (§2.6) |
| 12 | `semgrep` | `--config=.semgrep/semgrep.yaml` (propias) / `--config=auto` (registry) | sí / no |
| 13 | `deps-scan` | — | no configurado (§2.6) |

**[Decisión propuesta]** Las cuatro suites de [Constitución §7] son un solo paso porque viven en el mismo source set (`app/src/test`): `testDebugUnitTest` ejecuta Unit, Data (integración), Security y Arquitectura (Konsist) juntas, como ya dice `AGENTS.md §1`. Separarlas en cuatro invocaciones repetiría el mismo task sin ganar señal.

### 2.3 Estados

- **Verde:** todas las puertas en `0` → el PR puede fusionarse con el revisor de `CHECKLIST_PR.md`.
- **Rojo:** el paso fallido nombra la puerta; `Reports` sube `app/build/reports/` para diagnóstico.
- `continue-on-error` solo en lo advisory: `delta-check` y `semgrep --config=auto`.

### 2.4 Validaciones

- YAML parseado con `yaml.safe_load` (21 pasos).
- Los cinco scripts de puerta corrieron localmente sobre esta rama antes de escribir el workflow.
- `check_coverage.sh` se probó con reportes Kover sintéticos: ≥ umbral → `COVERAGE-OK`; < umbral → `COVERAGE-FAIL`; sin reporte → lo genera con `koverXmlReportDebug`.

### 2.5 Dudas abiertas

- **Cobertura medida: 28,16 %** (1104/3920, `koverXmlReportDebug`, 2026-11-05) frente al umbral ≥ 45 % de [Constitución §7]: el paso `coverage` **fallará** hasta que suban las pruebas. No se rebaja el umbral; se llena el faltante.
- `lint { warningsAsErrors = true }`: no se ejecutó `lintDebug` en local; si hay avisos previos, `lint` será el primer rojo.
- El paso `ui` solo tiene un test instrumentado (`UserProfileDaoTest`); sirve de humo, no de cobertura.

### 2.6 No-alcance

- **mutación (Pitest)** y **deps-scan (Dependency-Check)** no están en `libs.versions.toml`: añadirlos exige plugin nuevo, acuerdo y excepción documentada ([Constitución §3]). Se dejan para una spec posterior; el paso no se simula en el workflow para no dar un verde falso.
- Sin despliegue de APK ni publicación ([Constitución §9]).

## 3. Contratos (API y navegación)

### 3.1 Workflow

- **Disparadores:** `pull_request` (todo PR) y `push` a `main`. `permissions: contents: read`. Concurrencia por ref con `cancel-in-progress`.
- **Runner:** `ubuntu-latest`, 60 min. Java 21 (temurin), Android SDK con `platforms;android-36`, `gradle/actions/setup-gradle@v4` para caché.
- **Sin `local.properties`:** `app/build.gradle.kts` aporta `wherever.stagingApiBaseUrl` de respaldo, así que `build` no depende de un archivo no versionado.

### 3.2 Puertas locales (mismas en CI)

| Script | Salida esperada |
|---|---|
| `critic-guardrails.sh` | `PASS` (B1-B4: secretos, logs, cadena de red, rutas) |
| `check_routes.sh` | `OK` (rutas tipadas ↔ `docs/routes.md`, ambos sentidos) |
| `check_strings.sh` | `OK` |
| `check_specs.sh` | `SPECS-OK (36 specs)` |
| `check_sdd.sh` | `SDD-OK` |
| `check_coverage.sh --min 45` | `COVERAGE-OK` (hoy: `COVERAGE-FAIL`, ver §2.5) |
| `check_delta_coverage.py --min 80 --advisory` | informa siempre, sale `0` |

### 3.3 Semgrep

`.semgrep/semgrep.yaml` con dos reglas propias, ambas en 0 hallazgos hoy: `wherever-no-destructive-migration` (`fallbackToDestructiveMigration`, prohibido por [Constitución §5]) y `wherever-no-cleartext-http` (`http://` en `app/src/main/java`). El registry (`--config=auto`) corre con `continue-on-error` hasta afinar el baseline.

## 4. Modelo de datos

Sin persistencia nueva. Artefactos: reportes Gradle/Kover y el XML de cobertura que leen `check_coverage.sh` y `check_delta_coverage.py` (contadores `LINE` por `<sourcefile>`). `app/build/` y `.github/` no versionan secretos.

## 5. Seguridad y privacidad

- Workflow de solo lectura (`contents: read`); no hay tokens, claves ni credenciales en `.github/`.
- `critic-guardrails.sh` B1 barre también `.github` y `scripts`; `local.properties` y `google-services.json` deben seguir sin versionar.
- El emulador corre sin datos de la app real; las pruebas usan Room en memoria y `MockWebServer`.
- Sin datos personales en artefactos de CI; los reportes se borran con la caché del runner.

## 6. Tests / Gates

- Unit: parser de cobertura con reportes Kover sintéticos (≥ umbral → `COVERAGE-OK`, < umbral → `COVERAGE-FAIL`) y `check_delta_coverage.py` sin reporte → sale `0` en advisory.
- Data: el reporte real se genera con `./gradlew koverXmlReportDebug` y se lee del XML; baseline 28,16 % medido el 2026-11-05.
- Security: `critic-guardrails.sh` en `PASS` sobre la rama completa (incluye `.github`) y `semgrep.yaml` sin hallazgos; workflow sin secretos.
- UI: sin pantalla nueva; el paso `ui` ejecuta `connectedDebugAndroidTest` en emulador API 35 con KVM.
- Gates: `check_specs.sh`, `check_routes.sh`, `check_strings.sh`, `check_sdd.sh` en verde; cobertura ≥ 45 % y delta ≥ 80 % (advisory) según `CHECKLIST_PR.md`.

## 7. Plan de entrega

Slice de infraestructura, sin pantallas: `scripts → .semgrep → workflow → spec → evidencia local`. Requiere `nv-00`…`nv-03` ejecutadas y esta rama sincronizable en Android Studio. `plan.md` y `task.md` se generan al tomar la spec. El tag se corta cuando el workflow esté en verde en GitHub.

## Aprobación

- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
