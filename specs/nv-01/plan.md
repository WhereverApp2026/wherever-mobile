# plan.md — NV-01 Foundation: proyecto, tema, navegación, red segura y datos locales

1. Mapeo
- Crear: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `core/*`, `navigation/*`, `app/schemas/`, `res/xml/network_security_config.xml`, `docs/*`.
- Modificar: `libs.versions.toml`.

2. Guardarraíles
- `critic B3`: `cleartextTrafficPermitted=false`, orden de interceptores fijo.
- `allowBackup=false` o reglas explícitas.
- Sin `fallbackToDestructiveMigration`.
- Sin textos fijos en Composables.

3. Pruebas
- Los listados de §Tests, en `app/src/test` y `app/src/androidTest`.
- Arquitectura (Konsist) llega en `nv-02`.

4. Secuencia
1. Proyecto Gradle + Hilt + catálogo.
2. Tema y componentes base.
3. Navegación y `docs/routes.md`.
4. Cadena de red + `AppError` + `Authenticator`.
5. `TokenStore` cifrado + `networkSecurityConfig`.
6. Room + esquema exportado + ping de salud.
7. Pruebas y gates.

5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores`
- `./gradlew testDebugUnitTest → verdes`
- `./gradlew connectedDebugAndroidTest → verdes (con emulador)`
