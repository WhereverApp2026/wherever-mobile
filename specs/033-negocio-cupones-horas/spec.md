# 033 — Negocio: cupones y cupo

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 11 · Negocio. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Atraer clientes en horas tranquilas con cupones y cupo por hora.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como negocio quiero publicar cupones.

- [ ] Veo cómo lo verá el cliente.
- [ ] Puedo activar y desactivar.
- [ ] Limito los usos.

**HU propuesta.** Como negocio quiero definir mi cupo.

- [ ] Ajusto mesas por hora y día.
- [ ] Wherever usa esto para sugerir horas tranquilas.

### UI

| Frame | Contenido |
|---|---|
| `53_NegCupones` | Activos, Programados, Terminados; tarjetas con interruptor y usos; Crear cupón. |
| `54_NegCrear` | Tipo (2x1, Porcentaje, Monto fijo), descuento, cuándo aplica, límite de usos, vista previa, Publicar cupón. |
| `55_NegHoras` | Día de la semana; cupo por hora con barras; leyenda; Guardar cambios. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto. |
| Vacío | Sin cupones: Crear cupón. |
| Error | Interruptor regresa si falla. `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Cupón publicado; cupo guardado. |

### Validaciones

- Porcentaje de 1 a 100.
- Monto mayor que 0.
- Límite de usos ≥ 1.
- Cupo ≥ 0.

### Dudas abiertas

- **D27** Cuenta de negocio (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `NegCupones` → `NegCrear`.
- `NegPerfil` o `NegPanel` → `NegHoras`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- CRUD de cupones.
- Cupo por hora.

## 4. Modelo de datos

Caché de cupones y cupos.

## 5. Seguridad y privacidad

- Rol comerciante validado en el servidor.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: validaciones por tipo.
- UI: vista previa.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
