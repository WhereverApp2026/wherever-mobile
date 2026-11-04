# Memoria — Wherever Móvil

> Constitución §10. Por fase o slice: estado, decisiones con porqué, errores a evitar. Máx 10000 líneas. Sin datos sensibles.
> Regla operativa: todo cambio o información nueva se registra aquí (fase, decisión, evidencia y pendientes) antes de cerrar la tarea.

## Fase nv-00 — Estructura SDD (2026-10-28)
- Estado: estructura SDD creada (`.specify/memory/constitution.md` canónico + espejo raíz + templates + specs `nv-00…nv-04` + docs vivos + `.claude` con 5 sub-agentes y skills indexadas por fase). **Tareas de código sin ejecutar.**
- Decisión: canónico en `.specify/memory` (estándar Spec Kit) + espejo en raíz. Porqué: compatibilidad con herramientas y visibilidad.
- Decisión: Kotlin + Compose + MVVM, Room como fuente de verdad. Porqué: requisito del proyecto móvil y modo sin conexión.
- Decisión: tokens con Keystore, solo HTTPS. Porqué: seguridad y privacidad desde el día 1.
- Decisión: etapa recortada a arquitectura base. Porqué: pedido del docente; las pantallas se difirieron y hoy llegan con `nv-05+`.
- Deuda: Crashlytics/analítica, publicación en Google Play, firma automática, MobSF, rendimiento (Macrobenchmark) y mutación como gate.
- A evitar: token falso, repositorio simulado en pruebas de datos, `Dispatchers.IO` fijo, `fallbackToDestructiveMigration`, workaround sin issue, pantallas de negocio en `nv-00…nv-04`, referenciar artefactos futuros sin `[planificado nv-0X]`.

## Fase nv-00 — Alineación SDD (2026-10-29)
- Estado: alineación documental completada (26 skills y 5 agentes). **Tareas de código sin ejecutar.**
- Decisión: se conserva el formato actual de spec (`Contexto/Requisitos/Diseño/Contratos/Tests/Criterios/Límites/Aprobación`) y se actualiza la plantilla canónica.
- Decisión: tag único `vX.Y.Z-featN`; constitución enmendada a 1.1.0.
- Decisión: `03-api-vertical-slice` pasa a `02-feature-mold`; se añade `08-observability`.
- Decisión: `nv-02`, `nv-03` y `nv-04` los ejecuta el agente principal; `foundation-builder` se queda en `nv-00/nv-01`.
- Decisión: `.specify/scripts/README.md` y `specs/_template.md` se conservan como placeholders.
- Regla nueva: toda referencia a un artefacto no creado lleva `[planificado nv-0X]`.
- Verificación final: 4 specs de entonces (hoy 5 con `nv-04`) con las 8 secciones canónicas, 26 skills y 5 agentes; `git diff --check` limpio y sin referencias al nombre antiguo fuera del histórico.
- Regla operativa (2026-10-29): todo cambio o información nueva se registra en `Memoria.md`; reforzada en `AGENTS.md §2`.
- Pendiente: fijar `compileSdk`/`targetSdk` en `nv-01`; licencias de DM Sans/Sora; proveedor de mapa/catálogo; nombre del revisor.
- Deuda: scripts (`check_sdd.sh`, cobertura, delta), CI, Dependabot y Semgrep aún no existen.

## Integración Figma (2026-11-02)
- Archivo: `https://www.figma.com/design/9IjBujkogqaUPAkDbluclQ/` (file key `9IjBujkogqaUPAkDbluclQ`, nodo inicial `96:130`); el embed es público. La exportación final tiene 194 PNGs: 158 pantallas/estados de 390×844 (2x) + 36 etiquetas; el conteo inicial de 198 frames se corrigió. Las 842 transiciones siguen inferidas.
- Dev Mode requiere plan/seat de pago; la cuenta actual no lo tiene.
- MCP remoto `https://mcp.figma.com/mcp`: Figma rechazó el registro dinámico del cliente (HTTP 403) y opencode 1.18.35 no figura en el catálogo de clientes soportados; se revirtió la configuración MCP.
- Decisión: usar la API REST de Figma en modo lectura con un PAT en `FIGMA_TOKEN`; el token no se guarda en el repositorio.
- Estado: el PAT está fijado y la lectura del archivo funcionó; la API quedó en `429` con `Retry-After: 398872` s (~4.6 días, plan `starter`, límite `low`). No se debe insistir con llamadas.
- Decisión: continuar el análisis visual con PNG exportados por el usuario a `C:\Users\mafer\AppData\Local\Temp\opencode\figma-frames\` (2x, nombres originales); el grafo exacto de transiciones queda inferido hasta que se libere la API.
- Estado: 158 pantallas/estados y 36 etiquetas recibidas; inventario `docs/figma-inventory.md` con sistema visual, flujos, módulos, rutas propuestas y riesgos. El bloqueo de la API ya no impide avanzar.
- Pendiente: abrir specs `nv-06+` por módulo y validar rutas/contratos con el backend; `nv-05` auth ya está redactada.

## Fase nv-04 — Arquitectura de la aplicación móvil (2026-11-02)
- Estado: `specs/nv-04/{spec,plan,task}.md` + `checklists/gates.md` y `docs/architecture.md` creados; documentación alineada. **Sin código, tests, CI ni pantallas.**
- Decisión: `nv-04` es arquitectura de producto (módulos, MVVM, navegación, datos, contratos, auth, UX y calidad); las pantallas y funcionalidades pasan a `nv-05+`.
- Decisión: los contratos REST se incluyen en `nv-04` como propuestas por módulo, pero no son definitivos hasta el OpenAPI del backend Golang; no autorizan clientes ni pruebas reales.
- Decisión: `docs/architecture.md` es el resumen vivo; `specs/nv-04/spec.md` manda y no se duplican tablas.
- Decisión: modelos compartidos entre features en `core/common/model`; sin capa de casos de uso por defecto.
- Mapa: 11 módulos → `feature/{auth,feed,friends,explore,plans,social,booking,dayof,memories,profile,business}`; `feature → core` y contratos de navegación, nunca `feature → feature`.
- Renumeración: refresh real, FCM, Maps, permisos, sync, chat, pagos e IA pasan de `nv-04` a `nv-05+`; referencias actualizadas en specs, docs, agentes, skills y plantillas.
- Evidencia: 8 secciones en `nv-04`; R1-R8 mapeados en plan/task/gates; T6 = R8; 11 módulos y 158 frames cubiertos por `docs/figma-inventory.md`; 18/19 gates verificados (aprobación pendiente); sin referencias `nv-04` de implementación; `git diff --check` sin errores (solo avisos de fin de línea).
- A evitar: declarar endpoints propuestos como definitivos, meter pantallas en `nv-04`, duplicar contratos en docs y spec, o dejar referencias `nv-04` para implementación.

## Fase nv-05 — Acceso, registro y onboarding (2026-11-02)
- Estado: `specs/nv-05/{spec,plan,task}.md` + `checklists/gates.md` creados; rutas, permisos, MASVS y documentos vivos alineados. **Sin código, tests ni pantallas.**
- Decisión: `nv-05` cubre 11 frames de auth/onboarding con email/contraseña. Google, contactos, Facebook y `CAMERA` no se solicitan ni simulan; Google/contactos/Facebook quedan `[planificado nv-06]` y cámara solo se explica.
- Decisión: onboarding de 4 pasos (registro, preferencias/ubicación, amigos, permisos); ubicación y notificaciones son opcionales; el paso se persiste en Room (`onboarding_state`) y la sesión en `TokenStore`.
- Decisión: refresh real en `nv-05` con un solo vuelo; rechazo del servidor limpia sesión, error de red conserva y muestra sin conexión. Se añade `AppError.RateLimited` para 429.
- Decisión: reset con respuesta 202 genérica, código de un solo uso de 30 min y deep link `auth/recover/reset?code=...`; única excepción documentada a rutas sin datos sensibles.
- Decisión: rutas `auth/*` registradas en `docs/routes.md`; `home` queda como placeholder hasta `nv-06`. El email de recuperación no viaja por ruta.
- Evidencia: 8 secciones; R1-R11 en spec/plan/task/gates; 11 frames cubiertos; 88 tests objetivo; cobertura ≥45 %; `git diff --check` limpio.
- A evitar: simular Google/contactos, pedir `CAMERA` en onboarding, doble prompt de ubicación, tokens en Room/logs, códigos de reset persistidos, URLs legales hardcodeadas y migración destructiva.
- Abierto: enmienda de etapa de la constitución (`1.2.0`) y nombre del revisor; contratos pendientes de OpenAPI.

## nv-05 — Implementación auth real (2026-11-04)
- Estado: **auth implementado y verificado; onboarding R5-R8 pendiente.** 9 pantallas: `auth/gate`, `welcome`, `login`, `register`, `recover`, `recover/sent`, `recover/reset`, `verify-email` y `sessions`; app inicia en `AuthGate`.
- Backend real: `POST /auth/login`, `POST /usuarios`, `POST /auth/recuperar`, `POST /auth/restablecer`, `POST /auth/verificar-correo`, `POST /auth/refresh`, `GET /auth/sesiones`, `POST /auth/logout-todos`. `POST /auth/login` usa `{correo,password,dispositivo{id,nombre,plataforma,version_app}}`; registro usa `{nombre,fecha_nacimiento,correo,password,version_terminos}`.
- Decisión: `AuthSessionParser` tolera tokens en raíz/`data`/`session`/`tokens`/`sesion`/`resultado`; no inventa tokens si el registro solo pide verificación. Porqué: respuestas exitosas no documentadas y el artifact de API no es accesible.
- Decisión: el reset ya no usa deep link `?code=...`; el código se captura en `auth/recover/reset`. Evita parámetros sensibles en rutas y simplifica el grafo. Sustituye la decisión del 2026-11-02.
- Decisión: `AndroidDeviceInfoProvider` usa UUID app-scoped en `SharedPreferences` (`device_info/device_id`) en lugar de `Settings.Secure.ANDROID_ID`; no es token y elimina el lint de hardware ID. Los tokens siguen en Keystore.
- Decisión: `WhereverNavHost(startDestination = AuthGateRoute)` permite tests sin Hilt; `NavigationTest` arranca en `HomeRoute`.
- Decisión: detekt `LongParameterList.ignoreAnnotated=[Composable]`; `ArchitectureTest` reconoce `Feature` y valida ViewModels por `containingFile.path`, excluyendo `*DispatcherProvider.kt`.
- Evidencia: 68 tests JVM y 15 instrumentados verdes, 0 fallos; `assembleDebug`, `assembleDebugAndroidTest`, ktlint, detekt, lint, `check_strings`, `check_routes`, `check_sdd` y `critic-guardrails` verdes; smoke manual de Welcome/Login/Register en emulador.
- Corrección: la primera verificación final detectó imports desordenados en `DeviceInfoProvider.kt` y línea final mixta en `WhereverNavHost.kt`; corregidos y reverificados.
- A evitar: reescribir `strings.xml` entero (mojibake preexistente); mezclar CRLF/LF al final de archivos Kotlin (ktlint marca línea en blanco); nombrar funciones terminadas en `Route` dentro de `navigation/` (`check_routes.sh` las confunde con clases); literales `password = "..."` en tests (falso positivo de critic); asumir que registro devuelve tokens.
- Abierto: respuestas exitosas/OpenAPI, `dispositivo`, sesiones, `fecha_nacimiento`/`version_terminos`, onboarding R5-R8, cobertura ≥45 %, `permissions-matrix`/`masvs`, enmienda `1.2.0`, revisor y Git limpio.

## nv-05 — Onboarding R5-R8 (2026-11-04)
- Estado: **onboarding implementado y verificado.** Pantallas `auth/onboarding`, `auth/register/preferences`, `auth/register/location`, `auth/register/friends` y `auth/register/permissions`; login/registro/gate entran al arranque de onboarding.
- Persistencia: Room v2 con `onboarding_state` y `MIGRATION_1_2`; pasos `REGISTER → PREFERENCES → LOCATION → FRIENDS → PERMISSIONS → COMPLETE`. `LOCATION` se persiste como paso propio para no repetir el racional, aunque comparte el paso visible 2.
- Decisión: `OnboardingRepository.connect()` es local-first; solo consulta `GET /me` cuando el paso local es `REGISTER`. Si el remoto falla o no confirma, continúa en preferencias. Evita bloquear el arranque sin red y llamadas innecesarias.
- Decisión: `clearSession()` es `suspend` y limpia `onboarding_state`; logout individual, `logoutAll` y reset también lo limpian. Consecuencia: volver a iniciar sesión reinicia el onboarding.
- Red: `GET /me`, `PUT /me/preferences`, `POST /me/onboarding/complete`, `GET /friend-suggestions?limit=20` y `POST /friend-requests`; payloads con `intereses`, `ambiente`, `presupuesto`, `compania` y `usuario_id`. Respuestas exitosas sin confirmar; parser tolerante.
- UI: ubicación/notificaciones opcionales y solicitadas en contexto; cámara solo informativa; sin contactos/Facebook/Google. “Solicitud enviada” deshabilita la acción y el estado vacío permite omitir.
- Correcciones: `OnboardingRemote` extraído por `TooManyFunctions`; composables partidos por `LongMethod`; `OPTION_LABELS` por complejidad; `@Suppress("FunctionNaming")` por función. Lint: constante para `POST_NOTIFICATIONS` y plural de amigos en común.
- Prueba: quitar `withTimeoutOrNull` en `connect()`; con `runTest` + `UnconfinedTestDispatcher` cancelaba la petición real y `takeRequest()` quedaba esperando.
- Evidencia: 75 JVM + 17 instrumentados verdes; `lintDebug`, `detekt`, ktlint y scripts `check_routes`, `check_sdd`, `check_strings`, `critic-guardrails` verdes.
- A evitar: asumir que la red está disponible al reanudar onboarding; duplicar el prompt de ubicación; declarar verdes tests de onboarding sin ejecutarlos; olvidar que el script de rutas normaliza clase y ruta (renombrar a `AuthRegister*Route` y `AuthOnboardingRoute`).
- Abierto: contratos/OpenAPI de onboarding, `MigrationTestHelper` de `1→2`, cobertura ≥45 %, enmienda `1.2.0`, revisor y Git limpio.

## Fase nv-01 — Foundation (2026-11-02)
- Estado: implementada y verificada. Proyecto Gradle/Compose/Hilt, tema, navegación, red segura, Room, salud y scripts de gobernanza; **sin pantallas del producto**.
- Stack real: Gradle 9.2.1, AGP 9.0.1 con Kotlin built-in 2.2.10, KSP 2.3.6, Hilt 2.60.1, Compose 1.9.2, Material3 1.4.0, Navigation 2.9.5, Lifecycle 2.9.4, Room 2.8.5, Retrofit 2.11.0 y OkHttp 4.12.0.
- Decisión: `compileSdk`/`targetSdk` 36, `minSdk` 26 y JVM 17 porque es el SDK instalado y verificado; AGP 9.0.1 evita depender de un SDK 37 no instalado.
- Decisión: KSP 2.3.6 por AGP 9; KSP 2.2.10-2.0.2 falla con `kotlin.sourceSets DSL ... not allowed with built-in Kotlin`.
- Decisión: Navigation 2.9.5 y Lifecycle 2.9.4; sus versiones nuevas exigen compileSdk 37/AGP 9.1.
- Fuentes: DM Sans y Sora variables, con licencias OFL en `docs/licenses/`.
- Seguridad: `TokenStore` AES/GCM con Android Keystore; `allowBackup=false` + reglas de backup; HTTPS obligatorio; CA de usuario solo en `debug-overrides`; `Authenticator` de un reintento con refresher nulo (refresh real en `nv-05`).
- Room: entidad técnica `app_metadata`, DB versión 1, esquema `app/schemas/com.wherever.mobile.core.database.WhereverDatabase/1.json`; sin `fallbackToDestructiveMigration`.
- Red: cadena `RequestId → AuthHeader → RedactedLogging`; `AppError` sellado; `/health` y `/ready` con MockWebServer; el ping se muestra solo en `debug/conexion`.
- Evidencia: `assembleDebug` OK; 25/25 unitarias; 15/15 instrumentadas en AVD `Wherever_API_35`; `ktlintCheck detekt lintDebug` OK; `check_sdd.sh → SDD-OK`, `critic-guardrails.sh → PASS`, `check_routes.sh → OK`, `check_strings.sh → OK`; `MainActivity` abre `home` sin red.
- A evitar: usar `avdmanager` legacy con JDK 21 (falla por JAXB); el AVD teléfono se creó manualmente. No versionar `local.properties`; no declarar terminada una fase sin ejecutar los instrumentados; no confundir actividades exportadas de librerías debug (`PreviewActivity`, `ComponentActivity`) con componentes propios.
- Pendiente: `nv-02` (molde), `nv-03` (CI/cobertura) y `nv-05` (auth/onboarding); revisor y enmienda constitucional.

## Pendientes de decidir
- Consolidación de los 158 frames inventariados en rutas/pantallas desde `nv-05`; validar duplicados y estados.
- Abrir las specs `nv-06+` por módulo desde `docs/figma-inventory.md`.
- Aprobar o descartar Google Sign-In/Credential Manager y la importación de contactos/Facebook; hoy están diferidos.
- Proveedor de mapa y de catálogo de lugares: licencias.
- Contratos REST definitivos y OpenAPI del backend Golang.
- Enmienda de etapa de la constitución (`1.2.0`) para fijar `nv-05+` como producto; el código auth ya existe y la enmienda sigue pendiente de aprobación.
- Nombre del revisor para firmar las specs.

## Mapa de fases
- `nv-00` = constitución, stack y gobernanza · `nv-01` = foundation (implementada) · `nv-02` = molde Repository→ViewModel→UiState · `nv-03` = puerta de calidad incremental (advisory) · `nv-04` = arquitectura de producto y contratos propuestos · `nv-05` = auth y onboarding implementados. Pantallas y funcionalidades restantes desde `nv-06+`.
