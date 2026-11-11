# plan.md — NV-41 — Identificador de la app y build de release

> Deriva de `specs/nv-41/spec.md`. Respeta constitución §6 (sin secretos) y §8 (release manual). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `app/build.gradle.kts`: `signingConfigs.release` desde variables de entorno; `release` la usa solo si están todas
- `app/proguard-rules.pro`: por qué no hay reglas propias; `-keepattributes SourceFile,LineNumberTable`
- `.gitignore`: ya ignora `*.jks` y `*.keystore` (sin cambios)

## 2. Guardarraíles
- `applicationId` y `namespace` no cambian (`com.wherever.mobile`).
- Sin keystore, contraseñas ni alias en el repo, los logs o CI.

## 3. Pruebas
### Unitarias
- `test/.../security/ReleaseConfigTest.kt`: id, namespace, sin `package` en el manifiesto, `versionCode`/`versionName`

### Integración
- Sin capa nueva: `AuthIntegrationTest` y `ConnectionIntegrationTest` cubren los DTO; la build de release minificada se prueba a mano contra staging.

### Seguridad
- `test/.../security/ReleaseConfigTest.kt`: firma por entorno, sin contraseñas literales, keystores ignorados y ninguno versionado, release minificado

## 4. Secuencia
1. Leer el estado (id, namespace, manifiesto, versión, firma, R8).
2. Firma por entorno, `.gitignore` y reglas de R8.
3. `assembleRelease` firmado con un keystore de prueba fuera del repo e instalación en el teléfono.

## 5. Verificación
- `./gradlew ktlintCheck detekt lintDebug testDebugUnitTest koverVerifyDebug assembleDebug` (JDK 17)
- `./gradlew assembleRelease` sin variables → APK sin firmar; con variables → APK firmado (`apksigner verify`)
