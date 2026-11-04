# Scripts SDD — estado

> Los gates de `nv-01` ya existen en `scripts/`. Cobertura y delta siguen `[planificado nv-03]`.

| Script | Fase | Qué verifica |
|---|---|---|
| `scripts/check_sdd.sh` | `nv-01` | Plantilla, secciones, aprobación y marcadores de trabajo |
| `scripts/critic-guardrails.sh` | `nv-01` | Bloqueantes B1-B4 |
| `scripts/check_routes.sh` | `nv-01` | `docs/routes.md` vs. navegación real |
| `scripts/check_strings.sh` | `nv-01` | Textos en `strings.xml` |
| `scripts/check_coverage.sh --min 45` | `nv-03` `[planificado nv-03]` | Cobertura global Kover (provisional) |
| `scripts/check_delta_coverage.py --min 80 --advisory` | `nv-03` `[planificado nv-03]` | Cobertura del diff (no bloquea) |

> YAML validado con `yaml.safe_load`. Tras la mutación siempre `./gradlew assembleDebug` (constitución §8).
