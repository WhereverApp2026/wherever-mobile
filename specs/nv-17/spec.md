# NV-17 — Inicio y feed

> Rector: `.specify/memory/constitution.md`. Plantilla: `.specify/templates/spec-template.md`. Arquitectura: `specs/nv-04/spec.md`. Mapa: `specs/nv-05/spec.md`.
> Sección de la maqueta: 2 · Inicio, feed y notificaciones. Posterior al Sprint 1, salvo el Inicio provisional que ya existe; ver "Implementado".

## 1. Objetivo

Dar la entrada diaria a la app: sugerencias cerca, actividad de amigos y acceso a crear un plan.

## 2. Alcance / No-alcance

### Historias y criterios de aceptación

**HU propuesta.** Como usuario quiero ver qué hacer hoy para decidir rápido.

- [ ] Muestra la ciudad actual y lugares cercanos.
- [ ] El buscador abre `Buscar`; el botón IA abre `IAPalabra`.
- [ ] Los lugares abren su detalle.

**HU propuesta.** Como usuario quiero ver y sumarme a los planes de mis amigos.

- [ ] Elijo a qué paradas voy.
- [ ] El autor recibe aviso y entro al chat del plan.
- [ ] Hacer mi versión copia el plan como borrador.

### UI

| Frame | Contenido |
|---|---|
| `10_Inicio` | Foto de encabezado, ciudad, campana, "¿Qué hacemos hoy, Osmar?", buscador con botón IA, pestañas Cerca de ti y Amigos, carrusel de lugares, Planes de tus amigos, barra inferior. |
| `11_InicioAmigos` | Encabezado, historias, tarjeta Falta tu voto, publicación de plan con paradas, Me apunto, Hacer mi versión, Ver más publicaciones. |
| `12_InicioMas` | Publicación de visita con reacciones, Planear aquí, Personas que quizá conozcas. |
| `InicioMas · solicitud enviada` | Variante: los botones Agregar pasan a Pendiente. |
| `13_Reacciones` | Hoja: pestañas Todos, Me gusta, Van; lista de personas con Va al plan, corazón o Agregar. |
| `Reacciones · solicitud enviada` | Variante: Agregar pasa a Pendiente. |
| `14_MeApunto` | Hoja: plan, ¿A qué paradas vas? con casillas, aviso, Apuntarme a N paradas, Ahora no. |
| `15_Crear` | Hoja del botón +: Armar con IA, Crear a mano, Planear con amigos, Compartir un momento, Escribir una reseña. |
| `16_Historia` | Historia a pantalla completa: progreso, autor, lugar verificado, título, Hacer mi versión, responder, corazón, enviar. |
| `Historia · *` | Variantes con otro autor y lugar (Alexander, Mariana, Paola). |
| `17_PostDetalle` | Publicación: autor, foto, estrellas, Sello, texto, reacciones, comentarios, escribir comentario. |
| `29_PlanAmigo` | Plan de un amigo: portada, autor, paradas con horarios y traslados, Me apunto, Hacer mi versión. |
| `PlanAmigo · *` | Variante "Domingo de jardín". |

Colores y tipografía desde `core/designsystem`; textos en `strings.xml`; áreas táctiles de 48 dp.

### Estados

| Estado | Comportamiento |
|---|---|
| Cargando | Esqueletos por sección, encima de lo que haya en Room. |
| Vacío | Sin amigos: invita a agregarlos. Sin lugares: "Aún no hay lugares aquí". |
| Error | `AppError` con mensaje y Reintentar; conserva los datos en pantalla. |
| Sin red | Lectura desde Room con aviso; acciones que escriben, deshabilitadas o encoladas. |
| Éxito | Secciones con datos; paginación por cursor en el feed. |

### Validaciones

- Me apunto exige al menos una parada.
- Comentario no vacío, con límite por definir.

### Dudas abiertas

- **D22** Barra inferior inconsistente (`specs/README.md`)
- **D24** Un solo botón naranja por pantalla (`specs/README.md`)

### No-alcance

- Lo marcado como "Pendiente de backend" no se implementa con datos simulados (constitución §2.7).

## 3. Contratos (API y navegación)

### Navegación

- Barra → `Inicio`, `Explorar`, hoja `Crear`, `Planes`, `Perfil`.
- `Inicio` → `Notificaciones`, `Buscar`, `Detalle`, `PlanAmigo`, `PerfilAmigo`, `AmigosLista`, `Votar`, `Historia`, `PostDetalle`.
- Hoja `Crear` → `IAPalabra`, `CrearPlan`, `CrearGrupo`, `CrearPub`, `ResPend`.

Nombres de ruta propuestos. Se registran en `docs/routes.md` al implementar la spec.

### Endpoints

### Pendiente de backend

Estos datos no tienen endpoint publicado. Lista de necesidades, no un contrato.

- Lugares cercanos por ciudad o coordenadas.
- Feed de amigos paginado con publicaciones, reacciones y comentarios.
- Historias activas con lectura y respuesta.
- Sugerencias de personas.
- Apuntarse a un plan por paradas.
- Copiar un plan (Hacer mi versión).

## 4. Modelo de datos

Entidades de feed y lugares en Room, con caché y cursor.

## 5. Seguridad y privacidad

- Respeta la visibilidad elegida por cada autor; la aplica el servidor.
- Sin tokens ni datos personales en logs; solo HTTPS (constitución §6).

## 6. Tests / Gates

- Unit: `HomeViewModel` por estado.
- Data: feed paginado con Room y MockWebServer.
- UI: pestañas y hoja de creación.
- Security: los logs del feed no incluyen el access token ni nombres o correos de otros usuarios.
- Estados cargando, vacío, error y sin red cubiertos. Cobertura ≥ 45 % (`CHECKLIST_PR.md`).

## 7. Plan de entrega

Slice propio: `spec→datos→dominio→ViewModel→UI→tests→gates→tag`. Requiere `nv-01` y `nv-02` ejecutadas, y los endpoints de "Pendiente de backend". `plan.md` y `task.md` se generan al tomar la spec.

## Implementado (2026-11-04)

**Hecho**

- Inicio provisional para cerrar el flujo de cuenta: encabezado de `10_Inicio` con saludo y ciudad, leídos del perfil en Room, y un aviso de que falta el contenido.
- Barra provisional con Inicio y Perfil (D22).

**Difiere de lo especificado**

- El resto de esta spec no está implementado: feed, sugerencias, amigos, reacciones y crear plan no tienen endpoint publicado.

**Archivos** (bajo `app/src/main/java/com/wherever/mobile/`)

- `feature/home/ui/HomeScreen.kt`
- `feature/home/ui/HomeViewModel.kt`

**Límites de la verificación**

- Sin prueba unitaria propia: solo lee el perfil de Room y lo muestra. Probado el 2026-11-04 en un teléfono contra staging.

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución

**Estado:** 🚧 Borrador; solo el Inicio provisional está implementado.
