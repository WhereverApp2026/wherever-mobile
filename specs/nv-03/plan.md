# plan.md — NV-03 Puerta de calidad incremental (advisory)

1. Mapeo
- Crear: `scripts/check_coverage.sh`, `scripts/check_delta_coverage.py`, `.github/workflows/ci-pr.yml`, `.semgrep/semgrep.yaml`, `pitest` en Gradle.

2. Guardarraíles
- `continue-on-error` solo en delta y mutación.
- Tras mutación, siempre `assembleDebug`.

3. Pruebas
- Casos de prueba de los scripts y validación del YAML.

4. Secuencia
1. Medir la cobertura actual.
2. Script global.
3. Script delta advisory.
4. Pitest enfocado.
5. Workflow `ci-pr.yml`.
6. Registrar mediciones en `Memoria.md`.

5. Verificación
- `scripts/check_coverage.sh --min 45 → OK`
- `python scripts/check_delta_coverage.py --min 80 --advisory → informe`
- `python -c "import yaml;yaml.safe_load(open('.github/workflows/ci-pr.yml'))" → sin error`
