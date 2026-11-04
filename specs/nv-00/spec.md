# NV-00 — Constitución base de Wherever Móvil

## Contexto
Filosofía del proyecto aplicada por funcionalidad liberable, pero en esta etapa solo bases, sin ninguna pantalla del producto. Esta fase es exclusivamente de gobernanza y documentación: no crea Gradle, código, tests ni scripts. **Depende de**: —.

## Requisitos
1. R1 — Fijar el stack: `Kotlin + Compose + Material 3 + Hilt + Room + Retrofit/OkHttp`, sin `Play Store, CD, Crashlytics, MobSF ni Macrobenchmark` en esta etapa.
2. R2 — Exigir `spec-first + DoD por funcionalidad + trazabilidad spec↔código↔test + 1 revisor + Memoria.md`.
3. R3 — Fijar NFR verificables por comando: `minSdk 26, compileSdk/targetSdk fijados en nv-01, assembleDebug sin errores, cobertura >=45%`. Los comandos se ejecutan cuando exista el proyecto (`nv-01`) `[planificado nv-01]`.
4. R4 — Registrar reglas no negociables: `offline primero, sin secretos en Git, sin TODO sin issue, inglés en código, textos en strings.xml, real-first sin repositorios simulados`.

## Diseño
- Principios: `offline primero, seguridad y privacidad, sin estado oculto, fail-fast, empírico antes que teórico, documentar límites`.
- Stack: ver tabla de `constitution.md §3` (v1.1.0).
- Naming: inglés en código, español en textos de UI; paquetes `core/*` y `feature/<nombre>/{ui,domain,data}`. Los campos del contrato JSON pueden conservar el nombre del backend.
- `AGENTS.md` cita este stack. `Memoria.md` inicial creada.
- Fases: `nv-00` gobernanza; `nv-01` foundation y herramientas de calidad; `nv-02` molde; `nv-03` puerta de calidad.

## Contratos
N/A — constitucional. Referencia normativa en `AGENTS.md`.

## Tests
N/A — verificación manual de estructura. `LocalPropertiesExampleTest` (3) se ejecuta en `nv-01`, cuando exista el proyecto Gradle `[planificado nv-01]`.

## Criterios
- `AGENTS.md + Memoria.md` existen y citan el stack.
- Cada NFR tiene comando para `nv-01` `[planificado nv-01]`: `./gradlew assembleDebug → sin errores`, `scripts/check_coverage.sh --min 45 → >=45%` `[planificado nv-01/nv-03]`.
- `scripts/check_sdd.sh` `[planificado nv-01]` valida plantilla, secciones, aprobación y referencias.
- Toda referencia a un artefacto futuro está marcada `[planificado nv-0X]`.
- `scripts/critic-guardrails.sh → PASS` en vacío `[planificado nv-01]`.

## Límites
Sin `Google Play, MobSF, Macrobenchmark, gate de mutación, Crashlytics` — van como deuda documentada, no verde falso. Sin pantallas del producto. Sin Gradle, código, tests ni scripts en esta fase; se crean en `nv-01`.

## Aprobación y Control de Cambios
- **Estado:** 🚧 Borrador sin ejecutar
- **Revisores:** (pendiente de nombre)
- **Fecha:** —
- **Detalle:** especificada; tareas aún sin ejecutar. La evidencia irá en `specs/nv-00/checklists/gates.md` + `Memoria.md`.
