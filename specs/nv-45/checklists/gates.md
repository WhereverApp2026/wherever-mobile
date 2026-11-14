# Checklist — NV-45 — Transiciones entre pantallas y pulido de UI

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, pendiente de revisión.

- [x] `ktlintCheck`, `detekt`, `lintDebug` sin errores
- [x] `testDebugUnitTest` verde (Unit, Data, Security, Arquitectura): 240
- [x] `koverVerifyDebug` ≥45%: 75,27 %
- [x] `assembleDebug`, `assembleDebugAndroidTest` y `assembleRelease`
- [ ] Pruebas de UI verdes (CI del PR)
- [x] Sin cambios en Room ni en la API; sin rutas nuevas
- [x] Duraciones solo en `Motion.kt`; animaciones apagadas respetadas
- [x] `Memoria.md` actualizado
- [ ] Aprobación: Revisor Jorge — fecha:
