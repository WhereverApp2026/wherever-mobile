Responsable: Diego

# NV-32 — Reseñas

> Spec: `specs/nv-32/spec.md`. Plan: `specs/nv-32/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-32-reviews`.

## T0 — Preparación

- [x] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `68_PostVisita`, `69_Publicado`, `77_ResPend`, `78_ResCom`, `79_MisRes`, `MisRes · confirmar eliminar`, `MisRes · reseña eliminada`, `80_ResLugar`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `GET /resenas/pendientes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /resenas/mias`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /resenas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PATCH /resenas/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /resenas/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /lugares/{id}/resenas`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /resenas/{id}/util`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /resenas/{id}/reportes`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /checkins`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Reutiliza `PhotoRepository` (nv-45), sin cambios.
- [x] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Reutiliza `PhotoRepository` (nv-45), sin cambios.

## T2 — Pantallas

- [x] `68_PostVisita`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `69_Publicado`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `77_ResPend`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `78_ResCom`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `79_MisRes`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `MisRes · confirmar eliminar`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `MisRes · reseña eliminada`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `80_ResLugar`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Perfil → `MisRes` y `ResPend` (filas nuevas en `81_Perfil`).
- [x] Ficha del lugar (`33_Detalle`) → `ResLugar` (botón «Ver reseñas»).
- [ ] Aviso push `resena_pendiente` → `ResPend` (cuando el backend lo mande; hoy cae en Avisos). Sin cambio: depende del backend y de `core/notifications` (D-32 en `docs/revision-jorge.md`).
- [x] `ResPend` → `PostVisita` → `ResCom` → `Publicado`.
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `ReviewFormViewModel`: validaciones (estrellas obligatorias, ≤ 500, ≤ 3 fotos, ≤ 5 chips) y 422 de moderación.
- [x] Unit: `MyReviewsViewModel`: editable, eliminar con Deshacer (reloj inyectado).
- [x] Data: MockWebServer: `POST /resenas`, `PATCH` con `If-Match` (409 → recargar), `GET /lugares/{id}/resenas` con filtros en la query.
- [x] Security: La reseña no manda campos que la API no pide (sin ubicación ni ids ajenos).
- [x] UI: Compose: Útil cambia el contador; Reportar abre los motivos.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/diego.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
