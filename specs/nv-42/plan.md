# plan.md — NV-42 — Fecha de nacimiento sin fechas futuras

> Deriva de `specs/nv-42/spec.md`. Respeta constitución §5 (textos en `strings.xml`, relojes inyectados) y §7 (pruebas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/auth/domain/RegistrationValidator.kt`: `BirthDateIssue`, `validateBirthDate(fecha, reloj)`, `oldestBirthDate`; `isAdult` pasa a ser privada
- `feature/auth/ui/BirthDateBounds.kt`: límites del calendario en milisegundos UTC (`isSelectable`, `yearRange`, mes inicial)
- `feature/auth/ui/RegisterScreen.kt`: `BirthDatePicker` con `SelectableDates` y `initialDisplayedMonthMillis`
- `feature/auth/ui/RegisterViewModel.kt`: `today` en el estado, mensajes por caso; 422 en `fecha_nacimiento` sin cambios
- `res/values/strings.xml`: `validation_birth_date_future`, `validation_birth_date_too_old`
- `app/build.gradle.kts`: Compose Testing (`ui-test-junit4` y `ui-test-manifest`, ya en el catálogo) para `androidTest`

## 2. Guardarraíles
- "Hoy" solo del `Clock` inyectado; sin `LocalDate.now()` sin reloj.
- La lógica de fechas fuera de los Composables (`BirthDateBounds`, dominio).
- Textos solo en `strings.xml`.

## 3. Pruebas
### Unitarias
- `test/.../feature/auth/domain/BirthDateValidatorTest.kt`
- `test/.../feature/auth/ui/BirthDateBoundsTest.kt`
- `test/.../feature/auth/ui/AuthViewModelsTest.kt` (3 nuevas)

### Integración
- `test/.../integration/AuthIntegrationTest.kt`: 422 en `fecha_nacimiento`

### Seguridad
- Sin superficie nueva: `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes

### UI
- `androidTest/.../feature/auth/ui/BirthDatePickerTest.kt`

## 4. Secuencia
1. Validador y pruebas unitarias.
2. Límites del calendario y `SelectableDates`.
3. Prueba de integración del 422 y prueba de UI.
4. Compuertas una por una.

## 5. Verificación
- Con JDK 17, una por una con `--no-daemon --max-workers=1`: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, `assembleRelease`
- `assembleDebugAndroidTest` compila la prueba de UI; `connectedDebugAndroidTest` corre en CI (emulador)
