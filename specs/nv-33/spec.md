# NV-33 — Ayuda

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Resolver dudas frecuentes y ofrecer contacto.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero resolver mis dudas sin salir de la app.

- [ ] Busco entre preguntas frecuentes.
- [ ] Abro WhatsApp o correo con intent.
- [ ] Puedo reportar un problema.

### UI

| Frame | Contenido |
|---|---|
| `86_Ayuda` | Buscador, preguntas frecuentes en acordeón, WhatsApp, correo, reportar un problema. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Contenido local al instante. |
| Vacío | Búsqueda sin resultados: ofrece contacto. |
| Error | No hay app para el intent: copia el dato. |
| Sin red | Preguntas locales disponibles. |
| Éxito | Respuesta expandida. |

### Validaciones

- Búsqueda local sin distinguir acentos.

### Dudas abiertas

- **D36** Detalles de texto en la maqueta (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Ajustes` → `Ayuda`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Reportar un problema.

## 4. Modelo de datos

Preguntas en `strings.xml` (propuesta), mientras no haya contenido remoto.

## 5. Seguridad y privacidad

- Reporte sin datos personales automáticos.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: filtro.
- UI: acordeón.
- Data: las entradas de ayuda se cachean en Room y se muestran sin red.
- Security: el reporte de problema no incluye datos personales automáticos.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
