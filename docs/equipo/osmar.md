# Osmar — Modo negocio

> GitHub: `OsmarYahir`. Reglas: `CONTRIBUTING.md`. Revisor: Jorge. Actualizado: 2026-11-20.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR.

## nv-34 — Negocio: registro

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

## nv-35 — Negocio: panel, reservas y check-in

- [ ] Spec leída: `specs/nv-35/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-35-business` desde `main` actualizado
- [ ] Pantalla `48_NegPanel`
- [ ] Pantalla `49_NegReservas`
- [ ] Pantalla `50_NegCheckin`
- [ ] Pantalla `NegCheckin · *`
- [ ] Pantalla `56_NegStats`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## nv-36 — Negocio: reseñas y momentos

- [ ] Spec leída: `specs/nv-36/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-36-business` desde `main` actualizado
- [ ] Pantalla `51_NegResenas`
- [ ] Pantalla `52_NegResp`
- [ ] Pantalla `NegMomentos`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## nv-37 — Negocio: cupones y cupo

- [ ] Spec leída: `specs/nv-37/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-37-business` desde `main` actualizado
- [ ] Pantalla `53_NegCupones`
- [ ] Pantalla `54_NegCrear`
- [ ] Pantalla `55_NegHoras`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## nv-38 — Negocio: perfil, fotos y cobros

- [ ] Spec leída: `specs/nv-38/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-38-business` desde `main` actualizado
- [ ] Pantalla `58_NegPerfil`
- [ ] Pantalla `87_NegGaleria`
- [ ] Pantalla `88_NegAgregarFoto`
- [ ] Pantalla `57_NegCobros`
- [ ] Pantalla `89_NegBanco`
- [ ] Pantalla `90_NegFiscal`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde)

## Notas

- Empieza por nv-34 (registro): sin un negocio dado de alta no hay panel que probar.
- nv-35 crea `BusinessHomeRoute` con la barra de negocio; nv-36, nv-37 y nv-38 agregan sus pestañas ahí.
- Para probar con datos reales necesitas que un administrador apruebe tu negocio de prueba en staging (pídelo a Jorge).
- QR del check-in, mapa y Custom Tabs dependen de librerías por acordar: Issue y seguir con lo demás.
