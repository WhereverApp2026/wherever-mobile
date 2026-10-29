# NV-00 — Constitución base de Wherever Móvil

## Contexto
Filosofía del proyecto aplicada por funcionalidad liberable, pero en esta etapa solo bases, sin ninguna pantalla del producto. **Depende de**: —.

## Requisitos
1. Fijar el stack: `Kotlin + Compose + Material 3 + Hilt + Room + Retrofit/OkHttp`, sin `Play Store, CD, Crashlytics, MobSF ni Macrobenchmark` en esta etapa.
2. Exigir `spec-first + DoD por funcionalidad + trazabilidad spec↔código↔test + 1 revisor + Memoria.md`.
3. Fijar NFR verificables por comando: `minSdk 26, targetSdk vigente, assembleDebug sin errores, cobertura >=45%`.
4. Registrar reglas no negociables: `offline primero, sin secretos en Git, sin TODO sin issue, inglés en código, textos en strings.xml, real-first sin repositorios simulados`.

## Diseño
- Principios: `offline primero, seguridad y privacidad, sin estado oculto, fail-fast, empírico antes que teórico, documentar límites`.
- Stack: ver tabla de `constitution.md §3`.
- Naming: inglés en código, español en textos de UI; paquetes `core/*` y `feature/<nombre>/{ui,domain,data}`.
- `AGENTS.md` cita este stack. `Memoria.md` inicial creada.

## Contratos
N/A — constitucional. Referencia normativa en `AGENTS.md`.

## Tests
N/A — verificación manual de estructura + `ConfigTest` (3): `local.properties.example válido + sin secretos + sin llaves futuras`.

## Criterios
- `AGENTS.md + Memoria.md` existen y citan el stack.
- Cada NFR tiene comando: `./gradlew assembleDebug → sin errores`, `scripts/check_coverage.sh --min 45 → >=45%`.
- `scripts/critic-guardrails.sh → PASS` en vacío.

## Límites
Sin `Google Play, MobSF, Macrobenchmark, gate de mutación, Crashlytics` — van como deuda documentada, no verde falso. Sin pantallas del producto.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador sin ejecutar
- **Revisores:** (pendiente de nombre)
- **Fecha:** —
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-00/checklists/gates.md` + `Memoria.md`.
