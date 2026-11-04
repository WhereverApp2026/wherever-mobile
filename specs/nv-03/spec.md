# NV-03 — Puerta de calidad incremental (advisory)

## Contexto
Medir primero, bloquear después: cobertura y mutación sobre lo que cambia en cada PR, sin romper el flujo mientras no haya datos. **Depende de**: nv-02.

## Requisitos
1. R1 — Cobertura global con Kover y `scripts/check_coverage.sh --min 45` (único bloqueo, umbral provisional hasta la primera medición).
2. R2 — Cobertura del diff con `scripts/check_delta_coverage.py --min 80 --advisory` (informa, no bloquea).
3. R3 — Mutación enfocada con Pitest sobre lo cambiado, `continue-on-error`; cualquier alternativa exige excepción documentada.
4. R4 — Reglas Semgrep propias (mapeadas a B1-B4) y reporte de dependencias (informativo).
5. R5 — Dependabot activo mediante `.github/dependabot.yml`.

## Diseño
- Orden CI-PR de la constitución §8.
- Configuración Kover en Gradle y dependencias Python del script delta.
- `.semgrep/semgrep.yaml` con reglas B1-B4; `.github/dependabot.yml`.
- Umbrales provisionales; se fijan con datos reales (principio empírico) y se registran en `Memoria.md`.
- Los reportes se suben siempre como artefactos.

## Contratos
N/A — sin contratos nuevos.

## Tests
- Verificación de scripts con casos de prueba (cobertura por debajo y por encima del umbral).
- YAML de CI y de Dependabot validados con `yaml.safe_load`.
- Reglas Semgrep probadas contra un caso positivo y uno negativo de B1-B4.

## Criterios
- Con 45 % o más el paso global pasa; por debajo falla. El 45 % es provisional hasta la primera medición.
- El delta y la mutación nunca rompen el PR.
- Cada PR deja reportes descargables.
- Dependabot y Semgrep están configurados y validados.

## Límites
Sin umbral de mutación bloqueante, sin MobSF, sin Macrobenchmark, sin análisis de imágenes.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador sin ejecutar
- **Revisores:** (pendiente de nombre)
- **Fecha:** —
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-03/checklists/gates.md` + `Memoria.md`.
