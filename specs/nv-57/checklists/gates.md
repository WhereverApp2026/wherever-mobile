# Checklist — NV-57 — Correcciones de la prueba en teléfono

> Trazabilidad: `spec §1-§7 ↔ plan §1-§5 ↔ task T1-T3`. Estado: borrador con evidencia (Borre, 2026-11-26).

- [ ] `./gradlew assembleDebug` y lint (ktlint, Android Lint) sin errores (detekt: CI)
- [ ] Unitarias, Data, Security y Arquitectura verdes
- [ ] Capturas de Roborazzi revisadas (claro, oscuro, letra grande)
- [ ] Cobertura ≥45% (`koverVerifyDebug`)
- [ ] Sin secretos, sin tokens en logs, solo HTTPS
- [ ] Probado en teléfono contra staging
- [ ] `Memoria.md` actualizada
- [ ] Aprobación: Revisor Osmar — fecha:
