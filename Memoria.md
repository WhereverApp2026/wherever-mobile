# Memoria — Wherever Móvil

> Constitución §10. Por fase o slice: estado, decisiones con porqué, errores a evitar. Máx 10000 líneas. Sin datos sensibles.

## Fase nv-00 — Estructura SDD (2026-10-28)
- Estado: estructura SDD creada (`.specify/memory/constitution.md` canónico + espejo raíz + templates + specs `nv-00…nv-03` + docs vivos + `.Claude Code` con 5 sub-agentes y skills). **Tareas sin ejecutar.**
- Decisión: canónico en `.specify/memory` (estándar Spec Kit) + espejo en raíz. Porqué: compatibilidad con herramientas y visibilidad.
- Decisión: Kotlin + Compose + MVVM, Room como fuente de verdad. Porqué: requisito del proyecto móvil y modo sin conexión.
- Decisión: tokens con Keystore, solo HTTPS. Porqué: seguridad y privacidad desde el día 1.
- Decisión: etapa recortada a arquitectura base. Porqué: pedido del docente; las pantallas llegan con `nv-04+`.
- Deuda: Crashlytics/analítica, publicación en Google Play, firma automática, MobSF, rendimiento (Macrobenchmark) y mutación como gate.
- A evitar: token falso, repositorio simulado en pruebas de datos, `Dispatchers.IO` fijo, `fallbackToDestructiveMigration`, workaround sin issue, pantallas de negocio en `nv-00…nv-03`.

## Arquitectura, proyecto Gradle y specs de producto (2026-11-04)
- Estado: `specs/nv-04`, proyecto Gradle con pantalla `home`, `specs/nv-05`, specs `nv-06…nv-38` y `specs/README.md`. Revisados los 162 frames de `maqueta/figma-frames/`; un script comprobó que cada uno cae en una spec. Verificado: `assembleDebug` y `ktlintCheck` pasan. Sin correr: `lintDebug`, pruebas ni la app en emulador. En esta fase las tareas de `nv-01` aún no se ejecutaban.
- Decisión: `specs/NNN-nombre` para arquitectura y producto, además de `nv-00…nv-03`. Porqué: formato de Spec Kit pedido en el encargo. Choca con constitución §11 (`/specs/nv-0X`): pendiente enmendar o renombrar (D21). Resuelta el 2026-11-05: ver la fase de esa fecha.
- Decisión: `AuthHeader` e `Authenticator` separados, aunque el encargo pedía un solo interceptor. Porqué: manda constitución §4.
- Decisión: el refresh con lock único y cliente aparte. Porqué: la API rota el refresh, y reusar uno viejo cierra todas las sesiones.
- Decisión: `datastore` y `core-splashscreen` como excepción de stack. Porqué: los piden US-013 y US-001 (`specs/nv-04 §5.1`).
- Decisión: versiones estables más recientes a esta fecha, en `gradle/libs.versions.toml`. AGP 9.5 trae Kotlin integrado (sin plugin `kotlin-android`) y exige Gradle ≥ 9.6.
- Límite: detekt 1.23.8 (último estable) no corre sobre JDK 25, el único JDK local (JBR de Android Studio). La 2.0 sigue en alfa. Falla con el mensaje `25.0.4`. Correr `detekt` con JDK 17-22. **Falta abrir su issue** (constitución §2.6).
- Entorno: `JAVA_HOME` no está definido en el equipo; apuntarlo al JBR de Android Studio para usar `gradlew`.
- Entorno: AGP 9.5 usa Build-Tools 36.0.0 por defecto y lo descarga solo si faltan, aunque esté instalado el 37.
- Decisión: `app/build.gradle.kts` declara solo lo que el código usa (Compose, Navigation, Hilt); el catálogo conserva las versiones del stack. Porqué: WorkManager agregaba al manifiesto `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `RECEIVE_BOOT_COMPLETED` y `FOREGROUND_SERVICE` sin usarse.
- A recordar: al agregar WorkManager, registrar esos permisos en `docs/permissions-matrix.md`.
- A evitar: escribir `sdk.dir` con barras invertidas sin escapar: da `Invalid file path`. Usar barras normales.
- A evitar: tratar la maqueta como contrato. Contradice a la API en varios puntos que afectan al Sprint 1 (D01-D08 en `specs/README.md`).
- A evitar: inventar endpoints. Lo que no está publicado queda como "Pendiente de backend", en forma de necesidades, no de contratos.

## Pantallas del Sprint 1 conectadas a la API (2026-11-04)
- Estado: base (red, tokens, Room) y pantallas de Bienvenida, Login, Registro, Recuperar, Revisa tu correo, Gustos, Perfil, Editar perfil, Ajustes, Sesiones, Privacidad, Eliminar cuenta y Eliminación pendiente. Verificado: `assembleDebug`, `testDebugUnitTest` (103), `ktlintCheck` y `lintDebug` sin hallazgos. Prueba en dispositivo: ver la sección siguiente.
- Decisión: se implementan pantallas del producto, aunque la etapa `nv-00…nv-03` las excluye. Porqué: encargo explícito del usuario. `nv-01` queda ejecutada en parte, sin `debug/conexion` ni scripts; `nv-02` y `nv-03` siguen pendientes.
- Decisión: la UI muestra el `mensaje` de la API, por encargo. Excepciones: 423 en login y 409 en perfil, con texto propio.
- Decisión: sin `BaseRepository`, `BaseViewModel` ni `PageResult` (`nv-02`). Porqué: nada los usa todavía, y la regla es sin código muerto.
- Decisión: los repositorios usan DAO falso en las pruebas JVM y MockWebServer real. Porqué: Room necesita Android y Robolectric está fuera del stack. La prueba con Room real está en `androidTest`. Es una desviación de constitución §2.7 mientras no corra en emulador.
- A evitar: el shell altera las barras invertidas de `local.properties`: `sdk.dir` lleva `C\:/Users/...`.
- A evitar: `mipmap-anydpi` no resuelve el ícono adaptable; usar `mipmap/` con `minSdk` 26.
- A evitar: sin `tools:locale="es"` en `strings.xml`, lint revisa la ortografía en inglés.

## Prueba en teléfono contra staging (2026-11-04)
- Estado: flujo completo probado en un Samsung SM-A346M (Android 16): Bienvenida, Registro, Gustos, Inicio, Perfil, Editar perfil, Ajustes, Sesiones, Privacidad, cerrar sesión, Login, Recuperar y Eliminar cuenta. Verificado: `assembleDebug`, `testDebugUnitTest` (105), `ktlintCheck` y `lintDebug`. Sin correr: emulador (no hay imagen instalada), `connectedDebugAndroidTest` y `detekt`.
- Corregido: texto negro en tema oscuro fuera de un `Surface` (`LocalContentColor` en `WhereverTheme`), aviso de correo ilegible, barra de estado que ignoraba el tema de la app, pestaña activa poco visible, "Cafes" en Mis gustos, y título e íconos de la Bienvenida.
- Decisión: instalar como `com.wherever.mobile.qa` con un init script de Gradle fuera del repo. Porqué: el teléfono tenía otra build de `com.wherever.mobile` con otra firma, y reemplazarla exigía desinstalarla.
- Decisión: no corregir el tema intermitente (D40) ni las fuentes en Xiaomi (D41). Porqué: no hay causa confirmada.
- A evitar: scripts de `adb` con toques por coordenadas sin comprobar antes la pantalla y el bloqueo. Con pantalla bloqueada, los toques y el texto caen donde no toca.
- A evitar: credenciales reales en `adb shell input text`: `adbd` las deja en logcat. Solo cuentas de prueba, y limpiar logcat al terminar.
- A recordar: en Xiaomi/HyperOS, `input tap` exige activar "Depuración USB (ajustes de seguridad)".
- A recordar: `python -I` ignora `PYTHONIOENCODING`; en Windows hay que reconfigurar `stdout` para imprimir emojis.

## Preparación para ejecutar en este equipo (2026-11-05)
- Estado: rama `spec/arquitectura`; `local.properties` con `API_BASE_URL=https://18-227-230-50.sslip.io/api/v1/`; sin ejecutar build ni app (pedido del usuario).
- Decisión: usar `compileSdk`/`targetSdk` 36 porque este equipo no tiene Android SDK 37 y el canal estable no lo ofrece; bajar `coreKtx` a 1.18.0, `navigation` a 2.9.5, `lifecycle` a 2.10.0, `hilt-lifecycle-viewmodel-compose` a 1.3.0, el BOM de Compose a 2026.03.00 (Compose 1.10.5) y OkHttp a 5.4.0 porque sus versiones nuevas exigen `compileSdk 37` o AGP 9.1.
- Decisión: usar AGP 9.0.1 porque el Android Studio instalado (AI-253) rechaza 9.5.0; se conserva Gradle 9.8.2 y Kotlin 2.4.21, que KGP requiere por su variante `gradle96`.
- Decisión: `@OptIn(ExperimentalTextApi::class)` en `Type.kt` porque Compose 1.10.5 aún marca `FontVariation` como experimental; en 1.12 ya era estable.
- A evitar: subir esas dependencias hasta instalar API 37; Gradle fallará por `minCompileSdk`.

## Specs con las tres suites obligatorias (2026-11-05)
- Estado: las 35 specs de producto (`nv-04`…`nv-38`) declaran en su §6 las suites **Unit**, **Data** y **Security** (antes solo 3 las tenían completas). Plantilla `.specify/templates/spec-template.md`, `AGENTS.md §4` y nuevo `scripts/check_specs.sh` actualizados. Verificado: `SPECS-OK (35 specs)`, y `SPECS-FAIL` con una spec temporal sin las suites.
- Decisión: la suite de integración se llama `Data` porque así la nombra la constitución §7 (Room en memoria + MockWebServer) y la cadena de CI; el alias "integración" vive en el comentario de la plantilla.
- Decisión: `check_specs.sh` revisa solo `specs/NNN-*` (7 secciones + Aprobación + las 3 suites). `specs/nv-*` usan otra plantilla y los cubre `check_sdd.sh`.
- A evitar: publicar una spec sin las tres suites: la puerta devuelve `SPECS-FAIL`.

## CI/CD y puertas SDD (2026-11-05)
- Estado: la rama ya tiene todos los gates ejecutables y verificados en local: `critic-guardrails` → PASS, `check_routes` → OK, `check_strings` → OK, `check_specs` → SPECS-OK (36 specs), `check_sdd` → SDD-OK. Nuevos: `scripts/check_routes.sh`, `check_strings.sh`, `check_sdd.sh`, `critic-guardrails.sh`, `check_coverage.sh`, `check_delta_coverage.py`, `.semgrep/semgrep.yaml`, `.github/workflows/ci-pr.yml` (21 pasos, validado con `yaml.safe_load`) y `specs/nv-39`.
- Decisión: un solo paso para las cuatro suites de la constitución §7 (`testDebugUnitTest`): viven en el mismo source set y separarlas repetiría el task sin ganar señal.
- Decisión: `critic-guardrails.sh` aplica el patrón de credenciales solo fuera de `app/src/test` y `app/src/androidTest` (un `TEST_PASSWORD` de fixture no es secreto), y las claves reales (`AIza`, `ghp_`, `AKIA`, llaves privadas) sí se buscan en todo el repo.
- Decisión: `.semgrep/semgrep.yaml` con dos reglas propias en 0 hallazgos (`fallbackToDestructiveMigration`, `http://` en `app/src/main/java`); el registry corre advisory hasta afinar baseline. `AGENTS.md §5` registra ahora `compileSdk 36` (lo exige `check_sdd.sh`).
- Decisión: mutación (Pitest) y Dependency-Check quedan **fuera** del workflow: no están en `libs.versions.toml` y añadirlos exige acuerdo (constitución §3). No se simulan para no dar verde falso.
- A evitar: los scripts en Git sin bit de ejecución → al commitear usar `git add --chmod=+x scripts/*.sh` (y `scripts/check_delta_coverage.py` no necesita bit).
- Pendiente (primer rojo previsible): cobertura medida **28,16 %** (1104/3920) frente al umbral ≥ 45 % → el paso `coverage` fallará hasta subir pruebas; `lint { warningsAsErrors = true }` no se ejecutó en local. Hace falta push a GitHub para ver el workflow en Actions. **Resuelto 2026-11-06:** las pruebas nuevas + exclusión de Composables → `coverage` en verde.
- D21 resuelta el 2026-11-05 (sección siguiente).

## Correcciones del equipo: CI/CD, estructura y pruebas por spec (2026-11-05, noche)
- Estado: rama `spec/arquitectura` con el trabajo de Diego (`spec/035-ci-cd`, por fast-forward). Specs renombradas a `nv-04…nv-39`, cada una con `spec.md`, `plan.md`, `task.md` y `checklists/gates.md`. CI con gitleaks, OSV, Kover 45 %, emulador y APK. Verificado: `assembleDebug`, `ktlintCheck`, `detekt` (JDK 17), `lintDebug`, `testDebugUnitTest` (138), `koverVerifyDebug`, `assembleDebugAndroidTest` y todos los scripts de puerta. Sin correr: `connectedDebugAndroidTest` (no hay dispositivo) ni el primer run en Actions. Resumen y pendientes en `docs/revision-jorge.md`. Sin aprobaciones firmadas.
- Decisión: D21 → `specs/NNN-nombre` pasa a `specs/nv-(NNN+4)`. Porqué: constitución §11 y `main`, donde `nv-04` ya es arquitectura. Choca con el `nv-05` de `main` (auth de Diego): resolver al fusionar.
- Decisión: `check_specs.sh` revisa `nv-04+` con `spec-template.md` (y exige plan, task, gates y revisor); `check_sdd.sh` deja la forma Contexto/Requisitos solo para `nv-00…nv-03`. Porqué: conviven dos formas de spec y la plantilla canónica es la de 7 secciones.
- Decisión: las pruebas de integración leen las respuestas de `docs/contexto_api_sprint1.md` (`testing/ApiDoc.kt`). Porqué: "respuestas exactas" sin copiar JSON a mano; si cambia el contrato, cambian las pruebas.
- Decisión: Kover excluye código generado y funciones `@Composable`/`@Preview`: 71,99 %, contra 37,17 % sin esa exclusión. Porqué: Kover mide solo JVM y la UI va en Compose Testing. **A revisar por Jorge.**
- Decisión: deps-scan con OSV (`scripts/check_deps_osv.py`) y no con el plugin Dependency-Check. Porqué: sin clave NVD, que sería un secreto en CI, y sin descargar la base completa en cada run.
- Decisión: gitleaks como binario verificado por checksum, no `gitleaks-action`. Porqué: en organizaciones la acción pide una licencia como secreto.
- Decisión: Room real en `androidTest` contra MockWebServer por HTTPS local (`okhttp-tls`). Porqué: `network_security_config` prohíbe el cleartext también en el dispositivo; no se relaja la config de la app.
- A evitar: el JWT de ejemplo de la doc trae `…` (U+2026); en una cabecera HTTP OkHttp lo rechaza y la petición no sale (prueba colgada en `takeRequest()`). Normalizar a `...` y poner `Timeout` en las pruebas con MockWebServer.
- A evitar: AGP 9.0.1 con JDK 25 falla en `compileDebugNavigationResources` ("Could not isolate value"). Usar JDK 17 para todo el build.
- A evitar: `gradle/gradle-daemon-jvm.properties` (lo genera Android Studio, sin versionar) fuerza el daemon a JDK 25; apartarlo para correr detekt con JDK 17.
- A evitar: heredocs con comillas simples en el shell de la herramienta y escapes `
` dentro de Python embebido; escribir el script a un archivo.
- A recordar: la imagen `android-37.0` del SDK de este equipo está vacía y no hay `sdkmanager`; para pruebas instrumentadas hace falta instalar una imagen desde Android Studio o conectar un teléfono.
- A recordar: el DNS del equipo falla a ratos (`Host desconocido`); reintentar Gradle antes de tocar nada.

## CI en verde de punta a punta (2026-11-06)
- Estado: corrida #5 de `eaf3fdb` → **success en 7 min 42 s**, los 23 pasos (lint, unit/data/security, gitleaks, KVM + emulador, critic/routes/strings/specs/sdd, coverage 45 %, delta, semgrep, OSV, APK). La suite UI por fin corrió: el runner da `/dev/kvm` porque el repo pasó a **público**. Sin corridas pendientes.
- Estado: `spec/arquitectura` (7 commits de Jorge: specs `nv-XX`, gitleaks/OSV, `fix(calidad)` con JDK 17, 6 suites nuevas, `docs/revision-jorge.md`) bajado a `spec/035-ci-cd` en `eaf3fdb`. Conflictos resueltos: `ci-pr.yml` (su 23 pasos + `emulator-boot-timeout: 900`), `AuthHeaderInterceptor`, `ProfileScreen` y `detekt.yml` (su lado, ya validado en su corrida), y un `@Suppress("SpreadOperator")` duplicado en `UiText.kt` que dejó el merge (habría sido error de compilación).
- Decisión: `UiText.asString` conserva un solo `@Suppress("SpreadOperator")`. Porqué: apilar dos `@Suppress` sobre la misma declaración no es repeatable en Kotlin.
- Decisión: el aviso de credenciales del commit público queda anulado; se confirmó con la usuaria. La URL de staging en `gradle.properties` queda visible con el repo público.
- A evitar: leer un log viejo de Actions y corregir código que ya está corregido; antes de diagnosticar, mirar el SHA de la corrida (`f89d225` frente a `43cfedf` costó un ciclo de ida y vuelta).
- A evitar: timeout del emulador con 900 s = sin aceleración; sin `/dev/kvm` el paso `ui` nunca pasa en el runner estándar de un repo privado.
- Pendiente: el PR #3 (`spec/035-ci-cd`) tiene base `main` y sale `mergeable: dirty` (27 commits, 304 archivos); cambiar la base a `spec/arquitectura` o decidir cómo se retoma `main` (ver sección siguiente).

## Pendientes de decidir
- Numeración única de pantallas: los frames repiten números con pantallas sin relación (D23).
- Proveedor de mapa y de catálogo de lugares: licencias (D25).
- Firma de Jorge como revisor en cada spec (`docs/revision-jorge.md`).
- Exclusión de Composables en Kover y numeración frente a `main`.
- Dudas de la maqueta frente a la API: `specs/README.md`.

## Mapa de fases
- `nv-00` = constitución, stack y gobernanza · `nv-01` = foundation (proyecto, tema, navegación, red segura, Room) · `nv-02` = molde Repository→ViewModel→UiState · `nv-03` = puerta de calidad incremental (advisory). Funcionalidades desde `nv-04`.
