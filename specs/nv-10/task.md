# NV-10 — Onboarding de gustos

## T1 — Implementación
- [x] Se siguen las historias, no la maqueta, donde difieren (D05): 15 chips con emoji y deslizador de presupuesto.
- [x] Guarda en orden: `PUT /usuarios/yo/gustos`, `PATCH /usuarios/yo/preferencias` (`movilidad_default`) y `PATCH /usuarios/yo` (`ciudad_id`). Si una falla, se muestra el error y se puede reintentar.
- [x] Máximo 10 categorías. Autocompletado de ciudad con espera de 300 ms y mínimo 2 letras.
- [x] Omitir guarda una marca local por usuario (D12).
- [x] También se abre desde Ajustes, con los datos actuales.

## T2 — Verificación
- [x] `assembleDebug`, `testDebugUnitTest`, `ktlintCheck` y `lintDebug` en verde
- [ ] Recorrido en emulador contra staging

## T3 — Pruebas (unitarias, integración y seguridad)
- [x] Unit: `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — `TastesViewModel`: mínimo 3 y máximo 10, presupuesto inicial, alergias ≤ 200, omitir, autocompletar
- [x] Unit: `test/.../feature/tastes/ui/TasteLabelTest.kt` — etiquetas del catálogo
- [x] Integración: `test/.../integration/ProfileIntegrationTest.kt` — `PUT /usuarios/yo/gustos`, preferencias 422 y autocompletar con respuestas de la doc
- [x] Seguridad: `test/.../security/SourceSecurityTest.kt` — SharedPreferences solo guarda la marca de omitir, sin tokens
- [x] Seguridad: `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — alergias limitadas a 200 caracteres
- **No hacer**: usar el servidor de staging en las pruebas.

## T4 — Aprobación
- [ ] Revisor: Jorge
