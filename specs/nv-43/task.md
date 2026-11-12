# NV-43 — Consentimiento de datos sensibles para alergias

## T1 — Implementación
- [x] `datos_sensibles` en DTOs, dominio, entidad y mapeos.
- [x] Room versión 2 con `MIGRATION_1_2` y esquema `2.json` exportado.
- [x] Gustos: casilla de consentimiento expreso; sin marcarla no se envía nada.
- [x] Gustos: `PATCH privacidad {datos_sensibles: true}` antes de `PUT gustos`, solo si hay alergias y el perfil no tenía el permiso.
- [x] Privacidad: interruptor "Datos sensibles"; retirarlo con alergias guardadas pide confirmación.
- [x] Textos en `strings.xml`.

## T2 — Verificación
- [x] Compuertas locales con JDK 17, una por una (2026-11-12): `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest` (205), `koverVerifyDebug` (74,23 %), `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, scripts de puerta y Semgrep (0 hallazgos).
- [ ] CI del PR en verde (incluye `MigrationTest` en emulador).
- [ ] Contra staging: Gustos con alergias y la casilla marcada guarda (200); Privacidad con el permiso apagado deja `alergias` vacío.
- [ ] En el celular: actualizar desde la versión anterior conserva la sesión y el perfil (migración 1 → 2).

## T3 — Pruebas (unitarias, integración, migración y seguridad)
- [x] Unit: `TastesViewModelTest`, `PrivacyViewModelTest`, `ProfileViewModelsTest`
- [x] Integración: `ProfileRepositoryImplTest` (2 nuevas)
- [x] Migración: `MigrationTest` (compila; corre en CI)
- [x] Seguridad: sin superficie nueva
- **No hacer**: marcar la casilla por defecto, deducir el consentimiento del chip de Alergias o usar `fallbackToDestructiveMigration`.

## T4 — Aprobación
- [ ] Revisor: Jorge — fecha:
