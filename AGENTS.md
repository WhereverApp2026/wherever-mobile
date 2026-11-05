# Agents.md — Wherever Móvil

> Rector: `.specify/memory/constitution.md` manda ante conflicto. Espejo: `./constitution.md`.
> Este archivo dice **cómo** trabajar. La constitución dice **qué** no se negocia.

## 1. Forma de trabajo
> Stack: ver `constitution.md §3`. Nada fuera sin excepción documentada.
- Setup: `local.properties.example → local.properties` con `API_BASE_URL` (ej. `https://10.0.2.2:8443/api/v1/` para el emulador con certificado de desarrollo); `google-services.json` en `app/` (no se sube a Git); `./gradlew assembleDebug`.
- Comandos: `./gradlew assembleDebug`, `./gradlew ktlintCheck detekt lintDebug`, `./gradlew testDebugUnitTest` (Unit, Data, Security, Arquitectura), `./gradlew connectedDebugAndroidTest` (UI y migraciones), `./gradlew koverVerify`.
- CI `ci-pr.yml` (ubuntu-latest): deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan. Sin deploy. `mutation` y `deps-scan` aún no configurados: ver `specs/035-ci-cd` §2.6.
- PR = 1 slice (funcionalidad); `CHECKLIST_PR.md` + 1 revisor + `Memoria.md` al día.
- Naming: inglés en código y paquetes; textos en español en `strings.xml`.
- Sin secretos, sin TODO sin issue, sin imports muertos, sin código muerto.

## 2. Memoria
- Plantilla canónica `.specify/templates/spec-template.md` (alias `specs/_template.md`), DoD por funcionalidad en `nv-04+`.
- `AGENTS.md`: manual operativo vivo.
- `Memoria.md`: estado, decisiones y lecciones aprendidas por fase. Actualizar al cerrar cada fase.
- Al empezar, lee `Memoria.md` para conocer el estado del proyecto y las decisiones tomadas.
- Al terminar una tarea, actualízalo: estado actual, decisiones importantes (con su porqué) y errores a evitar.
- Al cerrar una fase, concilia su `spec.md`/`plan.md` con lo implementado (rutas de tests, criterios, nombres reales de archivos) y resuelve el bloque de aprobación: Borrador con evidencia, o firmado solo si el usuario indicó revisor.
- Mantenlo breve (máximo ~10000 líneas): resume o elimina lo que ya no aporte.
- Si algo se convierte en regla permanente, propón moverlo a `AGENTS.md`.
- No guardes nunca datos sensibles (claves, tokens, datos personales).

## 3. Límites — no hacer
- No lógica de negocio en Composables.
- No tokens en `SharedPreferences` sin cifrar, ni en logs.
- No hardcodear URLs ni llaves; todo por `BuildConfig`/`local.properties`.
- No `fallbackToDestructiveMigration`.
- No `Dispatchers.IO` fijo en lógica de negocio: se inyecta.
- No librerías fuera del stack sin acuerdo y excepción documentada.
- No tocar la cadena de red sin el orden de la constitución §4.
- No duplicar tablas vivas: lo canónico está en `docs/`.
- No incluir pantallas del producto en `nv-00…nv-03`.

## 4. Verificación por funcionalidad
- `assembleDebug` sin errores antes de test.
- `Unit/Data/Security/UI/Arquitectura` 100% verdes.
- `scripts/critic-guardrails.sh → PASS`, `scripts/check_sdd.sh → SDD-OK`, `scripts/check_specs.sh → SPECS-OK`, `scripts/check_routes.sh → OK`, `scripts/check_strings.sh → OK`, `scripts/check_coverage.sh --min 45`, `check_delta_coverage.py --min 80 --advisory` informa (no bloquea), `semgrep scan --config=.semgrep/semgrep.yaml --error` 0 hallazgos y `semgrep scan --config=auto` advisory.
- Mutación PR advisory (Pitest sobre lo cambiado, `continue-on-error`) + nightly opcional; tras ella siempre `assembleDebug`. Pitest no está en el build: pendiente en `specs/035-ci-cd` §2.6.
- La app abre sin red y muestra el estado correspondiente.

## 5. Convenciones técnicas
- `compileSdk` y `targetSdk` 36, `minSdk` 26 (no hay SDK 37 en este equipo; ver `app/build.gradle.kts`).
- Un `UiState` por pantalla; el ViewModel lo expone con `StateFlow`; el Composable envía eventos.
- Room fuente de verdad; el repositorio guarda lo que llega de la red y la UI lee de Room.
- Cadena de red: `RequestId → AuthHeader → RedactedLogging`; `Authenticator` renueva el token una vez.
- Errores de la API → `AppError`; paginación por cursor `PageResult`.
- Textos en `strings.xml`; colores y tipografías solo desde `core/designsystem`.
- Navegación tipada con rutas registradas en `docs/routes.md`.
- Molde `nv-02`: `Result<T>` + `AppError` + `PageResult` + `Repository` base + `UiState` demo sin pantalla.

## 6. Seguridad DevSecOps
- Tokens cifrados con Keystore; refresh seguro; sesión limpia si falla.
- `networkSecurityConfig` solo HTTPS; `allowBackup` controlado.
- Permisos en contexto; la app funciona sin ellos.
- SAST Semgrep bloqueante; Dependency-Check informativo; `Dependabot` activo. MobSF/Frida y firma automática diferidos, documentados, no exigidos.

## 7. Testing quirks
- JUnit + MockK + Turbine; `MockWebServer` y Room en memoria para datos; Compose Testing para UI; Konsist para arquitectura.
- Dispatchers y relojes se inyectan; el estado compartido se reinicia en `@Before`.
- `MigrationTestHelper` con los esquemas exportados.
- Las pruebas instrumentadas necesitan emulador; no corren en paralelo local con otro emulador abierto.
- Buscar nodos por `testTag` o por texto de `strings.xml`, nunca por texto fijo.

## 8. Observabilidad mínima
- Logs con redacción y `request_id`. Sin Crashlytics/analítica por ahora.

## 9. Operación manual — sin publicación aún
- APK de depuración como artefacto de CI. La firma y la publicación en Google Play se especifican en una spec posterior.
- Verificación manual en emulador con el backend de desarrollo.

## 10. Lecciones
- Fuente: `Memoria.md`. Si se vuelve regla, mover a `AGENTS.md`.

## 11. Sub-agentes
- `foundation-builder` ejecuta `nv-00/nv-01`; `slice-scaffolder` desde `nv-04`; `security-reviewer` gate sin editar; `traceability-clerk` reporta drift sin editar; `readme-keeper` edita solo `**/*.md` (`bash: deny`).
- En CI solo el `critic` ligero con bloqueantes B1-B4.

## Solution structure
| Carpeta | Tipo |
|---|---|
| `app/src/main/java/.../core/designsystem` | Tema, colores, tipografía, componentes |
| `.../core/network` | Retrofit, interceptores, autenticador, errores |
| `.../core/database` | Room y migraciones |
| `.../core/security` | Almacenamiento cifrado de tokens |
| `.../core/sync` | WorkManager |
| `.../core/notifications` | FCM |
| `.../core/di` | Módulos de Hilt |
| `.../navigation` | Grafo y rutas |
| `.../feature/<nombre>` | `ui`, `domain`, `data` (`nv-04+`) |
| `app/schemas` | Esquemas de Room exportados |
| `app/src/test` · `app/src/androidTest` | Unit/Data/Security/Arquitectura · UI y migraciones |
| `scripts/` | critic, routes, strings, sdd, coverage, delta |
| `docs/` | routes, permissions-matrix, masvs-checklist |
| `.semgrep/` | Reglas propias |
