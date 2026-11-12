# NV-41 — Identificador de la app y build de release

## T1 — Implementación
- [x] Confirmado: `namespace` y `applicationId` = `com.wherever.mobile`; el manifiesto no declara `package`. Sin cambios.
- [x] `signingConfigs.release` por variables de entorno; sin ellas, APK sin firmar.
- [x] `*.jks` y `*.keystore` en `.gitignore`.
- [x] `proguard-rules.pro`: sin reglas propias (las traen las librerías) y con números de línea.

## T2 — Verificación
- [x] Gates: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (158), `koverVerifyDebug`, `assembleDebug` y scripts de puerta (2026-11-11, JDK 17).
- [x] `assembleRelease` sin variables: `app-release-unsigned.apk`.
- [x] `assembleRelease` con un keystore de prueba fuera del repo: `apksigner verify` correcto (keystore borrado después).
- [ ] APK de release instalado en el teléfono: login y perfil contra staging (R8 no rompe Retrofit ni la serialización). **Pendiente:** no hay dispositivo conectado; R8 avisa que no lee el metadata de Kotlin 2.4.21.

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../security/ReleaseConfigTest.kt`: id, namespace, manifiesto y versión
- [x] Integración: sin capa nueva; `AuthIntegrationTest` y `ConnectionIntegrationTest` siguen verdes
- [x] Seguridad: `test/.../security/ReleaseConfigTest.kt`: firma por entorno y keystores fuera del repo
- **No hacer**: versionar un keystore ni escribir contraseñas en Gradle o en CI.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
