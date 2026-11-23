# NV-28 — Mis planes

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 7 · Reserva, pago y mis planes.
> **Conciliada el 2026-11-22** con lo que implementó `specs/nv-48` y con `wherever-backend/docs/endpoints.md` (`origin/main`). Estado: **frames hechos, criterios pendientes**: falta el estado de cada plan y el uso sin red (`nv-56`).

## 1. Objetivo

Ver todos los planes propios por estado.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero encontrar mis planes rápido.

- [x] Separa próximos, guardados y pasados (la maqueta dice "Borradores"; la API, `guardados`).
- [ ] Muestra el estado de cada uno (Reservado, En grupo, Sin reservar). `PlanSummary` no lo trae y `GET /planes` no lo documenta: se cruza con reservas (nv-27) y grupos (nv-25), o se pide al backend.
- [ ] Disponible sin red (`nv-56`).

### Conciliación con lo implementado (2026-11-22)

| Frame | Estado | Detalle |
|---|---|---|
| `24_MisPlanes` | funciona en parte | Pestañas Próximos, Guardados y Pasados; tarjeta con nombre, fecha, paradas y costo por persona; Mis grupos; botón para armar uno. Falta la etiqueta de estado |
| `41_PlanesVacio` | funciona | Vacío con Armar mi primer plan y Explorar lugares |

### UI

| Frame | Contenido |
|---|---|
| `24_MisPlanes` | Pestañas Próximos, Borradores, Pasados; tarjetas con estado (Reservado, En grupo, Sin reservar) y costo; Mis grupos; botón +. |
| `41_PlanesVacio` | "Aún no tienes planes", Armar mi primer plan, Explorar lugares. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Datos de Room (`nv-56`); actualiza detrás. |
| Vacío | `41_PlanesVacio`. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas (`nv-56`). |
| Éxito | Lista con cursor. |

### Validaciones

- Un plan pasa a Pasados tras su fecha: lo decide la API con `pestana=pasados`; sin red, el reloj inyectado.

### Dudas abiertas

- **D22** Barra inferior inconsistente (`specs/README.md`).
- Estado de cada plan: ver criterio 2.

### No-alcance

- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → pestaña Planes → `MyPlansRoute` (nv-48, `docs/routes.md`) → `PlanRoute(id)`, `MisGrupos` (nv-25), `CreatePlanRoute`.
- `41_PlanesVacio` → `CreatePlanRoute`, Explorar.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md`.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/planes` | `?pestana=&cursor=&limite=`, con `pestana` en `proximos`, `guardados` o `pasados`; solo los planes activos de quien llama |

## 4. Modelo de datos

Hoy lee de la red (`PlanSummary`, nv-48). Leer `PlanEntity` desde Room queda en `nv-56`.

## 5. Seguridad y privacidad

- Dato personal: se borra al cerrar sesión (con Room, en `nv-56`, por `sessionCleaners`).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit (hecho, nv-48): `test/.../feature/plans/ui/PlansViewModelsTest.kt`: Mis planes cambia de pestaña.
- Unit (falta): `MyPlansViewModel` muestra carga, vacío y error por pestaña.
- Data (hecho, nv-48): `test/.../integration/PlansIntegrationTest.kt`: lista por pestaña.
- Data (falta, nv-56): lectura por pestaña desde Room sin red.
- Security (falta, nv-56): los planes se borran de Room al cerrar sesión.
- UI (falta): estado vacío.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Lo hecho entró con nv-48. Sin red y limpieza al cerrar sesión: `nv-56` (Fase 1 de `docs/itinerario-planes.md`). Estado de cada plan: se decide en la Fase 0. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Conciliación del 2026-11-22 en borrador, con evidencia, pendiente de Osmar.
