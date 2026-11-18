# Checklist — NV-49 — Autenticación en el teléfono

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, pendiente de revisión.

- [x] `ktlintCheck`, `detekt`, `lintDebug` sin errores
- [x] `testDebugUnitTest` verde (Unit, Data, Security, Arquitectura): 244
- [x] `koverVerifyDebug` ≥45%
- [x] `assembleDebug`, `assembleRelease` y `assembleDebugAndroidTest`
- [ ] Pruebas de UI verdes (CI del PR)
- [x] Probado en el teléfono de Jorge: login (error visible), registro, cerrar sesión y recuperar
- [x] Sin rutas ni permisos nuevos; cadena de red intacta
- [x] `versionCode` 2, `versionName` 0.1.1
- [x] `Memoria.md` actualizado
- [ ] Aprobación: Revisor Jorge — fecha:
