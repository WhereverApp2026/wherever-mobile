# NV-24 — Plan: detalle y edición

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 5 · Crear plan con IA.
> **Conciliada el 2026-11-22** con lo que implementó `specs/nv-48` y con `wherever-backend/docs/endpoints.md` (`origin/main`). Estado: **parcial**. Lo que falta va en la Fase 2 de `docs/itinerario-planes.md`.

## 1. Objetivo

Ver un plan con mapa y ajustarlo parada por parada.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero ajustar un plan para que me quede.

- [x] Cambio, quito, agrego y reordeno paradas (nv-48: subir y bajar, ±15 min, cambiar por alternativa, quitar con Deshacer, agregar buscando).
- [x] Se recalculan horarios, costos y traslados: los recalcula la API en cada edición (nv-48).
- [ ] Puedo crear un plan a mano (`CrearPlan`).

### Conciliación con lo implementado (2026-11-22)

| Frame | Estado | Detalle |
|---|---|---|
| `39_Plan` | funciona en parte | Título, fecha, compartir, resumen, paradas con horario y traslado, cambiar, quitar, agregar, renombrar, repetir y borrar. Faltan el mapa (D25, nv-30), Invitar amigos (`InvitarPlan`, nv-25) y Reservar todo (nv-27) |
| `Plan · *` | funciona | Variante "Paseo familiar": mismo detalle con otros parámetros |
| `40_Cambiar` | funciona | Hoja de alternativas cercanas del mismo giro; Quitar esta parada |
| `41_AgregarParada` | falta | Solo existe la búsqueda en una hoja. Faltan la pestaña Sugeridas, los minutos adicionales y "cómo queda" |
| `CrearPlan` | falta | No existe el plan a mano |

El conteo de `docs/avance.md` (3 de 5) se dedujo del código; se confirma frame por frame en la Fase 2.

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
| Cargando | Datos de Room (`nv-56`); recálculo con indicador por parada. |
| Vacío | Plan sin paradas: invita a agregar. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. 409 (otro dispositivo cambió el plan): recarga y avisa (nv-48). |
| Sin red | Plan en modo lectura (disponible sin internet, `nv-56`). |
| Éxito | Plan guardado. |

### Validaciones

- Nombre hasta 40 caracteres.
- Paradas: la API acepta de 1 a 6 en `POST /planes`. La app deja al menos 1 (nv-48: la última no se quita) y como máximo 4, igual que `17_AjustaPlan` (decisión 4 de `docs/itinerario-planes.md`).
- Fecha en el futuro.

### Dudas abiertas

- **D25** Mapas y navegación (`specs/README.md`): el mapa de `39_Plan` espera la misma decisión que nv-30.
- **D31** Qué funciona sin red: se resuelve en `nv-56`, con Room y `GET /planes/{id}/paquete-offline`.
- **Sugeridas en `41_AgregarParada`:** el contrato no tiene sugerencias por plan. Propuesta: `GET /recomendaciones` con la ubicación de la última parada. Los minutos adicionales y "cómo queda" no tienen vista previa en la API: el plan recalculado llega después de agregar, con Deshacer.

### No-alcance

- Arrastrar paradas: se reordenan con subir y bajar, que también funciona con TalkBack (nv-48).
- Invitar amigos (`InvitarPlan`, nv-25) y Reservar todo (nv-27): los botones navegan a esas specs cuando existan.
- Lo que no tiene endpoint no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `CreatePlanRoute` o `MyPlansRoute` → `PlanRoute(id)` (nv-48, `docs/routes.md`).
- `PlanRoute` → hojas de Cambiar y Agregar parada (sin ruta propia), `MapaFull`/`Llegar` (nv-30), `InvitarPlan` (nv-25), `Reserva` (nv-27).
- `17_AjustaPlan` o pestaña Planes → `CrearPlan`: ruta nueva al implementarla.

### Endpoints

Contrato vivo: `wherever-backend/docs/endpoints.md`. Toda edición manda `If-Match` con la versión; versión vieja = 409.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/planes` | Crear: `{nombre, fecha, personas, presupuesto, movilidad, origen?, paradas[{negocio_id, llega, minutos, precio, nota?}]}` (1 a 6 paradas) |
| GET | `/planes/{id}` | Plan con traslados, costos y `version`; ajeno o borrado = 404 |
| PATCH / DELETE | `/planes/{id}` | Renombrar y otros cambios / borrar |
| POST | `/planes/{id}/clonar` | Repetir mañana |
| POST | `/planes/{id}/paradas` | Agregar parada |
| PUT | `/planes/{id}/paradas/orden` | Reordenar |
| PATCH / DELETE | `/planes/{id}/paradas/{parada_id}` | Mover la hora / quitar |
| GET | `/planes/{id}/paradas/{parada_id}/alternativas` | Alternativas, con `?q=` para buscar |
| POST | `/planes/{id}/paradas/{parada_id}/cambiar` | Cambiar por `{negocio_id}` |
| POST / DELETE | `/planes/{id}/compartir` | Enlace de 7 días / retirarlo |
| GET | `/lugares/buscar`, `/lugares/{id}` | Buscar lugar para agregar; nombre de cada parada |
| GET | `/recomendaciones` | Propuesta para la pestaña Sugeridas |

## 4. Modelo de datos

Hoy no hay Room (nv-48: modelos `Plan`, `PlanStop`, `Transfer` en `feature/plans/domain/Plans.kt`). `PlanEntity` y `PlanStopEntity`, con sincronización, quedan en `nv-56`.

## 5. Seguridad y privacidad

- El servidor valida quién edita: un integrante del grupo lee el plan pero no lo edita (403); un plan ajeno da 404.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit (hecho, nv-48): `test/.../feature/plans/ui/PlansViewModelsTest.kt`: quitar y deshacer, la última no se quita, subir y bajar, +15 min, cambiar por alternativa, agregar después de la última, 409 recarga, compartir y repetir, renombrar.
- Unit (falta): límites de `CrearPlan` (nombre ≤ 40, 1 a 4 paradas, fecha futura) y pestañas de `41_AgregarParada`.
- Data (hecho, nv-48): `test/.../integration/PlansIntegrationTest.kt`: `If-Match` en reordenar, mover y quitar; 409 → `Conflict`; nombres de parada con una sola ficha por lugar.
- Data (falta, nv-56): guardar el plan en Room y leerlo sin red.
- Security (falta): editar un plan ajeno recibe 403/404 del servidor y la app no aplica el cambio localmente.
- UI (falta): acciones de parada con Compose Testing.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Lo hecho entró con nv-48. Lo que falta: rama `feat/nv-24-plan` (Fase 2 de `docs/itinerario-planes.md`), después de `nv-56`. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] Revisor de la conciliación (2026-11-22): Osmar — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). Conciliación del 2026-11-22 en borrador, con evidencia, pendiente de Osmar.
