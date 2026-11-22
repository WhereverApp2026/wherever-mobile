# Checklist — NV-33 — Ayuda

> Responsable: Diego. Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T0-T5`. Estado: lista para ejecutar (2026-11-20).

- [x] `ktlintCheck` (con `--no-daemon --max-workers=1`)
- [x] `detekt`
- [x] `lintDebug`
- [x] `testDebugUnitTest` (unit, data, security, arquitectura y capturas)
- [x] `koverVerifyDebug` (≥ 45 %)
- [x] `assembleDebug`
- [x] Scripts: `critic-guardrails.sh` PASS, `check_routes.sh` OK, `check_strings.sh` OK, `check_specs.sh` SPECS-OK
- [x] Sin secretos, sin tokens en logs, solo HTTPS
- [x] Estados cubiertos: cargando, vacío, error, sin red
- [ ] Probado en teléfono o emulador contra staging
- [ ] PR con Jorge como revisor y CI en verde
- [x] Aprobación: Revisor Jorge — 2026-11-06
