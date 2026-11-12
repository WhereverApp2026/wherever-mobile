# plan.md — NV-40 — Conexión con la API en wifi y datos móviles

> Deriva de `specs/nv-40/spec.md`. Respeta constitución §4 (cadena de red) y §6 (solo HTTPS). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `core/common/AppError.kt`: `SecureConnection`, `InvalidResponse`
- `core/network/SafeApiCall.kt`: `IOException.toAppError()`, `failureChain()`
- `core/network/RedactedLoggingInterceptor.kt`: línea `FAILED <clases>`
- `core/ui/UiText.kt` y `res/values/strings.xml`: `error_secure_connection`, `error_invalid_response`
- `core/di/NetworkModule.kt`: connect 15 s, read/write 30 s
- `app/build.gradle.kts`: release cae en `wherever.stagingApiBaseUrl`; `okhttp-tls` en `testImplementation`

## 2. Guardarraíles
- Cadena `RequestId → AuthHeader → RedactedLogging` sin cambios de orden.
- Log sin mensajes de excepción, cabeceras ni cuerpos.
- Sin permisos nuevos ni chequeo previo de conectividad.

## 3. Pruebas
### Unitarias
- `test/.../core/network/SafeApiCallTest.kt`: `UnknownHost`, `Connect`, `NoRouteToHost`, `Socket` e `IOException` → `Network`; SSL y cleartext → `SecureConnection`; `Serialization` → `InvalidResponse`; TLS suprimida o como causa; un texto distinto por error

### Integración (MockWebServer; sin staging)
- `test/.../integration/ConnectionIntegrationTest.kt`: timeout, 503 HTML, 422 JSON con `traza_id`, 200 HTML, conexión rechazada, certificado no confiable (HTTPS con `okhttp-tls`) y cleartext bloqueado; el log trae la clase de la excepción

### Seguridad (`docs/masvs-checklist.md` NETWORK)
- `test/.../security/ConnectionSecurityTest.kt`: `BuildConfig.API_BASE_URL` HTTPS, debug y release con staging, sin `example.invalid`/`sslip.io`/`http://`, timeouts y sin `ConnectivityManager` bloqueante
- `test/.../security/PlatformSecurityTest.kt`: solo HTTPS y CA del sistema (sin cambios)

## 4. Secuencia
1. Diagnóstico sin tocar código (curl, `dumpsys`, Logcat).
2. Mapeo de errores y pruebas.
3. Timeouts, URL de release y log de fallas.
4. Prueba manual en el teléfono (datos móviles y wifi).

## 5. Verificación
- `./gradlew ktlintCheck detekt lintDebug testDebugUnitTest koverVerifyDebug assembleDebug` (JDK 17)
- Prueba manual: registro, login, cerrar sesión y eliminar cuenta, con datos móviles y con wifi.
