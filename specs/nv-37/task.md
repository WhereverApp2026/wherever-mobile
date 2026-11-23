Responsable: Osmar

# NV-37 — Negocio: cupones y cupo

> Spec: `specs/nv-37/spec.md`. Plan: `specs/nv-37/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-37-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `53_NegCupones`, `54_NegCrear`, `55_NegHoras`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `POST /negocios/{id}/cupones`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/cupones`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/cupones/{cid}/estadisticas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PATCH /negocios/{id}/cupones/{cid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /negocios/{id}/cupones/{cid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/cupones/sugerir-titulo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/cupones/validar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /lugares/{id}/disponibilidad`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /negocios/{id}/reservas-config`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/bloqueos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [x] `53_NegCupones`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `54_NegCrear`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `55_NegHoras`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Barra de negocio → `NegCupones` → `NegCrear`.
- [x] `NegPerfil` o `NegPanel` → `NegHoras`.
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `CouponFormViewModel`: reglas por tipo (porcentaje 1–100, monto > 0), vigencia fin > inicio, título 1–40.
- [x] Unit: `CouponsViewModel`: interruptor con 409 `con_canjes`.
- [x] Unit: `CapacityViewModel`: múltiplos de 15 y 1–500.
- [x] Data: MockWebServer: `POST cupones`, `PATCH` con `If-Match`, `PUT reservas-config` con `If-Match` (422 `cobros_no_activos`).
- [x] Security: La validación de cupón no registra el código en logs.
- [x] UI: Compose: la vista previa del cupón refleja el tipo y el valor.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
