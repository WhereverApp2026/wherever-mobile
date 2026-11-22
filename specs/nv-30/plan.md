# plan.md — NV-30 — El día del plan

> Deriva de `specs/nv-30/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.
> Contrato conciliado el 2026-11-22 (backend nv-62 y nv-68).

## 1. Mapeo
- Sin código todavía. Propuesta: `feature/dayof/{data,domain,ui}` (como sugiere `docs/figma-inventory.md`), con `CheckinsApi`, paquete sin conexión sobre `PlanStopEntity` (`nv-56`) y el lector QR que ya usa nv-20.
- Rutas nuevas en `docs/routes.md` al implementar.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.
- Ubicación solo en primer plano; nunca en disco ni en logs.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Pruebas

### Unitarias
- Pasos de la ruta; check-in 201, 409 y 422.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- El check-in queda en Room y se lee sin red; paquete sin conexión con `ETag`.

### Seguridad (`docs/masvs-checklist.md`)
- Sin histórico de ubicación en disco ni en logs.

### Otras
- UI: estados sin permiso.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. `nv-56` integrada y D25 decidida con Jorge.
2. `26_CheckinOk` con `POST /checkins` (QR o ubicación).
3. Paquete sin conexión.
4. `13_Mapa`, `14_MapaFull`, `15_Llegar`, `18_Navegando` según D25.
5. Pruebas, gates y PR.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
