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
- [ ] Revisor
