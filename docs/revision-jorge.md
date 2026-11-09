# Revisión para Jorge — actualización del 2026-11-08 (noche)

> Rama `feat/api-cloudfront`, creada desde `spec/035-ci-cd`. Sin merge a `main` y sin firmas nuevas.

## A. Qué hicieron los demás

| Quién | Dónde | Qué | Valoración |
|---|---|---|---|
| Nadie | `wherever-mobile`, desde el 2026-11-06 | Solo hay 4 commits, todos tuyos de la madrugada del 6: `7394f99`, `3b9a22e`, `d3aa814`, `dea2af0`. Sin ramas nuevas | — |
| Diego (noche del 5) | `spec/035-ci-cd`: `1762ca6`, `483dcb0`, `9efafd2` | Merge de `main` con `-X ours`, triaje de 96 archivos en `docs/reconciliacion-main.md` y `ArchitectureTest` con Konsist | Respeta `nv-XX` y la constitución. `9efafd2` se subió sin correrlo y rompió el run #11; lo arregló `d3aa814`. `ThemeAndStateTest`, `NavigationTest` y `EncryptedTokenStoreTest` de §3 siguen sin portar |
| Osmar (6 nov) | `wherever-backend`: nv-12, nv-13, nv-14, nv-15 | Dos APIs tras balanceador y CloudFront; retira Caddy y sslip.io | No toca este repo. Introduce el cambio de contrato de §C |

**CI: no verificado.** La API y la web de GitHub dan 404 sin sesión a `wherever-mobile` y `wherever-backend`, y aquí no hay `gh`. Lo último confirmado es la corrida #5 (`eaf3fdb`) en verde. Si el repo volvió a privado, el paso `ui` puede fallar por falta de `/dev/kvm` (lección del 2026-11-06).

## B. Verificaciones de `feat/api-cloudfront`

| Verificación | Resultado |
|---|---|
| `ktlintCheck`, `detekt`, `lintDebug`, `assembleDebug` (JDK 17) | ✅ |
| `testDebugUnitTest` | ✅ 144 pruebas, 0 fallos |
| `koverVerifyDebug` | ✅ 71,97 % |
| critic, routes, strings, specs, sdd, coverage | ✅ |
| `BuildConfig.API_BASE_URL` | ✅ `https://d302bvc0018nfp.cloudfront.net/api/v1/` |
| Certificado HTTPS | ✅ `*.cloudfront.net`, Amazon, vence 2027-03-10, TLS 1.3, verifica con openssl. `network_security_config` solo admite CA del sistema; falta confirmarlo en un dispositivo |
| `GET /health`, `/ready`, `/api/v1/ping` | ❌ **503** del balanceador (23:45 y 23:53 del domingo) |
| Login con correo inexistente | ❌ **503**: no se pudo comprobar el error documentado (`401 no_autenticado`) |
| Flujo real en dispositivo | ⏭️ **No corrido**: `adb devices` vacío y backend apagado |

El 503 coincide con el horario: nv-12 §11 del backend y su Terraform mantienen apagado diario a las 22:00 y encendido L-V a las 08:00. Por eso **la nota de horario no se quitó**: se actualizó con la URL nueva y con el 503 en HTML. La app lo trata como `AppError.Server` y conserva la sesión; ya hay pruebas de ambos casos.

Para repetir a partir del lunes 08:00: las tres rutas, el login y el flujo de cuenta. Hay un AVD `Pixel_8` (API 36) con WHPX que sirve si no conectas teléfono.

## C. Hallazgos que necesitan decisión

1. **Gustos con alergias dará 422.** El backend exige `privacidad.datos_sensibles` para guardar `alergias` (nv-15). La app envía `alergias`, pero `PrivacyDto` no tiene ese campo y no hay interruptor. Afecta a US-022 (Alexander). Cambiar la UI requiere spec, así que no se tocó.
2. **Amigos (US-232/233): Pendiente de backend.** Ninguna rama del backend tiene rutas de amigos; solo existe `codigo_amigo` en el perfil. No se crearon pantallas, spec nueva ni contratos; `nv-20` registra el cotejo.
3. **Tus firmas del 6 de noviembre siguen puestas** en `nv-00…nv-39` (`dea2af0`). No las quité porque las pediste entonces. Si quieres volver a `- [ ] Revisor: Jorge — fecha:`, dímelo. Toqué el texto de `nv-04` (URL) y `nv-20` (cotejo), ya firmadas.
4. **La URL de CloudFront cambia si se recrea la distribución** (nv-12 del backend): habría que recompilar.

## D. Integración a `main`

1. **`spec/arquitectura` → `spec/035-ci-cd`**: trae `7394f99` (acciones fijadas por SHA; en 035 hay 0 fijadas) y `3b9a22e` (reglas Konsist). La simulación de merge no da conflictos, pero deja dos `ArchitectureTest` (`arch/` y `architecture/`): conviene unificarlos.
2. **PR #3 `spec/035-ci-cd` → `main`**: `main` ya es ancestro, así que entra sin conflictos.
3. **PR `feat/api-cloudfront` → `main`**, después del #3. Antes conviene verificar la API con el backend encendido.

---

# Revisión para Jorge — trabajo del 2026-11-05 (noche)

> Rama `spec/arquitectura` (no estaba fusionada en `main`, así que se siguió en ella). Push hecho; **sin pull request**.
> **Ninguna aprobación está firmada.** Cada spec deja `- [ ] Revisor: Jorge — fecha:` para que la firmes.

## 0. Lo primero que conviene decidir

1. **Cobertura (nv-39 §2.5).** Kover pasa el 45 % con **71,99 %** porque excluye código generado y funciones `@Composable`/`@Preview`. Excluyendo solo el código generado da **37,17 %** y `koverVerifyDebug` fallaría. Porqué: Kover solo mide pruebas JVM, y las pantallas se prueban con Compose Testing. Si no aceptas la exclusión, quita la línea `annotatedBy(...)` en `app/build.gradle.kts` y hacen falta pruebas de UI o más pruebas JVM. El umbral no se rebajó.
2. **Trabajo de Diego incorporado.** Diego subió hoy `spec/035-ci-cd` encima de esta rama: `ci-pr.yml`, scripts de puerta, spec 035 y la bajada a AGP 9.0.1 y `compileSdk`/`targetSdk` 36 (su Android Studio no acepta AGP 9.5). Se trajo por fast-forward (`abfcfae`, `43cfedf`) para no duplicar el CI, y sobre eso se hizo lo que pidió el equipo. Si no querías esos commits aquí, hay que revertirlos.
3. **Choque con `main` al fusionar.** `main` tiene otra línea de trabajo de Diego, con su propia `specs/nv-04` (arquitectura, mismo tema que la nuestra) y `specs/nv-05` ("Acceso, registro y onboarding"; aquí `nv-05` es "Flujo general"). También hay código distinto. Hay que acordar la numeración antes del PR.

## 1. Specs a revisar y firmar

D21 quedó resuelta con la constitución §11: `specs/NNN-nombre` → `specs/nv-(NNN+4)`. Todas tienen `spec.md`, `plan.md`, `task.md` y `checklists/gates.md`. No cambió el contenido funcional de ninguna.

| Spec nueva | Antes | Código | Qué cambió |
|---|---|---|---|
| `nv-04` | `000-arquitectura` | sí | Renombrada. `plan.md` a la plantilla, con las pruebas de los tres tipos; `task.md` con T3 (pruebas) y T4 (aprobación); `gates.md` nuevo |
| `nv-05` | `001-flujo-general` | sí | Renombrada. `plan.md`, `task.md` y `gates.md` nuevos, con sus pruebas |
| `nv-06` | `002-splash-arranque` | sí | Renombrada. `plan.md` a la plantilla y pruebas; `task.md` T3/T4; `gates.md` nuevo |
| `nv-07` | `003-login` | sí | Igual que nv-06 |
| `nv-08` | `004-recuperar-contrasena` | sí | Igual que nv-06 |
| `nv-09` | `005-registro` | sí | Igual que nv-06 |
| `nv-10` | `006-onboarding-gustos` | sí | Igual que nv-06 |
| `nv-11` | `007-permisos` | no | Renombrada. `plan.md`, `task.md` y `gates.md` nuevos con las pruebas previstas |
| `nv-12` | `008-sesion-tokens` | sí | Igual que nv-06 |
| `nv-13` | `009-ajustes-cerrar-sesion` | sí | Igual que nv-06 |
| `nv-14` | `010-perfil` | sí | Igual que nv-06 |
| `nv-15` | `011-privacidad-datos` | sí | Igual que nv-06 |
| `nv-16` | `012-sin-conexion` | no | Igual que nv-11 |
| `nv-17` | `013-inicio-feed` | solo Inicio provisional | Archivos nuevos con sus pruebas; la nota "sin prueba unitaria" ahora cita `HomeViewModelTest` |
| `nv-18` … `nv-38` | `014` … `034` | no | Igual que nv-11 (21 specs) |
| `nv-39` | `035-ci-cd` (de Diego) | sí (CI) | Spec ampliada con lo que pidió el equipo; `plan.md`, `task.md` y `gates.md` nuevos |

En todas: `Aprobación` con `- [ ] Revisor: Jorge — fecha:`, y §7 apunta a `plan.md`, `task.md` y `checklists/gates.md`.
`nv-00…nv-03` no se tocaron: usan otra forma de spec (Contexto/Requisitos/…), distinta de `spec-template.md`. `check_sdd.sh` revisa esa forma y `check_specs.sh` la de la plantilla (nv-04 en adelante).

## 2. Qué se hizo

1. **CI/CD (nv-39).** `ci-pr.yml` corre en cada `push` y `pull_request`, en el orden de la constitución §8: deps → build (+ artefacto `wherever-debug-apk`) → `ktlintCheck detekt lintDebug` (JDK 17) → `testDebugUnitTest` → gitleaks (binario verificado por checksum, sin secretos ni licencia) → `connectedDebugAndroidTest` (`reactivecircus/android-emulator-runner`, API 35) → critic, routes, strings, specs, sdd → `koverVerifyDebug` (45 %) → delta (advisory) → Semgrep propio (bloqueante) y registry (advisory) → deps-scan OSV (informativo). Además: `.gitleaks.toml`, `scripts/check_deps_osv.py`, `.github/dependabot.yml`. Sin `secrets.*` ni despliegue.
2. **Estructura.** Renombrado con `git mv` y referencias actualizadas en specs, `docs/routes.md`, `README.md`, `AGENTS.md`, `Memoria.md`, comentarios de código y el workflow.
3. **Pruebas por spec** (de 105 a 147 en JVM, más 4 instrumentadas nuevas):
   - Integración: `ApiDoc` lee las respuestas **del propio** `docs/contexto_api_sprint1.md`, y `RealNetwork` arma la misma cadena que `NetworkModule`. `AuthIntegrationTest` (login 200/401/422/423/429, registro 201/409, recuperar 202/429, 401 con refresh rotativo, refresh rechazado, logout, logs) y `ProfileIntegrationTest` (perfil, `If-Match`, 409, 422, 401 con refresh, gustos, preferencias, privacidad, eliminar y cancelar). Nada usa staging.
   - Room real: `androidTest/.../ProfileRepositoryRoomTest` (Room en memoria + MockWebServer por HTTPS local con `okhttp-tls`, solo en `androidTest`).
   - Seguridad: `PlatformSecurityTest` (sin backup, solo HTTPS, solo CA del sistema, solo el launcher exportado, solo `INTERNET`) y `SourceSecurityTest` (sin secretos ni URLs fijas, tokens solo cifrados, logs solo en el interceptor redactado, rutas sin tokens). Se comprobó que fallan si se activa el cleartext.
   - Unit: `HomeViewModelTest` (nv-17).
   - Arquitectura (sesión 2, 2026-11-06): `ArchitectureTest` con Konsist (`testImplementation`, ya estaba en el catálogo), 9 reglas: `domain` sin Android ni red, `ui` sin red/Room/`data`, ViewModels con `@HiltViewModel` y estado mutable privado, ningún ViewModel con Retrofit/OkHttp/`*Api`, Composables fuera de `data`/`domain` y sin repositorios, `Dispatchers` solo en `core/di`, repositorios interfaz/implementación y sufijos `Dao`/`Entity`/`Api`/`Dto` (nv-02 §R, nv-04). Se comprobó que fallan al meter un import prohibido en `ui`, `domain` y `MainViewModel`.
   - `docs/masvs-checklist.md` enlaza la evidencia de cada control, sin marcar casillas.

## 3. Resultado de cada verificación (local, Windows, JDK 17)

| Verificación | Resultado |
|---|---|
| `./gradlew clean … assembleDebug` | ✅ `BUILD SUCCESSFUL`, `app-debug.apk` 14,7 MB |
| `ktlintCheck` | ✅ |
| `detekt` (JDK 17) | ✅ tras corregir (ver §4) |
| `lintDebug` | ✅ tras desactivar `OldTargetApi` (ver §4) |
| `testDebugUnitTest` | ✅ 147 pruebas, 0 fallos (9 de arquitectura) |
| `koverXmlReportDebug koverVerifyDebug` | ✅ 71,99 % (37,17 % sin excluir Composables) |
| `assembleDebugAndroidTest` | ✅ compila |
| `connectedDebugAndroidTest` | ❌ **no se pudo correr**: sin teléfono conectado; la imagen `android-37.0` del SDK está vacía (1 KB) y no hay `sdkmanager`/`avdmanager` para bajar otra |
| `critic-guardrails.sh` / `check_routes.sh` / `check_strings.sh` | ✅ PASS / OK / OK |
| `check_specs.sh` / `check_sdd.sh` | ✅ SPECS-OK (36 specs) / SDD-OK |
| `check_coverage.sh --min 45` | ✅ COVERAGE-OK |
| `check_delta_coverage.py --min 80 --advisory` | ⚠️ 75,65 % < 80 % (advisory, no bloquea) |
| gitleaks 8.28.0 (historia completa) | ✅ sin hallazgos; sí detecta un secreto de prueba |
| Semgrep reglas propias | ✅ 0 hallazgos |
| Semgrep registry (advisory) | ✅ acciones fijadas por SHA (sesión 2), cooldown de Dependabot (corregido), launcher exportado (necesario), AES-GCM (correcto), `xml.etree` en `check_delta_coverage.py` |
| OSV (`check_deps_osv.py`) | ✅ 164 dependencias, 0 hallazgos; detecta log4j 2.14.1 con una entrada de prueba |
| YAML (`yaml.safe_load`) | ✅ `ci-pr.yml` y `dependabot.yml` |
| Primer run en GitHub Actions | ❓ **sin ver**: no hay `gh` en el equipo y la API de GitHub no respondió |

## 4. Cambios de código y configuración que conviene mirar

- `config/detekt/detekt.yml`: `constantPattern` aceptaba solo `MaxLength` y no `MAX_LENGTH` (la convención de Kotlin), y daba 48 hallazgos; ahora acepta guion bajo. `ReturnCount` no cuenta las cláusulas de guarda.
- Supresiones con su motivo: `MagicNumber` en el dibujo de Bienvenida, `TooManyFunctions` en repositorios (un método por endpoint), `TastesViewModel` y `Surfaces.kt`, `SpreadOperator` en `UiText`, `ReturnCount` en `TastesViewModel.save`.
- `AuthHeaderInterceptor` sin returns de más; el comportamiento es el mismo y lo cubren `InterceptorsTest` y las de integración.
- `lint`: `OldTargetApi` desactivado mientras `targetSdk` sea 36 (decisión de Diego en AGENTS §5).
- `okhttp-tls` agregado al catálogo, solo para `androidTest` (misma familia y versión que OkHttp).
- `ApiDoc` cambia el `…` del JWT de ejemplo de la doc por `...`: con `…` la cabecera `Authorization` es inválida y OkHttp no manda la petición.

## 5. Pendiente o fallido

- [ ] Correr `connectedDebugAndroidTest` con un teléfono o un emulador (instalar una imagen con el SDK Manager de Android Studio).
- [ ] Ver el primer run de `ci-pr.yml` en GitHub Actions (push hecho a las 21:4x).
- [ ] Decidir la exclusión de Composables en Kover (§0.1).
- [ ] Acordar con Diego la numeración frente a `main` (§0.3).
- [ ] Sin pruebas de UI con Compose Testing (necesitan emulador). Konsist ya está: `ArchitectureTest`.
- [ ] Mutación (Pitest) sin configurar (nv-39 §2.6).
- [x] Acciones de GitHub fijadas por SHA, en el último commit de la misma versión mayor (checkout v4.4.0, setup-java v4.9.1, setup-gradle v4.4.4, upload-artifact v4.6.2, emulator-runner v2.38.0). Dependabot propondrá las versiones mayores nuevas.
- [ ] Recorridos manuales en emulador que piden las specs.
- [ ] Entorno local: el JDK 17 usado vive en la carpeta temporal de la sesión; instala uno (Temurin 17) y apunta `JAVA_HOME` para detekt. El archivo sin versionar `gradle/gradle-daemon-jvm.properties` (pide JDK 25 al daemon) se apartó mientras corrían las pruebas y se restauró al final. El cambio local de `gradle.properties` (`org.gradle.tooling.parallel`) se dejó sin commit.

## 6. CHECKLIST_PR.md, punto por punto

| Punto | Estado |
|---|---|
| Spec aprobada y enlazada | ❌ pendiente de tu firma en cada spec |
| `assembleDebug` y `ktlintCheck detekt lintDebug` | ✅ |
| Unit verdes | ✅ 147 |
| Data (Room en memoria + MockWebServer) | ⚠️ MockWebServer ✅; Room real instrumentada sin correr (sin dispositivo) |
| Security + UI + Arquitectura | ⚠️ Security ✅; Arquitectura ✅ (Konsist, 9 reglas); UI no existe todavía |
| Rutas en `docs/routes.md` | ✅ `check_routes.sh → OK`; sin rutas nuevas |
| Permisos en `docs/permissions-matrix.md` | ✅ solo `INTERNET`, comprobado por `PlatformSecurityTest` |
| MASVS en `docs/masvs-checklist.md` | ⚠️ evidencia enlazada; las casillas las marca el revisor |
| Semgrep sin altos; Dependency-Check | ✅ propias en 0; OSV en 0; registry advisory listado arriba |
| Cobertura ≥ 45 % | ✅ 71,99 % (con la exclusión de §0.1) |
| Sin secretos, URLs fijas ni tokens en logs; solo HTTPS | ✅ gitleaks, critic y `SourceSecurityTest` |
| Estados cargando, vacío, error, sin red | ⚠️ cubiertos en pruebas de ViewModel; sin revisión en emulador |
| Accesibilidad | ❌ no revisada esta noche |
| `Memoria.md` | ✅ actualizada |
| YAML válido; `assembleDebug` tras mutación | ✅ YAML; no hubo mutación |
| Revisor y fecha | ❌ pendiente |

## 7. Commits

`abfcfae`, `43cfedf` (Diego) · `21f08c4` CI · `7806113` detekt y ktlint · `36fad3e` renombrado D21 · `9e945b9` pruebas · `50a0c19` plan, task y gates · `1281bc2` OSV, Dependabot y MASVS · y el de este documento con `Memoria.md`.
