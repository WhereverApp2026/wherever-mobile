Responsable: Diego

# NV-32 — Reseñas

> Spec: `specs/nv-32/spec.md`. Plan: `specs/nv-32/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-32-reviews`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `68_PostVisita`, `69_Publicado`, `77_ResPend`, `78_ResCom`, `79_MisRes`, `MisRes · confirmar eliminar`, `MisRes · reseña eliminada`, `80_ResLugar`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `GET /resenas/pendientes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /resenas/mias`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /resenas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /resenas/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /resenas/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /lugares/{id}/resenas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /resenas/{id}/util`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /resenas/{id}/reportes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /checkins`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `68_PostVisita`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `69_Publicado`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `77_ResPend`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `78_ResCom`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `79_MisRes`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `MisRes · confirmar eliminar`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `MisRes · reseña eliminada`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `80_ResLugar`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Perfil → `MisRes` y `ResPend` (filas nuevas en `81_Perfil`).
- [ ] Ficha del lugar (`33_Detalle`) → `ResLugar` (botón «Ver reseñas»).
- [ ] Aviso push `resena_pendiente` → `ResPend` (cuando el backend lo mande; hoy cae en Avisos).
- [ ] `ResPend` → `PostVisita` → `ResCom` → `Publicado`.
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `ReviewFormViewModel`: validaciones (estrellas obligatorias, ≤ 500, ≤ 3 fotos, ≤ 5 chips) y 422 de moderación.
- [ ] Unit: `MyReviewsViewModel`: editable, eliminar con Deshacer (reloj inyectado).
- [ ] Data: MockWebServer: `POST /resenas`, `PATCH` con `If-Match` (409 → recargar), `GET /lugares/{id}/resenas` con filtros en la query.
- [ ] Security: La reseña no manda campos que la API no pide (sin ubicación ni ids ajenos).
- [ ] UI: Compose: Útil cambia el contador; Reportar abre los motivos.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/diego.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
