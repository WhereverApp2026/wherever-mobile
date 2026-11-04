# NV-01 — Foundation: proyecto, tema, navegación, red segura y datos locales

## T1 — Proyecto, tema y herramientas base
- **Crear**: Gradle, módulo `app`, Hilt, `gradle/libs.versions.toml`, `config/detekt/detekt.yml`, `.editorconfig`, `LocalPropertiesExampleTest.kt`, `core/designsystem` (Color, Type, Theme, componentes base).
- **Verificar**: `assembleDebug`, `LocalPropertiesExampleTest` 3/3, test UI del tema 3.
- **No hacer**: pantallas del producto.

## T2 — Navegación
- **Crear**: `navigation/*`, rutas `home` y `debug/conexion`; actualizar `docs/routes.md`.
- **Verificar**: test de navegación 2.

## T3 — Red
- **Crear**: Retrofit/OkHttp, interceptores, `Authenticator` con doble controlado, `TokenRefresher` (interfaz), `AppError`, mapper.
- **Verificar**: Unit 18.
- **No hacer**: refresh real; llega en `nv-05` `[planificado nv-05]`.

## T4 — Seguridad
- **Crear**: `TokenStore`, `network_security_config.xml` de debug/release, ajustes de manifiesto; actualizar `docs/permissions-matrix.md` y `docs/masvs-checklist.md`.
- **Verificar**: Security 7; confianza de desarrollo solo en debug.

## T5 — Datos locales y salud
- **Crear**: Room vacío con esquema exportado (o entidad técnica mínima), `HealthApi`, `debug/conexion`; validar exportación con 0 entidades o documentar la excepción.
- **Verificar**: Data 7.

## T6 — Gates
- **Crear**: `scripts/check_sdd.sh`, `scripts/critic-guardrails.sh`, `scripts/check_routes.sh`, `scripts/check_strings.sh`.
- **Verificar**: todos devuelven OK.
