Responsable: Osmar

# NV-37 — Negocio: cupones y cupo

> Spec: `specs/nv-37/spec.md`. Plan: `specs/nv-37/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-37-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `53_NegCupones`, `54_NegCrear`, `55_NegHoras`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `POST /negocios/{id}/cupones`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/cupones`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/cupones/{cid}/estadisticas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /negocios/{id}/cupones/{cid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /negocios/{id}/cupones/{cid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/cupones/sugerir-titulo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/cupones/validar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /lugares/{id}/disponibilidad`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/reservas-config`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/bloqueos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `53_NegCupones`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `54_NegCrear`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `55_NegHoras`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Barra de negocio → `NegCupones` → `NegCrear`.
- [ ] `NegPerfil` o `NegPanel` → `NegHoras`.
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `CouponFormViewModel`: reglas por tipo (porcentaje 1–100, monto > 0), vigencia fin > inicio, título 1–40.
- [ ] Unit: `CouponsViewModel`: interruptor con 409 `con_canjes`.
- [ ] Unit: `CapacityViewModel`: múltiplos de 15 y 1–500.
- [ ] Data: MockWebServer: `POST cupones`, `PATCH` con `If-Match`, `PUT reservas-config` con `If-Match` (422 `cobros_no_activos`).
- [ ] Security: La validación de cupón no registra el código en logs.
- [ ] UI: Compose: la vista previa del cupón refleja el tipo y el valor.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
