# NV-46 — Notificaciones push con Firebase

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Cubre "Registro del token FCM del dispositivo" de `specs/nv-18` §3 (ya hay endpoint). Rama `feat/notificaciones-push`.

## 1. Objetivo

Que el teléfono reciba los push de la API. La app se liga a la cuenta con su token de Firebase Cloud Messaging (FCM) al abrir sesión, lo renueva cuando FCM lo cambia, se desliga al cerrar sesión y, al tocar un push, abre el destino según `data.tipo` y sus ids.

## 2. Alcance / No-alcance

### Alcance
- **Firebase.** Plugin `com.google.gms.google-services` 4.6.0, BoM `com.google.firebase:firebase-bom` 35.2.0 y `firebase-messaging` sin versión propia.
  - `google-services.json` va en `app/`, fuera de Git (`.gitignore`, constitución §6). Cada quien lo copia a mano.
  - El plugin solo se aplica si el archivo existe. Sin él (CI, equipo sin configurar) la app compila, Firebase no se inicializa y no hay push. Gradle lo avisa con un `warn`.
- **Permiso `POST_NOTIFICATIONS`** (Android 13+), pedido en contexto la primera vez que se llega a Inicio en cada apertura de la app. Con o sin permiso la app funciona; tras dos rechazos Android ya no muestra el diálogo.
- **Registro.** `PUT /api/v1/dispositivos` con `{token, plataforma: "android"}`. El token va en el cuerpo, nunca en la ruta. Se manda:
  - al iniciar sesión y al registrarse (en segundo plano, sin frenar la entrada),
  - al arrancar la app con sesión, por si el token cambió con la app cerrada,
  - en `onNewToken`, igual.
  - Sin sesión o sin token de FCM no se llama a la API. Un error (p. ej. 422 por el sexto teléfono) no afecta la sesión.
- **Baja.** `DELETE /api/v1/dispositivos` con `{token}` al cerrar sesión y al cerrar todas, **antes** de `auth/logout`, porque necesita el JWT. Si falla, la sesión se cierra igual.
- **Tocar un push.** El título y el texto son genéricos a propósito; el destino sale de `data`:

| `tipo` | Ids que se leen | Destino previsto | Hoy abre |
|---|---|---|---|
| `solicitud_amistad` | `usuario_id` | Perfil del amigo / Notificaciones | Inicio |
| `amistad_aceptada` | `usuario_id` | Perfil del amigo | Inicio |
| `invitacion_plan` | `grupo_id`, `plan_id` | Plan del amigo | Inicio |
| `votacion_abierta` | `grupo_id`, `votacion_id` | Votar | Inicio |
| `votacion_cerrada` | `grupo_id`, `votacion_id` | Votar (resultado) | Inicio |
| otro, o falta el id | — | Notificaciones (`nv-18` §2) | Inicio |

  Esas pantallas aún no existen (nv-17, nv-18, nv-20, nv-25). `navigation/PushNavigation.kt` es el único lugar que se cambia cuando existan.
- Sin sesión, o con la cuenta por eliminarse, tocar un push no navega: la app abre donde le toca.
- Con la app abierta, el push llega a `WhereverMessagingService.onMessageReceived` y se pinta en el canal `general` con los mismos extras. Con la app cerrada lo pinta Android.

### No-alcance
- Lista de avisos, marcar leídos y contador (`GET /notificaciones`…): `nv-18`.
- Respetar `preferencias.notificaciones` y `horario_silencioso`: lo filtra el servidor.
- Iniciar sesión y luego ir al destino cuando la sesión venció (`nv-18` §3): queda para cuando existan los destinos.
- Quitar el teléfono al pedir la eliminación de la cuenta.

## 3. Contratos (API y navegación)

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `PUT` | `/api/v1/dispositivos` | JWT | token, plataforma (`android`) | 204 | 400, 401, 403, 422 (sexto teléfono) |
| `DELETE` | `/api/v1/dispositivos` | JWT | token | 204 | 400, 401, 403, 404 (no es suyo), 422 |

Fuente: hoja de endpoints del equipo (grupo `notificaciones`, `A6`). Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError`.

### Datos del push (`data`)
`tipo` y, según el tipo, `usuario_id`, `grupo_id`, `plan_id`, `votacion_id`. Solo ids, sin datos personales.

### Navegación
Sin rutas nuevas. `MainActivity` (`launchMode="singleTask"`) lee los extras en `onCreate` y `onNewIntent`; `MainViewModel.pushes` los entrega a `WhereverNavHost`.

## 4. Modelo de datos

- `core/notifications/PushDestination`: destino tipado, Kotlin puro, con `from(data)`.
- `DeviceRegistration` / `DeviceRegistrationImpl`: `register`, `registerInBackground`, `unregister`.
- `PushTokenSource` (`FirebasePushTokenSource`): token actual o `null` si Firebase no está configurado.
- Sin cambios en Room: el token de FCM no se guarda; se le pide a Firebase cada vez.

## 5. Seguridad y privacidad

- `google-services.json` fuera de Git. Su `api_key` no es secreta, pero gitleaks la marcaría y la constitución §6 la deja fuera.
- El token de FCM va solo en el cuerpo (no en la URL, así no queda en logs de proxies); `RedactedLoggingInterceptor` solo escribe método y ruta.
- `WhereverMessagingService` no se exporta. La librería agrega su propio receptor exportado, protegido por el permiso `com.google.android.c2dm.permission.SEND`.
- La actividad launcher es exportada y cualquier app puede abrirla con extras: los ids se validan (`[A-Za-z0-9_-]`, máximo 64) y uno raro manda a Notificaciones. Sin sesión no se navega.
- `PendingIntent` inmutable.

## 6. Tests / Gates

- **Unit:** `test/.../core/notifications/PushDestinationTest.kt` (tipos, ids que faltan, tipo desconocido). `AuthRepositoryImplTest`: el login registra el teléfono, el login fallido no, y logout y logout-todos lo quitan con la sesión todavía abierta. `MainViewModelTest`: registro al arrancar con sesión, push con y sin sesión, cuenta por eliminarse y apertura sin push.
- **Data:** `test/.../integration/DevicesIntegrationTest.kt` con la cadena de red real y MockWebServer: `PUT` con token y plataforma en el cuerpo y el JWT, `onNewToken` en segundo plano, `DELETE` con cuerpo, sin sesión o sin token no llama, 422 no rompe la sesión.
- **Security:** el token de FCM no sale en los logs (`DevicesIntegrationTest`); ids con caracteres raros se descartan (`PushDestinationTest`); `PlatformSecurityTest` acepta solo `INTERNET` y `POST_NOTIFICATIONS`, y sigue exigiendo que solo la launcher se exporte.
- **Manual** (teléfono con `google-services.json`): permiso en Android 13+, `PUT` al iniciar sesión, push de prueba desde la consola de Firebase con la app cerrada y abierta, tocarlo, y `DELETE` al cerrar sesión.

## 7. Plan de entrega

PR "feat: notificaciones push con Firebase". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
