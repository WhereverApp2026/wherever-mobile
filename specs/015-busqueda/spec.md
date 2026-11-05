# 015 — Búsqueda

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 2 · Inicio, feed y notificaciones. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Encontrar lugares, personas o planes desde un solo campo.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero buscar por texto para llegar directo.

- [ ] Filtra por pestaña.
- [ ] Guarda recientes y permite borrarlos.
- [ ] Sin resultados ofrece alternativas y la IA.

### UI

| Frame | Contenido |
|---|---|
| `19_Buscar` | Campo, Cancelar, pestañas Lugares, Personas, Planes; Recientes con Borrar; Prueba con una idea; Cerca de ti. |
| `20_BuscarPersonas` | Resultados con Amigos, Agregar o Pendiente; ¿Está contigo? (QR). |
| `BuscarPersonas · solicitud enviada` | Variante: Agregar pasa a Pendiente. |
| `21_SinResultados` | "No encontramos «sushi»", filtros, Quizá te sirva, Pedirle a la IA algo parecido, Quitar filtros. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador bajo el campo, con espera tras escribir. |
| Vacío | `21_SinResultados`. |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Solo recientes y lugares en caché. |
| Éxito | Resultados con cursor. |

### Validaciones

- Mínimo 2 letras; acentos y mayúsculas dan igual.
- Recientes solo en el dispositivo.

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Inicio` o `Explorar` → `Buscar`.
- → `Detalle`, `PerfilAmigo`, `PlanAmigo`, `MiCodigo`, `IAPalabra`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Búsqueda de lugares, personas y planes.
- Sugerencias de búsqueda.

## 4. Modelo de datos

Recientes en Room, borrados al cerrar sesión.

## 5. Seguridad y privacidad

- Las búsquedas de personas respetan `buscable_por_telefono`.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: espera y cancelación con dispatcher inyectado.
- UI: estado sin resultados.
- Data: los resultados con cursor se guardan en Room y la UI lee de Room sin red.
- Security: la búsqueda de personas respeta `buscable_por_telefono` y no registra correos ni teléfonos en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
