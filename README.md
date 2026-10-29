# Wherever Móvil

App Android en Kotlin + Jetpack Compose para Wherever, desarrollada con **Spec Driven Development**. Este repositorio está en la etapa de **arquitectura base** (`specs/nv-00…nv-03`): no incluye pantallas del producto.

## Estructura SDD
- `.specify/memory/constitution.md`: constitución (canónica). `constitution.md` en raíz es su espejo.
- `.specify/templates/`: plantillas de spec, plan, tasks y checklist.
- `specs/nv-00…nv-03/`: `spec.md`, `plan.md`, `task.md` y `checklists/gates.md` de cada spec.
- `AGENTS.md`: cómo se trabaja. `Memoria.md`: estado, decisiones y lecciones. `CHECKLIST_PR.md`: gates de PR.
- `.claude/agents/` (5 sub-agentes) y `.claude/skills/` (skills de apoyo).
- `docs/`: `routes.md`, `permissions-matrix.md`, `masvs-checklist.md` (vivos).

## Mapa de specs
| Spec | Qué fija |
|---|---|
| `nv-00` | Constitución, stack y gobernanza |
| `nv-01` | Foundation: proyecto, tema, navegación, red segura, Room |
| `nv-02` | Molde Repository → ViewModel → UiState sin pantallas |
| `nv-03` | Puerta de calidad incremental (advisory) |

Estado: tareas **sin ejecutar**. La funcionalidad específica tendrá sus propias specs (`nv-04+`).
