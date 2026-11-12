# NV-40 — Conexión con la API en wifi y datos móviles

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Sesión: `specs/nv-12/spec.md`.
> Corrección transversal (sin pantalla nueva). Rama `fix/conexion-api`.

## 1. Objetivo

Que la app hable con la API de staging (`https://d302bvc0018nfp.cloudfront.net/api/v1/`) por wifi y por datos móviles, y que el aviso "Sin conexión" salga **solo** cuando de verdad no hay red. Cualquier otra falla (TLS, tráfico en claro, timeout, 4xx/5xx, cuerpo ilegible) muestra su propio mensaje.

## 2. Alcance / No-alcance

### Síntoma reportado (2026-11-11)

En el teléfono de Jorge, al crear cuenta, la app decía "Sin conexión. Revisa tu internet e inténtalo de nuevo." con wifi y con datos móviles funcionando.

### Causas encontradas en el código

1. `safeApiCall` convertía **cualquier** `IOException` en `AppError.Network`. `SSLHandshakeException`, `SSLPeerUnverifiedException` y `UnknownServiceException` (CLEARTEXT bloqueado) son `IOException`: un problema de certificado o de configuración se mostraba como "Sin conexión".
2. OkHttp prueba varias rutas (IPv6 e IPv4) y lanza la **primera** falla, con las demás en `suppressed`. Una falla TLS en la ruta IPv4 llegaba como `ConnectException` de IPv6 y también se mostraba como "Sin conexión". La prueba `ConnectionIntegrationTest` lo reprodujo.
3. `release` caía en `https://example.invalid/api/v1/` si `local.properties` no definía `API_BASE_URL`: un APK de release siempre daba `UnknownHostException`, que también se mostraba como "Sin conexión".
4. Una build instalada antes del 2026-11-06 apunta a `18-227-230-50.sslip.io`, que el backend retiró (nv-12/nv-13 del backend): `UnknownHost`/`Connect`, de nuevo "Sin conexión". Evidencia del dispositivo en §7.
5. El cuerpo 2xx ilegible (portal cautivo, HTML) era `Unknown` sin mensaje propio.

### Qué cambia

- `AppError.SecureConnection` (TLS o cleartext) y `AppError.InvalidResponse` (2xx fuera de contrato), cada uno con su texto en `strings.xml`.
- `IOException.toAppError()` revisa la excepción, sus causas y las suprimidas: TLS/cleartext → `SecureConnection`; `InterruptedIOException` → `Timeout`; el resto (`UnknownHost`, `Connect`, `NoRouteToHost`, `Socket`) → `Network`.
- `RedactedLoggingInterceptor` registra `FAILED <clases>` cuando no hay respuesta, para leer la causa real en Logcat (`WhereverNet`). Sin mensajes, cabeceras ni cuerpos.
- Timeouts: connect 15 s, read y write 30 s.
- `release` usa la URL de staging si `local.properties` no define otra (hoy no hay URL de producción).

### No-alcance

- **No se añade `ACCESS_NETWORK_STATE`** ni un chequeo previo con `ConnectivityManager`: la app no consulta la red antes de intentar la petición, y un permiso sin uso contradice `docs/permissions-matrix.md` y "sin código muerto". Si una spec futura (nv-16, pantalla Sin conexión) observa la red, lo añade con `NET_CAPABILITY_INTERNET` y cualquier transporte. `ConnectionSecurityTest` impide hoy un chequeo que bloquee.
- `network_security_config` no cambia: ya es solo HTTPS con CA del sistema, que incluye la de Amazon de `*.cloudfront.net`.
- La pantalla Sin conexión (`nv-16`) no se implementa aquí.

## 3. Contratos (API y navegación)

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/health`, `/ready` | Diagnóstico manual (fuera de `/api/v1`) |
| `GET` | `/api/v1/ping` | Diagnóstico manual |

Mapa de fallas a `AppError` y texto:

| Falla | `AppError` | `strings.xml` |
|---|---|---|
| `UnknownHostException`, `ConnectException`, `NoRouteToHostException`, `SocketException`, otra `IOException` | `Network` | `error_network` ("Sin conexión…") |
| `SocketTimeoutException` / `InterruptedIOException` | `Timeout` | `error_timeout` |
| `SSLException` o `UnknownServiceException` en la excepción, su causa o sus suprimidas | `SecureConnection` | `error_secure_connection` |
| `SerializationException` (2xx ilegible) | `InvalidResponse` | `error_invalid_response` |
| HTTP con `{codigo, mensaje}` | según `codigo` (`specs/nv-04 §3.3`) | `mensaje` de la API |
| HTTP ≥ 500 sin JSON (503 HTML de CloudFront) | `Server` | `error_server` |

Sin rutas de navegación nuevas.

## 4. Modelo de datos

Sin cambios en Room ni en DTO. `AppError` gana dos objetos.

## 5. Seguridad y privacidad

- Solo HTTPS en debug y release; sin `sslip.io` ni `example.invalid` (MASVS NETWORK).
- El log de fallas guarda solo nombres de clase: sin `getMessage()`, que puede llevar hosts o datos (constitución §9).
- No se relaja `network_security_config` ni se confía en certificados propios.

## 6. Tests / Gates

- **Unit**: `SafeApiCallTest`: cada excepción de red cae en su `AppError`; TLS suprimida o como causa → `SecureConnection`; cada error tiene un texto distinto.
- **Data** (integración, MockWebServer): `ConnectionIntegrationTest`: timeout, 503 HTML, error JSON con `traza_id`, 200 no JSON, conexión rechazada, certificado no confiable y cleartext bloqueado, con la cadena `RequestId → RedactedLogging` real.
- **Security**: `ConnectionSecurityTest`: URL HTTPS en debug y release, sin hosts viejos, timeouts y sin chequeo de conectividad bloqueante; `PlatformSecurityTest` y `SourceSecurityTest` siguen en verde.
- Cobertura ≥ 45 %.

## 7. Plan de entrega

PR "fix: conexión con la API en wifi y datos móviles". Ver `plan.md`, `task.md` y `checklists/gates.md`. Diagnóstico del dispositivo y prueba manual en `task.md` T2.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
