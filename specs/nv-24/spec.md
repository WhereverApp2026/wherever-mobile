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

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- CRUD de planes y paradas.
- Alternativas y sugerencias por parada.
- Recálculo de horarios y traslados.

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
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
