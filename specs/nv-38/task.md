Responsable: Osmar

# NV-38 — Negocio: perfil, fotos y cobros

> Spec: `specs/nv-38/spec.md`. Plan: `specs/nv-38/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-38-business`.

## T0 — Preparación

- [ ] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `58_NegPerfil`, `87_NegGaleria`, `88_NegAgregarFoto`, `57_NegCobros`, `89_NegBanco`, `90_NegFiscal`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `GET /negocios/{id}/panel`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PATCH /negocios/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /negocios/{id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/pausa`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /negocios/{id}/reservas-config`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/fotos`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /negocios/{id}/fotos/orden`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `DELETE /negocios/{id}/fotos/{foto_id}`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/subida`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /imagenes/{id}/confirmar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /negocios/{id}/cobros/conectar`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/saldo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `GET /negocios/{id}/liquidaciones`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /negocios/{id}/cuenta-deposito`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `PUT /negocios/{id}/datos-fiscales`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.
- [x] `POST /usuarios/yo/modo`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [x] `58_NegPerfil`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `87_NegGaleria`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `88_NegAgregarFoto`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `57_NegCobros`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `89_NegBanco`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.
- [x] `90_NegFiscal`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Barra de negocio → `NegPerfil` → `NegGaleria` → hoja `NegAgregarFoto`.
- [x] `NegPerfil` → `NegCobros`, `NegBanco`, `NegFiscal`, `NegHoras` (nv-37).
- [x] Volver a Wherever → Inicio del modo explorar.
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `BusinessProfileViewModel`: `If-Match`, 409 → recargar, pausa hasta ≤ 90 días.
- [x] Unit: `GalleryViewModel`: orden, mínimo 3 fotos y máximo 10.
- [x] Unit: `BankAccountViewModel`: CLABE de 18 dígitos con dígito verificador 3-7-1.
- [x] Data: MockWebServer: `PUT cuenta-deposito` (422 `clabe_invalida`), `GET liquidaciones` con cursor, `POST cobros/conectar` devuelve `url`.
- [x] Security: Ningún log ni archivo guarda CLABE completa ni RFC.
- [x] UI: Compose: Eliminar negocio habilita el botón solo con el nombre exacto.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/osmar.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
