# Wherever Móvil

App Android en Kotlin + Jetpack Compose para Wherever, desarrollada con **Spec-Driven Development (SDD)**.

## Estado actual
**Foundation, auth y onboarding implementados.** `nv-01` aporta Gradle, tema, navegación, red segura, Room, scripts y APK; `nv-05` implementa 9 pantallas auth y los 4 pasos de onboarding contra el backend real. `assembleDebug`, `ktlintCheck`, `detekt`, `lintDebug`, 75 tests JVM y 17 instrumentados pasan. CI y cobertura llegan en `nv-03`.

## Estructura SDD
- `.specify/memory/constitution.md`: constitución canónica (v1.1.0). `constitution.md` en raíz es su espejo.
- `.specify/templates/`: plantillas de spec (`spec-template.md`), plan (`plan-template.md`), tareas de fase (`phase-task-template.md`), tareas de funcionalidad (`tasks-template.md`), gate de fase (`phase-gate-template.md`) y checklist de PR (`checklist-template.md`).
- `specs/nv-00…nv-05/`: `spec.md`, `plan.md`, `task.md` y `checklists/gates.md` de cada fase o slice.
- `specs/_template.md`: alias placeholder conservado por compatibilidad.
- `scripts/`: gates `check_sdd.sh`, `critic-guardrails.sh`, `check_routes.sh` y `check_strings.sh`; cobertura y delta `[planificado nv-03]`. `.specify/scripts/README.md` conserva el placeholder de los scripts de cobertura.
- `AGENTS.md`: cómo se trabaja. `Memoria.md`: estado, decisiones y lecciones. `CHECKLIST_PR.md`: gates de PR.
- `.claude/agents/` (5 sub-agentes) y `.claude/skills/` (26 skills indexadas por fase).
- `app/`: proyecto Android con Compose, Hilt, Room, Retrofit/OkHttp y tema Wherever; `app/schemas/` y `app/src/{test,androidTest}`.
- `docs/`: `architecture.md`, `figma-inventory.md`, `routes.md`, `permissions-matrix.md`, `masvs-checklist.md` (vivos); `feature-mold.md` `[planificado nv-02]`.

## Mapa de specs
| Spec | Qué fija | Estado |
|---|---|---|
| `nv-00` | Constitución, stack y gobernanza | Borrador sin ejecutar |
| `nv-01` | Foundation: proyecto, tema, navegación, red segura, Room y herramientas base | Implementado y verificado |
| `nv-02` | Molde Repository → ViewModel → UiState sin pantallas | Borrador sin ejecutar |
| `nv-03` | Puerta de calidad incremental (advisory) | Borrador sin ejecutar |
| `nv-04` | Arquitectura de la aplicación móvil: módulos, MVVM, navegación, datos, contratos, auth y calidad | Borrador con evidencia (documental) |
| `nv-05` | Acceso, registro y onboarding: auth y onboarding | Implementado y verificado |

`nv-05` es la primera spec de producto; las funcionalidades y pantallas restantes tendrán sus propias specs (`nv-06+`).

## Reglas de alineación
- Un solo formato de spec: `Contexto → Requisitos → Diseño → Contratos → Tests → Criterios → Límites → Aprobación`.
- Toda ruta citada debe existir o llevar la marca `[planificado nv-0X]`.
- Cada requisito `R#` se refleja en plan, tarea y gate.
- Cada fase tiene un dueño explícito en `AGENTS.md` §11.
- Los documentos vivos están solo en `docs/`; las specs enlazan, no duplican.
