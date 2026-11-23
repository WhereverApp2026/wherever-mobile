# plan.md — NV-28 — Mis planes

> Deriva de `specs/nv-28/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.
> Conciliado el 2026-11-22: lo hecho entró con `specs/nv-48`.

## 1. Mapeo
- Hecho (nv-48): `feature/plans/ui/{MyPlansScreen,MyPlansViewModel}.kt`, `PlanSummary` y `PlansTab` en `feature/plans/domain/Plans.kt`, `PlansApi.list`.
- Ruta `MyPlansRoute` en `docs/routes.md`; pestaña Planes en la barra (`MainTab.PLANS`).
- Falta: lectura desde Room (`nv-56`) y la etiqueta de estado en `PlanRow`.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad (`nv-56`), cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Pruebas

### Unitarias
- Hecho: cambio de pestaña en `PlansViewModelsTest`.
- Falta: `MyPlansViewModel` muestra carga, vacío y error por pestaña.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- Hecho: lista por pestaña en `PlansIntegrationTest`.
- Falta (nv-56): lectura por pestaña desde Room.

### Seguridad (`docs/masvs-checklist.md`)
- Falta (nv-56): los planes se borran de Room al cerrar sesión.

### Otras
- UI: estado vacío.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. Decidir de dónde sale el estado de cada plan (Fase 0).
2. `nv-56`: Room, sin red y limpieza al cerrar sesión.
3. Etiqueta de estado.
4. Pruebas que faltan, gates y PR.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
