# 031 — Negocio: panel, reservas y check-in

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 11 · Negocio. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Operar el día: ver el negocio, atender reservaciones y confirmar llegadas.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como negocio quiero confirmar llegadas para validar reseñas.

- [ ] Busco por código o escaneo el QR.
- [ ] Confirmar habilita la reseña verificada.
- [ ] Puedo reprogramar.

**HU propuesta.** Como negocio quiero medir cómo me va.

- [ ] Filtro por periodo.
- [ ] Veo el origen de mis clientes.

### UI

| Frame | Contenido |
|---|---|
| `48_NegPanel` | Portada, Sello, Reservas hoy, Anticipos, Ocupación, gráfica semanal, Próximas hoy, barra de negocio. |
| `49_NegReservas` | Hoy, Próximas, Pasadas; filas con hora, cliente, personas y estado. |
| `50_NegCheckin` | Búsqueda por código, QR, cliente, detalle, Confirmar llegada, Reprogramar. |
| `NegCheckin · *` | Variantes por cliente y estado de pago. |
| `56_NegStats` | Semana, Mes, Año; reservas, anticipos, asistencia, cupones; por día; de dónde llegan. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueletos de tarjetas. |
| Vacío | Sin reservas hoy: mensaje (sin frame). |
| Error | Código inválido: "No encontramos esa reservación". `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lista en caché; confirmar llegada encolado solo si se valida el riesgo. |
| Éxito | Estado pasa a Llegó. |

### Validaciones

- Código con formato `WH-####`.
- Confirmar una sola vez (idempotente).

### Dudas abiertas

- **D27** Cuenta de negocio (`specs/README.md`)
- **D37** Check-in con pago pendiente (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra de negocio: `NegPanel`, `NegReservas`, `NegCupones`, `NegResenas`, `NegPerfil`.
- `NegReservas` → `NegCheckin`. `NegPanel` → `NegStats`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `POST` | `/api/v1/usuarios/yo/modo` | JWT | modo: explorar | negocio | 200: modo_actual | 400, 401, 403, 422 |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Panel y métricas.
- Reservaciones del negocio.
- Check-in por código y QR.
- Reprogramar.

## 4. Modelo de datos

`BusinessReservationEntity` separada del lado consumidor.

## 5. Seguridad y privacidad

- Rol comerciante validado en el servidor (403).
- Datos del cliente mínimos.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: flujo de check-in.
- UI: barra de negocio.
- Data: lista por pestaña.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
