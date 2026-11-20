Responsable: Osmar

# NV-36 — Negocio: reseñas y momentos

> Spec: `specs/nv-36/spec.md`. Plan: `specs/nv-36/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-36-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `51_NegResenas`, `52_NegResp`, `NegMomentos`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `GET /negocios/{id}/resenas/resumen`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /lugares/{id}/resenas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/resenas/{rid}/respuesta`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/resenas/{rid}/sugerir-respuesta`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/panel`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/momentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /negocios/{id}/momentos/{mid}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `51_NegResenas`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `52_NegResp`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `NegMomentos`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Barra de negocio → `NegResenas` → `NegResp`, `NegMomentos`.
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `BusinessReviewsViewModel`: periodo mes/trimestre, respuesta 1–500, 422 de moderación.
- [ ] Unit: `BusinessMomentsViewModel`: Agradecer y Destacar sin duplicar al tocar dos veces.
- [ ] Data: MockWebServer: `PUT respuesta`, `POST sugerir-respuesta` (sin IA → aviso), `PATCH momentos/{mid}` con al menos un campo.
- [ ] Security: La sugerencia de IA no manda datos del autor (solo `rid`).
- [ ] UI: Compose: Publicar respuesta deshabilitado con texto vacío.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
