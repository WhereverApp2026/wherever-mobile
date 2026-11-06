# NV-01 — Foundation: proyecto, tema, navegación, red segura y datos locales

## Contexto
Base técnica común que cualquier funcionalidad reutiliza: proyecto que compila, tema, navegación, cadena de red, tokens cifrados y Room. Sin pantallas del producto. **Depende de**: nv-00.

## Requisitos
1. Proyecto Gradle (Kotlin DSL) con módulo `app`, Hilt y catálogo de versiones.
2. Tema Wherever (colores, tipografía DM Sans/Sora, formas) y componentes base mínimos en `core/designsystem`.
3. Navegación tipada con `home` (vacía) y `debug/conexion` (solo debug), registradas en `docs/routes.md`.
4. Cadena OkHttp `RequestId → AuthHeader → RedactedLogging` + `Authenticator` de un solo reintento; errores → `AppError`.
5. Almacenamiento cifrado de tokens (Keystore) y `networkSecurityConfig` solo HTTPS; `allowBackup` controlado.
6. Room vacío con esquema exportado y política de migraciones; ping `/health` y `/ready`.

## Diseño
- Paquetes `core/{designsystem,network,database,security,sync,notifications,di}` y `navigation`.
- `AppError` sellado: `Network`, `Timeout`, `Unauthorized`, `Validation(campos)`, `Server`, `Unknown`.
- `TokenStore` (interfaz) con implementación cifrada; `reset()` para pruebas.
- Dispatchers y reloj inyectados (`DispatcherProvider`, `Clock`).
- Estados de la UI: cargando, vacío, error, sin red, con su componente base.

## Contratos
- Consume del backend solo `GET /health` y `GET /ready` (spec `nv-01` del backend).
- Errores `{codigo, mensaje, campos[]}` → `AppError`.
- Rutas: ver `docs/routes.md`. Permisos: ver `docs/permissions-matrix.md`.

## Tests
- Unit — interceptores (`RequestId`, `AuthHeader`, `RedactedLogging`) 6, `AppError` mapper 8, `Authenticator` 4.
- Data — Room (apertura y esquema) 3, ping con MockWebServer 4 (200, 503, timeout, sin red).
- Security — manifiesto (`allowBackup`, HTTP en claro, componentes exportados) 4, `TokenStore` cifrado 3.
- UI — tema y estados base 3, navegación `home` 2.

## Criterios
- La app compila y abre `home` sin red.
- Ningún token aparece en logs; todo tráfico va por HTTPS.
- El 401 provoca un único intento de refresh.
- `ktlintCheck detekt lintDebug` sin errores.

## Límites
Sin onboarding, planes, amigos, mapa ni reservas; sin FCM real ni Maps real (solo dependencias y canal de notificación declarados); sin analítica.

## Aprobación y Control de Cambios
- **Estado:** ✅ Aprobada (Jorge, 2026-11-06)
- **Revisores:** Jorge
- **Fecha:** 2026-11-06
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-01/checklists/gates.md` + `Memoria.md`.
