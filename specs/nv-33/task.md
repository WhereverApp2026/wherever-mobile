Responsable: Diego

# NV-33 — Ayuda

> Spec: `specs/nv-33/spec.md`. Plan: `specs/nv-33/plan.md`. Reglas: `CONTRIBUTING.md`. Rama: `feat/nv-33-help`.

## T0 — Preparación

- [x] `git switch main && git pull` y crear la rama desde ahí.
- [x] Leer la spec completa, la constitución y `CONTRIBUTING.md`.
- [x] Abrir en Figma (`9IjBujkogqaUPAkDbluclQ`, «Boceto 2») los frames: `86_Ayuda`.
- [ ] Probar los endpoints en staging con una cuenta de prueba (`@example.com`) y comparar con `wherever-backend/docs/endpoints.md`.

## T1 — Datos

- [x] `POST /errores`: DTO, método en la API y en el repositorio, con su caso en MockWebServer.

## T2 — Pantallas

- [x] `86_Ayuda`: ViewModel + `UiState`, `Content` con `@Preview` y estados (cargando, vacío, error, sin red), `Route`, captura.

## T3 — Navegación y entrada

- [x] Ajustes (`85_Ajustes`) → `Ayuda` (fila nueva, punto de entrada permitido).
- [x] Rutas en `docs/routes.md` (y `scripts/check_routes.sh` en verde).

## T4 — Pruebas

- [x] Unit: `HelpViewModel`: filtro sin acentos, vacío y envío del reporte (éxito, 422, sin red).
- [x] Data: MockWebServer: `POST /errores` manda `modulo`, `mensaje` y `version`; 202 sin cuerpo.
- [x] Security: El mensaje se recorta a 500 y no agrega correo ni teléfono del perfil.
- [x] UI: Compose: el acordeón abre y cierra; sin resultados muestra contactos.

## T5 — Cierre

- [x] Compuertas una por una en verde (`CONTRIBUTING.md`).
- [ ] Probado en teléfono o emulador contra staging; capturas adjuntas al PR.
- [ ] `CHECKLIST_PR.md` completo y PR a `main` con Jorge como revisor; CI en verde.
- [ ] Marcar la casilla en `docs/equipo/diego.md` y avisar a Jorge para sumar las pantallas en `docs/avance.md`.

## Terminado cuando

Todas las casillas de arriba están marcadas y cada frame cumple la DoD de la spec (§6).

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-20
