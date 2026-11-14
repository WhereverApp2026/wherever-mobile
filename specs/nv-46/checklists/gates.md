# Checklist — NV-46 — Notificaciones push con Firebase

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, sin compilar en local.

- [ ] `ktlintCheck`, `detekt`, `lintDebug` sin errores
- [ ] `testDebugUnitTest` verde (Unit, Data, Security, Arquitectura)
- [ ] `koverVerifyDebug` ≥45%
- [ ] `assembleDebug` y `assembleRelease`, con y sin `app/google-services.json`
- [ ] Prueba manual del push en teléfono
- [x] Sin cambios en Room; sin rutas nuevas
- [x] `google-services.json` fuera de Git
- [x] Textos en `strings.xml`
- [x] `Memoria.md` y `docs/permissions-matrix.md` actualizados
- [ ] Aprobación: Revisor Jorge — fecha:
