# plan.md — NV-26 — Vaca virtual y gastos

> Deriva de `specs/nv-26/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- Sin código todavía. Paquete `feature/<nombre>/{ui,domain,data}` según `specs/nv-04`; los archivos se fijan al tomar la spec.
- Ruta en `docs/routes.md` al implementar.

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.
- Lo marcado "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Pruebas
Previstas, sin implementar (de `spec.md §6`):

### Unitarias
- Unit: el ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70).
- Unit: redondeo sin centavos perdidos.

### Integración (MockWebServer con el contrato publicado; Room real instrumentada; sin staging)
- Data: gastos y saldos se guardan en Room y se recalculan tras reiniciar sin red.

### Seguridad (`docs/masvs-checklist.md`)
- Security: no hay datos bancarios ni de tarjeta en los gastos y su cuerpo no se registra en logs.

### Otras
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 4. Secuencia
1. Spec aprobada.
2. Entidad Room + DAO + migración.
3. DTO + mapeos + `AppError`.
4. Repositorio.
5. ViewModel con `UiState`.
6. Pantalla y ruta.
7. Pruebas de los tres tipos.
8. Gates.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
