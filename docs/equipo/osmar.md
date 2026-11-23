# Osmar — Reserva, pago, cupones, feed social y revisión

> GitHub: `OsmarYahir` (y `WhereverApp2026` solo para aprobar tus propios PR). Reglas: `CONTRIBUTING.md`. Actualizado: 2026-11-22.

Eres el **único revisor**: apruebas todo PR a `main` (ver "Aprobación" en `CONTRIBUTING.md`). nv-34 (alta del negocio), que antes era tuya, la hizo Diego en el PR #48; el resto del modo negocio pasó a Borre (nv-35, nv-36, nv-38) y a Diego (nv-37).

## Revisión

- [ ] PR abiertos de Diego: #42 (Ayuda), #45 (Recuerdos) y #48 (alta del negocio)
- [ ] Cada PR: CI en verde, `CHECKLIST_PR.md`, spec al día y prueba en teléfono o emulador

Avance de nv-34 en el PR #48 (antes en esta lista):

- [x] Spec leída: `specs/nv-34/spec.md`, `plan.md`, `task.md`
- [x] Rama `feat/nv-34-business` desde `main` actualizado
- [ ] Pantalla `42_NegCuenta` (se salta con sesión, D-34b en `docs/revision-jorge.md`)
- [x] Pantalla `43_NegRegistro`
- [x] Pantalla `44_NegUbic`
- [x] Pantalla `45_NegOper`
- [x] Pantalla `46_NegVerif`
- [x] Pantalla `47_NegListo`
- [x] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## Backend que bloquea a la app

- [ ] `GET /usuarios/yo/insignias` para `83_Insignias` (hoy con datos locales)
- [ ] `GET /lugares` con filtro de Sello y `sello_excelencia` en la tarjeta (`34_Sello`, Jorge)
- [ ] Historias, reacciones y comentarios para el feed (`nv-17`; hoy dan 404 en staging)
- [ ] Semilla de negocios en staging para probar Explorar, ficha y reservas

- [x] Spec leída: `specs/nv-35/spec.md`, `plan.md`, `task.md`
- [x] Rama `feat/nv-35-business` desde `main` actualizado
- [x] Pantalla `48_NegPanel`
- [x] Pantalla `49_NegReservas`
- [x] Pantalla `50_NegCheckin`
- [x] Pantalla `NegCheckin · *`
- [x] Pantalla `56_NegStats`
- [x] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## nv-27 — Reserva y pago (lo que falta)

- [ ] Pantalla `35_Pasarela`
- [ ] Pantalla `36_PagoError`
- [ ] Hoja `27_Cancelar`
- [ ] Hoja `38_CambiarHora`
- [ ] Variantes `DetReserva · *`
- [ ] Compuertas, teléfono y PR (lo aprueba `WhereverApp2026`)

## nv-29 — Cupones

- [ ] Spec leída: `specs/nv-29/spec.md`, `plan.md`, `task.md`
- [ ] Pantalla `29_Cupones`
- [ ] Pantalla `47_DetCupon` y `DetCupon · *`
- [ ] Compuertas, teléfono y PR

## nv-17 — Inicio y feed (lo que falta)

- [ ] Pantalla `13_Reacciones` y `Reacciones · solicitud enviada`
- [ ] Pantalla `16_Historia` y `Historia · *`
- [ ] Probar en teléfono `14_MeApunto` y `29_PlanAmigo` (hace falta un momento de staging con `plan_id`)
- [ ] Terminar la hoja `15_Crear` (3 de sus 5 opciones aún no navegan)
- [ ] Compuertas, teléfono y PR
