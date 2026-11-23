# NV-24 — Plan: detalle y edición

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 5 · Crear plan con IA. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Ver un plan con mapa y ajustarlo parada por parada.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero ajustar un plan para que me quede.

- [ ] Cambio, quito, agrego y reordeno paradas.
- [ ] Se recalculan horarios, costos y traslados.
- [ ] Puedo crear un plan a mano.

### UI

| Frame | Contenido |
|---|---|
| `39_Plan` | Título, fecha, compartir, mapa con Ver mapa completo, resumen, paradas con horario y traslado, cambiar y quitar, Agregar parada, Invitar amigos, Reservar todo. |
| `Plan · *` | Variante "Paseo familiar". |
| `40_Cambiar` | Hoja "Cambiar parada": alternativas con afluencia, Buscar otro lugar, Quitar esta parada, Cambiar parada. |
| `41_AgregarParada` | Pestañas Sugeridas y Buscar; lugares con minutos adicionales; resumen de cómo queda. |
| `CrearPlan` | Nombre (15/40), quién va, día, hora, presupuesto, paradas, Reordenar, Agregar parada, Visible para mis amigos, Crear plan. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Datos de Room; recálculo con indicador por parada. |
| Vacío | Plan sin paradas: invita a agregar. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Plan en modo lectura (disponible sin internet). |
| Éxito | Plan guardado. |

### Validaciones

- Nombre hasta 40 caracteres.
- Paradas de 2 a 4 según `17_AjustaPlan`: confirmar el máximo al editar (`41_AgregarParada` llega a 4).
- Fecha en el futuro.

### Dudas abiertas

- **D25** Mapas y navegación (`specs/README.md`)
- **D31** Qué funciona sin red (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Generando` o `MisPlanes` → `Plan`.
- `Plan` → `Cambiar`, `AgregarParada`, `MapaFull`, `Llegar`, `Compartir`, `InvitarPlan`, `Reserva`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Uso |
|---|---|---|
| GET | `/planes/{id}` | 39_Plan (la foto y el nivel de precio de cada parada salen de `GET /lugares/{id}`, una vez por lugar). |
| POST | `/planes` | CrearPlan, sin `origen`; «Visible para mis amigos» = `PATCH /planes/{id}` con `visibilidad: publico`. |
| PATCH/DELETE | `/planes/{id}` | Renombrar y borrar (`If-Match`). |
| POST | `/planes/{id}/paradas` | Agregar parada (41). |
| PUT | `/planes/{id}/paradas/orden` | Subir o bajar una parada. |
| PATCH/DELETE | `/planes/{id}/paradas/{parada_id}` | Mover la hora ±15 min y quitar la parada. |
| GET | `/planes/{id}/paradas/{parada_id}/alternativas` | 40_Cambiar (con `?q=` para «Buscar otro lugar») y Sugeridas de 41 (alternativas de la última parada). |
| POST | `/planes/{id}/paradas/{parada_id}/cambiar` | Cambiar parada. |

La API recalcula horarios, traslados y costos en cada edición.

### Decisiones al implementar (2026-11-22)

- **D-24a Sin mapa:** la tarjeta del mapa, «Ver mapa completo» y «Cómo llegar» esperan al proveedor de mapa (D25, nv-30).
- **D-24b Sin Room:** el plan vive en la API (fuente de verdad, nv-48); el modo sin red llega con el paquete offline de nv-30.
- **D-24c «Invitar amigos» y «Reservar todo»** no se dibujan: InvitarPlan es de nv-25 y la API no reserva varias paradas a la vez (nv-27).
- **D-24d CrearPlan:** la API pide `personas` y el frame no tiene contador; se toma de «¿Quién va?» (Solo 1, Pareja 2, Amigos 3, Familia 4). Cada parada dura 60 min y la siguiente empieza 15 min después.
- **D-24e Afluencia** («Poco concurrido a las 11:00») no viene en las alternativas; no se dibuja.
- **D-24f Hasta 4 paradas** al editar y al crear (la API acepta 6). Subir, bajar y ±15 min siguen en la hoja 40.

## 4. Modelo de datos

`PlanEntity` y `PlanStopEntity`, con sincronización.

## 5. Seguridad y privacidad

- Permisos de edición del plan se validan en el servidor.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: reordenar y límites.
- Data: guardar borrador con Room.
- UI: acciones de parada.
- Security: editar un plan ajeno recibe 403 del servidor y la app no aplica el cambio localmente.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Implementada el 2026-11-22 (Diego) en `feat/nv-24-plan-detail`, sobre nv-48 y nv-23; sin probar contra staging.
