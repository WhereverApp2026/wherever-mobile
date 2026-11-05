# 024 — Mis planes

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 7 · Reserva, pago y mis planes. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Ver todos los planes propios por estado.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero encontrar mis planes rápido.

- [ ] Separa próximos, borradores y pasados.
- [ ] Muestra el estado de cada uno.
- [ ] Disponible sin red.

### UI

| Frame | Contenido |
|---|---|
| `24_MisPlanes` | Pestañas Próximos, Borradores, Pasados; tarjetas con estado (Reservado, En grupo, Sin reservar) y costo; Mis grupos; botón +. |
| `41_PlanesVacio` | "Aún no tienes planes", Armar mi primer plan, Explorar lugares. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Datos de Room; actualiza detrás. |
| Vacío | `41_PlanesVacio`. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Lista con cursor. |

### Validaciones

- Un plan pasa a Pasados tras su fecha; reloj inyectado.

### Dudas abiertas

- **D22** Barra inferior inconsistente (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → `Planes` → `Plan`, `MisGrupos`, hoja `Crear`.
- `PlanesVacio` → `IAPalabra`, `Explorar`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Listar planes propios por estado, con cursor.

## 4. Modelo de datos

Lee `PlanEntity`.

## 5. Seguridad y privacidad

- Dato personal: se borra al cerrar sesión.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Data: lectura por pestaña.
- Unit: `PlansViewModel` muestra carga, vacío y error según el resultado del repositorio por pestaña.
- Security: los planes se borran de Room al cerrar sesión.
- UI: estado vacío.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
