Responsable: Osmar

# NV-34 — Negocio: registro

> Spec: `specs/nv-34/spec.md`. Plan: `specs/nv-34/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-34-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [ ] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [ ] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `42_NegCuenta`, `43_NegRegistro`, `44_NegUbic`, `45_NegOper`, `46_NegVerif`, `47_NegListo`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `POST /usuarios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /categorias`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/mios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PATCH /negocios/{id}/alta/{paso}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/fotos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/fotos/orden`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `DELETE /negocios/{id}/fotos/{foto_id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/documentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/documentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/datos-fiscales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/cuenta-deposito`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/enviar-revision`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /negocios/{id}/ia/descripcion`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /usuarios/yo/modo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [ ] `42_NegCuenta`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `43_NegRegistro`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `44_NegUbic`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `45_NegOper`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `46_NegVerif`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [ ] `47_NegListo`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [ ] Ajustes → «Registrar mi negocio» → `NegCuenta` (fila nueva en `85_Ajustes`).
- [ ] `NegCuenta` → `NegRegistro` → `NegUbic` → `NegOper` → `NegVerif` → `NegListo` → `BusinessHomeRoute` (Panel, nv-35).
- [ ] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [ ] Unit: `BusinessOnboardingViewModel`: avance por pasos, retomar en `paso_alta`, Guardar y salir, 422 con lista de faltantes.
- [ ] Unit: Validaciones: nombre 3–60, descripción ≤ 300, CLABE de 18 dígitos, documento ≤ 5 MB.
- [ ] Data: MockWebServer: `POST /negocios` con `requiere_refresh` dispara la renovación; `PATCH alta/{paso}` manda `If-Match`; multipart de documentos con `tipo` y `archivo`.
- [ ] Security: Ningún log contiene RFC, CLABE ni nombres de archivo del expediente (prueba sobre el interceptor de logs).
- [ ] UI: Compose: el botón Continuar se habilita solo con el paso válido; 422 de enviar-revision lista lo que falta.

## T5 — Cierre

- [ ] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
