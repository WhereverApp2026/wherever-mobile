# plan.md — NV-24 — Plan: detalle y edición

> Deriva de `specs/nv-24/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.
> Conciliado el 2026-11-22: lo hecho entró con `specs/nv-48`.

## 1. Mapeo
- Hecho (nv-48): `feature/plans/domain/Plans.kt` (`Plan`, `PlanStop`, `Alternative`, `PlansRepository`), `feature/plans/data/{PlansApi,PlansRepositoryImpl}.kt`, `feature/plans/ui/{PlanDetailScreen,PlanDetailViewModel,PlanViews}.kt`.
- Ruta `PlanRoute(id)` en `docs/routes.md`.
- Falta: `CrearPlan` (pantalla, ViewModel y ruta nuevas en `feature/plans/ui`) y `41_AgregarParada` con pestañas (hoy hoja de búsqueda en `PlanDetailScreen.kt`).

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad (`nv-56`), cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.
- Toda edición manda `If-Match`; un 409 recarga en vez de reintentar (nv-48).
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Pruebas

### Unitarias
- Hecho: `PlansViewModelsTest` (edición de paradas).
- Falta: límites de `CrearPlan` y pestañas de `41_AgregarParada`.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- Hecho: `PlansIntegrationTest` (`If-Match`, 409, nombres de parada).
- Falta (nv-56): plan en Room y lectura sin red.

### Seguridad (`docs/masvs-checklist.md`)
- Falta: 403/404 al editar un plan ajeno no cambia nada en local.

### Otras
- UI: acciones de parada.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. `nv-56` integrada (Room para planes).
2. `CrearPlan` con `POST /planes`.
3. `41_AgregarParada` con Sugeridas y Buscar.
4. Pruebas que faltan.
5. Gates, teléfono con catálogo en staging y PR.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
