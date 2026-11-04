# 025 — Cupones

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 7 · Reserva, pago y mis planes. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Consultar y usar descuentos.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero usar mis cupones al reservar.

- [ ] Se aplican solos al reservar.
- [ ] Puedo copiar el código para usarlo en caja.
- [ ] Se distingue lo que vence pronto.

### UI

| Frame | Contenido |
|---|---|
| `29_Cupones` | Disponibles y Usados; tarjetas con valor, lugar, vigencia y Usar. |
| `47_DetCupon` | Valor, código con Copiar, Cómo usarlo, Condiciones, Reservar con este cupón, Ver lugar. |
| `DetCupon · *` | Variantes: $50 en tu consumo, 15% en tu cuenta, Postre de cortesía. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto. |
| Vacío | Sin cupones: mensaje e invitación a explorar (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Lista y detalle. |

### Validaciones

- La vigencia se calcula con reloj inyectado y fechas UTC.

### Dudas abiertas

- **D22** Barra inferior inconsistente (`specs/README.md`)
- **D36** Detalles de texto en la maqueta (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Perfil` → `Cupones` → `DetCupon` → `Reserva`, `Detalle`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Cupones del usuario y detalle con condiciones.

## 4. Modelo de datos

`CouponEntity`.

## 5. Seguridad y privacidad

- El código es personal: fuera de los logs.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: textos de vigencia.
- UI: Copiar.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
