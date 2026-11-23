# plan.md — NV-26 — Vaca virtual y gastos

> Deriva de `specs/nv-26/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.
> Contrato conciliado el 2026-11-22 (backend nv-58).

## 1. Mapeo
- Sin código todavía. Propuesta: `feature/groups/{data,domain,ui}` junto a nv-25 (la Vaca cuelga del grupo), con `ExpensesApi`, `ExpenseEntity`/DAO y `SplitViewModel`. Se confirma con Jorge al tomar la spec.
- Rutas nuevas en `docs/routes.md` al implementar.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.
- Montos en centavos (`Long`), nunca `Float`.
- Toda edición de gasto manda `If-Match`; un 409 recarga.

## 3. Pruebas

### Unitarias
- El ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70).
- Validaciones en centavos y suma personalizada.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- Gastos y saldos en Room tras reiniciar sin red; reenvío con el mismo `id_cliente`.

### Seguridad (`docs/masvs-checklist.md`)
- Sin datos bancarios ni de tarjeta; el cuerpo no se registra en logs.

### Otras
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. `46_Grupo` (nv-25) en `main`.
2. Entidad Room + DAO + migración.
3. DTO + mapeos + `AppError`.
4. Repositorio con cola de envío por `id_cliente`.
5. ViewModel con `UiState`.
6. Pantallas y rutas.
7. Pruebas de los tres tipos.
8. Gates.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
