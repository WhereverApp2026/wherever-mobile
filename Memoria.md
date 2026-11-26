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
- Estado: rama `spec/arquitectura` con el trabajo de Diego (`spec/035-ci-cd`, por fast-forward). Specs renombradas a `nv-04…nv-39`, cada una con `spec.md`, `plan.md`, `task.md` y `checklists/gates.md`. CI con gitleaks, OSV, Kover 45 %, emulador y APK. Verificado: `assembleDebug`, `ktlintCheck`, `detekt` (JDK 17), `lintDebug`, `testDebugUnitTest` (147, con 9 reglas Konsist en `ArchitectureTest`), acciones fijadas por SHA, `koverVerifyDebug`, `assembleDebugAndroidTest` y todos los scripts de puerta. Sin correr: `connectedDebugAndroidTest` (no hay dispositivo) ni el primer run en Actions. Resumen y pendientes en `docs/revision-jorge.md`. Sin aprobaciones firmadas.
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

## ArchitectureTest portado de main, en verde (2026-11-06)
- Estado: el run #11 del PR #3 fallaba en `testDebugUnitTest` por 2 reglas de `arch/ArchitectureTest` (portado en `9efafd2` sin correrlo en local). Corregido; 144 pruebas JVM verdes, cobertura 71,97 %.
- Decisión: `core/database` queda fuera de la regla de capas y tiene la suya: solo puede importar `*Entity`/`*Dao` de `feature/*/data`. Porqué: `WhereverDatabase` debe listar las entidades de cada feature (Room lo exige; nv-04 lo pide así).
- Decisión: `Dispatchers.IO` vive en `core/common/DefaultDispatcherProvider.kt`; `AppModule` solo lo instancia. Porqué: es la convención que la regla espera y deja un único sitio que nombra el despachador.
- A evitar: portar reglas de Konsist de otra rama sin correr `testDebugUnitTest` antes del push; los paquetes y nombres de archivo no son los mismos.

## Specs firmadas (2026-11-06)
- Estado: Jorge firmó como revisor las 40 specs (`nv-00…nv-39`) en `spec/035-ci-cd`: línea de revisor en `spec.md`, `task.md` y `checklists/gates.md`, y **Estado** pasado a aprobada. Las casillas "CHECKLIST_PR.md OK" y "Sin conflicto con constitución" de cada spec siguen sin marcar: son comprobaciones aparte.

## API en CloudFront y cotejo con el backend (2026-11-08)
- Estado: rama `feat/api-cloudfront` desde `spec/035-ci-cd`, con URL `https://d302bvc0018nfp.cloudfront.net/api/v1/`. Verificado: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (144), `koverVerifyDebug` (71,97 %), `assembleDebug` y puertas de scripts. Sin verificar: `/health`, `/ready`, `/ping`, login y flujo real (503, backend apagado por horario; sin dispositivo). CI sin ver: GitHub da 404 sin sesión. Detalle en `docs/revision-jorge.md`.
- Decisión: se conserva la nota de apagado 22:00/08:00 y se añade el 503 en HTML. Porqué: nv-12 §11 del backend y su Terraform mantienen el horario.
- Decisión: US-232/233 quedan "Pendiente de backend", sin pantallas. Porqué: ninguna rama de `wherever-backend` (`main` = `f79917e`) tiene rutas de amigos.
- Decisión: no se implementa `datos_sensibles` en esta rama. Porqué: requiere spec y UI de consentimiento (US-010/US-022).
- Pendiente: Gustos con alergias dará 422 contra el backend actual. **Resuelto 2026-11-12 en nv-43** (consentimiento de datos sensibles).
- A evitar: leer un 503 de CloudFront como fallo de la app; fuera de horario el balanceador responde HTML.
- A recordar: `git add` del `gradle.properties` local arrastra `org.gradle.tooling.parallel`, que no es parte de ningún cambio; preparar solo la línea que toca.

## Integración a main (2026-11-09)
- Estado: `main` = `c56da67`, con `spec/arquitectura`, `spec/035-ci-cd` (PR #3) y `feat/api-cloudfront` (PR #13). CI de `main` en verde. Etiqueta de respaldo: `respaldo-main-2026-11-09`. Pendientes en `docs/revision-jorge.md`.
- Decisión: un solo `ArchitectureTest`, en `arch/`, y `Dispatchers` permitido solo en `DefaultDispatcherProvider`. Porqué: la regla de `spec/arquitectura` (solo `core/di`) chocaba con `d3aa814`.
- Decisión: paso de CI que borra SDK sin uso antes del emulador. Porqué: faltaban 30 MB para `userdata`; libera unos 14 GB y no toca las pruebas.
- A evitar: diagnosticar "Timeout waiting for emulator to boot" como KVM sin leer el `FATAL` del emulador al inicio del paso.
- A evitar: tocar una regla de arquitectura sin correrla con el código de la rama destino.
- A recordar: los 504 al descargar (Gradle, GitHub Releases) se relanzan sin cambiar código; primero se lee el log, porque un fallo en el paso gitleaks puede ser solo la descarga.
- A recordar: `gh` está en `C:\Program Files\GitHub CLI` y puede no estar en el `PATH` de la herramienta. El repo es privado, y `/dev/kvm` siguió disponible en el runner.

## Identificador y build de release (2026-11-11)
- Estado: rama `chore/application-id`, `specs/nv-41`. `applicationId`/`namespace` = `com.wherever.mobile` confirmados (sin cambios); firma de release por `WHEREVER_KEYSTORE_PATH`/`_PASSWORD`/`WHEREVER_KEY_ALIAS`/`WHEREVER_KEY_PASSWORD`. Verificado con JDK 17: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (158), `koverVerifyDebug`, `assembleDebug`, scripts de puerta, `assembleRelease` sin variables (APK sin firmar) y con keystore de prueba (`apksigner verify` OK). Sin verificar: APK de release en teléfono contra staging (sin dispositivo).
- Decisión: sin variables de firma, `assembleRelease` sale sin firmar en vez de fallar. Porqué: CI y otros equipos compilan release sin el keystore.
- Riesgo: R8 de AGP 9.0.1 avisa "error occurred when parsing kotlin metadata" con Kotlin 2.4.21. No rompe el build; probar login y perfil con el APK de release antes de publicar.
- A recordar: el JDK 17 descargado no trae `keytool`; usar el de `Android Studio/jbr/bin`.
- Estado (2026-11-12): `main` (nv-40, PR #15) integrado en `chore/application-id` sin conflictos. Puertas otra vez en verde con JDK 17, en tres partes: (1) `assembleDebug ktlintCheck detekt lintDebug`; (2) `testDebugUnitTest koverVerify` con 173 pruebas y 0 fallos; (3) scripts de puerta (critic PASS, SDD-OK, SPECS-OK con 38 specs, routes OK, strings OK, cobertura 73,08 %) y `assembleRelease` con APK sin firmar.
- A evitar: correr todas las puertas en una sola invocación de Gradle en este equipo, porque Claude Code la mata por falta de memoria. Correrlas por partes con `--max-workers=2`, y `assembleRelease` con `--no-daemon --max-workers=1`.
- Estado (2026-11-12): nv-41 integrado en `main` por el PR #16 (`3dbe85d`, merge manual de Jorge). La CI de `main` (corrida 43178363977) está en verde. Pendiente: probar el APK de release en un teléfono contra staging, sobre todo login y perfil, por el aviso de R8.
- A recordar: el modo automático bloquea `gh pr merge` sin revisión, así que el merge lo hace Jorge desde GitHub.

## Fecha de nacimiento sin fechas futuras (2026-11-12)
- Estado: rama `fix/fecha-nacimiento`, `specs/nv-42`. El calendario de Registro solo deja escoger entre hoy hace 100 años y hoy (`SelectableDates`) y abre en hace 18 años. `validateBirthDate` da tres mensajes: fecha futura, demasiado vieja y menor de edad. Verificado con JDK 17, una compuerta por vez: ktlint, detekt, lint, 190 pruebas, cobertura 73,32 %, `assembleDebug`, `assembleRelease` y `assembleDebugAndroidTest`. La prueba de UI pasó en la CI del PR #17. Contra staging (12:15): fecha futura → 422 en `fecha_nacimiento`; `/health`, `/ready`, `/ping` → 200; login con correo inexistente → 401.
- Decisión: "hoy" sale del `Clock` inyectado en la zona del teléfono y se pasa a medianoche UTC para el calendario (`BirthDateBounds`). Porqué: Material 3 representa cada día como medianoche UTC, y comparar con la hora UTC actual dejaría escoger mañana de noche en México.
- Decisión: el límite de 100 años es solo de la app. Porqué: la doc de la API pide 18+ y AAAA-MM-DD, nada más.
- Decisión: Compose Testing (`ui-test-junit4`, `ui-test-manifest`) entra en `androidTest`. Porqué: ya estaba en el catálogo y en el stack (constitución §3), pero no se usaba.
- A evitar: `LocalDate.EPOCH` y otras APIs de `java.time` que llegaron en API 34; con minSdk 26, lint las marca `NewApi` y la app se cae en teléfonos viejos. Usar `LocalDate.ofEpochDay(0)`.
- A recordar: en las pruebas de UI, un día del `DatePicker` se busca por su descripción (esqueleto `yMMMMEEEEd` en UTC y el idioma del dispositivo), no por el número del día, que la librería oculta de la semántica.

## Consentimiento de datos sensibles (2026-11-12)
- Estado: rama `fix/datos-sensibles` (sale de `fix/fecha-nacimiento`, PR #17), `specs/nv-43`. Gustos pide una casilla de consentimiento expreso al escribir alergias y manda `PATCH privacidad {datos_sensibles: true}` antes de `PUT gustos`. Privacidad tiene el interruptor "Datos sensibles"; si hay alergias guardadas, retirarlo pide confirmación. Room pasa a la versión 2 con `MIGRATION_1_2`. Verificado con JDK 17, una compuerta por vez: ktlint, detekt, lint, 205 pruebas, cobertura 74,23 %, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts y Semgrep. Sin verificar: `MigrationTest` en emulador (corre en CI) y el guardado real contra staging.
- Decisión (Jorge): casilla en Gustos más interruptor en Privacidad. Porqué: las alergias son un dato de salud, y la LFPDPPP pide consentimiento expreso para datos sensibles; deducirlo del chip de Alergias no lo es.
- Decisión: el `PATCH` de privacidad solo se manda si hay alergias y el perfil no tenía el permiso, y su fallo corta el guardado. Porqué: sin el permiso, el `PUT` de gustos daría 422; y así reintentar no repite el `PATCH`.
- A evitar: `addMigrations(*arreglo)`, porque detekt marca `SpreadOperator`. Pasar cada migración por nombre.
- A recordar: un campo nuevo en una entidad de Room necesita `@ColumnInfo(defaultValue = …)` igual al `DEFAULT` de la migración, o `runMigrationsAndValidate` falla al comparar esquemas.

## Presupuesto con barra y cantidad manual (2026-11-12)
- Estado: rama `feat/presupuesto-manual`, `specs/nv-44`. Gustos tiene la barra (libre, paso de $50 al soltar) y los campos Mínimo/Máximo sincronizados; `BudgetValidator` valida vacío, $0 a $50,000 y mínimo ≤ máximo; el 422 sale bajo su campo. Verificado con JDK 17, una compuerta por vez: ktlint, detekt, lint, 230 pruebas, cobertura 75,44 %, `assembleDebugAndroidTest`, `assembleRelease`, scripts y Semgrep. `BudgetSectionTest` 6/6 en el teléfono de Jorge.
- Decisión (Jorge, 2026-11-13): cada paso en su rama desde `main` actualizado y su PR; sin push directo a `main`, y el merge lo hace Jorge en GitHub. Revierte la idea del 2026-11-12 de trabajar directo en `main`.
- Decisión: lo escrito a mano no se redondea y una cantidad mayor de $1000 deja la barra al tope sin recortar lo escrito. Porqué: la barra solo es un atajo visual; la API acepta cualquier entero con max ≥ min.
- Decisión: tope de $50,000 solo en la app. Porqué: la API no lo pide; evita errores de dedo.
- A evitar: dejar una clase auxiliar (`BudgetActions`) en un archivo con otro nombre; detekt marca `MatchingDeclarationName`.
- A recordar: las pruebas instrumentadas pueden correr en el teléfono de Jorge por USB (`ANDROID_SERIAL`) en lugar del emulador, que consume mucha memoria en este equipo.

## Transiciones entre pantallas y pulido de UI (2026-11-13)
- Estado: rama `feat/transiciones-ui`, `specs/nv-45`. `core/designsystem/Motion.kt` concentra duraciones, curvas, transiciones del `NavHost`, `PhaseCrossfade`, `LoadingSwap` y `AnimatedFieldError`. `navigation/SafeNavigation.kt` guarda cada navegación. Fuentes estáticas por peso. Verificado con JDK 17, una compuerta por vez: ktlint, detekt, lint, 240 pruebas, cobertura 75,27 %, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts y Semgrep. Sin correr en local: las 8 pruebas de UI nuevas (van a la CI).
- Decisión: los toques navegan solo desde `RESUMED` (`navigateFrom`/`popFrom`), y los resultados de la API si la pantalla sigue arriba de la pila (`navigateOnResult`/`popOnResult`). Porqué: los eventos se recogen con `LaunchedEffect`, que sigue activo en segundo plano. Con la guarda de `RESUMED`, un login terminado con la app en segundo plano se perdía y la pantalla se quedaba atorada.
- Decisión: Sora y DM Sans como archivos estáticos por peso (fontTools, OFL). Porqué: en el teléfono, la fuente variable con `FontVariation` dibujaba todos los pesos en 400; se midió con una prueba instrumentada.
- A evitar: `togetherWith` en `AnimatedContent` cuando no debe cambiar de tamaño: trae `SizeTransform()` con resorte y deja el contenido anterior a la vista aunque la entrada y la salida sean `None`. Usar `ContentTransform(..., sizeTransform = null)`. Lo detectó la prueba de UI en la CI del PR #21.
- A evitar: `android:enableOnBackInvokedCallback` sin `tools:targetApi="33"`, porque lint marca `UnusedAttribute` con minSdk 26.
- A recordar: el teléfono de Jorge rechaza `input tap` de `adb` y, a veces, la instalación de la APK de pruebas (`INSTALL_FAILED_USER_RESTRICTED`); hay que aceptar en pantalla.
## Notificaciones push con Firebase (2026-11-14)
- Estado: rama `feat/notificaciones-push`, `specs/nv-46`. FCM con plugin google-services 4.6.0 y BoM 35.2.0. `PUT /dispositivos {token, plataforma}` tras login/registro, al arrancar con sesión y en `onNewToken`; `DELETE /dispositivos {token}` antes de logout y logout-todos. Tocar un push lee `data.tipo` y los ids (`PushDestination`); hoy todos abren Inicio porque Notificaciones, Votar y los perfiles de amigos aún no existen (`navigation/PushNavigation.kt`). Escrito sin SDK de Android: sin compilar en local; lo valida la CI del PR.
- Decisión: el plugin google-services solo se aplica si existe `app/google-services.json`. Porqué: el archivo no va en Git (constitución §6) y la CI no usa secretos; sin él la app compila sin push.
- Decisión: la baja del teléfono va antes de `auth/logout`. Porqué: el `DELETE` necesita el JWT, que el logout revoca.
- A evitar: valores por defecto en los DTO que deben viajar siempre; `ApiJson` no escribe defaults y el campo desaparece (pasó con `plataforma`).

## Autenticación en el teléfono (2026-11-18, tarde)
- Estado: rama `fix/auth-telefono` desde `main`, `specs/nv-49`, versión 2 (0.1.1). Reproducido en el teléfono de Jorge con logcat: login, registro, logout y recuperar llegan bien a la API; fallaban la presentación (campos y error ocultos en horizontal o con teclado) y un DNS momentáneo. El correo de recuperación no sale porque staging no tiene SMTP (backend).
- Decisión: `FormScreen` pasa el pie al scroll bajo 520 dp de alto, en vez de bloquear la rotación. Porqué: la app publicada ya gira y bloquearla es una regresión para quien la usa así.
- Decisión: `RetryingDns` reintenta una vez solo `UnknownHostException`. Porqué: la petición no salió, así que no duplica un `POST`.
- A evitar: escribir con `adb shell input` sin esperar: el teclado mueve el diseño y el texto cae en otro campo. Usar Tab (`keyevent 61`) entre campos y `uiautomator dump` con `MSYS_NO_PATHCONV=1` en Git Bash.
- A recordar: en MIUI `settings put system user_rotation` no gira la app; los casos de poca altura se prueban con `FormScreenCompactTest` o con el teclado abierto.
- A recordar: una build de depuración con `versionCode` mayor se reemplaza con `adb install -r -d`.
- A evitar: `scripts/check_coverage.sh` sin reporte previo lanza Gradle con el JDK del sistema y daemon; correr antes `koverVerifyDebug` con `gate.sh`.

## Inicio, Explorar y ficha del lugar (2026-11-18)
- Estado: rama `feat/inicio-recomendaciones`, `specs/nv-47`, `versionCode 2`. Inicio real (`/inicio`, clima, compañía, "Por qué", "No me interesa" con deshacer, eventos), ubicación aproximada en contexto, Explorar (búsqueda, filtros, recientes, vacío), ficha completa y favoritos. Verificado con JDK 17, una compuerta por vez: ktlint, detekt, lint, 272 pruebas, cobertura 74,18 %, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts y Semgrep. Staging responde 200 pero sin negocios: no se vio una ficha real.
- Decisión: medir el avance contra el backlog v3 (684 SP) en `docs/avance.md`; una historia cuenta si se usa de punta a punta, y las de solo servidor si ya están en `wherever-backend`. Porqué: el usuario pidió el % contra historias y SP.
- Decisión: PR apilados, cada uno con base `main` y sobre la rama anterior, como nv-43 sobre nv-42. Porqué: cada slice usa pantallas del anterior (Inicio abre la ficha) y los merges los hace Jorge después; además así `versionCode` sube sin conflictos.
- Decisión: tres repositorios de lugares (catálogo, reseñas y favoritos). Porqué: detekt `TooManyFunctions` y los siguientes slices (check-in, reseñas propias, favoritos de planes) los extienden por separado.
- Decisión: ubicación con `LocationManager` y solo `ACCESS_COARSE_LOCATION`. Porqué: Play Services está fuera del stack y la app no necesita la ubicación precisa.
- A evitar: Coil 3.6.3, porque exige `compileSdk 37`; se usa 3.3.0 con `coil-network-okhttp` (Coil 3 no trae red por defecto).
- A recordar: el JDK 17 está en el scratchpad de la sesión `ceb6bb66…/jdk17dl/jdk-17.0.20.1+1`; `gradle-daemon-jvm.properties` ya no existe en `gradle/`.

## Foto de perfil (2026-11-18, tarde)
- Estado: rama `feat/fotos-perfil` (worktree `../wherever-mobile-nv50`), `specs/nv-50`, versión 3. Galería y cámara probadas en el teléfono de Jorge contra staging.
- Decisión: Photo Picker y `TakePicture` sin pedir `CAMERA` ni `READ_MEDIA_IMAGES`. Porqué: el sistema da acceso solo a la foto elegida; pedir permisos de más es peor para la privacidad y para Play.
- Decisión: cliente OkHttp aparte (`@UploadClient`) para S3. Porqué: el formulario firmado no lleva token y el token no debe salir del dominio de la API.
- A evitar: probar fotos con la galería real del teléfono: hay fotos personales. Copiar una imagen de prueba con `adb push` a `Pictures/`.
- A evitar: `TaskStop` de una cadena de compuertas no mata el bucle `for` ni Gradle: quedan procesos `java` del JDK 17; cerrar esos PID uno por uno.
- A recordar: varias ramas a la vez con `git worktree`; solo una compila a la vez.

## Integración en main y cuenta (2026-11-19)
- Estado: a pedido de Jorge ("ve subiendo todo a main"), se integraron en `main` nv-46 (push), nv-49 (auth en el teléfono), nv-47 (Inicio), nv-50 (foto de perfil) y nv-48 (plan con IA); los PR #22, #24 y #25 se cerraron solos. Versiones: nv-47 → 3 (0.2.0), nv-50 → 4 (0.2.1), nv-48 → 5 (0.3.0), nv-51 → 6 (0.4.0).
- Estado: nv-51 (`feat/cuenta-seguridad`): cambiar contraseña y correo, Mis datos (exportar y borrar historial). 347 pruebas, ktlint, detekt, lint, cobertura y scripts en verde con JDK 17.
- Decisión (Jorge, 2026-11-19): push directo a `main`, cada cosa en cuanto pase sus compuertas. Revierte la del 2026-11-13 (PR y merge de Jorge). Porqué: el equipo esperaba los cambios.
- A evitar: confiar en `checklists/gates.md` de una rama WIP. nv-48 y nv-50 decían "compuertas en verde" y en la CI de `main` fallaron detekt (`MagicNumber`, `TooManyFunctions`, `LongParameterList`, `ReturnCount`) y lint (`UseKtx`, `Bitmap.scale`). Correr las compuertas antes de subir.
- A evitar: constantes que se llamen `*_PASSWORD = "..."`: `SourceSecurityTest` las toma por secreto.
- A evitar: el 401 de "contraseña incorrecta" en `PUT /auth/password` y `PUT /usuarios/yo/correo` pasa por el `Authenticator`: renueva, repite y el segundo 401 llega a la app sin cerrar la sesión. Tratarlo como error de campo, no como sesión vencida.
- A recordar: compilar desde cero tarda ~27 min en este equipo; trabajar en el worktree principal para aprovechar la compilación incremental (~4 min). Los daemons de Gradle de tareas detenidas quedan vivos: `./gradlew --stop`.

- Estado: nv-52 (`feat/notificaciones`): Avisos (lista por cursor, grupos Hoy/Semana/Antes, filtros, leídas, borrar), campana con no leídos en Inicio y horario silencioso en Ajustes. Push con destino inválido abre Avisos. 363 pruebas y todas las compuertas locales en verde.
- Bloqueo (para Jorge): gitleaks de la CI de `main` falla por `"key":"cuarentena/img1"` en `PhotoUploadIntegrationTest` (commit `9672d7c` de nv-50; regla `generic-api-key`, entropía 3,51). Es la ruta S3 de prueba, no un secreto, pero gitleaks revisa todo el historial: hace falta una excepción en `.gitleaks.toml` (como la del refresh de ejemplo). No la agregué: cambiar el escáner de secretos es decisión de Jorge. Mientras falle, la CI se salta ui, critic, rutas, specs, cobertura y Semgrep.
- A evitar: plurales sin `many` en español; lint (`MissingQuantity`) lo marca error.
- A evitar: `rm` de `app/build/generated/ksp` con Gradle vivo; Windows bloquea los archivos y KSP falla con `FileNotFoundException`. `./gradlew --stop` antes.

- Estado: nv-53 (`feat/amigos`): Amigos (pestañas, filtro, aceptar/rechazar/cancelar), Agregar amigos (usuario o teléfono, código, sugerencias, mi usuario y mi código), Perfil de la persona (agregar, eliminar, bloquear). Room 4 con `username`, `friendCode` y `priv_searchableByUser`. Push sociales abren `PersonRoute`. 379 pruebas y compuertas locales en verde.
- Decisión: el QR del código de amigo (US-234) queda fuera: no hay librería de QR ni cámara en el stack; el código se comparte como texto. Porqué: constitución §3, sin librerías fuera del stack sin acuerdo.
- A evitar: heredocs grandes con `python - <<'EOF'` en la herramienta Bash a veces fallan con "unexpected EOF"; escribir el script con Write y ejecutarlo.

- Estado (2026-11-19): se trabaja directo en `main`, sin ramas ni PR (pedido de Jorge). Métrica: pantallas que funcionan / 162 en `docs/avance.md`. CI de `main` otra vez en verde tras la excepción de gitleaks (`40e8ff7`). nv-54 (reservas sin pago) integrado.
- A evitar: `adb shell uiautomator dump /sdcard/...` desde Git Bash convierte la ruta; usar `MSYS_NO_PATHCONV=1`.
- A evitar: `connectedDebugAndroidTest` desinstala la app al terminar y borra la sesión del teléfono. Para una clase sola: `installDebug installDebugAndroidTest` y `adb shell am instrument -w -e class …`.
- A recordar: en el Xiaomi (MIUI) las pruebas de UI se cuelgan ("MIUILOG- Permission Denied Activity") hasta dar `adb shell appops set com.wherever.mobile 10021 allow` (ventanas en segundo plano); se pierde al reinstalar.
- Estado: Insignias (nv-14) con catálogo local; Registro probado en el teléfono contra staging (cuenta QA `@example.com`).
- Estado: nv-08 NuevaContrasena y enlaces de correo (`7f8866f`); nv-18 aviso dentro de la app (8 s), notificación privada en el bloqueo y Aceptar desde Avisos, probado con push real (cuenta QA B → cuenta del teléfono).
- A evitar: el login de staging limita a ~10 por minuto (429); en scripts de prueba guardar el token un rato. No bloquear el teléfono de Jorge con `keyevent 26`: tiene PIN y adb no lo desbloquea.
- A evitar: rutas con parámetros `token`/`password`: `SourceSecurityTest` lo prohíbe; guardar el token en memoria (`PendingEmailLink`).

- Pausa (2026-11-19, tarde): `main` = `7791a86`, CI verde. Búsqueda (nv-19) quedó en la rama `wip/pausa` con pruebas unitarias en verde y sin lint/release ni prueba de Personas en el teléfono. Dónde seguir: `docs/pendientes.md`.
- 2026-11-19 (noche): Búsqueda (nv-19) integrada en `main` desde `wip/pausa` con las 7 compuertas en verde (JDK 17, `--no-daemon --max-workers=1`). `docs/pendientes.md` reescrito como lista viva (% por área, prioridades, endpoints, bugs, ramas). A evitar: correr las 7 compuertas en una sola llamada de más de 10 min (se va a segundo plano); separar `assembleRelease`.

- 2026-11-19 (noche, 2): limpieza de GitHub. `wip/pausa` ya estaba en `main` (368f097); borradas `wip/pausa`, `feat/plan-ia`, `feat/fotos-perfil`, `feat/inicio-recomendaciones`, `fix/auth-telefono` (local y remoto, `git log main..` vacío) y sus worktrees. #24/#25 ya fusionados (comentados). #12 core-ktx 1.19.1 cerrado: exige compileSdk 37 + AGP 9.1. Después, con permiso de Jorge: Dependabot #5–#7 integrados (a333916), #8–#11 cerrados (compileSdk 37), todas las ramas salvo `main` borradas y 36 runs fallidos viejos de Actions eliminados. nv-55 en `main` (ed5c83f), CI verde.
- nv-55 (continuación): paleta oscura corregida (`RouteBlueLight`, `InkRaised`, `InkTrack`): el indicador de la barra inferior y la píldora del selector eran del mismo color que `surface`. `WhereverTextLink` sin relleno lateral. Bienvenida en horizontal sin ilustración si no cabe. Perfil: `consumeWindowInsets` (la barra del sistema se sumaba dos veces).
- A evitar: `colorScheme.primaryContainer`/`surfaceVariant` iguales a `surface` en oscuro; `RouteBlue` fijo en textos sobre superficies del tema. `FormScreen` dentro de un `Scaffold` con barra inferior necesita `consumeWindowInsets(padding)`.
- A evitar: detener con TaskStop un script que lanza `./gradlew` deja el daemon vivo y bloquea el siguiente build; usar `./gradlew --stop`.

- Pausa (2026-11-19, noche): Área 1 en curso. `main` = `8c306fc` (08_Permisos, nv-11). `wip/pausa` = `8f0004c` con 89_SinConexion (nv-16) sin compilar. Siguiente paso exacto en `docs/pendientes.md` ("⏸ Pausa"). Decisiones del área en `docs/revision-jorge.md`.
- A recordar: las compuertas se corren en un worktree aparte (`../wm-gates`) con `scratchpad/gates.sh <sha>` para seguir trabajando mientras tanto.
- A evitar: rutas con `/` en argumentos de scripts Python desde Git Bash: usar `MSYS_NO_PATHCONV=1`.

- Estado (2026-11-20): nv-16 Sin conexión, mantenimiento y versión mínima; previsualizaciones y capturas de NuevaContrasena, Ajustes, Privacidad y EliminarCuenta; Buscar Personas con Amigos/Solicitud enviada. Área 1 16/17 (Insignias bloqueada por backend), Descubrir 14/29 (resto bloqueado por backend o D25). 57/162 = 35,2 %.
- Decisión (2026-11-20): Reintentar de Sin conexión siempre intenta la llamada; el monitor de red (`core/common/ConnectivityMonitor`) solo informa a la UI. Por qué: la regla de nv-40 (`ConnectionSecurityTest`) prohíbe frenar peticiones con un chequeo previo de red, que en datos móviles daba falsos "sin conexión".
- Decisión (2026-11-20): `GET /personas/buscar` no trae la relación; la búsqueda la cruza con `amistades?estado=amigos|enviadas`. En Personas no se ofrece la IA.
- A evitar: `lintDebug` no corre en el ciclo rápido (ktlint/detekt/tests); fallos de lint (`InlinedApi`, `UseKtx`) y de Konsist aparecieron solo en las compuertas completas. Correr `lintDebug` y `testDebugUnitTest` completo antes de dar algo por listo.
- A evitar: `adb` no está en el PATH de Git Bash (`$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe`); un `| tail` escondió "command not found" y se reportó "sin teléfono" por error.
- A evitar: el interruptor apagado de Material usa `outline`/`surfaceContainerHighest` por defecto; con `OutlineSoft` no llegaba a 3:1. `ToggleRow` fija los colores de apagado.

- Estado (2026-11-20, tarde): equipo de tres. Jorge = Social, Planes, Reserva y lo compartido; Osmar = Modo negocio (nv-34 a nv-38); Diego = Recuerdos, Reseñas y Ayuda (nv-31 a nv-33). Specs listas con endpoints reales del backend, `CONTRIBUTING.md`, `docs/equipo/`, Issues por spec y `main` protegida (solo PR con CI verde). Decisiones en `docs/revision-jorge.md`.
- A recordar: el contrato de la API está en `wherever-backend/docs/endpoints.md` de `origin/main`; la copia local del backend puede ir atrasada (`git fetch` y `git show origin/main:docs/endpoints.md`).

- Estado (2026-11-20, noche): nv-17 feed de amigos en `feat/nv-17-feed` con compuertas verdes y PR a `main`. 61/162 = 37,7 %. Falta 29_PlanAmigo/14_MeApunto en teléfono (ningún momento de staging enlaza un plan).
- A evitar: en Xiaomi sin "Instalar vía USB" activo, `installDebug`/`connectedDebugAndroidTest` se quedan colgados o fallan con `INSTALL_FAILED_USER_RESTRICTED`; y detener un `connectedDebugAndroidTest` desinstala la app y el APK de pruebas. Instalar con `adb install -r` y correr con `adb shell am instrument -w -e class …`.
- A evitar: una tarjeta `clickable` fusiona la semántica de sus hijos; un `testTag` en un texto interno se busca con `useUnmergedTree = true` o `assertIsDisplayed` falla aunque esté en pantalla.
- A evitar: en el teléfono, tocar por coordenadas de una captura falla si la lista aún carga; sacar los límites con `uiautomator dump` justo antes del toque. Escribir credenciales con `adb input text` y pasar de campo con Tab (keyevent 61), nunca tocando el segundo campo con el teclado abierto.
- Estado (2026-11-20, noche): nv-20 27_AmigoOpc (Silenciar con `PUT /amistades/{id}/silencio`, Eliminar, Bloquear) en `feat/nv-20-amigo-opciones`, apilada sobre nv-17. 62/162 = 38,3 %. Silenciar probado contra staging y devuelto a apagado.
- A evitar: probar una `ModalBottomSheet` con `createComposeRule` en el Xiaomi deja la prueba colgada; se prueba el contenido aparte (`FriendOptionsContent`) y la hoja a mano en el teléfono.
- A evitar: `uiautomator dump` da `bounds=[0,0][0,0]` en etiquetas de la barra inferior (semántica fusionada); el helper `scratchpad/tap.sh` las salta.
- A recordar: desde la protección de `main`, también Jorge (y Claude) integra por PR; nada de push directo.

- Pausa (2026-11-20, tarde): `main` = `810b91e`. nv-17 (feed de amigos) en `wip/pausa`, compilado y con la suite unitaria verde, sin compuertas ni teléfono ni PR. Bug de `mapApiError` (códigos específicos → `Unknown`) corregido ahí con prueba. Siguiente paso exacto en `docs/pendientes.md` ("⏸ Pausa (2026-11-20, tarde)").
- A recordar: desde el 2026-11-20 todo va por PR a `main`, también el trabajo de Jorge.

- Estado (2026-11-21): 25_MiCodigo con ZXing (generar con `core`, leer con `zxing-android-embedded`); #34 cerrado, #35/#36/#37 en `main`. Pantallas 63/162. Pruebas que esperan el teléfono en `docs/pruebas-telefono.md`.
- A evitar: dejar que el lector de ZXing pida la cámara: si se niega muestra "la cámara ha encontrado un problema". Pedirla antes con `RequestPermission` y mostrar un aviso propio.
- A evitar: `printf | grep -q` con `set -o pipefail` en scripts: falla al azar con "Broken pipe". Usar `grep -q … <<<"$var"`.
- A recordar: QR en Compose con celdas de píxeles enteros (`floor`); con fracciones el antialias deja rayas.
- A recordar: staging se apaga 22:00–08:00; de noche solo se prueba el modo sin servidor.

- Estado (2026-11-21, mediodía): nv-25 44_MisGrupos y 45_GrupoChat en `feat/nv-25-grupos`. Compuertas locales en verde (ktlint, detekt, debug, androidTest compila, unitarias + capturas, lint, kover) y scripts (critic, sdd, specs, routes, strings). En el teléfono solo sin servidor (staging 503); no suman al % hasta probarlas con la API (`docs/pruebas-telefono.md` #9-#10).
- A evitar: correr `ScreenshotTest` con el JDK 17 descargado: falla con `Could not initialize class sun.awt.Win32FontManager`. detekt con JDK 17 y pruebas/capturas con el JBR de Android Studio.
- A recordar: revisar las capturas grabadas en claro y oscuro; así salieron los chips sin contraste en oscuro (`StatusPill` con tintes fijos necesita `contentColor = Ink`).

- Estado (2026-11-21, tarde): PR #40 (nv-25) integrado en `main` (`2c62246`). Rediseño Boceto 2 en `feat/boceto2-rediseno` (guía: `docs/diseno_boceto2.md`). Hecho: 1) tokens: 7 hex de §2.2, botones 52 dp, campo 14 y tarjeta 18 (`Radius.Field`/`Radius.Card`), Sora solo Bold, escala de §3 con espaciado 0; 2) logo nuevo (`docs/logo/logo-nuevo.jpg`) en el ícono adaptativo (fondo blanco, `drawable-nodpi/ic_launcher_logo.webp`) y en `LogoMark` (Login y Bienvenida, `logo_wherever.webp`). Falta: barra de 5 con "+" y hoja 15_Crear; "Continuar con Google"; campo de 52 dp de alto.
- Decisión: `labelLarge` queda en DM Sans Bold 14 (etiquetas de campo, enlaces, chips: su uso mayoritario) y los botones usan `ButtonText` (DM Sans Bold 17), en vez de subir `labelLarge` a 17 como sugiere §3. Así se iguala Figma sin agrandar 15 etiquetas.
- Decisión: el logo nuevo trae el nombre dentro; en Bienvenida sustituye a la fila "pin + Wherever" (el nombre queda como `contentDescription`). El ícono monocromo (Android 13+) y la pantalla de arranque siguen con el pin vectorial: el logo es una foto y no sirve como silueta.
- A evitar: grabar capturas con `-Proborazzi.test.record=true`: la tarea solo lee `-Proborazzi.record=true`. Con la otra, verifica, y con el umbral de 1 % un cambio de tipografía pasa sin grabar.

- Estado (2026-11-22): Boceto 2, pasos 3-5 en `feat/boceto2-rediseno`. Barra de 5 (`core/designsystem/BottomBar.kt`): Inicio, Explorar (brújula), "+" naranja, Planes y Perfil; el "+" abre la hoja 15_Crear (`CreateOption`). Navegan "Armar un plan con IA" (`CreatePlanRoute`) y "Planear con amigos" (`MyGroupsRoute`); las otras 3 dicen "Próximamente". "Continuar con Google" en Login y Registro (`GoogleSignInBlock`) avisa que llega pronto: la API no tiene Google. Campo de texto a 52 dp con `BasicTextField` + `DecorationBox`.
- A evitar: `fillMaxHeight()` dentro de `NavigationBar`: solo tiene alto mínimo y la barra se estira a toda la pantalla (salió así en el teléfono). Centrar con `align(Alignment.CenterVertically)`; lo cuida `BottomBarCreateTest`.
- A recordar: `OutlinedTextField` fija 56 dp de alto mínimo; para 52 hay que usar `BasicTextField` con `OutlinedTextFieldDefaults.DecorationBox` y relleno vertical de 14.
- A evitar: correr la suite de UI en el Xiaomi en horizontal: 4 pruebas fallan porque la pantalla queda baja. Fijar vertical (`settings put system accelerometer_rotation 0` y `user_rotation 0`) y devolver `accelerometer_rotation 1` al terminar.
- A evitar: instalar el APK de debug mientras otro Gradle reempaqueta (`assembleDebugAndroidTest` en paralelo): quedó sin `Hilt_WhereverApp` y la app se cerraba al abrir. Volver a generar con `assembleDebug` solo.
- Pendiente para Jorge: los enlaces de términos y aviso en Registro esperan 87_Ayuda (nv-33).
- Estado (2026-11-22, mañana): PR #44 integrado (`5b68152`). 10_Inicio de Figma en `feat/boceto2-inicio`: `HomeCover.kt` (portada, buscador con chispa IA) y `HomeCarousels.kt` (carrusel "Cerca de ti" 160 × 184, "Planes de tus amigos"); captura `10_inicio_*`; espacio en "¿No tienes cuenta? Crea una". Decisiones en `docs/revision-jorge.md`.
- A recordar: el MCP de Figma (plan Starter) tiene límite de llamadas; si se acaba, trabajar con la captura ya descargada y `docs/diseno_boceto2.md`.
- Estado (2026-11-21, noche): nv-33 86_Ayuda (Diego) en `feat/nv-33-help`: buscador sin acentos, 9 preguntas en acordeón, contacto y «Reportar un problema» con `POST /errores`; fila Ayuda en Ajustes. Compuertas locales en verde (ktlint, detekt, lint, 527 unitarias + capturas, kover 81,24 %, assembleDebug) y scripts. Sin probar contra staging (503 en fin de semana) ni en teléfono. Correo y WhatsApp ocultos hasta que existan `SUPPORT_EMAIL`/`SUPPORT_WHATSAPP` en `BuildConfig` (Jorge). Decisiones D-33b a D-33e en `docs/revision-jorge.md`.
- A evitar: pruebas de Compose con Robolectric en un paquete que corre antes que `integration/`: dejan Conscrypt instalado en la JVM y `ConnectionIntegrationTest` (certificado no confiable) falla con `JarVerifier`/`SecurityException` en el JDK 21 de Oracle. Las pruebas de UI van en `androidTest`.
- A recordar: el JDK 21 de Oracle (`C:\Program Files\Java\jdk-21`) corre todas las compuertas, capturas incluidas; no hay JDK 17 en el equipo de Diego. Semgrep no está instalado: lo corre la CI.
- A recordar: el frame de Figma se abre sin sesión en el navegador con `?node-id=`; 86_Ayuda es `298:7902`. Así no se gastan llamadas del MCP de Figma (plan Starter).
- Estado (2026-11-21, noche): nv-32 Reseñas (Diego) en `feat/nv-32-reviews`: 68_PostVisita → 78_ResCom → 69_Publicado (un solo ViewModel por pasos), 77_ResPend, 79_MisRes con eliminar + Deshacer de 5 s y 80_ResLugar con filtros, Útil y Reportar. Entradas: filas en Perfil y «Ver reseñas» en la ficha. Sin probar contra staging (fin de semana) ni en teléfono; falta una cuenta con check-ins. Decisiones D-32b a D-32h en `docs/revision-jorge.md`; pendientes del backend: catálogo de chips y distribución por estrellas.
- A evitar: `x in mapa` sobre un `ConcurrentHashMap` en Kotlin llama a `contains(value)` y no compila (KT-18053); usar `containsKey`.
- A evitar: en `ProfileContent` y demás Composables, los parámetros con valor por omisión van después de `modifier` (lint `ModifierParameter`); si no, se dejan obligatorios.
- A recordar: los números de las vistas previas van como argumento con nombre o constante; detekt (`MagicNumber`) no excluye los `@Preview`.
- A recordar: una espera que debe sobrevivir a la pantalla (Deshacer) corre en `@ApplicationScope`; en las pruebas se pasa un `TestScope` con el despachador de `MainDispatcherRule` para mover el reloj.

- Estado (2026-11-22): nv-31 Recuerdos y postales (Diego) en `feat/nv-31-memories`, apilada sobre nv-32: 73/74/76 (álbum), 71 (cámara del sistema), 72 (postal dibujada en la app y subida), 75 (detalle con Compartir, Descargar y Eliminar) y 70 (Nuevo momento). Sin probar contra staging ni en teléfono. Decisiones D-31b a D-31f en `docs/revision-jorge.md`.
- A evitar: con `Response<T>` en Retrofit un 4xx no lanza `HttpException`: `safeApiCall` lo da por bueno. Traducir con `mapApiError` (lo hace `MemoriesRepositoryImpl` para el 202 + `Location` de `POST /postales`).
- A evitar: `TakePicture` con `CAMERA` declarado en el manifiesto (lo mete ZXing) lanza `SecurityException` si el permiso no está concedido: pedirlo antes.
- A recordar: Robolectric no soporta `ImageDecoder` (`DecodeException: Only supported on Android`); `BitmapFactory` sí. `ContentResolver` de Robolectric tampoco abre `file://`.
- A recordar: `android.media.ExifInterface` no tiene `latLong` como propiedad: `getLatLong(FloatArray(2))` y GPS con `setAttribute` en formato racional.

- Estado (2026-11-22, tarde): #42 (nv-33) y #43 (nv-32) integrados; #45 (nv-31) con base `main`, CI verde y **bloqueado por la regla de `main` (aprobación de Osmar)**. Staging encendido: las 22 pantallas de Diego y 44/45 de nv-25 probadas en el Xiaomi con cuentas QA → 87/162 = 53,7 %. Arreglos de esa prueba y del Boceto 2 (términos → Ayuda, Momento y Reseña en el "+") en `feat/boceto2-iconos-fotos`.
- A evitar: integrar a `main` sin la aprobación de Osmar: la regla «main: solo con aprobación de Osmar» está activa (code owner, se descarta con cada push). No se salta con `--admin` ni desactivándola.
- A recordar: en el Xiaomi `svc wifi disable` no apaga el Wi-Fi (MIUI); usar `cmd wifi set-wifi-enabled disabled|enabled`. Sin red la app pone la pantalla global 89_SinConexion encima de todo.
- A recordar: la moderación de staging rechaza a veces textos repetidos («el texto no cumple las normas»), 422 con `campos[texto]`.

- Pausa (2026-11-22, noche): rama `feat/cuenta-social-descubrir` (sale de #51). Hecho y compilando (ktlint, detekt, lint): 83_Insignias con `GET /usuarios/yo/insignias` real (Cuenta 17/17 al probar en teléfono) y 07_EncuentraAmigos (`POST /personas/contactos`, `READ_CONTACTS` en contexto, entrada «Mis contactos» en Agregar amigo). Falta: pruebas unitarias + capturas (grabar), UI en el Xiaomi y staging, PR. Después: nv-25 (42_Compartir, 43_CrearGrupo, 46_Grupo, 48_Votar, InvitarPlan), nv-17 (14_MeApunto/29_PlanAmigo con un momento con plan, 15_Crear a mano), variantes de Detalle; iconos y fotos del paso 2. Sin API: historias, reacciones, chat 1 a 1; mapa (D25) y Sello sin filtro.
- A recordar: #45 y #51 esperan la aprobación de Osmar (regla de `main`); no se integran sin ella.

- Estado (2026-11-23): rama `integracion/todo` (PR #67) une `main` con #52 (Jorge), #53 (Borre), #56/#57/#59/#58 (Diego: nv-23/24/30/26) y #48/#55/#60 (nv-34/35/37). Sobre ella, nv-25 de Jorge: 46_Grupo, 48_Votar, 43_CrearGrupo (y modo invitar) e InvitarPlan, con `GroupMembersRepository` y `VotesRepository`. 734 pruebas JVM, detekt, ktlint y los 5 scripts en verde; 75 instrumentadas en el Xiaomi, todas verdes. Falta probar lo nuevo contra staging para sumarlo al %. Sigue en 87/162 en `main`.
- Decisión: integrar todo en una rama y un PR para que Osmar apruebe una sola vez; al entrar, los PR de cada quien se cierran solos. En specs con conflicto manda lo implementado y se conserva la conciliación de Borre (contrato vivo, endpoints aún sin consumir como pendientes).
- Decisión: tema oscuro `tertiaryContainer = MintDeep`; era igual a `surface` y las 29 píldoras de estado sin colores propios no se veían. `InfoBanner` pinta el ícono con `colorScheme.primary`.
- A evitar: un repositorio con más de 16 funciones (detekt `TooManyFunctions`); separar por tema (grupos, integrantes, votaciones) desde el inicio.
- A evitar: recargar con `load()` después de poner un aviso de error: `load()` lo borra. Usar `reload(clearError = false)`.
- A evitar: `onNode(hasText(...))` cuando el texto sale dos veces (chip y vista previa); acotar con `hasAnyAncestor(hasTestTag(...))` y `useUnmergedTree = true`.
- A recordar: en el Xiaomi el teclado abierto tapa los avisos de arriba en pruebas de UI; cerrarlo con `LocalSoftwareKeyboardController` antes de comprobarlos.

- Estado (2026-11-23, tarde): #67 integrado por Osmar. #68 (`feat/seguridad-95`, base `main`) trae: constitución E-04 a E-07; pinning, `FLAG_SECURE`, root/depurador; SQLCipher con migración probada con datos reales; biometría opcional; CI con Semgrep (paquetes explícitos) y OSV bloqueantes, SBOM, MobSF y release firmado si hay secretos; Perfil 81–84 como Figma; me gusta, comentarios, reacciones e historias (nv-17); mapa 32_ExpMapa con osmdroid. % real 95/162 tras probar con staging.
- Decisión (Jorge, 2026-11-23): osmdroid para D25; root/depurador avisa en debug y bloquea en release; biometría opcional en Ajustes.
- Decisión: contadores, bio y número de nivel de `/usuarios/yo` en memoria (`ProfileExtras`), no en Room: la versión 6 la reserva `nv-56` (Borre).
- A evitar: SQLCipher 4.6.1 `rawExecSQL` solo compila (no ejecuta) y un `PRAGMA` sobre la base adjunta va a otra conexión; usar `execSQL` para ATTACH/DETACH, `rawQuery(...).moveToFirst()` para `sqlcipher_export`, abrir con `CREATE_IF_NECESSARY` y fijar `user_version` abriendo la base cifrada aparte.
- A evitar: `semgrep --config=auto` exige métricas; usar paquetes explícitos (`p/kotlin p/java p/secrets p/python p/github-actions p/owasp-top-ten`).
- A evitar: manejar formularios largos del teléfono con `adb input`: el teclado y el «atrás» sacan la app y el texto cae en el launcher. Preferir pruebas de UI.
- A evitar: probar en el teléfono de Jorge flujos que mandan su ubicación real o su agenda (alta del negocio paso 3, EncuentraAmigos) sin su visto bueno.
- A recordar: el MCP de Figma (Starter) se acaba; los frames se ven con Playwright en `figma.com/design/...?node-id=` y Ctrl+rueda para acercar.
- A recordar: en staging el 422 de `POST /planes/generar` es «sin lugares cerca»; los niveles 1 a 3 del backend se llaman igual («Explorador»).
- Estado (2026-11-23, 16:10): 32_ExpMapa probado con staging en el Xiaomi (pines reales en Tula, tarjeta, Ver detalle, A mi plan, Buscar en esta zona, filtro): % real 96/162. Arreglado: «Buscar en esta zona» salía sin mover el mapa (osmdroid avisa al acomodarse; ahora `MapArea.differsFrom` exige cambio de zoom o 15 % de desplazamiento) y pines propios `ic_map_pin` con el color del tema. Explorar y fichas con lugares reales, bien.
- Pausa (2026-11-23, 16:15): sin commit `core/designsystem/Buttons.kt` (botón con borde: relleno lateral `Spacing.L` y una línea con puntos; «Cómo llegar» se partía junto a «Llamar»). Falta regrabar las capturas (`-Proborazzi.record=true`; cambia `32_exp_mapa_tarjeta_chica_letra_grande`), revisar el diff de imágenes y hacer commit. Siguiente: punto 22 de `docs/pruebas-telefono.md` (la cuenta con sesión tiene 0 amigos: entrar con QA A), Inicio «Cerca de ti» vacío con lugares a 1 km, avisar a Osmar del 422 de `/planes/generar` con lugares cerca.
- A recordar: en Git Bash, `adb shell uiautomator dump /sdcard/...` necesita `MSYS_NO_PATHCONV=1`; `adb` está en `%LOCALAPPDATA%/Android/Sdk/platform-tools`, no en el PATH.
- A evitar: explicar el 422 de `/planes/generar` por falta de lugares: el 2026-11-23 falló con 15+ lugares abiertos a menos de 2 km.
- Estado (2026-11-24, 01:45): #67 ya en `main`; #68 con CI verde y la regla de `main` ahora pide 0 aprobaciones (solo CI `gates` al día). `Buttons.kt` en `78d4e39` con capturas regrabadas. Punto 22 probado con QA A/B: me gusta, comentar, responder, borrar, 13_Reacciones, historias y visor → % real 98/162 (60,5 %). Arreglado: el conteo de comentarios no se movía (`commentsDelta`), «Ver más comentarios» con `siguiente_cursor: ""` y el 422 mostraba «Datos inválidos» en vez del motivo de `campos[texto]`.
- Estado (2026-11-24, 02:00): InvitarPlan y 45_AgregarGasto/La vaca probados con QA A/B → 100/162 (61,7 %). Arreglado 26_PerfilAmigo: `GET /personas/{id}` manda `nivel` como objeto `{numero, nombre}` (antes texto); el DTO acepta los dos con `JsonElement`.
- A recordar: `PATCH /grupos/{id}/integrantes/yo` acepta `confirmado` o `declinado`; `POST /planes` pide `llega` en ISO 8601 con zona.
- Estado (2026-11-24, 03:00): #68 fusionado en `main` (`a7bf1da`). PR #71 `integracion/negocio` = #69/#70 de Diego sobre `main` (conflicto de `strings.xml` resuelto con los dos lados) + arreglo del alta: la API exige el consentimiento `datos_negocio` antes de documentos y CLABE (`422 consentimiento_requerido`) y la app no lo pedía; ahora hay casilla en el paso de documentos que manda `POST /usuarios/yo/consentimientos` con las versiones de `GET /configuracion`.
- A recordar: el backend está en `../wherever-backend` (hacer `git fetch`: el checkout local es viejo); leer su código antes de adivinar contratos. Expediente del negocio: 10 subidas por hora por usuario.
- A evitar: valores por defecto en DTOs de petición que la API necesita recibir: `ApiJson` no codifica los valores por defecto (`datos_negocio = true` no viajaba; lo atrapó la prueba con MockWebServer). Pasar el valor explícito o quitar el defecto.
- Para Osmar (backend): `POST /negocios/{id}/documentos` da 503 «el expediente no está disponible» en staging (bloquea enviar a revisión y todo Modo negocio); `GET /inicio` da `recomendaciones: []` con lugares a metros; algunas respuestas con `padre_id` dan 422 «no cumple las normas» con textos inocentes; 422 de `/planes/generar` con lugares cerca.
- A recordar: para una historia de QA B por API: `POST /imagenes/subida` (tipo `image/jpeg`) → formulario a S3 → `POST /imagenes/{id}/confirmar` → `POST /postales` (202, sin cuerpo; el id sale de `GET /postales`) → `POST /momentos {postal_id}`. Una imagen o postal solo se usa una vez (409).
- A evitar: correr todas las unitarias locales con el JDK 17 del scratchpad en paralelo con otros Gradle: MockK falla al adjuntarse a la JVM (`Could not self-attach`, 15 fallos en negocio y ayuda); por clase también fallan; la CI en Ubuntu es la fuente.

## Reparto entre cuatro y revisor único (2026-11-22)
- Estado: reparto de lo que falta entre Jorge (nv-20, nv-21, nv-25 y lo compartido), Diego (nv-23, nv-24, nv-26, nv-30, nv-37), Borre (nv-35, nv-36, nv-38) y Osmar (nv-17, nv-27, nv-29 y backend). Listas en `docs/equipo/`. Avance al corte: 63/162 en `main`.
- Decisión (Osmar): solo Osmar aprueba los PR a `main`. `CODEOWNERS` = `@OsmarYahir @WhereverApp2026` y regla `.github/rulesets/main.json` (1 aprobación de code owner, se anula con commits nuevos, CI `gates` en verde y al día, conversaciones resueltas, sin push directo ni force push), sin excepciones. Porqué: 15 commits entraron a `main` sin PR el 2026-11-19.
- Decisión: la cuenta dueña `WhereverApp2026` solo la usa Osmar, para aprobar sus propios PR. Porqué: GitHub no deja aprobar un PR propio, y con la cuenta dueña cualquiera podía saltarse la regla (Diego abría sus PR con ella).
- Límite: en un repo de cuenta personal no se puede restringir quién presiona "Merge"; la regla lo cubre exigiendo la aprobación.
- A evitar: activar la regla antes de integrar este `CODEOWNERS`, porque el code owner sería Jorge.

## Planes pasa a Borre: itinerario (2026-11-22)
- Estado: rama `spec/planes-itinerario` (solo `.md`). Planes (nv-23, nv-24, nv-26, nv-30) pasa de Diego a Borre, por acuerdo del equipo según Borre. `docs/itinerario-planes.md` fija 6 fases: 0 specs al día, 1 Room (`nv-56`), 2 nv-24, 3 nv-23, 4 nv-26, 5 nv-30. Reparto actualizado en `CONTRIBUTING.md`, `docs/pendientes.md` y `docs/equipo/`. Puertas: SPECS-OK, SDD-OK, routes OK, critic PASS.
- Hallazgo: las specs de Planes no se conciliaron con nv-48. nv-23, nv-24 y nv-28 siguen "sin código" y "Pendiente de backend"; nv-26 y nv-30 tienen el contrato viejo; nv-48 eligió "Sin cambios en Room" sin excepción documentada; nv-48 sigue sin firma. Las puertas no lo detectan porque solo revisan la estructura.
- Decisión: conciliar las specs (Fase 0) antes de escribir código. Porqué: metodología SDD; sin eso no se sabe qué falta en cada frame.
- Estado (Fase 0): specs nv-23, nv-24 y nv-28 conciliadas con nv-48 (lo hecho marcado con rutas reales; lo que falta, abierto); nv-26 y nv-30 con el contrato real; `specs/README.md` (18 frames, D29 resuelta, D31 en curso) y `docs/avance.md` al día. Contrato leído de una copia de `wherever-backend/docs/endpoints.md` del 2026-11-22. Falta: decisiones 1-5 con Osmar en el PR y la spec `nv-56`.
- Decisión: la conciliación deja la firma de Jorge del 2026-11-06 y agrega "Revisor de la conciliación: Osmar" sin marcar. Porqué: AGENTS §2, borrador con evidencia; solo se firma con revisor indicado.
- Hallazgo: el check-in lo inicia la persona (`POST /checkins` con QR del mostrador o ubicación), no el negocio; existe `GET /planes/{id}/paquete-offline` para el modo sin red; `GET /planes` no trae el estado (Reservado, En grupo) que pide Mis planes.
- A evitar: tomar una spec del reparto sin revisar `docs/equipo/` de `main` actualizado; el reparto cambió el mismo día.
- Estado: `specs/nv-56` (Planes sin conexión, Room v6) escrita y revisada contra el código; SPECS-OK con 53 specs. Tablas `plan`, `plan_tab_entry`, `plan_stop`, `plan_transfer`, `plan_draft`; `PlansCleaner` como `SessionCleaner`; paquete sin conexión con `ETag` y máximo una vez cada 30 min por plan.
- Decisión: sin red las ediciones se intentan y, si fallan, no cambian nada; no se encolan ni se apagan botones. Porqué: cada edición exige `If-Match` y un 409 obliga a recargar; y nv-16/nv-40 decidieron que el monitor de red solo informa (daba falsos "sin conexión" en datos móviles).
- Hallazgo de la revisión: `89_SinConexion` es una capa sobre toda la app que solo se cierra con "Atrás"; `GET /planes` devuelve `PlanDto` sin garantía de paradas (no debe pisar el detalle); registrar una migración toca `core/di/AppModule.kt`, que según `CONTRIBUTING.md` requiere Issue a Jorge.
- A evitar: escribir una spec que toque `core/di`, `navigation/`, `core/network` o `app/build.gradle.kts` sin prever el Issue a Jorge en su §7.
- Decisión: Room guarda `paquete-offline` aunque la respuesta diga `Cache-Control: no-store`. Porqué: esa cabecera es para cachés HTTP; guardarlo en la app es el propósito del endpoint, y OkHttp no tiene caché.
- A recordar: el índice de specs de `specs/README.md` termina en nv-39; nv-40 a nv-56 no están listadas.
- A evitar: heredocs largos con `cat > archivo <<'EOF'` en la herramienta Bash: falló con "unexpected EOF" y no escribió nada. Usar Write.
- A evitar: `|` dentro de una celda de tabla Markdown, aunque esté entre comillas invertidas: parte la columna.
- A recordar: `python` no está en el PATH de Git Bash en este equipo (alias de la Microsoft Store).

- Estado (2026-11-22): nv-23 Crear plan con IA (Diego) en `feat/nv-23-ai-plan`: IAPalabra, 17_AjustaPlan y Generando 1/4 a 4/4 sobre `POST /planes/generar` (base nv-48). Decisiones D-23a a D-23e en la spec. Sin probar contra staging.
- A evitar: `MIN_STOPS`/`MAX_STOPS` ya existen en `feature/plans/ui` (edición, 6 paradas); los de la IA son `AI_MIN_STOPS`/`AI_MAX_STOPS`.
- A recordar: un Composable que regresa valor va en minúscula (lint `ComposableNaming`), p. ej. `sparkleIcon()`.

- Estado (2026-11-22): nv-24 Plan, detalle y edición (Diego) en `feat/nv-24-plan-detail` (sobre nv-23): 39_Plan, 40_Cambiar, 41_AgregarParada y CrearPlan (`ManualPlanRoute`). Decisiones D-24a a D-24f en la spec.
- A recordar: las horas en capturas dependen de la zona del equipo (Windows local, Linux UTC en CI); las pantallas con horas reciben `zone` y las vistas previas pasan `PREVIEW_ZONE`.
- A evitar: pedir la ficha del lugar después de guardar o crear un plan: `PlansIntegrationTest` espera una sola llamada. `toPlan(lookupMissing = false)` al guardar.

- Estado (2026-11-22): nv-30 parcial (Diego) en `feat/nv-30-checkin` (sobre nv-24): check-in real y 26_CheckinOk en `feature/checkin`, «Cómo llegar» con la app de mapas. 13, 14, 15 y 18 esperan D25. `LocationProvider` sin precisión (D-30c).
- A recordar: el conector de Figma (plan Starter) tiene límite de llamadas; si se acaba, abrir el frame en el panel del navegador con `?node-id=` y arrastrar el lienzo con la mano.

- Estado (2026-11-22): nv-26 Vaca virtual (Diego) en `feat/nv-26-vaca`: 28_Vaca, variantes y 45_AgregarGasto en `feature/vaca` contra `/grupos/{id}/vaca`, gastos y deudas. Decisiones D-26a a D-26e en la spec.
- A recordar: `deudor`, `acreedor` y `pagado_por` se leen como id o como objeto (`JsonElement`) porque la doc no dice la forma.

- Estado (2026-11-22): nv-34 Negocio, registro (Osmar, hecho por Claude con su autorización) en `feat/nv-34-business`: 43 a 47 en un asistente que retoma en `paso_alta`; entrada «Registrar mi negocio» en Ajustes. Sin probar contra staging. Decisiones D-34a a D-34d y bloqueos en `docs/revision-jorge.md`.
- A evitar: un campo de un DTO de petición con valor por omisión no viaja (`ApiJson` no codifica los valores por omisión). Lo que la API exige va sin valor por omisión (`BizPointDto.type`).
- A evitar: helpers de prueba con nombres genéricos (`sampleDraft`, `FakeLocationProvider`) ya existen en `testing/`; buscar antes de crear uno.
- Estado (2026-11-22): nv-35 Negocio, panel, reservas y check-in (Osmar, hecho por Claude) en `feat/nv-35-business` (sale de nv-34). Modo negocio con `NavHost` y barra propios (Panel, Reservas); 48, 49, 50 y 56 contra la API real. Sin probar contra staging. Decisiones D-35a a D-35j y bloqueos en `docs/revision-jorge.md`; QR en el Issue #54.
- A recordar: los IDs de los frames de Figma salen de `get_metadata` del canvas `96:130` (respuesta enorme: filtrarla con Python por `name`); mapa de negocio en `docs/revision-jorge.md`.
- A evitar: datos de ejemplo de las vistas previas dentro de cada pantalla: detekt marca cada número (MagicNumber). Juntarlos en un archivo `*Samples.kt` con la supresión justificada.
- A evitar: en `strings.xml`, los `plurals` en español piden también `many` (lint `MissingQuantity`).

- Estado (2026-11-22): nv-37 Cupones y cupo (Diego) en `feat/nv-37-business` (sobre nv-35): 53, 54 y 55 con la API real. Decisiones D-37b a D-37f en la spec.
- A evitar: un `%` suelto en `strings.xml` rompe lint (`StringFormatInvalid`); usar `formatted="false"`.

- Estado (2026-11-23): nv-36 Reseñas y momentos del negocio (Diego, a pedido suyo; reparto: Borre) en `feat/nv-36-business` (sobre `integracion/todo`): 51, 52 y NegMomentos con la API real, pestaña Reseñas. Decisiones D-36b a D-36f en la spec. Sin probar contra staging.
- A evitar: fiarse del nombre del campo en la petición para leer la respuesta: el `PATCH` de momentos manda `agradecido` y la respuesta trae `agradecido_por_negocio`. Revisar la forma del objeto en `endpoints.md`.

- Estado (2026-11-23): nv-38 Perfil, fotos y cobros del negocio (Diego, a pedido suyo; reparto: Borre) en `feat/nv-38-business` (sobre nv-36): 58, Datos y horario, 87, 88, 57, 89 y 90 con la API real; pestaña Negocio; salidas a Inicio y Login. Decisiones D-38b a D-38h en la spec. Sin probar contra staging.
- A evitar: escribir scripts de Python con texto en español por la entrada estándar de Bash: la consola cambia la «Ñ» y el parche no encuentra el texto. Guardarlos en el scratchpad y correrlos con `python -X utf8`.
- A evitar: nombres de prueba con `;` o `:` entre comillas invertidas: Kotlin/JVM no los acepta.

- Estado (2026-11-24): `fix/nv-25-chat-baja-al-nuevo`. ChatAmigo y ChatGrupo bajan al mensaje nuevo solo si se veía lo último (`firstVisibleItemIndex <= 1` con `reverseLayout`); si se leía algo viejo no se mueve. El sondeo de ChatAmigo pregunta al reanudar y luego cada 5 s (antes esperaba 5 s, y al volver de InvitarPlan la invitación tardaba en aparecer). Prueba nueva en `DirectChatViewModelTest`. Verificado con JDK 17: ktlint, detekt, `assembleDebug`, 842 pruebas (16 fallos de siempre por MockK en Business/Help, solo local). Sin probar en teléfono.
- Estado (2026-11-25): `main` integra con `merge --no-ff` setup-java 6.3.0, serialization 1.13.0, roborazzi 1.79.0, mockk 1.14.12, el arreglo del chat, `chore/ci-devsecops` (CodeQL y SBOM de Gradle, además del de anchore), `feat/nv-31-memories` (docs) y `feat/nv-17-feed` (ya superada: lado de `main`). % real 101/162 = 62,3 % (ChatAmigo). Tabla por área en `docs/avance.md`, reparto en `docs/pendientes.md`. `cambios-restricciones-y-avance.diff` no existe en el equipo: no se aplicó nada.
- Decisión: coil 3.6.3 y AGP 9.5.2 fuera. Porqué: coil 3.6.3 arrastra Compose UI 1.12, que exige SDK 37 y AGP ≥ 9.1 (`checkDebugAarMetadata`). Entran juntos al subir a SDK 37.
- Decisión: en el conflicto de Semgrep del registro gana la versión de `main` (paquetes explícitos), no `--config=auto` de `chore/ci-devsecops`, que exige mandar métricas.
- Hallazgo (negocio): nv-34 a nv-38 están completos en código y en `main` (Diego hizo f0134e1, 64f56de, aa81281, cdc01fa y a6b37c5; Jorge, el consentimiento de nv-34). Ninguna pantalla se ha probado contra staging, por el 503 de `POST /negocios/{id}/documentos`; por eso cuentan 0/27.

## nv-57 correcciones de la prueba en teléfono (2026-11-26, Borre)
- Estado: rama `fix/negocio-detalles`. 48_NegPanel: «Aún no tienes reservas esta semana.» en lugar de rayas planas y «Sin reseñas esta semana» en lugar de «0.0 ★ (+0)». 33_Detalle: con letra grande, «Llamar» baja a su renglón (antes «Cómo lle…»). Probado: 87_NegGaleria sube fotos con Prueba QA (4 de 10, persiste).
- Hallazgo: tres cosas que parecían errores eran datos: el «recuadro gris» del Panel es la portada de prueba (naranja con marco blanco) bajo la capa oscura; el aviso «cortado» de la galería es contenido con scroll; el nombre «Cafe Prueba QANegocio de prueba de la app» se escribió así (el alta manda nombre y descripción por separado).
- Decisión: `requiere_refresh` no se arregla en `feature/business`. Porqué: forzar el refresh fuera del candado de `TokenRefreshAuthenticator` puede reusar un refresh rotado y cerrar todas las sesiones; va por Issue a Jorge (`core/network`).
- A evitar: tocar el teléfono mientras la persona lo usa; antes de cada gesto, leer qué pantalla está al frente y, si cambió sin que yo la moviera, detenerse.
- A recordar: «Cafe Prueba QA» (cuenta Prueba QA) está en revisión; sus fotos no salen en Explorar hasta que un administrador lo apruebe.

## Pendientes de decidir
- Numeración única de pantallas: los frames repiten números con pantallas sin relación (D23).
- Proveedor de mapa y de catálogo de lugares: licencias (D25).
- Exclusión de Composables en Kover y numeración frente a `main`.
- Dudas de la maqueta frente a la API: `specs/README.md`.

## Mapa de fases
- `nv-00` = constitución, stack y gobernanza · `nv-01` = foundation (proyecto, tema, navegación, red segura, Room) · `nv-02` = molde Repository→ViewModel→UiState · `nv-03` = puerta de calidad incremental (advisory). Funcionalidades desde `nv-04`.
