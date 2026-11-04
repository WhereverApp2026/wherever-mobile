# plan.md — NV-01 Foundation: proyecto, tema, navegación, red segura y datos locales

1. Mapeo
- Crear: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `gradle/libs.versions.toml`, `core/*`, `navigation/*`, `app/schemas/`, `res/xml/network_security_config.xml`, `config/detekt/detekt.yml`, `.editorconfig`, `LocalPropertiesExampleTest.kt` y `scripts/{check_sdd,critic-guardrails,check_routes,check_strings}.sh`.
- Actualizar (no crear): `docs/routes.md`, `docs/permissions-matrix.md`, `docs/masvs-checklist.md`.
- Modificar: `AGENTS.md` con `compileSdk`/`targetSdk` reales.

2. Guardarraíles
- `critic B3`: `cleartextTrafficPermitted=false`, orden de interceptores fijo.
- `allowBackup=false` o reglas explícitas.
- Sin `fallbackToDestructiveMigration`.
- Sin textos fijos en Composables.
- Confianza HTTPS de desarrollo solo en debug.

3. Pruebas
- Config 3 (`LocalPropertiesExampleTest`).
- Los listados de §Tests, en `app/src/test` y `app/src/androidTest`.
- Arquitectura (Konsist) llega en `nv-02`.

4. Secuencia
1. Proyecto Gradle + Hilt + catálogo + detekt/ktlint/editorconfig + `LocalPropertiesExampleTest`.
2. Tema y componentes base.
3. Navegación y actualización de `docs/routes.md`.
4. Cadena de red + `AppError` + `Authenticator` con doble controlado.
5. `TokenStore` cifrado + `networkSecurityConfig` (debug/release) + `allowBackup`.
6. Room + esquema exportado + ping de salud.
7. Scripts de gobernanza y gates.

5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores`
- `./gradlew testDebugUnitTest --tests '*LocalPropertiesExampleTest' → 3/3`
- `./gradlew testDebugUnitTest → verdes`
- `./gradlew connectedDebugAndroidTest → verdes (con emulador)`
- `scripts/check_sdd.sh → SDD-OK`, `scripts/critic-guardrails.sh → PASS`
