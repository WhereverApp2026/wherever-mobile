# NV-05 - Acceso, registro y onboarding

> Estado 2026-11-04: auth y onboarding R5-R8 implementados y verificados; cierre documental pendiente.

## T0 - Spec y documentación (R1-R11)
- [x] Crear `spec.md`, `plan.md`, `task.md` y `checklists/gates.md`.
- [x] Cubrir los 11 frames y declarar fuera de alcance Google, contactos, Facebook y cámara.
- [x] Registrar rutas, permisos, MASVS y contratos en los documentos vivos.
- [x] Verificar rutas reales del backend y documentar respuestas exitosas pendientes.
- [ ] Aprobación de revisor.

## T1 - Bienvenida, login y auth gate (R1, R2)
- [x] `WelcomeScreen`, `LoginScreen`, `AuthGate`, `LoginViewModel`; validación, loading, error genérico, 429 y toggle de contraseña.
- [x] Sin sesión → bienvenida; con sesión → sesiones; refresh rechazado limpia; red/timeout conserva sesión.
- [x] Google oculto, sin biometría, passkeys ni "recordarme".
- [x] Reanudación de onboarding implementada; salida a `auth/sessions` hasta el shell de `nv-06`.

## T2 - Registro y recuperación (R3, R4)
- [x] `RegisterScreen`, `RecoverScreen`, `RecoverSentScreen`, `ResetPasswordScreen`; términos, política de contraseña, respuesta genérica, reenvío 45 s y código manual.
- [x] `VerifyEmailScreen` y `SessionsScreen`; sesiones y cierre de todas.
- [x] 409 email, código inválido, password mismatch y vuelta a login; sin enumeración.
- [x] Sin deep link ni persistencia del código; redacción en logs.

## T3 - Onboarding y permisos (R5-R8)
- [x] `PreferencesScreen`, `LocationScreen`, `FriendsScreen`, `PermissionsScreen` y `OnboardingStartScreen`.
- [x] Entidad/DAO `onboarding_state`, preferencias, sugerencias y solicitud de amistad.
- [x] Pasos 1-4, skip, reanudación, ubicación/notificaciones opcionales, cámara educativa y contactos/Facebook ausentes.

## T4 - Red, DTOs y errores (R9, R11)
- [x] DTOs reales, `AuthRepository`, `RemoteTokenRefresher` y `AppError.RateLimited`.
- [x] Refresh single-flight, rotación de tokens y parser tolerante.
- [x] `Result`, `AppError`, sin `Dispatchers.IO` fijo y DTOs fuera de UI.
- [x] Identificador de dispositivo app-scoped; tokens solo en Keystore.
- [x] Endpoints de onboarding con DTOs en español, parser tolerante y `AppError`.
- [ ] Contratos de respuestas exitosas de onboarding (OpenAPI/backend).

## T5 - ViewModels y `UiState` (R1-R10)
- [x] `UiState`/ViewModel por pantalla auth; eventos de una sola vez.
- [x] `StateFlow`, sin lógica en Composables y sin Retrofit/Room en ViewModel.
- [x] `OnboardingStartViewModel` y ViewModels de los 4 pasos; estados offline locales.

## T6 - UI, strings y navegación (R10, R11)
- [x] Composables, `strings.xml`, `testTag`, grafo `auth` y rutas tipadas.
- [x] Welcome, login, registro, recuperación, reset, verificación y sesiones.
- [x] Diseño `core/designsystem`, 48 dp, contraste, IME/autofill y textos localizados.
- [x] Preferencias, ubicación, amigos, permisos y salida a `auth/sessions` (`home` en `nv-06`).

## T7 - Tests (R11)
- [x] 75 tests JVM verdes: auth 22, onboarding 7, red 22, common/UI/demo/health/config 19, arquitectura 5.
- [x] 17 tests instrumentados verdes (`connectedDebugAndroidTest`).
- [x] `MockWebServer` para auth y onboarding; sin repositorios simulados en datos.
- [x] Tests de onboarding, esquema 2 y DAO; `MigrationTestHelper` de `1→2` y cobertura ≥45 % pendientes `[planificado nv-03]`.

## T8 - Gates y cierre (R11)
- [x] `docs/routes.md`, `docs/architecture.md`, `docs/permissions-matrix.md` y `Memoria.md` actualizados con auth y onboarding.
- [x] `assembleDebug`, `ktlintCheck`, `detekt`, `lintDebug`, `assembleDebugAndroidTest` y scripts verdes.
- [ ] `CHECKLIST_PR.md`, `README.md`, cobertura y tag; `permissions-matrix` ya actualizado.
- [ ] Enmienda constitucional `1.2.0` y aprobación de revisor.
- **No hacer**: commit o tag sin aprobación explícita; no cerrar con deuda oculta.
