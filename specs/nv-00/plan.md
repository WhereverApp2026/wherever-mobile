# plan.md — NV-00 Constitución base de Wherever Móvil

1. Mapeo
- Crear: `specs/nv-00/*`, `AGENTS.md`, `Memoria.md` inicial, `local.properties.example`, `.gitignore`, `gradle/libs.versions.toml` (solo versiones), `config/detekt/detekt.yml`, `.editorconfig`.
- Modificar: ninguno, es la primera spec.

2. Guardarraíles
- `critic B1` secretos o `google-services.json` en Git → FAIL.
- `critic B2` tokens en logs → FAIL.
- `detekt` y `ktlint` como error desde el día 1.

3. Pruebas
- `app/src/test/.../config/LocalPropertiesExampleTest.kt (3)`: ejemplo válido + sin secretos + sin llaves futuras.
- Manual: `AGENTS.md` cita el stack, `Memoria.md` existe y cada NFR tiene su comando.

4. Secuencia
1. Fijar el stack en la spec.
2. Redactar gobernanza SDD + DoD + trazabilidad + 1 revisor.
3. Fijar NFR con comando.
4. Registrar reglas no negociables.
5. Crear `AGENTS.md + Memoria.md + local.properties.example` y verificar gates en vacío.

5. Verificación
- `./gradlew testDebugUnitTest --tests '*LocalPropertiesExampleTest' → 3/3`
- `scripts/critic-guardrails.sh → PASS exit 0`
