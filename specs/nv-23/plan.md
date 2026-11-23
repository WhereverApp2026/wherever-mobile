# plan.md — NV-23 — Crear plan con IA

> Deriva de `specs/nv-23/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.
> Conciliado el 2026-11-22: lo hecho entró con `specs/nv-48`.

## 1. Mapeo
- Hecho (nv-48): `feature/plans/domain/Plans.kt` (`PlanDraft`, `PlanRequest`, `PlanAssistantRepository`), `feature/plans/data/{PlansApi,PlansRepositoryImpl}.kt`, `feature/plans/ui/{CreatePlanScreen,CreatePlanViewModel,PlanViews}.kt`.
- Ruta `CreatePlanRoute(phrase)` en `docs/routes.md`.
- Falta: `17_AjustaPlan` (pantalla nueva o hoja ampliada) y el 4.º paso en `CreatePlanScreen.kt`.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad (`nv-56`), cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables: la frase, la fecha y los límites viven en `CreatePlanViewModel`.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Pruebas

### Unitarias
- Hecho: `PlansViewModelsTest` (asistente).
- Falta: Cancelar con dispatcher de prueba, 4 pasos, `17_AjustaPlan`.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- Hecho: `PlansIntegrationTest` (generar, otra idea).
- Falta (nv-56): el borrador persiste en Room y sobrevive a un reinicio sin red.

### Seguridad (`docs/masvs-checklist.md`)
- Falta: ni la frase ni los gustos en logs.

### Otras
- UI: pasos de "Generando".
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. `nv-56` integrada (Room para planes).
2. `17_AjustaPlan` y su ViewModel.
3. 4.º paso de "Generando".
4. Pruebas que faltan.
5. Gates, teléfono con catálogo en staging y PR.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
