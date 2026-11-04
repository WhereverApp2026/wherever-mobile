---
version: 1.1.0
ratified: 2026-10-28
last_amended: 2026-10-29
status: borrador (pendiente de firma del revisor)
source_of_truth: .specify/memory/constitution.md
mirror: ./constitution.md
methodology: SDD
---

> v1.1.0 (2026-10-29): tag unificado `vX.Y.Z-featN`, Pitest advisory, aclaración de identificadores de contrato, `minSdk`/`targetSdk`, cobertura provisional, Dependabot y `check_sdd.sh`.

# Constitución — Wherever Móvil

> Rector SDD. Ante conflicto con otro artefacto, **manda esta constitución**.
> Filosofía madre: cada funcionalidad sale liberable con todo `00+01+02+03+04` aplicado.
> Fuera de alcance en esta etapa (`nv-00…nv-03`): pantallas y funciones del producto (onboarding, planes, amigos, mapa, reservas), publicación en Google Play, CD, pruebas de rendimiento y de mutación obligatorias.

## 1. Propósito y alcance

- App Android en Kotlin + Jetpack Compose que consume la API de Wherever (`wherever-backend`).
- Metodología SDD. Toda funcionalidad nace de una spec en `/specs` (`nv-00…nv-0X`).
- Objetivo de esta etapa: dejar la **arquitectura base** (stack, cadena de red, tema, navegación, datos, molde de funcionalidad y puerta de calidad) sin ninguna pantalla del producto.

## 2. Principios no negociables

1. **Offline primero**: Room es la fuente de verdad. La red actualiza Room y la UI lee de Room. La app abre y muestra un estado claro sin red.
2. **Seguridad y privacidad primero**: tokens cifrados con Keystore, solo HTTPS, permisos pedidos en contexto, sin secretos en el repositorio ni en los logs.
3. **Sin estado oculto**: dependencias por Hilt o constructor; los singletons con estado exponen `reset()` para las pruebas.
4. **Fail-fast**: errores uniformes (`AppError`), tiempos de espera cortos, estados de carga/vacío/error visibles; nunca una UI colgada.
5. **Empírico antes que teórico**: medir (arranque en frío, cobertura, tiempos) antes de fijar umbrales.
6. **Documentar límites** en `Memoria.md`; prohibido el workaround sin issue.
7. **Real-first**: prohibido token falso o repositorio simulado en las pruebas de integración de datos; Room y MockWebServer reales.

## 3. Stack obligatorio

| Capa | Tecnología |
|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, Material 3 |
| Arquitectura | MVVM: `ViewModel → Repository → Room / Retrofit` |
| DI y navegación | Hilt, Navigation Compose |
| Red | Retrofit + OkHttp + kotlinx.serialization |
| Local | Room con esquemas exportados, WorkManager |
| Imágenes y mapas | Coil, Maps Compose (posterior) |
| Notificaciones | Firebase Cloud Messaging |
| Seguridad | Android Keystore + almacenamiento cifrado; `networkSecurityConfig` solo HTTPS |
| Tests | JUnit, MockK, Turbine, MockWebServer, Compose Testing, Konsist |
| Calidad | ktlint, detekt, Android Lint, Kover, Semgrep, OWASP Dependency-Check, Pitest (mutación advisory) |
| Build y CI | Gradle Kotlin DSL + catálogo de versiones; GitHub Actions solo `ci-pr.yml`, sin deploy |

No se agregan librerías sin evaluar licencia, mantenimiento y seguridad. Regla de excepción en `nv-00`.
Versiones: `minSdk 26`; `compileSdk`/`targetSdk` se fijan en `nv-01` con la última estable y se registran en `AGENTS.md`. Las licencias de fuentes, mapas y catálogos se anotan en `Memoria.md` antes de incorporarlos.

## 4. Arquitectura

- Paquetes `core/*` (designsystem, network, database, security, sync, notifications, di) y `feature/<nombre>/{ui,domain,data}`.
- Un `UiState` por pantalla expuesto con `StateFlow`; los Composables solo pintan y envían eventos.
- Cadena de red fija: `RequestId → AuthHeader → RedactedLogging`, con `Authenticator` aparte que renueva el token una vez ante un 401.
- Rutas de navegación tipadas, registradas en `docs/routes.md`.
- Diseño: Azul Ruta `#2455E8`, Tinta `#10243B`, Atardecer `#FF7A3D`, Menta `#12A37F`, Dorado `#F2B531`, Niebla `#F4F7FC`; fuentes DM Sans y Sora; un solo botón principal naranja por pantalla.
- Errores de la API `{ codigo, mensaje, campos[] }` se mapean a `AppError`. Paginación por cursor `PageResult{items, siguienteCursor, limite}`.
- Los campos del contrato JSON conservan el nombre del backend (`codigo`, `mensaje`, `campos`, `siguienteCursor`); los identificadores Kotlin internos van en inglés (`errorCode`, `message`, `fields`, `nextCursor`).

## 5. Código y UI

- Inglés en código y paquetes; comentarios y textos en español. Todo texto visible va en `strings.xml`.
- Colores y tipografías solo desde `core/designsystem`. Composables pequeños y sin estado.
- Dispatchers y relojes se inyectan; no se usa `Dispatchers.IO` fijo en lógica de negocio.
- Complejidad `detekt ≤ 15` por función. `ktlint` y `detekt` sin errores; Android Lint con advertencias como error.
- Accesibilidad: contraste suficiente, `contentDescription` en iconos informativos, áreas táctiles de 48 dp.
- Room: esquemas exportados y migraciones explícitas; sin `fallbackToDestructiveMigration`.

## 6. Seguridad y privacidad (DevSecOps)

- Tokens cifrados con Keystore; nunca en logs, `SharedPreferences` sin cifrar ni respaldos (`allowBackup=false` o reglas explícitas).
- `networkSecurityConfig`: solo HTTPS; sin certificados confiados a ciegas. En debug se puede confiar únicamente en una CA de desarrollo mediante un `networkSecurityConfig` de debug; release no confía en certificados de desarrollo.
- Sin llaves de API ni `google-services.json` en el repositorio (`local.properties` y secretos de CI).
- Dependabot se configura en `.github/dependabot.yml` `[planificado nv-03]`.
- Permisos (ubicación, cámara, notificaciones) en contexto; la app funciona si se niegan.
- Aviso de privacidad visible y opción de eliminar la cuenta (posterior).
- El `Authenticator` reintenta una sola vez; si el refresh falla, se limpia la sesión.
- `docs/routes.md`, `docs/permissions-matrix.md` y `docs/masvs-checklist.md` están vivos; las specs solo los enlazan.
- Trivy/MobSF/Frida y firma automática se documentan como deuda diferida, no exigidas en esta etapa.

## 7. Testing y QA

- Suites: `Unit` (ViewModel, repositorio, interceptores), `Data` (Room en memoria + MockWebServer), `Security` (manifiesto, red, almacenamiento), `UI` (Compose Testing) y `Arquitectura` (Konsist). Mutación y rendimiento son nightly u opcionales.
- Cobertura `≥ 45 %` con `scripts/check_coverage.sh --min 45` `[planificado nv-03]` (Kover); único bloqueo de cobertura hoy. El 45 % es provisional hasta la primera medición real y se revisa en `Memoria.md`.
- Puerta incremental advisory (`nv-03`): `scripts/check_delta_coverage.py --min 80 --advisory` `[planificado nv-03]` + mutación enfocada con Pitest sobre lo cambiado (`continue-on-error`) `[planificado nv-03]`. Cualquier alternativa a Pitest exige excepción documentada.
- Un test que depende del reloj o del orden no es un test: se inyectan dispatchers y relojes.

## 8. CI y release manual

- Orden CI-PR: `deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan`, con `--no-build` cuando aplique.
- Sin CD. Release manual: `./gradlew assembleRelease` + tag `vX.Y.Z-featN`; firma y publicación se especifican en una spec posterior (sin firma automática).
- `CHECKLIST_PR.md` obligatorio. 1 revisor. `main` protegida. Dependabot activo mediante `.github/dependabot.yml` `[planificado nv-03]`.
- YAML validado con `yaml.safe_load`.

## 9. Observabilidad mínima

- Logs con redacción (sin tokens ni datos personales) y `request_id` por llamada. Sin Crashlytics/analítica por ahora (spec posterior).

## 10. Memoria

- `Memoria.md` por fase o slice: estado, decisiones con su porqué y errores a evitar. Máximo 10000 líneas. Sin datos sensibles.

## 11. Gobernanza SDD

- Specs en `/specs/nv-0X` con la plantilla canónica de 7 secciones + Aprobación (`Contexto → Requisitos → Diseño → Contratos → Tests → Criterios → Límites → Aprobación`). Sin cambio de contrato con la API sin spec. `AGENTS.md` dice cómo; la constitución dice qué no se negocia.
- `scripts/check_sdd.sh` `[planificado nv-01]` valida plantilla, secciones, aprobación y referencias. Toda referencia a un artefacto aún no creado se marca `[planificado nv-0X]`; si existe, no lleva marca.
- Cada fase y sus skills principales se declaran en `AGENTS.md` §11; toda skill incluye una sección `Verify`.
