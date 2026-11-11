# Checklist — NV-41 — Identificador de la app y build de release

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T4`. Estado: implementada, pendiente de revisión.

- [x] `./gradlew assembleDebug` y lint (ktlint, detekt, Android Lint) sin errores
- [x] Unitarias y seguridad verdes (`ReleaseConfigTest`)
- [x] `assembleRelease` firmado y sin firmar
- [ ] Release minificado probado contra staging en el teléfono
- [x] Sin keystore ni contraseñas en el repo
- [x] Cobertura ≥45%
- [x] `Memoria.md` actualizada
- [ ] Aprobación: Revisor Jorge — fecha:
