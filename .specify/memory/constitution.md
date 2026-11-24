---
version: 1.0.0
ratified: 2026-10-28
last_amended: 2026-11-23
status: borrador (pendiente de firma del revisor)
source_of_truth: .specify/memory/constitution.md
mirror: ./constitution.md
methodology: SDD
---

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
| Imágenes y mapas | Coil; mapa con osmdroid + OpenStreetMap (excepción E-04) |
| Notificaciones | Firebase Cloud Messaging |
| Seguridad | Android Keystore + almacenamiento cifrado; Room cifrada con SQLCipher (E-06); `androidx.biometric` (E-05); `networkSecurityConfig` solo HTTPS con *pinning* |
| Tests | JUnit, MockK, Turbine, MockWebServer, Compose Testing, Konsist |
| Calidad | ktlint, detekt, Android Lint, Kover, Semgrep, OWASP Dependency-Check |
| Build y CI | Gradle Kotlin DSL + catálogo de versiones; GitHub Actions solo `ci-pr.yml`, sin deploy |

No se agregan librerías sin evaluar licencia, mantenimiento y seguridad. Regla de excepción en `nv-00`.

### Excepciones de stack vigentes

| Id | Librería | Para qué | Licencia | Acordada | Límite |
|---|---|---|---|---|---|
| E-01 | Robolectric + Roborazzi | Capturas de pantalla en pruebas JVM | Apache 2.0 | `specs/nv-55` §2 | Solo `testImplementation` |
| E-02 | ZXing `core` + `zxing-android-embedded` | Generar y leer el QR del código de amigo y del mostrador | Apache 2.0 | `specs/nv-20` §2.5 | El lector pide la cámara en contexto |
| E-03 | Konsist | Reglas de arquitectura en pruebas | Apache 2.0 | `nv-03` | Solo pruebas |
| E-04 | osmdroid (OpenStreetMap) | Mapas de Explorar, día del plan y punto de encuentro (D25) | Apache 2.0; datos ODbL con atribución visible | Jorge, 2026-11-23 | Sin llave; atribución «© OpenStreetMap» en cada mapa; rutas paso a paso en la app de mapas del teléfono; agente de usuario propio (política de teselas de OSM) |
| E-05 | `androidx.biometric` | Desbloqueo con huella o rostro al abrir la app | Apache 2.0 (Google) | Jorge, 2026-11-23 | Opcional, se activa en Ajustes; sin biometría la app funciona igual |
| E-06 | SQLCipher (`net.zetetic:sqlcipher-android`) | Cifrar la base de Room | BSD (Zetetic) | Jorge, 2026-11-23 | Llave aleatoria envuelta con Android Keystore; migración de la base sin cifrar probada con datos reales |
| E-07 | CycloneDX Gradle | SBOM de dependencias en la CI | Apache 2.0 | Jorge, 2026-11-23 | Solo build |

## 4. Arquitectura

- Paquetes `core/*` (designsystem, network, database, security, sync, notifications, di) y `feature/<nombre>/{ui,domain,data}`.
- Un `UiState` por pantalla expuesto con `StateFlow`; los Composables solo pintan y envían eventos.
- Cadena de red fija: `RequestId → AuthHeader → RedactedLogging`, con `Authenticator` aparte que renueva el token una vez ante un 401.
- Rutas de navegación tipadas, registradas en `docs/routes.md`.
- Diseño: Azul Ruta `#2455E8`, Tinta `#10243B`, Atardecer `#FF7A3D`, Menta `#12A37F`, Dorado `#F2B531`, Niebla `#F4F7FC`; fuentes DM Sans y Sora; un solo botón principal naranja por pantalla.
- Errores de la API `{ codigo, mensaje, campos[] }` se mapean a `AppError`. Paginación por cursor `PageResult{items, siguienteCursor, limite}`.

## 5. Código y UI

- Inglés en código y paquetes; comentarios y textos en español. Todo texto visible va en `strings.xml`.
- Colores y tipografías solo desde `core/designsystem`. Composables pequeños y sin estado.
- Dispatchers y relojes se inyectan; no se usa `Dispatchers.IO` fijo en lógica de negocio.
- Complejidad `detekt ≤ 15` por función. `ktlint` y `detekt` sin errores; Android Lint con advertencias como error.
- Accesibilidad: contraste suficiente, `contentDescription` en iconos informativos, áreas táctiles de 48 dp.
- Room: esquemas exportados y migraciones explícitas; sin `fallbackToDestructiveMigration`.

## 6. Seguridad y privacidad (DevSecOps)

- Tokens cifrados con Keystore; nunca en logs, `SharedPreferences` sin cifrar ni respaldos (`allowBackup=false` o reglas explícitas).
- `networkSecurityConfig`: solo HTTPS; sin certificados confiados a ciegas; *certificate pinning* de las CA raíz del servidor con respaldo (al menos dos pines) y fecha de vencimiento.
- Sin llaves de API ni `google-services.json` en el repositorio (`local.properties` y secretos de CI).
- Permisos (ubicación, cámara, notificaciones) en contexto; la app funciona si se niegan.
- Aviso de privacidad visible y opción de eliminar la cuenta (posterior).
- El `Authenticator` reintenta una sola vez; si el refresh falla, se limpia la sesión.
- `docs/routes.md`, `docs/permissions-matrix.md` y `docs/masvs-checklist.md` están vivos; las specs solo los enlazan.
- Pantallas con datos sensibles (contraseñas, código de amigo, datos personales) con `FLAG_SECURE`.
- Detección de root, depurador y app alterada: en debug solo avisa; en release bloquea con una pantalla propia.
- Base local cifrada (E-06) y desbloqueo biométrico opcional (E-05).
- CI: Semgrep completo y OSV bloqueantes; SBOM CycloneDX como artefacto; build de release firmado con secretos de la CI cuando existan.
- Trivy/MobSF/Frida y la publicación automática se documentan como deuda diferida, no exigidas en esta etapa.

## 7. Testing y QA

- Suites: `Unit` (ViewModel, repositorio, interceptores), `Data` (Room en memoria + MockWebServer), `Security` (manifiesto, red, almacenamiento), `UI` (Compose Testing) y `Arquitectura` (Konsist). Mutación y rendimiento son nightly u opcionales.
- Cobertura `≥ 45 %` con `scripts/check_coverage.sh --min 45` (Kover); único bloqueo de cobertura hoy.
- Puerta incremental advisory (`nv-03`): `scripts/check_delta_coverage.py --min 80 --advisory` + mutación enfocada con Pitest sobre lo cambiado (`continue-on-error`).
- Un test que depende del reloj o del orden no es un test: se inyectan dispatchers y relojes.

## 8. CI y release manual

- Orden CI-PR: `deps → build → lint → unit → data → security → ui → critic → routes → sdd → coverage → delta-check → mutation → semgrep → deps-scan`, con `--no-build` cuando aplique.
- Sin CD. Release manual: `./gradlew assembleRelease` + firma con keystore local + tag `vX.Y.Z-(feature)N`.
- `CHECKLIST_PR.md` obligatorio. 1 revisor. `main` protegida.
- YAML validado con `yaml.safe_load`.

## 9. Observabilidad mínima

- Logs con redacción (sin tokens ni datos personales) y `request_id` por llamada. Sin Crashlytics/analítica por ahora (spec posterior).

## 10. Memoria

- `Memoria.md` por fase o slice: estado, decisiones con su porqué y errores a evitar. Máximo 10000 líneas. Sin datos sensibles.

## 11. Gobernanza SDD

- Specs en `/specs/nv-0X` con plantilla de 7 secciones + Aprobación. Sin cambio de contrato con la API sin spec. `AGENTS.md` dice cómo; la constitución dice qué no se negocia.
