# Checklist — nv-00 Constitución base de Wherever Móvil

> Trazabilidad: `spec R1-R4 ↔ plan §3-§5 ↔ task T1`. Estado: aprobada (Jorge, 2026-11-06).

- [ ] `specs/nv-00/spec.md` existe con Contexto/Requisitos/Diseño/Contratos/Tests/Criterios/Límites/Aprobación
- [ ] Stack fijado (R1) sin Play/CD/Crashlytics
- [ ] Gobernanza SDD + DoD + trazabilidad + 1 revisor (R2)
- [ ] NFR con comando (R3)
- [ ] Reglas no negociables + real-first (R4)
- [ ] `AGENTS.md` cita el stack + `Memoria.md` existe
- [ ] `local.properties.example` sin secretos
- [ ] `detekt` ≤15 y `ktlint` activos
- [ ] Test de ejemplo (3) verdes
- [ ] `critic-guardrails.sh → PASS` en vacío
- [x] Aprobación firmada: Revisor Jorge — 2026-11-06
