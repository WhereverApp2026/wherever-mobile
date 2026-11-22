# Diego — Recuerdos, Reseñas y Ayuda

> GitHub: `DiegoLopEs1250`. Reglas: `CONTRIBUTING.md`. Revisor: Jorge. Actualizado: 2026-11-20.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR.

## nv-33 — Ayuda

- [ ] Spec leída: `specs/nv-33/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-33-help` desde `main` actualizado
- [ ] Pantalla `86_Ayuda`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

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

- [ ] Spec leída: `specs/nv-31/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-31-memories` desde `main` actualizado
- [ ] Pantalla `70_CrearPub`
- [ ] Pantalla `71_Camara`
- [ ] Pantalla `72_Postal`
- [ ] Pantalla `73_Album`
- [ ] Pantalla `74_RecPlan`
- [ ] Pantalla `75_PostalVer`
- [ ] Pantalla `PostalVer · *`
- [ ] Pantalla `76_RecVacio`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## Notas

- Empieza por nv-33 (Ayuda): es una sola pantalla y sirve para conocer el flujo de PR y compuertas.
- nv-32 (Reseñas) necesita check-ins válidos: en staging se crean con `POST /checkins` (pídele a Jorge una cuenta con visitas).
- nv-31 (Recuerdos) reutiliza `PhotoRepository` para subir fotos; no lo modifiques.
- Todavía no eres colaborador del repo: Jorge te invita para que puedas abrir PR y te asignen Issues.
