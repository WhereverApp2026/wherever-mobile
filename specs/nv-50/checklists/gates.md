# Checklist — NV-50 — Foto de perfil desde galería o cámara

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, pendiente de revisión.

- [x] `ktlintCheck`, `detekt`, `lintDebug` sin errores
- [x] `testDebugUnitTest` verde (Unit, Data, Security, Arquitectura)
- [x] `koverVerifyDebug` ≥45%
- [x] `assembleDebug`, `assembleRelease` y `assembleDebugAndroidTest`
- [ ] `MigrationTest` verde (CI del PR)
- [x] Probado en el teléfono: galería y cámara
- [x] Sin permisos nuevos; `FileProvider` no exportado
- [x] `versionCode` 3, `versionName` 0.2.1
- [x] `Memoria.md` actualizado
- [ ] Aprobación: Revisor Jorge — fecha:
