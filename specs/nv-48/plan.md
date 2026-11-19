# plan.md — NV-48 — Plan con IA e itinerario editable

> Deriva de `specs/nv-48/spec.md`. Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/plans/domain/Plans.kt`; `feature/plans/data/{PlansApi,PlansRepositoryImpl}.kt` y `data/di/PlansModule.kt`
- `feature/plans/ui/`: `CreatePlan*`, `PlanDetail*`, `MyPlans*`, `PlanViews.kt` (línea de tiempo, costos y etiquetas)
- `feature/home/ui/{HomeScreen,HomeViewModel}.kt`: caja de Inicio al asistente y planes populares
- `core/designsystem/Surfaces.kt`: `MainTab.PLANS`; `core/ui/Dates.kt`: `formatTime`, `shiftIso`
- `navigation/{Routes,WhereverNavHost}.kt`, `docs/routes.md`, `strings.xml`, `app/build.gradle.kts` (versión 4)

## 2. Guardarraíles
- La lógica de frase, horas, orden y deshacer vive en los ViewModels.
- Toda edición manda `If-Match`; un 409 recarga en vez de reintentar.
- El ánimo solo con consentimiento.

## 3. Pruebas
### Unitarias
- `PlansViewModelsTest` (17), `HomeViewModelTest` (+1)

### Integración
- `PlansIntegrationTest` (7)

### UI
- Revisión en el teléfono

### Seguridad
- `ArchitectureTest`

## 4. Secuencia
1. Contratos y repositorios con sus pruebas de datos.
2. Asistente, resultado y guardado.
3. Detalle editable y Mis planes; populares en Inicio.
4. Compuertas, teléfono y PR.

## 5. Riesgos
- Staging sin negocios: generar da 422 y no hay planes reales que editar. Las ediciones se probaron con MockWebServer.
