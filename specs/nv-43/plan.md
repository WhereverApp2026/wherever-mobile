# plan.md — NV-43 — Consentimiento de datos sensibles para alergias

> Deriva de `specs/nv-43/spec.md`. Respeta constitución §5 (textos en `strings.xml`, Room fuente de verdad, sin migración destructiva) y §7 (pruebas). Plantilla: `.specify/templates/plan-template.md`.

## 1. Mapeo
- `feature/profile/data/ProfileDtos.kt`: `datos_sensibles` en `PrivacyDto` y `PrivacyPatchDto`
- `feature/profile/domain/UserProfile.kt`: `Privacy.sensitiveData`, `PrivacyPatch.sensitiveData`
- `feature/profile/data/UserProfileEntity.kt` y `ProfileMappers.kt`: `PrivacyEntity.sensitiveData` (`defaultValue = "0"`)
- `core/database/WhereverDatabase.kt` (versión 2), `core/database/Migrations.kt` (`MIGRATION_1_2`), `core/di/AppModule.kt` (`addMigrations`)
- `app/schemas/com.wherever.mobile.core.database.WhereverDatabase/2.json`: esquema exportado
- `feature/tastes/ui/TastesViewModel.kt`: casilla, error y `PATCH` de privacidad antes de `PUT` de gustos
- `feature/tastes/ui/TastesScreen.kt`: `AllergiesConsent` bajo el campo de alergias
- `feature/settings/ui/PrivacyViewModel.kt` y `PrivacyScreen.kt`: interruptor "Datos sensibles" y confirmación al retirarlo
- `res/values/strings.xml`: `tastes_allergies_consent*`, `privacy_sensitive*`
- `app/build.gradle.kts`: `room-testing` y `app/schemas` como assets de `androidTest`

## 2. Guardarraíles
- Sin `fallbackToDestructiveMigration`: migración explícita y probada.
- La regla del consentimiento vive en el ViewModel, no en el Composable.
- Textos solo en `strings.xml`; alergias fuera de los logs.

## 3. Pruebas
### Unitarias
- `test/.../feature/tastes/ui/TastesViewModelTest.kt` (8)
- `test/.../feature/settings/ui/PrivacyViewModelTest.kt` (5)
- `test/.../feature/profile/ui/ProfileViewModelsTest.kt`: el guardado completo marca la casilla

### Integración
- `test/.../feature/profile/data/ProfileRepositoryImplTest.kt` (2): leer y retirar `datos_sensibles` por MockWebServer

### Migración
- `androidTest/.../core/database/MigrationTest.kt`

### Seguridad
- Sin superficie nueva: `SourceSecurityTest` y `PlatformSecurityTest` siguen verdes

## 4. Secuencia
1. Datos y migración de Room.
2. Gustos: casilla y orden de las rutas.
3. Privacidad: interruptor y confirmación.
4. Pruebas y compuertas una por una.

## 5. Verificación
- Con JDK 17, una por una: `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `koverVerifyDebug`, `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep
- `connectedDebugAndroidTest` (migración) corre en CI (emulador)
