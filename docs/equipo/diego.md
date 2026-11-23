# Diego — Planes y cupones del negocio

> GitHub: `DiegoLopEs1250`. Reglas: `CONTRIBUTING.md`. Revisor: Osmar. Actualizado: 2026-11-22.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR. Desde ahora abre tus PR con tu cuenta `DiegoLopEs1250`, no con `WhereverApp2026`.

## 1. Cerrar lo que ya hiciste

- [x] nv-32 Reseñas: PR #43 integrado
- [ ] nv-33 Ayuda: PR #42, atender la revisión y probar en teléfono o emulador
- [ ] nv-31 Recuerdos y postales: PR #45, atender la revisión y probar
- [ ] nv-34 Negocio, alta: PR #48, atender la revisión y probar (Borre arranca nv-35 sobre esto)

## nv-23 — Crear plan con IA (lo que falta)

- [x] Revisar contra la spec qué falta de `IAPalabra`, `17_AjustaPlan` y `Generando *` (4 de 6 frames funcionan)
- [x] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## nv-24 — Plan: detalle y edición (lo que falta)

- [x] Revisar contra la spec qué falta de `39_Plan`, `Plan · *`, `40_Cambiar`, `41_AgregarParada` y `CrearPlan` (3 de 5 frames funcionan)
- [ ] Compuertas (en verde), teléfono y PR

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

## Historial: nv-32 y nv-31 (hecho)

## nv-32 — Reseñas

- [x] Spec leída: `specs/nv-32/spec.md`, `plan.md`, `task.md`
- [x] Rama `feat/nv-32-reviews` desde `main` actualizado
- [x] Pantalla `68_PostVisita`
- [x] Pantalla `69_Publicado`
- [x] Pantalla `77_ResPend`
- [x] Pantalla `78_ResCom`
- [x] Pantalla `79_MisRes`
- [x] Pantalla `MisRes · confirmar eliminar`
- [x] Pantalla `MisRes · reseña eliminada`
- [x] Pantalla `80_ResLugar`
- [x] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## nv-31 — Recuerdos y postales

- [x] Spec leída: `specs/nv-31/spec.md`, `plan.md`, `task.md`
- [x] Rama `feat/nv-31-memories` desde `main` actualizado
- [x] Pantalla `70_CrearPub`
- [x] Pantalla `71_Camara`
- [x] Pantalla `72_Postal`
- [x] Pantalla `73_Album`
- [x] Pantalla `74_RecPlan`
- [x] Pantalla `75_PostalVer`
- [x] Pantalla `PostalVer · *`
- [x] Pantalla `76_RecVacio`
- [x] Compuertas una por una en verde
