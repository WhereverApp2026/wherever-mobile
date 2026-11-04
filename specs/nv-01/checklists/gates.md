# Checklist — nv-01 Foundation: proyecto, tema, navegación, red segura y datos locales

> Trazabilidad: `spec R1-R7 → plan §3-§5 → task T1-T6`. Estado: implementado y verificado; aprobación pendiente.

- [x] Proyecto compila y abre `home` (R1, R3)
- [x] Tema y componentes base (R2)
- [x] Cadena de red en orden y `Authenticator` de un reintento (R4)
- [x] `AppError` cubre los códigos de la API (R4)
- [x] Refresh real diferido a `nv-05`; `Authenticator` probado con doble controlado (R4)
- [x] Tokens cifrados y solo HTTPS; debug solo confía en CA de desarrollo (R5)
- [x] Room configurado; esquema exportado o decisión documentada, sin migración destructiva (R6)
- [x] Ping `/health` y `/ready` con 4 casos (R6)
- [x] `LocalPropertiesExampleTest` 3/3 (R1)
- [x] `check_sdd.sh`, `critic-guardrails.sh`, `check_routes.sh` y `check_strings.sh` OK (R7)
- [x] `compileSdk`/`targetSdk` fijados y registrados en `AGENTS.md` (R7)
- [x] `routes.md`, `permissions-matrix.md`, `masvs-checklist.md` actualizados
- [x] ktlint, detekt y Lint sin errores
- [x] Todas las suites de la spec verdes (25 unitarias + 15 instrumentadas = 40/40)
- [ ] Aprobación firmada: (pendiente de revisor)

Evidencia (2026-11-02):
- `.\gradlew.bat assembleDebug` → BUILD SUCCESSFUL.
- `.\gradlew.bat testDebugUnitTest` → 25/25.
- `.\gradlew.bat connectedDebugAndroidTest` → 15/15 en AVD `Wherever_API_35`.
- `.\gradlew.bat ktlintCheck detekt lintDebug` → BUILD SUCCESSFUL.
- `bash scripts/check_sdd.sh` → `SDD-OK`; `bash scripts/critic-guardrails.sh` → `PASS`; `bash scripts/check_routes.sh` → `OK`; `bash scripts/check_strings.sh` → `OK`.
- Apertura manual: `com.wherever.mobile/.MainActivity` en `home`, sin red.
