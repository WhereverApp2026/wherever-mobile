# NV-01 — Foundation: proyecto, tema, navegación, red segura y datos locales

## T1 — Proyecto y tema
- **Crear**: Gradle, módulo `app`, Hilt, `core/designsystem` (Color, Type, Theme, componentes base).
- **Verificar**: `assembleDebug`, test UI del tema 3.
- **No hacer**: pantallas del producto.

## T2 — Navegación
- **Crear**: `navigation/*`, rutas `home` y `debug/conexion`, `docs/routes.md`.
- **Verificar**: test de navegación 2.

## T3 — Red
- **Crear**: Retrofit/OkHttp, interceptores, `Authenticator`, `AppError`, mapper.
- **Verificar**: Unit 18.

## T4 — Seguridad
- **Crear**: `TokenStore`, `network_security_config.xml`, ajustes de manifiesto, `docs/permissions-matrix.md`, `docs/masvs-checklist.md`.
- **Verificar**: Security 7.

## T5 — Datos locales y salud
- **Crear**: Room vacío con esquema exportado, `HealthApi`, `debug/conexion`.
- **Verificar**: Data 7.

## T6 — Gates
- **Crear**: `scripts/critic-guardrails.sh`, `check_routes.sh`, `check_strings.sh`.
- **Verificar**: todos devuelven OK.
