# Wherever Móvil

App Android en Kotlin + Jetpack Compose para Wherever, desarrollada con **Spec Driven Development**. Las pantallas de cuenta y acceso del Sprint 1 ya consumen la API; el resto está especificado (`specs/002…034`), sin implementar.

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
| `000-arquitectura` | Capas, red, tokens, DI, paquetes y convenciones |
| `001-flujo-general` | Mapa de navegación y flujos de usuario y negocio |
| `002…034` | Una spec por pantalla o funcionalidad, con sus endpoints |

Índice, tabla de pantallas y endpoints, y dudas abiertas: [`specs/README.md`](specs/README.md).

Estado: pantallas del Sprint 1 en `specs/README.md`. Las suites de calidad de `nv-02` y `nv-03` siguen **sin ejecutar**. Staging se apaga de 22:00 a 08:00 y en fines de semana.

## Compilar
Requisitos: Android Studio con Android SDK (API 37; Gradle descarga las Build-Tools que falten si las licencias están aceptadas), y `JAVA_HOME` apuntando a un JDK, por ejemplo el de Android Studio (`<Android Studio>/jbr`).

```
copy local.properties.example local.properties   # ajusta sdk.dir; API_BASE_URL es opcional en debug
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
```

En debug, sin `API_BASE_URL`, la app usa staging (`gradle.properties`). `google-services.json` no se sube a Git. `detekt` requiere JDK 17 a 22 (ver `Memoria.md`).
