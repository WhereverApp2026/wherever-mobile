# plan.md — NV-17 — Inicio y feed

> Deriva de `specs/nv-17/spec.md`. Respeta constitución §4 (cadena de red y capas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/home/ui/HomeScreen.kt`
- `feature/home/ui/HomeViewModel.kt`

## 2. Guardarraíles
- Constitución §2-§6: Room como fuente de verdad, cadena de red, textos en `strings.xml`, sin secretos.
- Sin lógica de negocio en Composables.

## 3. Pruebas
### Unitarias
- `test/.../feature/home/ui/HomeViewModelTest.kt` — Inicio provisional lee el perfil de Room

### Integración (MockWebServer con respuestas de `docs/contexto_api_sprint1.md`; Room real instrumentada; sin staging)
- `androidTest/.../integration/ProfileRepositoryRoomTest.kt` — el perfil de la API queda en Room y la UI lee de Room (instrumentada)

### Seguridad (`docs/masvs-checklist.md`)
- `test/.../security/SourceSecurityTest.kt` — sin logs ni datos personales fuera del interceptor redactado — MASVS PRIVACY

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
