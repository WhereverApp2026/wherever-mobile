# NV-01 — Foundation: proyecto, tema, navegación, red segura y datos locales

## Contexto
Base técnica común que cualquier funcionalidad reutiliza: proyecto que compila, tema, navegación, cadena de red, tokens cifrados y Room. Sin pantallas del producto. **Depende de**: nv-00.

## Requisitos
1. R1 — Proyecto Gradle (Kotlin DSL) con módulo `app`, Hilt, catálogo de versiones, `detekt`, `ktlint`, `.editorconfig` y `LocalPropertiesExampleTest` (3).
2. R2 — Tema Wherever (colores, tipografía DM Sans/Sora, formas) y componentes base mínimos en `core/designsystem`.
3. R3 — Navegación tipada con `home` (vacía) y `debug/conexion` (solo debug), registradas en `docs/routes.md`.
4. R4 — Cadena OkHttp `RequestId → AuthHeader → RedactedLogging` + `Authenticator` de un solo reintento; errores → `AppError`.
5. R5 — Almacenamiento cifrado de tokens (Keystore) y `networkSecurityConfig` solo HTTPS; `allowBackup` controlado y confianza de desarrollo solo en debug.
6. R6 — Room configurado con esquema exportado (o entidad técnica mínima si Room no exporta 0 entidades) y política de migraciones; ping `/health` y `/ready`.
7. R7 — Herramientas de gobernanza: `scripts/check_sdd.sh`, `scripts/critic-guardrails.sh`, `scripts/check_routes.sh` y `scripts/check_strings.sh`; `compileSdk`/`targetSdk` fijados.

## Diseño
- Paquetes `core/{designsystem,network,database,security,sync,notifications,di}` y `navigation`.
- `AppError` sellado: `Network`, `Timeout`, `Unauthorized`, `Validation(campos)`, `Server`, `Unknown`; `RateLimited(retryAfterSeconds)` se añade en `nv-05` `[planificado nv-05]`.
- `TokenStore` (interfaz) con implementación cifrada; `reset()` para pruebas.
- Dispatchers y reloj inyectados (`DispatcherProvider`, `Clock`).
- Estados de la UI: cargando, vacío, error, sin red, con su componente base.
- Refresh: en `nv-01` solo se crea la interfaz `TokenRefresher` y se prueba el `Authenticator` con un doble controlado; el contrato real de refresh está en `specs/nv-05/spec.md` y su implementación es `[planificado nv-05]`.
- Room vacío: validar que Room exporte esquema con 0 entidades; si no lo hace, se usa una entidad técnica mínima o se difiere la exportación a `nv-02`, dejando la decisión en `Memoria.md`.
- HTTPS de debug: `networkSecurityConfig` de debug confía solo en la CA de desarrollo; release no confía en certificados de desarrollo.
- `LocalPropertiesExampleTest` verifica `local.properties.example` válido, sin secretos y sin llaves futuras.

## Contratos
- Consume del backend solo `GET /health` y `GET /ready` (spec `nv-01` del backend).
- Refresh real: contrato en `specs/nv-05/spec.md`; en `nv-01` no hay endpoint de refresh.
- Errores `{codigo, mensaje, campos[]}` → `AppError`.
- Rutas: ver `docs/routes.md`. Permisos: ver `docs/permissions-matrix.md`.

## Tests
- Config — `LocalPropertiesExampleTest` 3 (ejemplo válido, sin secretos, sin llaves futuras).
- Unit — interceptores (`RequestId`, `AuthHeader`, `RedactedLogging`) 6, `AppError` mapper 8, `Authenticator` 4.
- Data — Room (apertura y esquema) 3, ping con MockWebServer 4 (200, 503, timeout, sin red).
- Security — manifiesto (`allowBackup`, HTTP en claro, componentes exportados) 4, `TokenStore` cifrado 3.
- UI — tema y estados base 3, navegación `home` 2.
- Total: 40 pruebas.

## Criterios
- La app compila y abre `home` sin red.
- Ningún token aparece en logs; todo tráfico va por HTTPS y debug solo confía en la CA de desarrollo.
- El 401 provoca un único intento de refresh con el doble controlado; si falla, se limpia la sesión.
- `ktlintCheck detekt lintDebug` sin errores.
- `LocalPropertiesExampleTest` 3/3 y `scripts/check_sdd.sh` → SDD-OK.
- `compileSdk`/`targetSdk` fijados y registrados en `AGENTS.md`.

## Límites
Sin onboarding, planes, amigos, mapa ni reservas; sin refresh real (solo interfaz y doble), sin FCM real ni Maps real (dependencias y canal de notificación se incorporan en `nv-06+` `[planificado nv-06]`); sin sincronización en segundo plano real; sin analítica.

## Aprobación y Control de Cambios
- **Estado:** Borrador con evidencia (implementado; aprobación pendiente)
- **Revisores:** (pendiente de nombre)
- **Fecha:** 2026-11-02
- **Detalle:** `nv-01` implementado y verificado: 25 tests unitarios + 15 instrumentados, APK de depuración y `home` abierta en AVD. Evidencia en `specs/nv-01/checklists/gates.md` + `Memoria.md`; falta la firma del revisor.
