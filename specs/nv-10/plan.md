# plan.md — NV-10 — Onboarding de gustos

> Deriva de `specs/nv-10/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/tastes/ui/TastesScreen.kt`
- `feature/tastes/ui/TastesViewModel.kt`
- `feature/profile/ui/CitySearch.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — `TastesViewModel`: mínimo 3 y máximo 10, presupuesto inicial, alergias ≤ 200, omitir, autocompletar
- `test/.../feature/tastes/ui/TasteLabelTest.kt` — etiquetas del catálogo

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `test/.../integration/ProfileIntegrationTest.kt` — `PUT /usuarios/yo/gustos`, preferencias 422 y autocompletar con respuestas de la doc

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../security/SourceSecurityTest.kt` — SharedPreferences solo guarda la marca de omitir, sin tokens — MASVS STORAGE
- `test/.../feature/profile/ui/ProfileViewModelsTest.kt` — alergias limitadas a 200 caracteres — MASVS CODE

## 4. Secuencia
1. Datos y red.
2. ViewModel y pruebas.
3. Pantalla y navegación.

## 5. Verificación
- `./gradlew assembleDebug → BUILD SUCCESSFUL`
- `./gradlew ktlintCheck detekt lintDebug → sin errores` (detekt con JDK 17)
- `./gradlew testDebugUnitTest → verdes` (unitarias, integración con MockWebServer y seguridad)
- `./gradlew koverVerifyDebug → ≥ 45 %`
- `./gradlew connectedDebugAndroidTest → verdes` (Room real; requiere emulador o teléfono)
- Pendiente: recorrido manual en emulador.
