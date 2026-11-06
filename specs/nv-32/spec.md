# NV-32 — Reseñas

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 9 · Recuerdos y reseñas. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Dejar y leer reseñas verificadas de visitas reales.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero reseñar lugares que visité.

- [ ] Solo se reseña con reservación y check-in.
- [ ] El detalle es opcional.
- [ ] Puedo publicar en el feed o guardarla para mí.
- [ ] Puedo editar y eliminar.

### UI

| Frame | Contenido |
|---|---|
| `68_PostVisita` | ¿Cómo estuvo?: foto, estrellas, texto, con quién, quién lo ve, calificar a detalle, Publicar en el feed, Guardar solo en mis recuerdos. |
| `69_Publicado` | "Ya está en el feed", vista previa, ¿Repetimos?, Ver mi publicación, Volver al inicio. |
| `77_ResPend` | Por reseñar: aviso, visitas pendientes, ya reseñadas. |
| `78_ResCom` | Calificar a detalle: Sabor, Atención, Ambiente, Precio justo; ¿Qué fue lo mejor?; Listo. |
| `79_MisRes` | Publicadas y Por reseñar; reseña con respuesta del negocio; Editar, Eliminar. |
| `MisRes · *` | Variantes: confirmar eliminar y reseña eliminada con Deshacer. |
| `80_ResLugar` | Reseñas del lugar: promedio, distribución, filtros, reseñas, Útil, Reportar. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto. |
| Vacío | Sin pendientes: mensaje (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Borrador local con envío posterior. |
| Éxito | `69_Publicado`. |

### Validaciones

- Calificación general obligatoria (1 a 5).
- Texto con límite por definir.
- Confirmar antes de eliminar.

### Dudas abiertas

- **D30** Deshacer al eliminar reseña (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Hoja `Crear`, `Notificaciones`, `Perfil` → `ResPend` → `PostVisita` → `ResCom`, `Publicado`.
- `Perfil` → `MisRes`. `Detalle` → `ResLugar`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Reseñas: crear, editar, eliminar y listar.
- Visitas pendientes.
- Útil y Reportar.

## 4. Modelo de datos

`ReviewEntity` y borradores.

## 5. Seguridad y privacidad

- La verificación la decide el servidor, no la app.
- Reportar contenido queda registrado en el servidor.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `PostVisitViewModel`.
- Data: borrador sin red.
- UI: diálogo de eliminar.
- Security: el texto de la reseña y los datos del autor no aparecen en logs antes de publicar.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
