Responsable: Diego

# NV-31 — Recuerdos y postales

> Spec: `specs/nv-31/spec.md`. Plan: `specs/nv-31/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-31-memories`.

## T0 — Preparación

- [x] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `70_CrearPub`, `71_Camara`, `72_Postal`, `73_Album`, `74_RecPlan`, `75_PostalVer`, `PostalVer · *`, `76_RecVacio`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /imagenes/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /postales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /postales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [x] `DELETE /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /recuerdos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /planes/{id}/recuerdo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [ ] `GET /planes/{id}/recuerdo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [ ] `DELETE /recuerdos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [x] `POST /momentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [ ] `PATCH /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).
- [ ] `DELETE /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin pantalla en los frames de nv-31: no se conectó (ver `docs/revision-jorge.md`, 2026-11-22).

## T2 — Pantallas

- [x] `70_CrearPub`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `71_Camara`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `72_Postal`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `73_Album`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `74_RecPlan`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `75_PostalVer`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `PostalVer · *`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `76_RecVacio`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Perfil → `Album` (fila nueva en `81_Perfil`, punto de entrada permitido).
- [x] `Album` → `PostalVer`; `Album` (Por plan) → `RecPlan`.
- [x] `Camara` → `Postal` → `Album`. La entrada desde check-in (`CheckinOk`) llega con nv-30.
- [x] `CrearPub` se abre desde Perfil hasta que exista la hoja Crear (nv-17).
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `MemoriesViewModel`: paginación, vacío, error y reintentar.
- [x] Unit: `PostcardViewModel`: estilo, texto ≤ 80, guardar, compartir y eliminar con confirmación.
- [x] Unit: `CreateMomentViewModel`: etiquetados ≤ 10, visibilidad y doble toque en Publicar.
- [x] Data: MockWebServer: `POST /postales` + `GET /postales` con cursor; `DELETE` manda `If-Match`; 404 de postal ajena → `AppError.NotFound`.
- [x] Security: El archivo que se sube no trae EXIF GPS (prueba sobre un JPEG con ubicación).
- [x] UI: Compose: `76_RecVacio` con Ver mis planes; Eliminar abre la confirmación.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/diego.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
