# NV-00 — Constitución base de Wherever Móvil

## T1 — Reglas y stack sin pantallas
- **Crear**: `specs/nv-00/spec.md`, `plan.md`, `task.md`, `AGENTS.md`, `Memoria.md` inicial, `local.properties.example` con `API_BASE_URL=https://example.invalid/api/v1/`, `.gitignore` (excluye `local.properties`, `google-services.json`, `*.jks`).
- **Crear**: `LocalPropertiesExampleTest.kt (3)`, `config/detekt/detekt.yml` (complejidad ≤15), `.editorconfig` para ktlint.
- **Verificar**: `assembleDebug sin errores + 3/3 + critic PASS + cada NFR con su comando`.
- **No hacer**: pantallas del producto, FCM real, Maps, Crashlytics, publicación.
