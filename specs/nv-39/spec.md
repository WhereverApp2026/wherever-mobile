# NV-39 — CI/CD: gates del push y del pull request

> Rector: `.specify/memory/constitution.md` (manda ante conflicto). Plantilla: `.specify/templates/spec-template.md`.
> Convención de esta spec: **[Constitución §n]** = no negociable. **[Decisión propuesta]** = la constitución no lo define; se propone con su porqué.

## 1. Objetivo

Que cada push y cada pull request corran en GitHub Actions, **en el orden de [Constitución §8]**, las mismas puertas que `AGENTS.md §4` exige en local, sin CD ([Constitución §8]). El PR es integrable solo con el workflow en verde. El APK de debug queda como artefacto del run ([AGENTS.md §9]).

Traslada a CI lo que `nv-03` dejó escrito pero no ejecutable: los scripts de puerta y el registro de rutas, suites y cobertura.

## 2. Alcance / No-alcance

**In:** `.github/workflows/ci-pr.yml`, `.github/dependabot.yml`, `.gitleaks.toml`, los scripts de puerta (`critic-guardrails`, `check_routes`, `check_strings`, `check_specs`, `check_sdd`, `check_coverage`, `check_delta_coverage`, `check_deps_osv`), el umbral de Kover en `app/build.gradle.kts`, `.semgrep/semgrep.yaml`, esta spec y su fila en `specs/README.md`.

**Out:** despliegue o publicación ([Constitución §8]); mutación (Pitest no está en el build); el plugin OWASP Dependency-Check (ver §2.6); MobSF/Frida y firma automática (diferidos, [Constitución §6]); reintento de tests flaky; pruebas contra el servidor de staging.

### 2.1 Historias y criterios de aceptación

- **Como revisor**, quiero ver en el PR el estado de todas las puertas para saber si se puede fusionar.
- **Como desarrollador**, quiero el mismo orden en CI que en local para que un verde local sea significativo.
- **Como equipo**, queremos descargar el APK de debug de cada run para probarlo en un teléfono.
- Criterios:
  - [ ] `ci-pr.yml` corre en cada `push` y cada `pull_request`, ejecuta los pasos en el orden §8 y el YAML parsea con `yaml.safe_load`.
  - [ ] `ktlintCheck`, `detekt` (JDK 17), `lintDebug`, `testDebugUnitTest`, Kover con umbral 45 %, `assembleDebug` y `connectedDebugAndroidTest` en emulador (`reactivecircus/android-emulator-runner`) son bloqueantes.
  - [ ] Revisión de seguridad: secretos con gitleaks (bloqueante), dependencias vulnerables con OSV (informativo, [Constitución §6]) y lint de seguridad (Android Lint `Security` + Semgrep propio, bloqueantes).
  - [ ] El APK de debug se sube como artefacto `wherever-debug-apk`.
  - [ ] Los scripts de puerta pasan sobre esta rama: `PASS`, `OK`, `SPECS-OK`, `SDD-OK`, `COVERAGE-OK`.
  - [ ] `semgrep` con las reglas propias da 0 hallazgos; el registry corre como advisory.
  - [ ] El workflow no contiene secretos ni despliega nada.

### 2.2 Orden de pasos (constitución §8)

| # | Paso en `ci-pr.yml` | Comando | Bloqueante |
|---|---|---|---|
| 1 | `deps` | `./gradlew :app:dependencies --configuration debugRuntimeClasspath` → `build/deps.txt` | sí |
| 2 | `build` | `./gradlew assembleDebug` + artefacto `wherever-debug-apk` | sí |
| 3 | `lint` | `./gradlew ktlintCheck detekt lintDebug` (JDK 17) | sí |
| 4-5 | `unit · data · security` | `./gradlew testDebugUnitTest` | sí |
| 6 | `security · secretos` | `gitleaks git . --config .gitleaks.toml --redact` | sí |
| 7 | `ui · data instrumentada` | `./gradlew connectedDebugAndroidTest` (emulador API 35, `google_apis`) | sí |
| 8 | `critic` | `scripts/critic-guardrails.sh` | sí |
| 9 | `routes` | `scripts/check_routes.sh` | sí |
| — | `strings` | `scripts/check_strings.sh` | sí (AGENTS §4) |
| — | `specs` | `scripts/check_specs.sh` | sí (AGENTS §4) |
| 10 | `sdd` | `scripts/check_sdd.sh` | sí |
| 11 | `coverage` | `./gradlew koverXmlReportDebug koverVerifyDebug` + `scripts/check_coverage.sh --min 45` | sí |
| 12 | `delta-check` | `scripts/check_delta_coverage.py --min 80 --advisory` | no (advisory) |
| 13 | `mutation` | — | no configurado (§2.6) |
| 14 | `semgrep` | `--config=.semgrep/semgrep.yaml` (propias) / `--config=auto` (registry) | sí / no |
| 15 | `deps-scan` | `scripts/check_deps_osv.py build/deps.txt --advisory` | no (informativo, [Constitución §6]) |

**[Decisión propuesta]** Las suites Unit, Data (integración con MockWebServer) y Security de [Constitución §7] son un solo paso porque viven en el mismo source set (`app/src/test`): `testDebugUnitTest` las ejecuta juntas, como ya dice `AGENTS.md §1`. Separarlas repetiría el mismo task sin ganar señal. La parte instrumentada (Room real, UI) va en el paso 7.

### 2.3 Estados

- **Verde:** todas las puertas en `0` → el PR puede fusionarse con el revisor de `CHECKLIST_PR.md`.
- **Rojo:** el paso fallido nombra la puerta; `Reports` sube `app/build/reports/` para diagnóstico.
- `continue-on-error` solo en lo advisory: `delta-check`, `semgrep --config=auto` y `deps-scan`.
- `Reports` sube siempre (`if: always()`) los reportes de Gradle, lint, Kover, JUnit e instrumentadas.

### 2.4 Validaciones

- YAML parseado con `yaml.safe_load`.
- Los scripts de puerta corrieron localmente sobre esta rama antes de escribir el workflow.
- `check_coverage.sh` se probó con reportes Kover sintéticos: ≥ umbral → `COVERAGE-OK`; < umbral → `COVERAGE-FAIL`; sin reporte → lo genera con `koverXmlReportDebug`.

### 2.5 Dudas abiertas

- **[Decisión propuesta] Cobertura, a revisar.** Medido el 2026-11-05 tras las pruebas nuevas: **71,99 %** (1113/1546) excluyendo código generado y funciones `@Composable`/`@Preview`; **37,17 %** (1113/2994) excluyendo solo código generado. Antes de las pruebas nuevas, Diego midió 28,16 % (1104/3920) sin exclusiones. Porqué de la exclusión: Kover solo mide pruebas JVM y las pantallas se prueban con Compose Testing en el paso `ui`. Si el revisor no la acepta, `koverVerifyDebug` falla hasta escribir pruebas de UI o subir las de JVM; el umbral de 45 % no se rebaja.
- Semgrep registry (advisory) informa: acciones con etiqueta mutable (`@v4`, no fijadas por SHA), la actividad launcher exportada (necesaria), AES-GCM en `KeystoreTokenCipher` (correcto: IV aleatorio de Keystore) y `xml.etree` en `check_delta_coverage.py` (lee un XML local de Kover).
- El paso `ui` corre en API 35; los teléfonos de prueba usan Android 16. Subir la imagen cuando el runner la tenga en caché.
- Un push a la rama de un PR abierto corre el workflow dos veces (evento `push` y `pull_request`). Se acepta por el pedido "en cada push y PR".

### 2.6 No-alcance

- **mutación (Pitest)** no está en `libs.versions.toml`: añadirlo exige plugin nuevo, acuerdo y excepción documentada ([Constitución §3]). Se deja para una spec posterior; el paso no se simula en el workflow para no dar un verde falso.
- **[Decisión propuesta]** `deps-scan` usa la base pública OSV (`api.osv.dev`) con `scripts/check_deps_osv.py` en lugar del plugin OWASP Dependency-Check. Porqué: Dependency-Check descarga la base NVD completa en cada run sin clave de API (decenas de minutos), y la clave sería un secreto en CI; OSV no pide clave ni librería nueva. Si el equipo prefiere Dependency-Check, cambia solo este paso.
- Sin despliegue de APK ni publicación ([Constitución §8]).

## 3. Contratos (API y navegación)

### 3.1 Workflow

- **Disparadores:** `push` (cualquier rama) y `pull_request` (todo PR). `permissions: contents: read`. Concurrencia por ref con `cancel-in-progress`.
- **Runner:** `ubuntu-latest`, 90 min. Java 17 (temurin) para todo el build, porque detekt 1.23 no corre sobre JDK > 22. Android SDK con `platforms;android-36`, `gradle/actions/setup-gradle@v4` para caché.
- **Sin `local.properties`:** `app/build.gradle.kts` aporta `wherever.stagingApiBaseUrl` de respaldo, así que `build` no depende de un archivo no versionado. Ninguna prueba llama a esa URL.
- **Emulador:** `reactivecircus/android-emulator-runner@v2`, API 35 `google_apis` x86_64 con KVM, sin ventana ni animaciones.
- **Artefactos:** `wherever-debug-apk` y `reports`, 14 días.
- **Gitleaks:** binario oficial `v8.28.0` de GitHub Releases, verificado con su `checksums.txt`. No se usa `gitleaks-action` porque en cuentas de organización pide una licencia como secreto.
- **Dependabot:** `.github/dependabot.yml` semanal para Gradle y Actions ([AGENTS.md §6]).

### 3.2 Puertas locales (mismas en CI)

| Script | Salida esperada |
|---|---|
| `critic-guardrails.sh` | `PASS` (B1-B4: secretos, logs, cadena de red, rutas) |
| `check_routes.sh` | `OK` (rutas tipadas ↔ `docs/routes.md`, ambos sentidos) |
| `check_strings.sh` | `OK` |
| `check_specs.sh` | `SPECS-OK` |
| `check_sdd.sh` | `SDD-OK` |
| `check_coverage.sh --min 45` | `COVERAGE-OK` |
| `check_delta_coverage.py --min 80 --advisory` | informa siempre, sale `0` |
| `check_deps_osv.py build/deps.txt --advisory` | `DEPS-OK` o `DEPS-ADVISORY` con la lista; sale `0` |

### 3.3 Kover

`app/build.gradle.kts` declara `kover.reports.verify` con `minBound(45)` sobre líneas ([Constitución §7]). `koverVerifyDebug` falla bajo el umbral. Exclusiones y medición: `plan.md §3`.

### 3.4 Semgrep

`.semgrep/semgrep.yaml` con dos reglas propias, ambas en 0 hallazgos: `wherever-no-destructive-migration` (`fallbackToDestructiveMigration`, prohibido por [Constitución §5]) y `wherever-no-cleartext-http` (`http://` en `app/src/main/java`). El registry (`--config=auto`) corre con `continue-on-error` hasta afinar el baseline.

## 4. Modelo de datos

Sin persistencia nueva. Artefactos: reportes Gradle/Kover, el XML de cobertura que leen `check_coverage.sh` y `check_delta_coverage.py` (contadores `LINE` por `<sourcefile>`) y `build/deps.txt` que lee `check_deps_osv.py`. `app/build/` y `.github/` no versionan secretos.

## 5. Seguridad y privacidad

- Workflow de solo lectura (`contents: read`); no hay tokens, claves ni credenciales en `.github/`, ni `secrets.*` referenciados.
- Gitleaks revisa la historia completa con `--redact` (el log no imprime el valor encontrado). `.gitleaks.toml` solo permite los ejemplos públicos de `docs/contexto_api_sprint1.md` y las constantes de prueba.
- `critic-guardrails.sh` B1 barre también `.github` y `scripts`; `local.properties` y `google-services.json` siguen sin versionar.
- El emulador corre sin datos de la app real; las pruebas usan Room en memoria y `MockWebServer`.
- Sin datos personales en artefactos de CI.

## 6. Tests / Gates

- Unit: parser de cobertura con reportes Kover sintéticos (≥ umbral → `COVERAGE-OK`, < umbral → `COVERAGE-FAIL`) y `check_delta_coverage.py` sin reporte → sale `0` en advisory.
- Data: el reporte real se genera con `./gradlew koverXmlReportDebug` y se lee del XML; `check_deps_osv.py` lee la salida real de `:app:dependencies`.
- Security: `critic-guardrails.sh` en `PASS` sobre la rama completa (incluye `.github`), `semgrep.yaml` sin hallazgos, gitleaks sin hallazgos y workflow sin secretos.
- UI: sin pantalla nueva; el paso `ui` ejecuta `connectedDebugAndroidTest` en emulador API 35 con KVM.
- Gates: `check_specs.sh`, `check_routes.sh`, `check_strings.sh`, `check_sdd.sh` en verde; cobertura ≥ 45 % y delta ≥ 80 % (advisory) según `CHECKLIST_PR.md`.

## 7. Plan de entrega

Slice de infraestructura, sin pantallas: `scripts → .semgrep → workflow → spec → evidencia local`. Implementa la parte de CI de `nv-03`. Ver `plan.md`, `task.md` y `checklists/gates.md`. El tag se corta cuando el workflow esté en verde en GitHub.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Implementada y aprobada (Jorge, 2026-11-06).
