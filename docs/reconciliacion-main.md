# Reconciliación con `main` (2026-11-06)

> Rama `spec/035-ci-cd` (incluye `spec/arquitectura`). Documenta el acuerdo previo a que `main`
> vuelva a ser la rama integradora. Decisiones tomadas con el equipo; la evidencia de CI está en
> la corrida #5 (`eaf3fdb`, 23/23 pasos, ✅ 7 min 42 s).

## 0. Qué hay

- `main` = import propio de Diego (2 commits, 2026-11-04): una implementación completa y
  paralela, no documentación.
- Nuestra rama = línea del equipo desde `8da8e29` (CI verde, 138 pruebas JVM, 2 instrumentadas,
  specs `nv-04…nv-39` con `plan.md`/`task.md`/`gates.md`, scripts de puerta).
- merge-base = raíz: **57 conflictos `add/add`**, 440 archivos distintos, 96 archivos que solo
  existen en `main`.

## 1. Decisión

**`spec/arquitectura` es la implementación canónica.** `main` se pone al día con
`git merge origin/main -X ours` (gana nuestra versión en cada conflicto) **más el triaje de los
96 archivos exclusivos de `main`**: se portan los que aportan, se adaptan los que dependen de su
arquitectura, se descartan los que nuestra rama ya cubre mejor y se diferir los que exigen una
spec propia.

Por qué no al revés: `main` no tiene CI, ni gitleaks/semgrep/OSV, ni las suites de seguridad y
de integración, ni las specs `nv-XX` con plan/task/gates.

## 2. Portar tal cual

| Archivo de `main` | Motivo |
|---|---|
| `docs/architecture.md` | descripción de la arquitectura que falta en `docs/` |
| `docs/figma-inventory.md` | inventario de la maqueta, sirve para las specs de pantalla |
| `docs/licenses/DM-Sans-OFL.txt`, `Sora-OFL.txt` | `Type.kt` usa Sora y DM Sans: la licencia OFL acompaña la fuente |
| `.specify/templates/phase-gate-template.md`, `phase-task-template.md` | plantillas SDD que no teníamos |
| `.claude/skills/02-feature-mold/SKILL.md`, `08-observability/SKILL.md` | skills que no teníamos |

## 3. Portar adaptado (arquitectura nuestra)

| Archivo de `main` | Adaptación necesaria |
|---|---|
| `app/src/test/.../arch/ArchitectureTest.kt` | Konsist: la versión de `main` declara la capa `ui..`, que aquí no existe (nuestro UI vive en `feature/*/ui` y `core/ui`); capas reescritas a `core..`, `feature..`, `navigation..` |
| `app/build.gradle.kts` | añadir `testImplementation(libs.konsist)` — la librería ya está en `gradle/libs.versions.toml` (0.18.1), solo falta enchufarla |
| `app/src/androidTest/.../ui/ThemeAndStateTest.kt` | usar nuestro `Theme.kt`/`Surfaces.kt` y nuestros estados (cargando/vacío/error) |
| `app/src/androidTest/.../ui/NavigationTest.kt` | rutas y `testTag` de `navigation/WhereverNavHost.kt`, no los suyos |
| `app/src/androidTest/.../security/EncryptedTokenStoreTest.kt` | reescribir contra `DataStoreTokenStore` + `KeystoreTokenCipher` (nuestro almacén cifrado) |
| `app/src/main/res/values-night/themes.xml` | verificar nombres de tema antes de copiar |

## 4. Descartar (nuestra rama ya lo cubre)

| De `main` | Cubierto por |
|---|---|
| `core/network/{NetworkModule, ApiErrorParser, AppError, AppErrorMapper, HttpLogSanitizer, HttpLogger, TokenAuthenticator, TokenRefresher}` | cadena fija `RequestId → AuthHeader → RedactedLogging` + `TokenRefreshAuthenticator` + `SafeApiCall`; pruebas `InterceptorsTest`, `TokenRefreshAuthenticatorTest`, `SafeApiCallTest` |
| `core/security/{EncryptedTokenStore, SecurityModule, Tokens}` | `DataStoreTokenStore`, `KeystoreTokenCipher`, `EncryptedTokensSerializer` (prueba propia) |
| `core/ui/{BaseViewModel, UiState}` | un `UiState` por pantalla en `StateFlow`, patrón nv-02 |
| `core/designsystem/{Shape.kt, components/WhereverContentState, WhereverPrimaryButton, WhereverStateView}` | `Buttons/Inputs/Surfaces/Layouts/ConfirmDialog` |
| `feature/auth/*` propio (AuthDtos, AuthRepository, AuthSessionParser, AuthEntryScreens, AuthStates, AuthViewModels, RecoverScreens, SessionsScreen, SecurityScreens, AuthComponents, di/AuthModule, domain/AuthValidator…), `ui/home/HomeScreen.kt` | implementación del equipo con `AuthRepositoryImpl`, pantallas `feature/auth/ui` y `feature/home/ui`; probada contra staging |
| `core/database/{AppMetadata*, Onboarding*, DatabaseMigrations, DatabaseModule}` | con onboarding (§5); aquí `WhereverDatabase` v1 solo con `UserProfileEntity` |
| `demo/*` (repository, viewmodel y test) | el molde nv-02 se implementó sin pantalla demo |
| `app/src/debug/res/xml/network_security_config.xml` | `networkSecurityConfig` solo HTTPS también en debug; `PlatformSecurityTest` lo exige |
| `app/src/main/res/{drawable/ic_launcher.xml, backup_rules.xml}` | nuestro launcher y `data_extraction_rules.xml` |
| Pruebas: `AuthHeaderInterceptorTest`, `RedactedLoggingInterceptorTest`, `RequestIdInterceptorTest`, `TokenAuthenticatorTest`, `AppErrorMapperTest`, `AuthRepositoryTest`, `AuthValidatorTest`, `BaseViewModelTest`, `demo/*Test` | mismas rutas cubiertas por `InterceptorsTest`, `TokenRefreshAuthenticatorTest`, `SafeApiCallTest`, `AuthRepositoryImplTest`, `ValidatorsTest` |
| `androidTest/security/ManifestSecurityTest.kt` | `PlatformSecurityTest` (mismo alcance, ya en verde) |

## 5. Diferir: cada uno es un slice con su spec

| Qué | Por qué no entra ahora |
|---|---|
| **Onboarding completo** (permisos, ubicación, gustos, amigos + `OnboardingApi/Repository`, `Room v2` con `OnboardingDao`/`AppMetadataDao`, esquema `2.json`, `WhereverDatabaseTest`) | exige spec nueva `nv-XX` (no existe en nuestro catálogo), migración Room con `MigrationTestHelper` y cableado de rutas; se porta desde `origin/main` con `git show` |
| **`core/health/*` + `ui/debug/*`** (health/ready + pantalla de depuración de conexión) | funcionalidad sin spec ni ruta; candidato a `nv-XX` corta |
| **`core/common/PageResult.kt` + `core/data/BaseRepository.kt` + sus pruebas** | parte del molde nv-02 sin consumidor todavía: entraría como código muerto |

## 6. Método y verificación

1. `git merge origin/main -X ours` → 57 conflictos resueltos con nuestra versión.
2. `git rm` de los descartados (§4) y `git add` de los portados (§2).
3. Konsist enchufado y `ArchitectureTest` adaptado (§3), en un push aparte.
4. Verificación: `critic/routes/strings/specs/sdd` en local + CI de GitHub Actions (el paso `ui`
   ya corre con KVM; `connectedDebugAndroidTest` se valida en Actions aunque en local no haya
   emulador).
