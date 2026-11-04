# Checklist — nv-00 Constitución base de Wherever Móvil

> Trazabilidad: `spec R1-R4 → plan §3-§5 → task T1`. Estado: Borrador sin ejecutar.

- [ ] `specs/nv-00/spec.md` existe con Contexto/Requisitos/Diseño/Contratos/Tests/Criterios/Límites/Aprobación (R1-R4)
- [ ] Stack fijado (R1) sin Play/CD/Crashlytics
- [ ] Gobernanza SDD + DoD + trazabilidad + 1 revisor (R2)
- [ ] NFR con comando para `nv-01` `[planificado nv-01]` (R3)
- [ ] Reglas no negociables + real-first (R4)
- [ ] `AGENTS.md` cita el stack + `Memoria.md` existe
- [ ] `local.properties.example` sin secretos
- [ ] Sin Gradle, código, tests ni scripts en `nv-00`; se validan en `nv-01` `[planificado nv-01]`
- [ ] Referencias a artefactos futuros marcadas `[planificado nv-0X]`
- [ ] `critic-guardrails.sh` y `check_sdd.sh` `[planificado nv-01]`
- [ ] Aprobación firmada: (pendiente de revisor)
