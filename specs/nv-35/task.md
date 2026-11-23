Responsable: Osmar

# NV-35 — Negocio: panel, reservas y check-in

> Spec: `specs/nv-35/spec.md`. Plan: `specs/nv-35/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-35-business`.

## T0 — Preparación

- [x] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `48_NegPanel`, `49_NegReservas`, `50_NegCheckin`, `NegCheckin · *`, `56_NegStats`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `GET /negocios/mios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/panel`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/estadisticas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/reportes/mensual`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/consejo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/reservas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PATCH /negocios/{id}/reservas/{rid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/reservas/{rid}/mensajes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/reservas/escanear`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/bloqueos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /negocios/{id}/bloqueos/{bid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/reservas/{rid}/cobro`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /usuarios/yo/modo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [x] `48_NegPanel`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `49_NegReservas`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `50_NegCheckin`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `NegCheckin · *`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `56_NegStats`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Grafo principal → `BusinessHomeRoute` (Panel). Barra interna: Panel, Reservas, Cupones (nv-37), Reseñas (nv-36), Perfil (nv-38).
- [x] `NegReservas` → `NegCheckin`. `NegPanel` → `NegStats`.
- [x] Entrar al modo negocio: `POST /usuarios/yo/modo` con `negocio` (403 sin rol → ir a nv-34).
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `BusinessPanelViewModel`: campos nulos ocultos, error y reintentar.
- [x] Unit: `BusinessReservationsViewModel`: cambiar de pestaña, confirmar, asistió/no asistió (409 → recargar).
- [x] Unit: `CheckinViewModel`: código válido, 404 y 409 `codigo_ya_usado`.
- [x] Data: MockWebServer: `GET /negocios/{id}/reservas` con `fecha`, `estado` y `q`; `POST escanear`; PDF del reporte se guarda completo.
- [x] Security: El nombre del cliente no aparece en logs (prueba sobre `RedactedLoggingInterceptor`).
- [x] UI: Compose: barra de negocio cambia de pestaña; check-in con código vacío deshabilita Confirmar.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
