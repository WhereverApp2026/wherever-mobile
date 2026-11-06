# NV-36 — Negocio: reseñas y momentos

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 11 · Negocio. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Responder reseñas, cuidar el Sello y agradecer publicaciones de clientes.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como negocio quiero responder reseñas.

- [ ] Uso respuestas rápidas o texto propio.
- [ ] Responder cuenta para el Sello.

**HU propuesta.** Como negocio quiero agradecer los momentos públicos.

- [ ] Solo veo lo marcado como público.
- [ ] Puedo destacar y agradecer.

### UI

| Frame | Contenido |
|---|---|
| `51_NegResenas` | Sello con requisitos, Descargar insignia digital, rubros, reseñas, Momentos de clientes. |
| `52_NegResp` | Reseña, Respuesta rápida, texto (300), aviso de Sello, Publicar respuesta. |
| `NegMomentos` | Nuevos y Destacados; tarjetas con Agradecer; aviso de privacidad. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto. |
| Vacío | Sin reseñas: mensaje (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Respuesta publicada. |

### Validaciones

- Respuesta no vacía, hasta 300 caracteres.

### Dudas abiertas

- **D27** Cuenta de negocio (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `NegResenas` → `NegResp`, `NegMomentos`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Reseñas del negocio y responder.
- Estado del Sello e insignia.
- Momentos y agradecer.

## 4. Modelo de datos

Caché de reseñas del negocio.

## 5. Seguridad y privacidad

- La insignia se descarga solo si el Sello está vigente.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: límite de texto.
- UI: chips que llenan la respuesta.
- Data: las respuestas del negocio se guardan en Room y se leen sin red.
- Security: los datos del cliente en la reseña no aparecen en logs.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
