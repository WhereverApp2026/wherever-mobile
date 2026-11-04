# NV-03 — Puerta de calidad incremental (advisory)

## T1 — Scripts, CI y configuración
- **Crear**: `scripts/check_coverage.sh`, `scripts/check_delta_coverage.py`, `.github/workflows/ci-pr.yml`, `.github/dependabot.yml`, `.semgrep/semgrep.yaml`, Kover en Gradle, Pitest enfocado.
- **No hacer**: umbrales bloqueantes distintos de 45 %; `continue-on-error` fuera de delta/mutación.

## T2 — Implementar y medir
- **Verificar**: `check_coverage.sh --min 45`, delta advisory, YAML de CI/Dependabot, Semgrep B1-B4.
- **Registrar**: mediciones reales en `Memoria.md` antes de endurecer umbrales.
