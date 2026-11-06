# NV-03 — Puerta de calidad incremental (advisory)

## Contexto
Medir primero, bloquear después: cobertura y mutación sobre lo que cambia en cada PR, sin romper el flujo mientras no haya datos. **Depende de**: nv-02.

## Requisitos
1. Cobertura global con Kover y `scripts/check_coverage.sh --min 45` (único bloqueo).
2. Cobertura del diff con `scripts/check_delta_coverage.py --min 80 --advisory` (informa, no bloquea).
3. Mutación enfocada con Pitest sobre lo cambiado, `continue-on-error`.
4. Reglas Semgrep propias y reporte de dependencias (informativo).

## Diseño
- Orden CI-PR de la constitución §8.
- Umbrales provisionales; se fijan con datos reales (principio empírico).
- Los reportes se suben siempre como artefactos.

## Contratos
N/A — sin contratos nuevos.

## Tests
- Verificación de scripts con casos de prueba (cobertura por debajo y por encima del umbral).
- YAML de CI validado con `yaml.safe_load`.

## Criterios
- Con 45 % o más el paso global pasa; por debajo falla.
- El delta y la mutación nunca rompen el PR.
- Cada PR deja reportes descargables.

## Límites
Sin umbral de mutación bloqueante, sin MobSF, sin Macrobenchmark, sin análisis de imágenes.

## Aprobación y Control de Cambios
- **Estado:** ✅ Aprobada (Jorge, 2026-11-06)
- **Revisores:** Jorge
- **Fecha:** 2026-11-06
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-03/checklists/gates.md` + `Memoria.md`.
