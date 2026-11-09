# NV-27 — Reserva y pago

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 7 · Reserva, pago y mis planes. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Reservar con anticipo y administrar la reservación.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero reservar y pagar un anticipo.

- [ ] Elijo hora y personas.
- [ ] El cupón se aplica solo.
- [ ] Si el pago falla, no hay cargo y el lugar queda apartado 10 min.
- [ ] Recibo código y QR.

**HU propuesta.** Como usuario quiero cambiar o cancelar.

- [ ] Sin costo hasta 24 h antes.
- [ ] Las horas sin cupo aparecen tachadas.
- [ ] Cancelar con más de 24 h devuelve el anticipo.

### UI

| Frame | Contenido |
|---|---|
| `22_Reserva` | Lugar con Sello, afluencia, horarios, personas, cupón aplicado, desglose, Anticipo a pagar hoy, Pagar anticipo y reservar. |
| `35_Pasarela` | Pagar anticipo: total, tarjeta, vence, CVV, nombre, Pagar $74. |
| `36_PagoError` | "No se pudo procesar el pago", lugar apartado 10 min, ayuda, Intentar con otra tarjeta, Volver a mi reservación. |
| `23_Confirmacion` | "Reservación confirmada", boleto con código y QR, aviso de check-in, Compartir con el grupo, Ver mi plan. |
| `25_MisReservas` | Próximas y Pasadas; tarjetas con estado, Ver QR o Pagar; aviso de lugar por liberarse. |
| `26_DetReserva` | QR, código, fecha, hora, personas, anticipo, Cómo llegar, Compartir, Cambiar hora, Cancelar reservación. |
| `DetReserva · *` | Variantes: Café Raíz y Tacos El Güero (pago pendiente). |
| `27_Cancelar` | Hoja: resumen, política de reembolso, motivo, Mantener reservación, Sí, cancelar. |
| `38_CambiarHora` | Hoja: día, hora disponible, política, Confirmar cambio. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Disponibilidad con indicador; pago con pantalla bloqueada para evitar doble envío. |
| Vacío | Sin reservas: invita a explorar. |
| Error | `36_PagoError`. Conflicto de cupo (409): recarga los horarios. |
| Sin red | `DetReserva` en lectura con QR guardado. Reservar y pagar, deshabilitados. |
| Éxito | `23_Confirmacion`. |

### Validaciones

- Personas ≥ 1 y ≤ cupo.
- Cuenta atrás del apartado con reloj inyectado.
- Clave de idempotencia en el pago.

### Dudas abiertas

- **D26** Pasarela de pago (`specs/README.md`)
- **D31** Qué funciona sin red (`specs/README.md`)
- **D32** Lugares con y sin reserva (`specs/README.md`)
- **D36** Detalles de texto en la maqueta (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Detalle`, `Plan`, `GrupoChat`, `DetCupon` → `Reserva` → `Pasarela` → `Confirmacion` o `PagoError`.
- `Perfil` → `MisReservas` → `DetReserva` → `Cancelar`, `CambiarHora`, `Llegar`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Disponibilidad por hora.
- Crear, cambiar y cancelar reservación.
- Intención de pago y confirmación.
- QR o código de reservación.

## 4. Modelo de datos

`ReservationEntity` con QR o código para uso sin red.

## 5. Seguridad y privacidad

- Datos de tarjeta nunca pasan por la app ni por el backend propio, y nunca en logs.
- Bloquear capturas en la pantalla de pago (`FLAG_SECURE`).
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: desglose y cuenta atrás.
- Data: 409 con MockWebServer.
- Security: la tarjeta no aparece en logs.
- UI: confirmar cancelación.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** ✅ Aprobada (Jorge, 2026-11-06).
