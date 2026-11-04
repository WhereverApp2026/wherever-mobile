# NV-05 — Acceso, registro y onboarding

## Contexto
Primera funcionalidad de producto del proyecto: bienvenida, login, registro, recuperación de contraseña, verificación de correo, sesiones activas y onboarding de cuatro pasos. Cubre los **11 frames** del módulo `Acceso y registro` de `docs/figma-inventory.md`; dos acciones sociales del diseño (contactos y Facebook) y Google Sign-In quedan fuera de esta spec y se difieren. **Depende de**: `nv-04`; usa la foundation de `nv-01` (`TokenStore`, red, Room, navegación) y el molde de `nv-02` (`Result`, `AppError`, `UiState`).

**Estado 2026-11-04:** implementadas 9 pantallas auth (`gate`, `welcome`, `login`, `register`, `recover`, `recover/sent`, `recover/reset`, `verify-email`, `sessions`) y el onboarding R5-R8 (`preferences`, `location`, `friends`, `permissions`) contra el backend real. Las respuestas exitosas del backend no están documentadas; los parsers son tolerantes y los contratos exactos de onboarding siguen abiertos.

La spec abre la etapa de producto. La constitución `1.1.0` declara fuera de alcance las pantallas en `nv-00…nv-03`; `nv-05` no forma parte de ese rango. El código auth ya existe y la enmienda de etapa `1.2.0` sigue pendiente de aprobación; `Memoria.md` deja constancia del inicio de la etapa de producto.

El backend Golang responde en desarrollo, pero su OpenAPI/artifact no es accesible: las rutas de §Contratos están verificadas con `OPTIONS`/peticiones reales y las respuestas exitosas siguen sin documentar. El flujo obligatorio de esta slice es email/contraseña; Google, contactos y Facebook no se simulan ni se muestran como botones muertos.

| Frame | Pantalla | Ruta | Alcance en nv-05 |
|---|---|---|---|
| `01_Bienvenida` | Bienvenida | `auth/welcome` | Implementada |
| `03_Login` | Login | `auth/login` | Implementada; Google oculto |
| `03_Recuperar` | Recuperar | `auth/recover` | Implementada |
| `04_RevisaCorreo` | Revisa correo | `auth/recover/sent` | Implementada |
| `05_Registro` | Registro | `auth/register` | Implementada; Google oculto |
| `06_Gustos` | Preferencias | `auth/register/preferences` | Implementada (R5) |
| `06_PermisoSys` | Racional de ubicación | `auth/register/location` | Implementada (R6) |
| `07_EncuentraAmigos` | Amigos sugeridos | `auth/register/friends` | Implementada (R7) |
| `EncuentraAmigos · solicitud enviada` | Variante de estado | `auth/register/friends` | Implementada (R7) |
| `08_Permisos` | Permisos | `auth/register/permissions` | Implementada (R8) |
| `NuevaContrasena` | Nueva contraseña | `auth/recover/reset` | Implementada |
| `Verificación de correo` | Verificar correo | `auth/verify-email` | Implementada |
| `Sesiones activas` | Sesiones | `auth/sessions` | Implementada |

## Requisitos
1. R1 — Implementar la bienvenida y el auth gate: sin sesión válida se muestra `auth/welcome`; con sesión y onboarding completo se muestra `auth/sessions` (salida placeholder hasta el shell de `nv-06`); con sesión y onboarding incompleto se reanuda el paso local; con sesión inválida se limpia y se vuelve a bienvenida.
2. R2 — Implementar login email/contraseña con validación, mostrar/ocultar contraseña, loading, bloqueo de envío duplicado, error genérico de credenciales, 429 sin reintento automático y navegación según `onboardingComplete`. Google Sign-In no se implementa ni se muestra en esta slice `[planificado nv-06]`.
3. R3 — Implementar registro con nombre, fecha de nacimiento, email, contraseña, aceptación obligatoria de términos y privacidad, política de contraseña (mínimo 8, una mayúscula y un número), errores por campo y 409 de email existente. El backend puede exigir verificación de correo antes de crear sesión; la UI navega a `auth/verify-email` cuando no recibe tokens.
4. R4 — Implementar recuperación de contraseña sin enumeración de cuentas: solicitud con respuesta genérica, pantalla “Revisa tu correo”, reenvío con espera de 45 s, nueva contraseña con confirmación, invalidación de sesiones y retorno a login. El código se captura manualmente en `auth/recover/reset` (sin deep link) y se redacta de logs.
5. R5 — Implementar preferencias de onboarding: intereses, ambiente, presupuesto y compañía; selección múltiple solo en intereses; persistencia local del paso; envío a `/me/preferences`; “Omitir por ahora” avanza sin bloquear y sin guardar preferencias.
6. R6 — Implementar el permiso de ubicación en contexto, con racional previo y opción “Ahora no”; la denegación no bloquea el onboarding ni obliga a repetir el diálogo. Sin ubicación se usará ciudad manual en `explore` `[planificado nv-07]`.
7. R7 — Implementar sugerencias de amigos del backend y envío de solicitud con estado “Solicitud enviada” idempotente; la pantalla se puede omitir. Contactos, Facebook y `READ_CONTACTS` no se solicitan en `nv-05` `[planificado nv-06]`.
8. R8 — Implementar el paso de permisos: notificaciones opcionales en Android 13+ y educación de cámara sin solicitar `CAMERA`; la app funciona si se niegan. La ubicación ya solicitada se muestra como estado, sin doble prompt.
9. R9 — Implementar sesión y seguridad: tokens solo en `TokenStore` cifrado, refresh real con un único vuelo y un único reintento, rotación de refresh, conservación de sesión ante error de red y limpieza local ante rechazo de credenciales o reset; el logout visible queda `[planificado nv-13]`.
10. R10 — Implementar navegación tipada del grafo `auth` y rutas de `docs/routes.md`: `gate`, `welcome`, `login`, `register`, `recover`, `recover/sent`, `recover/reset`, `verify-email`, `sessions`, `onboarding` y los pasos `preferences`, `location`, `friends` y `permissions`; sin parámetros sensibles en rutas.
11. R11 — Entregar tests Unit/Data/Security/UI/Arquitectura, documentación viva (`routes`, `permissions`, `masvs`, `Memoria`) y gates; cobertura global ≥45 % y sin código muerto, tokens en logs, URLs hardcodeadas ni migración destructiva.

## Diseño

### Modelo de datos
| Elemento | Tipo | Campos | Nota |
|---|---|---|---|
| `OnboardingStateEntity` | Room | `id=1`, `userId`, `step`, `interests`, `vibe`, `budget`, `company`, `updatedAt` | Solo estado de onboarding; no guarda tokens, email ni contraseña |
| `OnboardingDao` | DAO | `observe()`, `get()`, `upsert()`, `clear()` | Se limpia en logout o reset de contraseña |
| Sesión | `TokenStore` cifrado | access, refresh, expiración, `userId` | Fuente de verdad de la sesión; no Room |
| `AuthFlowState` | ViewModel del grafo auth | email de recuperación | En memoria del ViewModel; no se pasa email por ruta |
| `OnboardingPreferences` | Dominio | listas/ids de intereses, ambiente, presupuesto, compañía | Los ids son opacos; las etiquetas viven en `strings.xml` |

- Flujo: `DTO → Mapper → Domain`; solo `OnboardingStateEntity` se persiste en Room. La sesión se persiste en `TokenStore`.
- Base de datos: se agrega la tabla `onboarding_state` con migración explícita `1→2` y esquema exportado en `app/schemas`; prohibido `fallbackToDestructiveMigration`. Si `nv-01` aún no fijó la versión 1, la tabla entra en la versión inicial y se documenta en `Memoria.md`.
- `OnboardingStateEntity.step`: `REGISTER`, `PREFERENCES`, `LOCATION`, `FRIENDS`, `PERMISSIONS`, `COMPLETE`. `LOCATION` se persiste como paso propio para reanudar sin reabrir el diálogo; visiblemente comparte el paso 2 con preferencias.
- La pantalla de registro es el paso 1 de 4; preferencias/ubicación es el 2; amigos el 3; permisos el 4.
- Al completar el paso 4 se actualiza `step=COMPLETE` en `onboarding_state` y en el backend con `POST /me/onboarding/complete`; si falla la red, no se marca completo localmente.

### Seguridad y privacidad
- Contraseñas y códigos de reset nunca se guardan, registran ni incluyen en `SavedStateHandle`; se limpian al salir de la pantalla.
- `TokenStore` usa Keystore; prohibido `SharedPreferences` sin cifrar, Room, logs o capturas con tokens. `allowBackup` debe impedir el respaldo de la sesión. El identificador de dispositivo es un UUID app-scoped persistido en `SharedPreferences` (`device_info/device_id`); no es un token.
- Refresh: `Authenticator` recibe 401 → un único vuelo de refresh. Si el backend responde 401/403, limpia sesión y navega a bienvenida; si hay `Network`/`Timeout`, conserva la sesión y muestra “Sin conexión”, sin logout ni reintentos en bucle. Un refresh exitoso rota refresh y access.
- Los endpoints de login, registro, recuperación, reset y refresh no pasan por `AuthHeader` ni por el `Authenticator`; usan un cliente de refresh sin recursión.
- Recuperación: respuesta genérica para evitar enumeración. El código se ingresa manualmente en `auth/recover/reset`, se redacta de logs y no se persiste; no viaja por ruta ni deep link.
- Reset exitoso: el backend revoca refresh tokens; el cliente limpia `TokenStore` y `onboarding_state`, y vuelve a login.
- Permisos: `ACCESS_COARSE_LOCATION` y `POST_NOTIFICATIONS` se piden en contexto y son opcionales. `CAMERA` no se solicita en `nv-05`; solo se explica. Contactos/Facebook no se solicitan.
- Logs con `request_id`; redacción de `Authorization`, `password`, `refresh_token`, `code`, email y cuerpos de auth. Sin Crashlytics ni analítica.
- URLs legales (`TERMS_URL`, `PRIVACY_URL`) se inyectan por `BuildConfig`/`local.properties`; no se hardcodean. Si faltan, se muestra el texto sin enlace y se registra deuda.
- Rate limit 429 → `AppError.RateLimited(retryAfterSeconds)`; se muestra espera y no se reintenta automáticamente.

### Arquitectura y componentes
- Paquete `feature/auth/{ui,domain,data}`; sin dependencias `feature → feature`.
- `AuthRepository` (data, consumido por ViewModels): `hasSession`, `login`, `register`, `requestRecovery`, `verifyEmail`, `resetPassword`, `sessions`, `logoutAll`, `clearSession`; `RemoteTokenRefresher` implementa el refresh real.
- `OnboardingRepository` (data, consumido por ViewModels): `connect`, `snapshot`, `savePreferences`, `skipPreferences`, `locationHandled`, `friendsHandled`, `suggestions`, `sendFriendRequest`, `complete`, `reset`; `OnboardingRemote` ejecuta la red y `OnboardingParser` tolera respuestas sin documentar.
- `TokenRefresher` real en `data`; `Authenticator` de `nv-01` lo consume. Un `Mutex`/single-flight evita refresh paralelos.
- `AuthFlowViewModel` con alcance del grafo `auth` guarda solo el email de recuperación; el paso de onboarding vive en `OnboardingRepository`. Las pantallas no comparten estado global mutable.
- ViewModels por pantalla con `StateFlow`; los Composables solo pintan y envían eventos. Sin Retrofit, Room ni DTO en UI.
- Errores: `Network`, `Timeout`, `Unauthorized`, `Validation(campos)`, `RateLimited`, `Server`, `Unknown`. `RateLimited` se añade a `AppError`; 409 de email y 410 de código se mapean a `Validation` con el campo correspondiente.

### Pantallas y estados
| Pantalla | Ruta | `UiState` | Estados/eventos |
|---|---|---|---|
| Bienvenida | `auth/welcome` | — | Crear cuenta, iniciar sesión |
| Login | `auth/login` | `LoginUiState` | idle, loading, field errors, credentials error, rate limit, offline, Google oculto |
| Registro | `auth/register` | `RegisterUiState` | idle, loading, email exists, validation, terms required, offline |
| Recuperar | `auth/recover` | `RecoverUiState` | idle, loading, sent genérico, rate limit, offline |
| Revisa correo | `auth/recover/sent` | `RecoverSentUiState` | espera de reenvío, reenviar, abrir correo si existe app |
| Nueva contraseña | `auth/recover/reset` | `ResetPasswordUiState` | código, reglas, mismatch, loading, error, success |
| Verificar correo | `auth/verify-email` | `VerifyEmailUiState` | código, loading, error, success |
| Sesiones | `auth/sessions` | `AuthSessionsUiState` | loading, list, empty, error, cerrar todas |
| Preferencias | `auth/register/preferences` | `PreferencesUiState` | loading, intereses/vibe/budget/company, intereses vacíos, saving, error, skip |
| Ubicación | `auth/register/location` | `LocationUiState` | racional, allow, ahora no, saving |
| Amigos | `auth/register/friends` | `FriendsUiState` | loading, lista, vacío, enviando, “Solicitud enviada”, error, skip |
| Permisos | `auth/register/permissions` | `PermissionsUiState` | estado ubicación, notificaciones 13+, cámara informativa, completando, error |
| Arranque onboarding | `auth/onboarding` | `OnboardingStartUiState` | loading, reanudar paso local, completar si `GET /me` lo indica |
| Auth gate | `auth/gate` | — | loading, unauthenticated, authenticated |

- Un solo botón principal naranja por pantalla; secundarios outlined/texto, desde `core/designsystem`.
- Formularios: `KeyboardType.Email`, `KeyboardType.Password`, autofill, IME next/done, toggle de visibilidad con `contentDescription`, errores asociados al campo, 48 dp.
- Accesibilidad: contraste, orden de foco, anuncio de errores, sin color como único indicador; textos solo en `strings.xml`.
- “Revisa tu correo” no confirma si el email existe; reenvío con contador de 45 s y límite del backend.
- Amigos: lista con avatar, nombre y acción; “Solicitud enviada” deshabilita la acción; vacío con “Ahora no”. Sin contactos/Facebook.
- Permisos: la hoja de ubicación ocurre en el paso 2; el paso 4 solo muestra el estado y solicita notificaciones. Cámara es una fila informativa, sin diálogo del sistema.
- `home` es el placeholder existente de `nv-01`; el shell real llega en `nv-06` `[planificado nv-06]`.

### Offline y sesión
- Login, registro, recuperación, reset y verificación requieren red; muestran error/retry y no cachean credenciales.
- La app abre sin red: sin sesión muestra `auth/welcome`; con sesión cacheada reanuda el onboarding local y, si ya está completo, muestra `auth/sessions`. `GET /me` solo se consulta al arrancar con paso local `REGISTER`; si falla, se continúa con preferencias.
- El cierre de sesiones (`logoutAll`) requiere red; un rechazo de credenciales limpia `TokenStore` y vuelve a bienvenida.
- Logout individual visible y eliminación de cuenta quedan para perfil `[planificado nv-13]`.

## Contratos
Base `API_BASE_URL` + `/api/v1`; `Authorization: Bearer` en sesiones y logout; errores `{codigo, mensaje, campos[], traza_id}`; fechas ISO-8601. Rutas verificadas con el backend de desarrollo; **respuestas exitosas no documentadas** y parser tolerante.

| Método y ruta | Uso | Request real | Response | Errores verificados |
|---|---|---|---|---|
| `POST /auth/login` | Login | `{correo,password,dispositivo:{id,nombre,plataforma,version_app}}` | Tokens (forma sin confirmar) | 401, 429 |
| `POST /usuarios` | Registro | `{nombre,fecha_nacimiento,correo,password,version_terminos}` | Sin confirmar; puede no incluir tokens | 409 email, 422 |
| `POST /auth/recuperar` | Solicitar reset | `{correo}` | Genérica (sin confirmar) | 429 |
| `POST /auth/restablecer` | Cambiar contraseña | `{token,password_nuevo}` | Sin confirmar | 400/410 |
| `POST /auth/verificar-correo` | Verificar correo | `{token}` | Sin confirmar | 400 |
| `POST /auth/refresh` | Renovar sesión | `{refresh_token}` | Tokens (forma sin confirmar) | 401 |
| `GET /auth/sesiones` | Sesiones activas | — | Forma sin confirmar; parser tolerante | 401 |
| `POST /auth/logout-todos` | Cerrar todas | — | Sin confirmar | 401 |
| `POST /auth/logout` | Revocación individual | — | Diferido `[planificado nv-13]` | 401 |
| `POST /auth/google` | Google | — | Diferido `[planificado nv-06]` | — |
| `GET /me` | Reconciliar onboarding | — | `onboarding_completo` (forma sin confirmar) | 401 |
| `PUT /me/preferences` | Guardar gustos | `{intereses,ambiente,presupuesto,compania}` | Sin confirmar | 400/422 |
| `POST /me/onboarding/complete` | Cerrar onboarding | — | Sin confirmar | 401 |
| `GET /friend-suggestions?limit=20` | Sugerencias | — | Lista (forma sin confirmar) | 401 |
| `POST /friend-requests` | Solicitar amistad | `{usuario_id}` | Sin confirmar | 400/409 |

- `version_terminos=1.0` y `fecha_nacimiento=YYYY-MM-DD` son supuestos de cliente pendientes de confirmación.
- `dispositivo.id` es un UUID app-scoped persistido en `SharedPreferences`; `nombre` usa el modelo del dispositivo; `plataforma=android`.
- `AuthSessionParser` acepta tokens en raíz, `data`, `session`, `tokens`, `sesion` o `resultado`; no inventa tokens si el registro solo devuelve verificación.
- `409` de registro se mapea a `Validation(email)`; 401 de refresh limpia sesión; 429 produce `RateLimited` y no reintenta.
- Errores: 400/409/410/422 → `Validation(campos)`; 401 login/verify → `Unauthorized`; 401 refresh → sesión expirada; 429 → `RateLimited`; 5xx → `Server`; red/timeout → `Network`/`Timeout`.
- No tokens en query strings ni parámetros de ruta.
- Onboarding: requests con campos en español por convención observada (`intereses`, `ambiente`, `presupuesto`, `compania`, `usuario_id`); respuesta exitosa y nombres definitivos pendientes de OpenAPI. `OnboardingParser` acepta datos en raíz, `data`, `usuario`, `items`, `sugerencias` o `usuarios`.
- Rutas del grafo `auth` en `docs/routes.md`; el correo de recuperación no viaja por ruta.

| Ruta | Pantalla | Permisos | Spec |
|---|---|---|---|
| `auth/gate` | Auth gate | ninguno | nv-05 |
| `auth/welcome` | Bienvenida | ninguno | nv-05 |
| `auth/login` | Login | ninguno | nv-05 |
| `auth/register` | Registro | ninguno | nv-05 |
| `auth/recover` | Recuperar | ninguno | nv-05 |
| `auth/recover/sent` | Revisa correo | ninguno | nv-05 |
| `auth/recover/reset` | Nueva contraseña | ninguno | nv-05 |
| `auth/verify-email` | Verificar correo | ninguno | nv-05 |
| `auth/sessions` | Sesiones activas | ninguno | nv-05 |
| `auth/onboarding` | Arranque de onboarding | ninguno | nv-05 |
| `auth/register/preferences` | Gustos | ninguno | nv-05 |
| `auth/register/location` | Racional de ubicación | `ACCESS_COARSE_LOCATION` | nv-05 |
| `auth/register/friends` | Encuentra amigos | ninguno | nv-05 |
| `auth/register/permissions` | Permisos | `POST_NOTIFICATIONS` | nv-05 |
| `home` | Placeholder post-onboarding | ninguno | nv-01 |

## Tests
| Suite | Objetivo | Medición 2026-11-04 |
|---|---|---|
| JVM | 75 | 75: auth 22, onboarding 7, red 22, common/UI/demo/health/config 19, arquitectura 5 |
| Instrumentadas | 17 | 17: base de datos 5, seguridad 7, UI 5 |
| **Total** | **92** | **92** (75 JVM + 17 instrumentadas), 0 fallos; cobertura pendiente `[planificado nv-03]` |

- `OnboardingRepositoryTest` cubre `GET /me`, fallback local, preferencias, skip, sugerencias, solicitud y complete; sin repositorios simulados en datos.
- `AuthRepositoryTest` cubre login OK/401/429, registro con/sin tokens, recuperación, sesiones, logout y refresh rechazado.
- `WhereverDatabaseTest` cubre esquemas 1 y 2, lectura/escritura de `onboarding_state` y limpieza.
- Unit: validación, emails, contraseñas, términos, sesión, refresh, errores de auth y reanudación de onboarding.
- Data: `MockWebServer` para auth, refresh y onboarding; migración `1→2` sin `MigrationTestHelper` todavía (deuda abierta).
- Security: `TokenStore` cifrado, ausencia de tokens/código en logs, `allowBackup`, `networkSecurityConfig` y refresh concurrente.
- UI: Compose Testing por `testTag` y textos de `strings.xml`; estados de auth, onboarding y navegación.
- Arquitectura: Konsist; `feature/auth` solo depende de `core`; `ui` no importa `data`.

## Criterios
- La app abre sin red: sin sesión muestra `auth/welcome`; con sesión cacheada reanuda el paso local o muestra `auth/sessions` si ya completó.
- Login y registro guardan tokens cifrados cuando el backend los devuelve; si no, navegan a verificación de correo. Al autenticar se entra al arranque de onboarding.
- Un 401 de refresh hace un solo intento; si el servidor rechaza, limpia sesión; si es red/timeout, conserva sesión y muestra sin conexión.
- “Olvidé mi contraseña” no revela si el email existe; el reset con código válido cambia la contraseña y vuelve a login.
- Preferencias guarda intereses/ambiente/presupuesto/compañía; “Omitir” avanza sin llamar a la red.
- Ubicación/notificaciones denegadas no bloquean; cámara/contactos/Facebook/Google no se solicitan ni se simulan.
- Amigos muestra sugerencias, permite enviar solicitud con estado “Solicitud enviada” y omitir; el paso 4 cierra con `POST /me/onboarding/complete`.
- `docs/routes.md`, `docs/architecture.md`, `docs/permissions-matrix.md` y `Memoria.md` actualizados con onboarding.
- `assembleDebug`, `testDebugUnitTest` (75), `connectedDebugAndroidTest` (17), ktlint, detekt, lint y scripts verdes.
- Cobertura ≥45 % pendiente `[planificado nv-03]`; enmienda constitucional y aprobación de revisor abiertas.

## Límites
Sin Google, contactos, Facebook, cámara, biometría, passkeys, 2FA, logout visible, eliminación de cuenta ni recuperación por SMS. Sin feed, planes, amigos completos, mapas, pagos, chat ni IA; esas funcionalidades llegan en `nv-06+` `[planificado nv-06]`. Sin FCM real, analítica, Crashlytics, URLs legales hardcodeadas ni contratos definitivos. Sin perfil persistido en Room en esta slice. Sin `fallbackToDestructiveMigration`, sin tokens en Room/logs, sin pedir `READ_CONTACTS` ni `CAMERA`. La enmienda de etapa de la constitución y la aprobación de revisor quedan abiertas; esta spec no se declara aprobada.

## Aprobación y Control de Cambios
- **Estado:** Borrador implementado (9 pantallas auth + 4 pasos de onboarding + arranque)
- **Revisores:** (pendiente de nombre y firmas)
- **Fecha:** 2026-11-02; actualizado 2026-11-04
- **Detalle:** 14 rutas auth (9 auth + arranque + 4 pasos); 3 acciones diferidas (Google, contactos, Facebook). Rutas reales verificadas; respuestas exitosas de onboarding pendientes de OpenAPI. Evidencia en `specs/nv-05/checklists/gates.md` + `Memoria.md`.
