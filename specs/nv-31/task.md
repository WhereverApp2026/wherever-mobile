Responsable: Diego

# NV-31 — Recuerdos y postales

> Spec: `specs/nv-31/spec.md`. Plan: `specs/nv-31/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-31-memories`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `70_CrearPub`, `71_Camara`, `72_Postal`, `73_Album`, `74_RecPlan`, `75_PostalVer`, `PostalVer · *`, `76_RecVacio`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /imagenes/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /postales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /postales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /postales/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /recuerdos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /planes/{id}/recuerdo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /planes/{id}/recuerdo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /recuerdos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /momentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /momentos/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `70_CrearPub`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `71_Camara`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `72_Postal`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `73_Album`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `74_RecPlan`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `75_PostalVer`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `PostalVer · *`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `76_RecVacio`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Perfil → `Album` (fila nueva en `81_Perfil`, punto de entrada permitido).
- [ ] `Album` → `PostalVer`; `Album` (Por plan) → `RecPlan`.
- [ ] `Camara` → `Postal` → `Album`. La entrada desde check-in (`CheckinOk`) llega con nv-30.
- [ ] `CrearPub` se abre desde Perfil hasta que exista la hoja Crear (nv-17).
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `MemoriesViewModel`: paginación, vacío, error y reintentar.
- [ ] Unit: `PostcardViewModel`: estilo, texto ≤ 80, guardar, compartir y eliminar con confirmación.
- [ ] Unit: `CreateMomentViewModel`: etiquetados ≤ 10, visibilidad y doble toque en Publicar.
- [ ] Data: MockWebServer: `POST /postales` + `GET /postales` con cursor; `DELETE` manda `If-Match`; 404 de postal ajena → `AppError.NotFound`.
- [ ] Security: El archivo que se sube no trae EXIF GPS (prueba sobre un JPEG con ubicación).
- [ ] UI: Compose: `76_RecVacio` con Ver mis planes; Eliminar abre la confirmación.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/diego.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
