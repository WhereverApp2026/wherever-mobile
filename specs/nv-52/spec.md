# NV-52 — Avisos: centro de notificaciones, campana en Inicio y horario silencioso

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Implementa `specs/nv-18` con la API ya publicada (backend nv-40, A6). Rama `feat/notificaciones`, desde `main`.

## 1. Objetivo

Que la persona vea en un solo lugar los avisos que no vio (US-147) y elija un horario en que no la molesten (US-148), contra la API real.

## 2. Alcance / No-alcance

### Alcance
- **Avisos (18_Notif):** lista paginada por cursor (`Ver más`), agrupada en Hoy, Esta semana y Antes; filtros Todo, Social y Planes; punto en los no leídos; "Marcar leídas"; borrar cada aviso. Tocar uno lo marca leído (sin esperar a la API; si falla, vuelve a no leído) y abre su destino.
- **Destinos:** los mismos que el push (`PushDestination.route()`). Invitación a plan con `plan_id` → `PlanRoute`; solicitud o amistad aceptada y votaciones → se quedan en Avisos hasta que existan Perfil de amigo y Votar. Un push con destino inválido abre Avisos (antes abría Inicio).
- **Inicio:** campana con el número de no leídos (`GET /notificaciones/no-leidas`), se refresca al volver a Inicio; "99+" arriba de 99.
- **Ajustes → Horario silencioso:** Apagado, 22:00 a 08:00, 23:00 a 07:00 o 00:00 a 09:00 (`PATCH /usuarios/yo/preferencias {horario_silencioso}`); se guarda en Room con la respuesta.
- Versión `versionCode 7`, `versionName 0.5.0`.

### No-alcance
- Acciones rápidas en la fila (Aceptar amistad, Reseñar): llegan con Amigos y Reseñas.
- Lectura sin conexión desde Room: los avisos se piden cada vez; sin red se ve el error con Reintentar.

## 3. Contratos (API y navegación)

wherever-backend `docs/endpoints.md` (`origin/main`, nv-40 y nv-05):

| Método | Ruta | Uso |
|---|---|---|
| GET | `/notificaciones?cursor=&limite=20` | Lista; `datos` solo trae ids |
| GET | `/notificaciones/no-leidas` | `{total}` para la campana |
| PATCH | `/notificaciones/{id}` `{leida: true}` | Al tocar |
| POST | `/notificaciones/leer-todas` | Marcar leídas |
| DELETE | `/notificaciones/{id}` | Borrar |
| PATCH | `/usuarios/yo/preferencias` `{horario_silencioso{desde, hasta}}` | "HH:MM"; la API no deja borrarlo: desde = hasta lo apaga (`inQuietHours`) |

Ruta nueva en `docs/routes.md`: `NotificationsRoute`.

## 4. Modelo de datos

Sin cambios en Room. `feature/notifications` (`NotificationsRepository`, `NotificationsApi`); `PreferencesPatch.quietHours` y `QuietHours.OFF`. `UiState`: `NotificationsUiState`; la campana usa `UnreadBadgeViewModel`.

## 5. Seguridad y privacidad

- Los avisos solo llevan ids; el texto sale de `strings.xml` por tipo. Un tipo desconocido muestra un texto genérico.
- Los ids para navegar se validan con `PushDestination.from` (longitud y caracteres), igual que los del push.
- Sin permisos nuevos y sin cambios en la cadena de red.

## 6. Tests / Gates

- **Unit:** `test/.../feature/notifications/ui/NotificationsViewModelTest.kt` (11): paginación sin repetidos, error y reintento, filtros, grupos por día, tocar y abrir el plan, rollback de leída, una leída no llama, marcar todas con rollback, borrar con rollback, rutas del push y contador de la campana.
- **Data:** `test/.../integration/NotificationsIntegrationTest.kt` (5) con la cadena real: cursor y límite, tipos conocidos y desconocidos, solo ids de texto, no leídas, PATCH, leer-todas, DELETE, 404 ajeno y horario silencioso en el DTO.
- **UI:** sin pruebas instrumentadas nuevas.
- **Security:** `SourceSecurityTest` y `ArchitectureTest` siguen verdes.

## 7. Plan de entrega

Commit directo a `main` a pedido de Jorge (2026-11-19). Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [ ] Revisor: Jorge — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
