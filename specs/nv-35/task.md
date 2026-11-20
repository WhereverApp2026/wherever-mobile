Responsable: Osmar

# NV-35 — Negocio: panel, reservas y check-in

> Spec: `specs/nv-35/spec.md`. Plan: `specs/nv-35/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-35-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `48_NegPanel`, `49_NegReservas`, `50_NegCheckin`, `NegCheckin · *`, `56_NegStats`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `GET /negocios/mios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/panel`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/estadisticas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/reportes/mensual`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/consejo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/reservas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /negocios/{id}/reservas/{rid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/reservas/{rid}/mensajes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/reservas/escanear`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/bloqueos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /negocios/{id}/bloqueos/{bid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/reservas/{rid}/cobro`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /usuarios/yo/modo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `48_NegPanel`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `49_NegReservas`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `50_NegCheckin`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `NegCheckin · *`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `56_NegStats`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Grafo principal → `BusinessHomeRoute` (Panel). Barra interna: Panel, Reservas, Cupones (nv-37), Reseñas (nv-36), Perfil (nv-38).
- [ ] `NegReservas` → `NegCheckin`. `NegPanel` → `NegStats`.
- [ ] Entrar al modo negocio: `POST /usuarios/yo/modo` con `negocio` (403 sin rol → ir a nv-34).
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `BusinessPanelViewModel`: campos nulos ocultos, error y reintentar.
- [ ] Unit: `BusinessReservationsViewModel`: cambiar de pestaña, confirmar, asistió/no asistió (409 → recargar).
- [ ] Unit: `CheckinViewModel`: código válido, 404 y 409 `codigo_ya_usado`.
- [ ] Data: MockWebServer: `GET /negocios/{id}/reservas` con `fecha`, `estado` y `q`; `POST escanear`; PDF del reporte se guarda completo.
- [ ] Security: El nombre del cliente no aparece en logs (prueba sobre `RedactedLoggingInterceptor`).
- [ ] UI: Compose: barra de negocio cambia de pestaña; check-in con código vacío deshabilita Confirmar.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
