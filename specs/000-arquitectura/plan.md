# plan.md — 000 Arquitectura

1. Mapeo
- Crear: `specs/000-arquitectura/*`, proyecto Gradle (`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, wrapper, `gradle.properties`, `.gitignore`, `local.properties.example`), `AndroidManifest.xml`, tema, navegación y `home`.
- Modificar: `README.md`, `Memoria.md`.
- Las piezas de red, seguridad y datos se crean en `nv-01`; esta spec fija cómo son.

2. Guardarraíles
- Constitución §3-§6: stack, cadena de red, solo HTTPS, sin secretos.
- Sin pantallas del producto (etapa `nv-00…nv-03`).
- Excepciones de stack documentadas en `spec.md §5.1`.

3. Pruebas
- Ver `spec.md §6`. Llegan con `nv-01` y `nv-02`.

4. Secuencia
1. Spec de arquitectura.
2. Proyecto Gradle y `home`.
3. `assembleDebug`.
4. `nv-01`, luego `nv-02`, luego `specs/002+`.

5. Verificación
- `gradlew.bat assembleDebug → BUILD SUCCESSFUL`
