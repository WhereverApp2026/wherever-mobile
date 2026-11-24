# NV-25 — Compartir, grupos y chats

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 6 · Compartir, grupos y chats. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Planear con otras personas: compartir, chatear, votar y elegir punto de encuentro.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como grupo queremos decidir juntos.

- [x] Cada quien vota una vez, y gana el lugar con más votos (48_Votar; el voto se cambia hasta el cierre).
- [ ] El plan queda fijado en el chat.
- [ ] El punto medio considera el tiempo de cada quien.

**HU propuesta.** Como usuario quiero compartir un plan.

- [ ] Elijo Solo ver o Puede editar.
- [ ] El enlace abre el plan en la app.

### UI

| Frame | Contenido |
|---|---|
| `42_Compartir` | Hoja: plan, Solo ver o Puede editar, Enlace, WhatsApp, Correo, Historia, Más, enlace con Copiar. |
| `43_CrearGrupo` | Nombre, tipo de plan, integrantes, Crear grupo. |
| `44_MisGrupos` | Pestañas Planes, Grupos, Reservas; grupos con estado; Nuevo grupo; Mensajes directos. |
| `45_GrupoChat` | Plan fijado con Ver plan y Reservar, mensajes, votación en línea, escribir. |
| `GrupoChat · *` | Variantes: Familia Reséndiz y Nosotros. |
| `46_Grupo` | Detalle: punto de encuentro con mapa, tiempos y costos por persona, La Vaca virtual, Ir a la votación. |
| `47_PuntoMedio` | Mapa con punto medio arrastrable, tiempos, opciones, Centrar, Usar este punto. |
| `48_Votar` | Lugares con votos, tiempo restante, Enviar mi voto. |
| `ChatAmigo` | Chat 1 a 1, Invitar a un plan, Compartir lugar. |
| `ChatAmigo · *` | Variante con la invitación enviada y "Jorge se unió al plan". |
| `InvitarPlan` | Hoja: elegir plan o crear uno, Enviar invitación. |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueleto de mensajes; envío optimista con indicador. |
| Vacío | Sin grupos: Nuevo grupo. Chat sin mensajes: invitación a escribir. |
| Error | Mensaje no enviado: marca con Reintentar. |
| Sin red | Mensajes de Room; los nuevos se encolan. |
| Éxito | Mensaje entregado; voto registrado. |

### Validaciones

- Nombre de grupo obligatorio.
- Un voto por persona; se puede cambiar hasta el cierre.
- La votación cierra a la hora indicada; reloj inyectado.

### Dudas abiertas

- **D25** Mapas y navegación (`specs/README.md`)
- **D28** Tiempo real en chats (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- `Planes` → `MisGrupos` → `GrupoChat` → `Grupo`, `Votar`, `Plan`, `Reserva`, `Vaca`.
- `Grupo` → `PuntoMedio`.
- `PerfilAmigo` → `ChatAmigo` → `InvitarPlan`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Endpoints (cotejo del 2026-11-21 con `wherever-backend` `origin/main`, `docs/endpoints.md`)

El bloqueo "Pendiente de backend" ya no aplica para grupos, chat y votaciones (backend nv-37, nv-39, nv-57, nv-69).

| Necesidad | Endpoint | En la app |
|---|---|---|
| Mis grupos con cursor y último mensaje | `GET /grupos?cursor=&limite=` | sí (44_MisGrupos, 2026-11-21) |
| Detalle del grupo | `GET /grupos/{id}` | sí (cabecera del chat) |
| Aceptar o rechazar la invitación | `PATCH /grupos/{id}/integrantes/yo` `{estado}` | sí (44_MisGrupos) |
| Mensajes: página, más viejos y nuevos | `GET /grupos/{id}/mensajes?cursor=&despues_de=&limite=` | sí (45_GrupoChat) |
| Enviar sin duplicar | `POST /grupos/{id}/mensajes` `{texto, id_cliente}` | sí: envío optimista; el reintento usa el mismo `id_cliente` |
| Plan fijado | `GET /planes/{id}` (los integrantes lo leen) | sí |
| Crear grupo desde un plan | `POST /grupos` `{plan_id, invitados}` y `PATCH /grupos/{id}` (nombre, `If-Match`) | sí (43_CrearGrupo, 2026-11-23) |
| Invitar, salir y sacar | `POST /grupos/{id}/integrantes`, `DELETE /grupos/{id}/integrantes/yo` y `/{usuario_id}` | sí (46_Grupo, 43 en modo invitar e InvitarPlan) |
| Votaciones | `GET /grupos/{id}/votaciones`, `PUT /votaciones/{id}/voto`, `POST /votaciones/{id}/cerrar` | sí (48_Votar, 2026-11-23); crear una votación (`POST /grupos/{id}/votaciones`) queda para el siguiente PR |
| Punto de encuentro | `PATCH /grupos/{id}/integrantes/yo` `{ubicacion, compartir_ubicacion}`, `GET …/punto-encuentro/sugerido`, `PUT /grupos/{id}/punto-encuentro` `{negocio_id, hora}`, `GET …/punto-encuentro/traslados` (backend nv-69) | sí (47_PuntoMedio, 2026-11-24) |
| Compartir plan | `POST /planes/{id}/compartir` | ya en nv-24 |
| Chat 1 a 1 (ChatAmigo) | — | **no existe en el backend**: no se dibuja (constitución §2.7) |

### Decisiones (2026-11-21)

- **Tiempo real (D28):** sin WebSocket en el backend; el chat pregunta por lo nuevo cada 5 s con `despues_de` **solo mientras está a la vista** (`LifecycleResumeEffect`).
- **Nuevo grupo:** un grupo nace de un plan (`plan_id` obligatorio); mientras no exista 43_CrearGrupo, "Nuevo grupo" lleva a Planes.
- **Título del grupo:** `nombre` (nv-69) o, si no tiene, los integrantes ("Ana, Luis y 2 más").
- **Invitado:** el chat es solo para confirmados (404 si no); en la lista, la tarjeta muestra Unirme / No voy en lugar de abrir el chat.

## 4. Modelo de datos

`GroupMessageEntity` (Room v5, `MIGRATION_4_5`): mensajes por grupo con estado `SENT`/`PENDING`/`FAILED`. Los grupos no se guardan (lista viva con cursor).

## 5. Seguridad y privacidad

- El enlace compartido no lleva tokens de sesión.
- Mensajes como dato personal: se borran al cerrar sesión.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: reglas de voto.
- Data: cola de mensajes.
- UI: votación.
- Security: el enlace de compartir no lleva tokens de sesión y los mensajes se borran de Room al cerrar sesión.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". Ver `plan.md`, `task.md` y `checklists/gates.md`.

## Aprobación
- [x] Revisor: Jorge — fecha: 2026-11-06
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

### Decisiones (2026-11-23)

- **46_Grupo sin mapa:** punto de encuentro, tiempos por persona y 47_PuntoMedio esperan D25 (proveedor de mapa); la pantalla muestra plan, votaciones, La vaca e integrantes. Se abre tocando el encabezado del chat.
- **InvitarPlan:** la API no busca el grupo de un plan; se recorren mis grupos (hasta 5 páginas) y, si el plan no tiene grupo, se crea con `POST /grupos`. Se entra desde el perfil de un amigo («Invitar a un plan»).
- **43_CrearGrupo:** el nombre es opcional y se pone con `PATCH` después de crear; si falla, el grupo ya existe y se renombra en 46. Hasta 11 amigos menos quienes ya están. Entrada: «Invitar amigos» en 39_Plan.
- **48_Votar:** el reloj se inyecta; una votación vencida según el teléfono ya no deja votar aunque la API aún no la cierre. Un 409 (cerrada) avisa y recarga.
- **ChatAmigo** sigue sin dibujarse: no hay chat 1 a 1 en el backend. **42_Compartir** ya existe como compartir del sistema en 39_Plan (nv-24).

### Decisiones (2026-11-24, 47_PuntoMedio)

- Con el mapa de osmdroid (D25) ya hay 47_PuntoMedio: se entra desde 46_Grupo («Punto de encuentro»). Compartir mi ubicación pide el permiso en contexto y manda solo la ubicación aproximada (`ACCESS_COARSE_LOCATION`); dura 6 h y la API nunca la devuelve. Con 2 o más personas compartiendo, la API sugiere 3 lugares abiertos cerca del punto medio; se eligen en la lista o en el mapa y «Usar este punto» los fija. Después se ve cuánto tarda cada quien (exacto para mí, rangos para los demás).
- El punto arrastrable del diseño no se hizo: la API no da el centro (privacidad, nv-69). La hora es la del punto, o la llegada a la primera parada, o las 19:00 del día del plan.

**Estado:** ✅ Aprobada (Jorge, 2026-11-06). 43, 46, 48 e InvitarPlan implementadas el 2026-11-23 (Jorge); falta probarlas contra staging.
