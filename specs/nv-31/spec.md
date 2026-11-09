# NV-31 — Recuerdos y postales

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 9 · Recuerdos y reseñas. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Guardar y compartir fotos de los lugares visitados.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero tomar postales de mis visitas.

- [ ] La cámara se pide al abrirla.
- [ ] Sin cámara, se usa la galería.
- [ ] Elijo estilo y guardo o comparto.

**HU propuesta.** Como usuario quiero publicar un momento.

- [ ] Elijo lugar, etiquetas y visibilidad.
- [ ] Puedo eliminar mis postales.

### UI

| Frame | Contenido |
|---|---|
| `70_CrearPub` | Nuevo momento: pestañas Foto, Plan, Reseña; texto con menciones; lugar, etiquetas, quién lo ve; Publicar. |
| `71_Camara` | Cámara con marco de postal, Postal o Normal, disparador, galería. |
| `72_Postal` | Vista previa, estilos Clásica, Atardecer, Minimal; Guardar en mi álbum, Compartir postal. |
| `73_Album` | Recuerdos: Todas o Por plan, cuadrícula. |
| `74_RecPlan` | Recuerdos agrupados por plan. |
| `75_PostalVer` | Postal con lugar, plan, compañía, Compartir, Descargar, Eliminar postal. |
| `PostalVer · *` | Variantes por lugar. |
| `76_RecVacio` | "Aún no tienes recuerdos", Cómo tomar una postal, Ver mis planes. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Miniaturas con Coil; subida con avance. |
| Vacío | `76_RecVacio`. |
| Error | Subida fallida: conserva la foto y permite reintentar. |
| Sin red | Álbum en caché; subidas encoladas con WorkManager. |
| Éxito | Postal en el álbum. |

### Validaciones

- Formato y tamaño de imagen; la API limita el cuerpo a 10 MB.
- Confirmar antes de eliminar.

### Dudas abiertas

- **D18** Permisos (`specs/README.md`)
- **D34** Subir fotos (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Hoja `Crear` → `CrearPub`.
- `CheckinOk` → `Camara` → `Postal` → `Album`.
- `Perfil` → `Album`, `RecPlan` → `PostalVer`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Subida de imágenes.
- Postales, álbum y publicaciones.

## 4. Modelo de datos

`PostcardEntity` con estado de subida.

## 5. Seguridad y privacidad

- Quitar metadatos GPS de la foto antes de subir.
- Cámara en contexto (`docs/permissions-matrix.md`).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: cola de subida.
- Data: la cola de subida de postales persiste en Room y se reanuda tras reiniciar.
- Security: sin EXIF de ubicación.
- UI: estado vacío.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
