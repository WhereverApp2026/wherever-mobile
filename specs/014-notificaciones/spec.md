# 014 — Notificaciones

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 2 · Inicio, feed y notificaciones. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Avisar de lo social y de lo operativo, dentro y fuera de la app.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero ver mis avisos en un solo lugar.

- [ ] Filtra por tipo.
- [ ] Marca todas como leídas.
- [ ] Cada fila abre su destino y permite su acción rápida.

**HU propuesta.** Como usuario quiero avisos push útiles que respeten mis preferencias.

- [ ] Respeta `preferencias.notificaciones` y `horario_silencioso`.
- [ ] Sin permiso, la app funciona y no hay push.

### UI

| Frame | Contenido |
|---|---|
| `18_Notif` | Filtros Todo, Social, Reservas; Marcar leídas; filas con avatar, punto de no leída y acciones Aceptar o Reseñar. |
| `Notif · solicitud aceptada` | Variante: la solicitud pasa a "ya es tu amigo". |
| `87_PushBloqueo` | Notificaciones en pantalla de bloqueo: reservación, voto y reseña. |
| `88_PushBanner` | Aviso en la app sobre `39_Plan`: Es hora de salir, Cómo llegar, Más tarde. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto de filas. |
| Vacío | "Aún no tienes avisos" (sin frame). |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Lista con cursor. |

### Validaciones

- La acción Aceptar es idempotente.
- Un push con destino inválido abre `Notificaciones`.

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Inicio` o `Perfil` → `Notificaciones`.
- → `PostDetalle`, `PerfilAmigo`, `ResCom`, `Votar`, `PlanAmigo`, `DetCupon`.
- Push → destino, con Login previo si la sesión venció.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

| Método | Ruta | Auth | Envía | Recibe | Errores |
|---|---|---|---|---|---|
| `PATCH` | `/api/v1/usuarios/yo/preferencias` | JWT | movilidad_default, tema, unidades, horario_silencioso{desde,hasta}, notificaciones{reservas,cupones,grupos,resenas,promociones} (parcial) | 200: preferencias completas | 400, 401, 422 (notificaciones.reservas:false) |

Errores con forma `{codigo, mensaje, campos[], traza_id}`, mapeados a `AppError` (`specs/000-arquitectura §3.3`). Detalle en `docs/contexto_api_sprint1.md`.

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Lista de notificaciones paginada y marcar leídas.
- Registro del token FCM del dispositivo.

## 4. Modelo de datos

`NotificationEntity` con estado de leída.

## 5. Seguridad y privacidad

- Sin datos sensibles en el texto del push (el código de reservación se muestra en bloqueo: revisar).
- `google-services.json` fuera de Git.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: filtros.
- Data: marcar leídas.
- UI: acción Aceptar.
- Security: el payload del push no lleva datos sensibles y `google-services.json` no está en el repositorio.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
