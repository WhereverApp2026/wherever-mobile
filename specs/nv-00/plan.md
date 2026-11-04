# plan.md — NV-00 Constitución base de Wherever Móvil

1. Mapeo
- Crear: `local.properties.example`, `.gitignore`.
- Consolidar: `specs/nv-00/*`, `AGENTS.md`, `Memoria.md` (ya creados como estructura SDD).
- Actualizar: `README.md` como índice SDD.
- No crear en esta fase: Gradle, módulo `app`, tests, `detekt`, `.editorconfig` ni scripts; pasan a `nv-01` `[planificado nv-01]`.

2. Guardarraíles
- `critic B1` secretos o `google-services.json` en Git → FAIL.
- `critic B2` tokens en logs → FAIL.
- Sin referencias a artefactos inexistentes sin la marca `[planificado nv-0X]`.
- Sin pantallas del producto.

3. Pruebas
- N/A en `nv-00`. `LocalPropertiesExampleTest` (3) se ejecuta en `nv-01` `[planificado nv-01]`.
- Verificación manual: `AGENTS.md` cita el stack, `Memoria.md` existe y cada NFR tiene su comando futuro.

4. Secuencia
1. Fijar el stack y la gobernanza en la spec.
2. Consolidar `AGENTS.md` y `Memoria.md`; crear `local.properties.example` y `.gitignore`.
3. Crear/actualizar el índice `README.md` con el estado real.
4. Verificar referencias y dejar los gates en borrador con evidencia manual.

5. Verificación
- `scripts/check_sdd.sh` `[planificado nv-01]` → SDD-OK.
- `scripts/critic-guardrails.sh` `[planificado nv-01]` → PASS exit 0.
- Revisión manual: cero rutas inexistentes sin marca `[planificado nv-0X]`.
