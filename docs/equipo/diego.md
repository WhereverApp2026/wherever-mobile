# Diego — Planes y cupones del negocio

> GitHub: `DiegoLopEs1250`. Reglas: `CONTRIBUTING.md`. Revisor: Osmar. Actualizado: 2026-11-22.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR. Desde ahora abre tus PR con tu cuenta `DiegoLopEs1250`, no con `WhereverApp2026`.

## 1. Cerrar lo que ya hiciste

- [x] nv-32 Reseñas: PR #43 integrado
- [ ] nv-33 Ayuda: PR #42, atender la revisión y probar en teléfono o emulador
- [ ] nv-31 Recuerdos y postales: PR #45, atender la revisión y probar
- [ ] nv-34 Negocio, alta: PR #48, atender la revisión y probar (Borre arranca nv-35 sobre esto)

## nv-23 — Crear plan con IA (lo que falta)

- [ ] Revisar contra la spec qué falta de `IAPalabra`, `17_AjustaPlan` y `Generando *` (4 de 6 frames funcionan)
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## nv-24 — Plan: detalle y edición (lo que falta)

- [ ] Revisar contra la spec qué falta de `39_Plan`, `Plan · *`, `40_Cambiar`, `41_AgregarParada` y `CrearPlan` (3 de 5 frames funcionan)
- [ ] Compuertas, teléfono y PR

## nv-26 — Vaca virtual y gastos

- [ ] Spec leída: `specs/nv-26/spec.md`, `plan.md`, `task.md`
- [ ] Pantalla `28_Vaca` y `Vaca · *`
- [ ] Pantalla `45_AgregarGasto`
- [ ] Compuertas, teléfono y PR

## nv-30 — El día del plan

- [ ] Spec leída: `specs/nv-30/spec.md`, `plan.md`, `task.md`
- [ ] Pantalla `13_Mapa`
- [ ] Pantalla `14_MapaFull`
- [ ] Pantalla `15_Llegar`
- [ ] Pantalla `18_Navegando`
- [ ] Pantalla `26_CheckinOk`
- [ ] Compuertas, teléfono y PR

## nv-37 — Negocio: cupones y cupo

- [ ] Spec leída: `specs/nv-37/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-37-business` cuando nv-35 (Borre) ya tenga `BusinessHomeRoute` en `main`
- [ ] Pantalla `53_NegCupones`
- [ ] Pantalla `54_NegCrear`
- [ ] Pantalla `55_NegHoras`
- [ ] Compuertas, teléfono y PR

## Notas

- nv-30 y `32_ExpMapa` (Jorge) dependen del proveedor de mapa (D25): pónganse de acuerdo en la misma librería.
- nv-31 y nv-32 reutilizan `PhotoRepository`; no lo modifiques sin Issue.
