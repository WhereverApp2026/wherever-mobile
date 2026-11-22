Responsable: Osmar

# NV-34 — Negocio: registro

> Spec: `specs/nv-34/spec.md`. Plan: `specs/nv-34/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-34-business`.

## T0 — Preparación

- [x] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `42_NegCuenta`, `43_NegRegistro`, `44_NegUbic`, `45_NegOper`, `46_NegVerif`, `47_NegListo`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [ ] `POST /usuarios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. No aplica: con sesión se salta 42_NegCuenta (D-34b).
- [x] `GET /categorias`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/mios`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PATCH /negocios/{id}/alta/{paso}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/fotos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `PUT /negocios/{id}/fotos/orden`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin control en los frames del alta: lo usa 87_NegGaleria (nv-38).
- [ ] `DELETE /negocios/{id}/fotos/{foto_id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Sin control en los frames del alta: lo usa 87_NegGaleria (nv-38).
- [x] `POST /negocios/{id}/documentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `GET /negocios/{id}/documentos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. El estado llega en `documentos{ine, comprobante}` del negocio; no hizo falta.
- [ ] `PUT /negocios/{id}/datos-fiscales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Vive en 90_NegFiscal (nv-38).
- [x] `PUT /negocios/{id}/cuenta-deposito`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/enviar-revision`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/ia/descripcion`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [ ] `POST /usuarios/yo/modo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer. Lo usa «Volver a Wherever» / el panel (nv-35, nv-38).

## T2 — Pantallas

- [ ] `42_NegCuenta`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura. Se salta con sesión (D-34b).
- [x] `43_NegRegistro`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `44_NegUbic`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `45_NegOper`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `46_NegVerif`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `47_NegListo`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Ajustes → «Registrar mi negocio» → `NegCuenta` (fila nueva en `85_Ajustes`).
- [ ] `NegCuenta` → `NegRegistro` → `NegUbic` → `NegOper` → `NegVerif` → `NegListo` → `BusinessHomeRoute` (Panel, nv-35). Hasta 47; «Ir al panel» se conecta en nv-35.
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `BusinessOnboardingViewModel`: avance por pasos, retomar en `paso_alta`, Guardar y salir, 422 con lista de faltantes.
- [x] Unit: Validaciones: nombre 3–60, descripción ≤ 300, CLABE de 18 dígitos, documento ≤ 5 MB.
- [x] Data: MockWebServer: `POST /negocios` con `requiere_refresh` dispara la renovación; `PATCH alta/{paso}` manda `If-Match`; multipart de documentos con `tipo` y `archivo`.
- [x] Security: Ningún log contiene RFC, CLABE ni nombres de archivo del expediente (prueba sobre el interceptor de logs).
- [x] UI: Compose: el botón Continuar se habilita solo con el paso válido; 422 de enviar-revision lista lo que falta.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
