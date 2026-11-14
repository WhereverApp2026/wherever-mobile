# plan.md — NV-46 — Notificaciones push con Firebase

> Deriva de `specs/nv-46/spec.md`. Respeta constitución §4 (cadena de red), §5 (sin lógica en Composables) y §6 (sin secretos en Git, permisos en contexto). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`: plugin google-services 4.6.0 (se aplica solo con `app/google-services.json`), BoM 35.2.0 y `firebase-messaging`
- `core/notifications/data/DevicesApi.kt`: `PUT`/`DELETE dispositivos` con cuerpo
- `core/notifications/DeviceRegistration.kt`: registro y baja; sin sesión o sin token no llama
- `core/notifications/PushTokenSource.kt`, `FirebasePushTokenSource.kt`: token de FCM o `null`
- `core/notifications/PushDestination.kt`: `data.tipo` + ids → destino
- `core/notifications/NotificationChannels.kt`, `WhereverMessagingService.kt`: canal `general`, `onNewToken`, push con la app abierta
- `core/di/NotificationsModule.kt`, `AppModule.kt` (`@ApplicationScope`), `Qualifiers.kt`
- `feature/auth/data/AuthRepositoryImpl.kt`: registro tras abrir sesión; baja antes de logout y logout-todos
- `navigation/MainViewModel.kt`: registro al arrancar con sesión; `onPushOpened` → `pushes`
- `navigation/PushNavigation.kt`, `WhereverNavHost.kt`, `NotificationPermission.kt`
- `MainActivity.kt`, `WhereverApp.kt`, `AndroidManifest.xml`, `res/drawable/ic_notification.xml`, `strings.xml`

## 2. Guardarraíles
- `core` no depende de `feature` ni de `navigation` (Konsist): el servicio abre la app con `getLaunchIntentForPackage`.
- El token de FCM solo en el cuerpo y fuera de los logs.
- `google-services.json` nunca en Git.

## 3. Pruebas
### Unitarias
- `test/.../core/notifications/PushDestinationTest.kt` (4)
- `test/.../feature/auth/data/AuthRepositoryImplTest.kt` (3 nuevas)
- `test/.../navigation/MainViewModelTest.kt` (5 nuevas)

### Integración
- `test/.../integration/DevicesIntegrationTest.kt` (6)

### Seguridad
- `DevicesIntegrationTest` (logs), `PushDestinationTest` (ids), `PlatformSecurityTest` (permisos y exportados)

## 4. Secuencia
1. Gradle y Firebase.
2. API, registro y pruebas de integración.
3. Destino del push y pruebas.
4. Login/logout, arranque y navegación.
5. Manifiesto, permiso y servicio.

## 5. Verificación
- Con JDK 17, una por una: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, `assembleRelease`, scripts de puerta y Semgrep
- Prueba manual en teléfono con `google-services.json` y un push de prueba desde la consola de Firebase
