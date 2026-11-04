# NV-00 — Constitución base de Wherever Móvil

## T1 — Reglas y stack sin código
- **Consolidar**: `specs/nv-00/spec.md`, `plan.md`, `task.md`, `AGENTS.md`, `Memoria.md` y `README.md`.
- **Crear**: `local.properties.example` con `API_BASE_URL=https://example.invalid/api/v1/`, `.gitignore` (excluye `local.properties`, `google-services.json`, `*.jks`, `.idea/`).
- **Mover a nv-01**: proyecto Gradle, `libs.versions.toml`, `config/detekt/detekt.yml`, `.editorconfig`, `LocalPropertiesExampleTest.kt (3)` y scripts de calidad `[planificado nv-01]`.
- **Verificar**: revisión manual de estructura + cero referencias sin marca `[planificado nv-0X]`; `check_sdd.sh` y `critic-guardrails.sh` se ejecutan en `nv-01` `[planificado nv-01]`.
- **No hacer**: Gradle, pantallas del producto, FCM real, Maps, Crashlytics, publicación, tests o scripts en esta fase.
