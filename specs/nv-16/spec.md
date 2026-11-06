# NV-16 — Sin conexión

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 10 · Perfil, ajustes y estados. **Sprint 1.** La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Mostrar un estado claro sin red y dar acceso a lo guardado (offline primero).

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**US-001.** Como usuario sin red quiero saberlo y poder reintentar.

- [ ] Pantalla Sin conexión con Reintentar.

**HU propuesta.** Como usuario quiero ver mis planes y reservas sin internet.

- [ ] Lista lo guardado en Room.
- [ ] Cada elemento abre su detalle en modo lectura.

### UI

| Frame | Contenido |
|---|---|
| `89_SinConexion` | Wi-Fi tachado, "Sin conexión", explicación, Disponible sin internet (plan y reserva con QR guardado), Reintentar. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Indicador en Reintentar. |
| Vacío | Sin datos guardados: se oculta la sección. |
| Error | Reintentar falla: sigue en la pantalla con aviso. |
| Sin red | Es esta pantalla. |
| Éxito | Vuelve la red: navega al destino pendiente. |

### Validaciones

- Detecta red con `ConnectivityManager`, no solo por el fallo de una llamada.
- Con staging apagado (`503` o tiempo agotado) se distingue de falta de red.

### Dudas abiertas

- **D17** Backend fuera de horario (`specs/README.md`)
- **D31** Qué funciona sin red (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Splash` → `SinConexion` → destino pendiente.
- → `Plan` (`specs/020`), `DetReserva` (`specs/023`).

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `GET` | `/health` | no | — | 200: estado, version | — |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/nv-04 §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

## 4. Modelo de datos

Lee entidades de plan y reserva cuando existan.

## 5. Seguridad y privacidad

- El QR guardado es dato personal: vive en almacenamiento privado, y se borra al cerrar sesión.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: reintento con monitor de red falso inyectado.
- UI: lista oculta sin datos.
- Data: sin red, la pantalla lee de Room lo ya guardado y `GET /health` reintenta con el monitor inyectado.
- Security: el QR guardado vive en almacenamiento privado y se borra al cerrar sesión (verificación sobre el archivo en disco).
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas. Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
