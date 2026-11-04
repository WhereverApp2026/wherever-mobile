# plan.md — NV-03 Puerta de calidad incremental (advisory)

1. Mapeo
- Crear: `scripts/check_coverage.sh`, `scripts/check_delta_coverage.py`, `.github/workflows/ci-pr.yml`, `.github/dependabot.yml`, `.semgrep/semgrep.yaml`, configuración Kover en Gradle y `pitest` en Gradle.
- Modificar: `AGENTS.md` si cambian umbrales o comandos.

2. Guardarraíles
- `continue-on-error` solo en delta y mutación.
- Tras mutación, siempre `assembleDebug`.
- El 45 % es provisional; no endurecer umbrales sin medición en `Memoria.md`.
- Pitest es la herramienta de mutación; si se cambia, excepción documentada.

3. Pruebas
- Casos de prueba de los scripts (bajo/sobre umbral).
- Validación YAML de CI y Dependabot con `yaml.safe_load`.
- Semgrep con caso positivo y negativo de B1-B4.

4. Secuencia
1. Medir la cobertura actual.
2. Configurar Kover y script global.
3. Script delta advisory y dependencias Python.
4. Pitest enfocado.
5. Workflow `ci-pr.yml`.
6. `.semgrep/semgrep.yaml` y `.github/dependabot.yml`.
7. Registrar mediciones en `Memoria.md`.

5. Verificación
- `scripts/check_coverage.sh --min 45 → OK`
- `python scripts/check_delta_coverage.py --min 80 --advisory → informe`
- `python -c "import yaml;yaml.safe_load(open('.github/workflows/ci-pr.yml'))" → sin error`
- `python -c "import yaml;yaml.safe_load(open('.github/dependabot.yml'))" → sin error`
- `semgrep scan --config=auto --config=.semgrep/semgrep.yaml --error → 0 nuevos`
