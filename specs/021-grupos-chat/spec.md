# 021 — Compartir, grupos y chats

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/000-arquitectura/spec.md`. Mapa: `specs/001-flujo-general/spec.md`.
> Sección de la maqueta: 6 · Compartir, grupos y chats. Posterior al Sprint 1. La implementación queda sujeta a la etapa vigente (`nv-00…nv-03` sin pantallas del producto).

## 1. Objetivo

Planear con otras personas: compartir, chatear, votar y elegir punto de encuentro.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como grupo queremos decidir juntos.

- [ ] Cada quien vota una vez, y gana el lugar con más votos.
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

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Grupos e integrantes.
- Mensajes y tiempo real.
- Votaciones.
- Punto de encuentro con tiempos y costos.
- Enlaces compartidos con permisos.
- Chat 1 a 1 e invitaciones.

## 4. Modelo de datos

`GroupEntity`, `MessageEntity`, `VoteEntity`.

## 5. Seguridad y privacidad

- El enlace compartido no lleva tokens de sesión.
- Mensajes como dato personal: se borran al cerrar sesión.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: reglas de voto.
- Data: cola de mensajes.
- UI: votación.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador.
