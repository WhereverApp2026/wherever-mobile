# Borre — Modo negocio

> GitHub: `Borre4023`. Reglas: `CONTRIBUTING.md`. Revisor: Osmar. Actualizado: 2026-11-22.

Orden recomendado: de arriba hacia abajo. Cada spec es una rama y un PR. Las specs están listas desde el 2026-11-20; nv-34 (alta del negocio) la hizo Diego en el PR #48 y es tu punto de partida.

## nv-35 — Negocio: panel, reservas y check-in

- [ ] Spec leída: `specs/nv-35/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-35-business` desde `main` actualizado (con #48 ya integrado)
- [ ] Pantalla `48_NegPanel`
- [ ] Pantalla `49_NegReservas`
- [ ] Pantalla `50_NegCheckin`
- [ ] Pantalla `NegCheckin · *`
- [ ] Pantalla `56_NegStats`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## nv-36 — Negocio: reseñas y momentos

- [ ] Spec leída: `specs/nv-36/spec.md`, `plan.md`, `task.md`
- [ ] Rama `feat/nv-36-business` desde `main` actualizado
- [ ] Pantalla `51_NegResenas`
- [ ] Pantalla `52_NegResp`
- [ ] Pantalla `NegMomentos`
- [ ] Compuertas una por una en verde
- [ ] Probada en teléfono o emulador contra staging
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

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
- [ ] PR a `main` integrado (CI en verde y aprobado por Osmar)

## Notas

- nv-35 crea `BusinessHomeRoute` con la barra de negocio; nv-36, nv-37 (Diego) y nv-38 agregan sus pestañas ahí. Avísale a Diego cuando esté.
- Para probar con datos reales, un administrador tiene que aprobar tu negocio de prueba en staging (pídelo a Osmar).
- QR del check-in, mapa y Custom Tabs dependen de librerías por acordar: Issue y seguir con lo demás.
- Antes de empezar: JDK 17, `local.properties` y `google-services.json` (pídeselo a Jorge). Trabaja con tu cuenta `Borre4023`.
