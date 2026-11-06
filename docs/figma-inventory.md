# Inventario visual de Figma — Wherever Móvil

> Fuente: archivo Figma `9IjBujkogqaUPAkDbluclQ`, canvas `96:130` (“Boceto 2”), exportado el 2026-11-02 en PNG 2x (780×1688).
> Uso: este documento inventaria la maqueta; las specs de `nv-05+` deciden rutas, estados y contratos. No guarda tokens ni datos personales.
> Estado: inventario cerrado; 158 pantallas/estados y 36 etiquetas de flujo revisadas.

## 1. Resumen

| Módulo Figma | Feature | Frames |
|---|---|---:|
| 1 · Acceso y registro | `feature/auth` | 11 |
| 2 · Inicio, feed y notificaciones | `feature/feed` | 13 |
| 3 · Amigos | `feature/friends` | 18 |
| 4 · Explorar y lugares | `feature/explore` | 10 |
| 5 · Crear plan con IA | `feature/plans` | 13 |
| 6 · Compartir, grupos y chats | `feature/social` | 14 |
| 7 · Reserva, pago y mis planes | `feature/booking` | 17 |
| 8 · El día del plan | `feature/dayof` | 5 |
| 9 · Recuerdos y reseñas | `feature/memories` | 21 |
| 10 · Perfil, ajustes y estados | `feature/profile` | 10 |
| 11 · Negocio | `feature/business` | 26 |
| **Total** | | **158** |

- Los 158 frames no son 158 destinos: incluyen estados, variantes, hojas modales y pantallas de sistema. La consolidación se hace al abrir cada spec.
- La numeración se reutiliza por módulo; no es un ID global. Ejemplo: `22_AmigosLista` y `22_Reserva` pertenecen a módulos distintos.
- Los nombres con `·` son variantes de estado del frame base; se agrupan en este inventario.
- Las 36 etiquetas (`label · ...`) marcan subflujos y no se cuentan como pantallas.
- La navegación principal tiene cinco destinos: **Inicio**, **Explorar**, **+**, **Planes** y **Perfil**. Crear plan, notificaciones y chat se abren desde acciones contextuales.

## 2. Sistema visual observado

### 2.1 Layout y navegación
- Lienzo lógico de 390×844; status bar de 9:41; contenido con safe areas.
- Barra inferior fija en el shell autenticado; botón central naranja para crear plan; se oculta en onboarding, modales y flujos de foco.
- Cabeceras con flecha atrás, título y acciones; muchas pantallas usan hoja inferior o modal sobre el contenido.
- Patrón recurrente: título grande, subtítulo, tarjetas blancas, CTA naranja fijo al pie y acción secundaria outline.
- Búsqueda global en Inicio; búsqueda local en Amigos, Explorar y filtros.

### 2.2 Color, tipografía y formas
- Muestras tomadas de los PNG, no tokens definitivos:
  - Azul primario: `#2351DF` (gradiente hacia azul profundo en bienvenida).
  - Naranja de acción: `#FF7B3F`.
  - Fondo: `#F4F7FC`.
  - Azul profundo de texto: `#142C59`.
  - Blanco para tarjetas; verde para éxito; rojo para error; dorado para Sello de Excelencia.
- Tipografía compatible con Sora (títulos) y DM Sans (cuerpo) de la constitución; validar licencias y jerarquía final en `core/designsystem`.
- Bordes redondeados (16–24 dp), botones tipo píldora, chips, segmentos, tarjetas con sombra suave, iconos en círculos de color.
- Mapas con marcadores numerados, rutas azules y etiquetas de tiempo/distancia.
- QR para reservas y para el código de amigo; estados de escaneo y copia.

### 2.3 Componentes y estados
- Botón primario naranja, secundario outline azul, terciario texto; botón de peligro rojo.
- Tabs y segmentos: Amigos/Solicitudes, Próximos/Borradores/Pasados, Disponibles/Usados, Todos/Social/Reservas.
- Tarjetas de lugar, plan, reserva, cupón, reseña, grupo y publicación.
- Hojas modales para reacciones, crear plan, compartir, cancelar, cambiar hora, invitar y agregar gasto.
- Estados vistos: vacío, pendiente, confirmado, pagado, publicado, error de pago, sin conexión, sin resultados, solicitud enviada, check-in confirmado.
- Indicadores: barras de votos, barras de ocupación por hora, rating con estrellas, badges, avatares apilados, contadores.
- Formatos: fechas “Vie 17 oct”, horas “17:00”, precios MXN “$420”, distancias “8 min a pie”, “A 350 m”.

### 2.4 Riesgos visuales
- Texto secundario pequeño y de bajo contraste en varias tarjetas; validar accesibilidad y tamaños mínimos.
- Se repiten pantallas equivalentes con números distintos (`13_Mapa`/`39_Plan`, `22_Reserva`/`26_DetReserva`); no crear rutas duplicadas.
- El flujo de permisos aparece dos veces (`06_PermisoSys` y `08_Permisos`); consolidar en una sola secuencia.
- Solo hay un estado global de “Sin conexión”; cada feature necesita sus estados de carga/vacío/error/sin red.
- La “Vaca virtual” calcula deudas, pero la UI dice que Wherever no mueve dinero; el contrato debe aclarar si el ledger es local o servidor.

## 3. Flujos principales

1. **Entrada y cuenta**: bienvenida → login o registro en cuatro pasos → gustos → permiso de ubicación → amigos → permisos → feed. Recuperación de contraseña y contraseña nueva son flujos laterales.
2. **Shell principal**: Inicio con feed, búsqueda, “Cerca de ti” y planes de amigos; Explorar; crear plan desde el botón central; Planes; Perfil. Notificaciones e historias se abren desde Inicio.
3. **Plan**: elegir intención (IA, manual, con amigos, compartir, reseñar) → ajustar preferencias → armar paradas → detalle del plan → invitar/compartir → unirse o reservar. El plan incluye mapa, horarios, presupuesto, participantes y comentarios.
4. **Reserva**: lugar → reserva con hora/personas/cupón/anticipo → pago → confirmación con QR → mis planes/reservas → detalle, cambio de hora o cancelación. El pago fallido tiene reintento.
5. **Día del plan**: ruta del plan → recorrido completo → llegada/navegación → check-in → postal y siguiente parada.
6. **Después de la visita**: reseña → publicación en feed → recuerdos/álbum → postal → mis reseñas. La reseña también se gestiona desde el perfil del lugar y del negocio.
7. **Social**: grupos → chat con plan, votación y reserva → punto de encuentro → vaca y gastos. Chat 1 a 1 para invitar a un plan.
8. **Perfil**: perfil → editar → insignias → datos personales → ajustes/ayuda → estados de push y sin conexión → eliminación de cuenta.
9. **Negocio**: alta en cinco pasos (cuenta, negocio, ubicación, operación, fotos/verificación) → solicitud enviada → panel → reservas/check-in → reseñas → cupones → horarios → estadísticas → cobros → perfil → galería/banco/fiscal.

## 4. Inventario por módulo

### 4.1 Acceso y registro — `feature/auth` (11)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `01_Bienvenida` | pantalla | Propuesta de valor; crear cuenta o iniciar sesión | — | `auth/welcome` |
| `03_Login` | pantalla | Email/contraseña, Google y recuperación | — | `auth/login` |
| `03_Recuperar` | pantalla | Solicitar enlace de recuperación | — | `auth/recover` |
| `04_RevisaCorreo` | pantalla | Confirmar envío de correo y reenviar | — | `auth/recover/sent` |
| `05_Registro` | pantalla | Crear cuenta; nombre, correo, contraseña y términos | — | `auth/register` |
| `06_Gustos` | pantalla | Preferencias: intereses, ambiente, presupuesto y compañía | — | `auth/register/preferences` |
| `06_PermisoSys` | hoja | Racional de ubicación antes del permiso del sistema | permitir / ahora no | `auth/register/location` |
| `07_EncuentraAmigos` | pantalla | Contactos/Facebook y sugerencias para seguir | `EncuentraAmigos · solicitud enviada` | `auth/register/friends` |
| `08_Permisos` | pantalla | Ubicación, notificaciones y cámara; todas opcionales | permitir / ahora no | `auth/register/permissions` |
| `NuevaContrasena` | pantalla | Nueva contraseña y confirmación con fortaleza | — | `auth/recover/reset?code={code}` |

`nv-05` implementa email/contraseña y onboarding; Google, contactos y Facebook se difieren y la cámara solo se explica, no se solicita.

### 4.2 Inicio, feed y notificaciones — `feature/feed` (13)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `10_Inicio` | pantalla | Feed principal; saludo, búsqueda, cerca de ti y planes de amigos | — | `feed/home` |
| `11_InicioAmigos` | pantalla | Pestaña Amigos; historias, plan de amigos y votación | — | `feed/friends` |
| `12_InicioMas` | pantalla | Pestaña Más; perfil, personas sugeridas y agregar | `InicioMas · solicitud enviada` | `feed/more` |
| `13_Reacciones` | hoja | Reacciones de una publicación; filtrar y agregar amigos | `Reacciones · solicitud enviada` | `feed/post/reactions` |
| `16_Historia` | pantalla | Historia a pantalla completa con lugar y CTA | `Historia · Alexander/Mariana/Paola` | `feed/story/{id}` |
| `17_PostDetalle` | pantalla | Detalle de publicación; lugar, rating, comentarios y reacciones | — | `feed/post/{id}` |
| `18_Notif` | pantalla | Notificaciones con tabs Todo/Social/Reservas | `Notif · solicitud aceptada` | `notifications` |

### 4.3 Amigos — `feature/friends` (18)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `19_Buscar` | pantalla | Búsqueda global con tabs Lugares/Personas/Planes | — | `friends/search` |
| `20_BuscarPersonas` | pantalla | Búsqueda de personas y filtros; agregar o solicitar | `BuscarPersonas · solicitud enviada` | `friends/search/people` |
| `21_SinResultados` | pantalla | Vacío de búsqueda con acciones sugeridas | — | `friends/search/empty` |
| `22_AmigosLista` | pantalla | Lista de amigos, búsqueda y acceso a grupos | — | `friends` |
| `23_AmigosSolic` | pantalla | Solicitudes recibidas y enviadas; aceptar, rechazar, cancelar | — | `friends/requests` |
| `24_AgregarAmigo` | pantalla | QR, escaneo, contactos y enlace; sugerencias | `AgregarAmigo · solicitud enviada` | `friends/add` |
| `25_MiCodigo` | pantalla | QR personal para agregar amigos | — | `friends/qr` |
| `26_PerfilAmigo` | pantalla | Perfil de amigo; estadísticas, publicaciones, planes y reseñas | `PerfilAmigo · Alexander/Diego/Luis/Mariana/Paola/Sofía` | `friends/{id}` |
| `27_AmigoOpc` | hoja | Opciones: silenciar, invitar, eliminar o bloquear | — | `friends/{id}/options` |
| `28_PrivSocial` | pantalla | Privacidad social y visibilidad de contenido | — | `friends/privacy` |

### 4.4 Explorar y lugares — `feature/explore` (10)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `30_Explorar` | pantalla | Lista de lugares con chips, rating y acceso al mapa | — | `explore` |
| `31_Filtros` | hoja | Distancia, tipo, precio, abierto, Sello y cupón | — | `explore/filters` |
| `32_ExpMapa` | pantalla | Mapa con tarjeta de lugar; detalle o agregar a plan | — | `explore/map` |
| `33_Detalle` | pantalla | Detalle de lugar; fotos, rating, Sello, ocupación, amigos y promo | `Detalle · Fonda Doña Meche/Jardín del Cerro/Mercado de Tula` | `places/{id}` |
| `34_Sello` | pantalla | Explicación y criterios del Sello de Excelencia | — | `explore/badge` |
| `35_Favoritos` | pantalla | Lugares guardados | — | `favorites` |
| `36_FavVacio` | pantalla | Vacío de favoritos con CTA a explorar | — | `favorites/empty` |

### 4.5 Crear plan con IA — `feature/plans` (13)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `15_Crear` | hoja | Elegir intención: IA, manual, con amigos, compartir o reseñar | — | `plans/create` |
| `CrearPlan` | pantalla | Formulario manual: nombre, compañía, fecha, presupuesto y paradas | — | `plans/create/manual` |
| `IAPalabra` | pantalla | Prompt de IA con ejemplos y generación | — | `plans/create/ai` |
| `17_AjustaPlan` | pantalla | Ajustar quién, día, hora, personas, paradas, presupuesto y transporte | — | `plans/create/adjust` |
| `14_MeApunto` | hoja | Unirse a un plan; paradas, precio y confirmación | — | `plans/{id}/join` |
| `39_Plan` | pantalla | Detalle del plan; mapa, paradas, participantes y comentarios | `Plan · Paseo familiar` | `plans/{id}` |
| `40_Cambiar` | hoja | Cambiar una parada por alternativas | — | `plans/{id}/stops/{stopId}` |
| `41_AgregarParada` | pantalla | Agregar parada por sugerencia o búsqueda | — | `plans/{id}/stops/add` |
| `42_Compartir` | hoja | Compartir por enlace, WhatsApp, historias o copiar | — | `plans/{id}/share` |
| `InvitarPlan` | hoja | Invitar a un amigo a un plan existente | — | `plans/{id}/invite` |
| `29_PlanAmigo` | pantalla | Plan de un amigo; unirse o hacer una versión | `PlanAmigo · Domingo de jardín` | `plans/{id}` |

### 4.6 Compartir, grupos y chats — `feature/social` (14)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `28_Vaca` | pantalla | Vaca virtual; total, por persona, quién pagó y deudas | `Vaca · Mariana pagó`, `Vaca · todos pagaron` | `plans/{id}/split` |
| `45_AgregarGasto` | pantalla | Agregar gasto y repartir entre participantes | — | `plans/{id}/split/expense` |
| `43_CrearGrupo` | pantalla | Crear grupo; nombre, tipo y miembros | — | `groups/new` |
| `44_MisGrupos` | pantalla | Grupos, planes y mensajes recientes | — | `groups` |
| `45_GrupoChat` | pantalla | Chat de grupo con plan, votación y reserva | `GrupoChat · Familia Reséndiz/Nosotros` | `groups/{id}/chat` |
| `46_Grupo` | pantalla | Detalle del grupo; punto de encuentro y vaca | — | `groups/{id}` |
| `47_PuntoMedio` | pantalla | Punto medio con tiempos por transporte | — | `groups/{id}/midpoint` |
| `48_Votar` | pantalla | Votación de lugar con barras y cierre | — | `plans/{id}/votes` |
| `ChatAmigo` | pantalla | Chat 1 a 1 con invitación a plan | `ChatAmigo · invitación enviada` | `chats/{id}` |

### 4.7 Reserva, pago y mis planes — `feature/booking` (17)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `22_Reserva` | pantalla | Reserva; hora, personas, cupón y anticipo | — | `places/{id}/reserve` |
| `23_Confirmacion` | pantalla | Reserva confirmada con código QR | — | `bookings/{id}/confirmed` |
| `24_MisPlanes` | pantalla | Planes próximos, borradores y pasados | — | `me/plans` |
| `41_PlanesVacio` | pantalla | Vacío de planes con CTA a crear | — | `me/plans/empty` |
| `25_MisReservas` | pantalla | Reservas próximas y pasadas con estado | — | `me/reservations` |
| `26_DetReserva` | pantalla | Detalle de reserva con QR y acciones | `DetReserva · Café Raíz/Tacos El Güero` | `bookings/{id}` |
| `27_Cancelar` | hoja | Cancelar reserva; política y motivo | — | `bookings/{id}/cancel` |
| `35_Pasarela` | pantalla | Pago con tarjeta; total, datos y seguridad | — | `payments` |
| `36_PagoError` | pantalla | Pago rechazado; reintentar o volver | — | `payments/error` |
| `38_CambiarHora` | hoja | Cambiar fecha/hora con disponibilidad y costo | — | `bookings/{id}/reschedule` |
| `29_Cupones` | pantalla | Cupones disponibles y usados | — | `coupons` |
| `47_DetCupon` | pantalla | Detalle de cupón y reserva | `DetCupon · $50/15%/Postre` | `coupons/{id}` |

### 4.8 El día del plan — `feature/dayof` (5)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `13_Mapa` | pantalla | Ruta del plan con paradas y tiempos | — | `plans/{id}/route` |
| `14_MapaFull` | pantalla | Recorrido completo; distancia, tiempo y costo | — | `plans/{id}/route/full` |
| `15_Llegar` | pantalla | Indicaciones de llegada y tiempos | — | `plans/{id}/arrive` |
| `18_Navegando` | pantalla | Navegación al siguiente destino | — | `plans/{id}/navigate` |
| `26_CheckinOk` | pantalla | Check-in confirmado y siguiente parada | — | `plans/{id}/checkin` |

### 4.9 Recuerdos y reseñas — `feature/memories` (21)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `68_PostVisita` | pantalla | Reseña posterior a la visita; estrellas, tags y publicación | — | `plans/{id}/review` |
| `69_Publicado` | pantalla | Confirmación de reseña publicada | — | `reviews/published` |
| `70_CrearPub` | pantalla | Nuevo momento: foto, plan o reseña con privacidad | — | `memories/new` |
| `71_Camara` | pantalla | Cámara para capturar el momento | — | `memories/camera` |
| `72_Postal` | pantalla | Crear postal; foto, lugar, privacidad y etiquetas | — | `memories/postcard` |
| `73_Album` | pantalla | Álbum de recuerdos por plan | — | `memories/album` |
| `74_RecPlan` | pantalla | Recuerdos agrupados por plan | — | `memories/plan/{id}` |
| `75_PostalVer` | pantalla | Detalle de postal; compartir, descargar o eliminar | `PostalVer · Fonda Doña Meche/Jardín del Cerro/Mirador/Parque Ecológico/Plaza Jardín/Tacos El Güero` | `memories/postcard/{id}` |
| `76_RecVacio` | pantalla | Vacío de recuerdos con CTA a crear | — | `memories/empty` |
| `77_ResPend` | pantalla | Reseñas pendientes; lugares visitados | — | `reviews/pending` |
| `78_ResCom` | pantalla | Calificar detalle; criterios y tags | — | `reviews/{id}` |
| `79_MisRes` | pantalla | Mis reseñas; editar o eliminar | `MisRes · confirmar eliminar/reseña eliminada` | `me/reviews` |
| `80_ResLugar` | pantalla | Reseñas del lugar; rating y distribución | — | `places/{id}/reviews` |

### 4.10 Perfil, ajustes y estados — `feature/profile` (10)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `81_Perfil` | pantalla | Perfil; stats, insignias, tabs y compartir | — | `profile` |
| `82_EditarPerfil` | pantalla | Editar foto, nombre, usuario, bio, grupos y privacidad | — | `profile/edit` |
| `83_Insignias` | pantalla | Insignias ganadas y bloqueadas | — | `profile/badges` |
| `84_DatosPers` | pantalla | Datos personales, correo verificado, teléfono y ciudad | — | `profile/data` |
| `85_Ajustes` | pantalla | Cuenta, preferencias, privacidad, ayuda y sesión | — | `settings` |
| `86_Ayuda` | pantalla | FAQ y contacto | — | `settings/help` |
| `87_PushBloqueo` | sistema | Push en pantalla de bloqueo | — | `system/push` |
| `88_PushBanner` | sistema | Banner de push dentro de la app | — | `system/push` |
| `89_SinConexion` | sistema | Sin conexión; datos guardados y reintento | — | `system/offline` |
| `EliminarCuenta` | pantalla | Confirmación de eliminación de cuenta | — | `profile/delete` |

### 4.11 Negocio — `feature/business` (26)

| Frame(s) | Tipo | Propósito | Estados/variantes | Grupo |
|---|---|---|---|---|
| `42_NegCuenta` | pantalla | Alta 1/5: cuenta del administrador | — | `business/onboarding/account` |
| `43_NegRegistro` | pantalla | Alta 2/5: datos del negocio, giro y experiencia | — | `business/onboarding/business` |
| `44_NegUbic` | pantalla | Alta 3/5: ubicación, mapa y contacto | — | `business/onboarding/location` |
| `45_NegOper` | pantalla | Alta 4/5: horario, mesas, precios, servicios y anticipo | — | `business/onboarding/operations` |
| `46_NegVerif` | pantalla | Alta 5/5: fotos, documentos y CLABE | — | `business/onboarding/verification` |
| `47_NegListo` | pantalla | Solicitud enviada; pasos siguientes | — | `business/onboarding/done` |
| `48_NegPanel` | pantalla | Panel; resumen, reservas de la semana y próximas | — | `business/panel` |
| `49_NegReservas` | pantalla | Reservas por estado y check-in | — | `business/reservations` |
| `50_NegCheckin` | pantalla | Check-in con código y confirmación | `NegCheckin · Ana/Familia Ortega/Jorge/Luis` | `business/checkin` |
| `51_NegResenas` | pantalla | Reseñas y Sello; métricas y filtros | — | `business/reviews` |
| `52_NegResp` | pantalla | Responder reseña con respuesta rápida | — | `business/reviews/{id}/reply` |
| `53_NegCupones` | pantalla | Cupones activos, programados y terminados | — | `business/coupons` |
| `54_NegCrear` | pantalla | Crear cupón; tipo, descuento, vigencia y límite | — | `business/coupons/new` |
| `55_NegHoras` | pantalla | Horas y cupo por día de la semana | — | `business/hours` |
| `56_NegStats` | pantalla | Estadísticas; reservas, asistencia y origen | — | `business/stats` |
| `57_NegCobros` | pantalla | Cobros, depósitos y estado de cuenta | — | `business/payouts` |
| `58_NegPerfil` | pantalla | Perfil del negocio; estado y accesos | — | `business/profile` |
| `87_NegGaleria` | pantalla | Galería del negocio; portada y fotos | — | `business/gallery` |
| `88_NegAgregarFoto` | hoja | Agregar foto desde cámara, galería o Instagram | — | `business/gallery/add` |
| `89_NegBanco` | pantalla | Cuenta para depósitos; banco y CLABE | — | `business/bank` |
| `90_NegFiscal` | pantalla | Datos de facturación y constancia fiscal | — | `business/fiscal` |
| `NegMomentos` | pantalla | Momentos de clientes; agradecer o destacar | — | `business/moments` |

## 5. Hallazgos que afectan la arquitectura

- `13_Mapa` y `14_MapaFull` son del **día del plan**, no de Explorar; `feature/dayof` debe absorberlos.
- `29_Cupones` y `47_DetCupon` son de **booking**, no de social; los cupones pertenecen al flujo de reserva.
- `45_NegOper` es el paso 4/5 del alta de negocio; no es una pantalla social.
- `28_Vaca`, `45_AgregarGasto` y las variantes de Vaca son **social**; la app no mueve dinero, solo calcula deudas.
- `79_MisRes` es “Mis reseñas” (memories), no “Mis reservas” (`25_MisReservas`, booking). La nomenclatura debe corregirse en las rutas.
- `06_PermisoSys` y `08_Permisos` se solapan; la secuencia de permisos debe consolidarse.
- El botón central `+` abre creación de plan; no hay tab de mensajes. El chat se abre desde grupos, perfil de amigo o plan.
- `42_Compartir` pertenece a plan; `InvitarPlan` y `ChatAmigo` cubren la invitación social.
- El alta de negocio recoge documentos y CLABE: requiere revisión de seguridad, privacidad y almacenamiento; no se implementa en `nv-04`.

### Contratos que la maqueta obliga a confirmar

- Feed: publicaciones, historias, reacciones y notificaciones (`/posts`, `/stories`, `/feed`, `/notifications`).
- Amigos: QR personal, privacidad y solicitudes (`/me/qr`, `/me/privacy`, `/friends`, `/friend-requests`).
- Explorar: favoritos y reseñas de lugar (`/favorites`, `/places/{id}/reviews`).
- Planes: unirse, compartir, paradas y versiones (`/plans/{id}/join`, `/plans/{id}/stops`, `/plans/{id}/share`).
- Social: grupos, chat, punto medio, votos y vaca (`/groups`, `/chats`, `/plans/{id}/votes`, `/plans/{id}/split`).
- Booking: cupones, reservas y pagos (`/coupons`, `/me/reservations`, `/payments`).
- Recuerdos: álbum, postales, reseñas propias (`/memories`, `/me/reviews`, `/reviews`).
- Perfil: insignias y datos (`/me/badges`, `/me/data`).
- Negocio: alta, verificación, galería, banco y fiscal (`/business/*`).
- Todos son propuestas pendientes del OpenAPI del backend Golang; no autorizan clientes ni pruebas reales.

## 6. Orden propuesto de specs

1. `nv-05` — Acceso y registro (11 frames): desbloquea el resto del producto.
2. `nv-06` — Feed, historias, notificaciones y amigos (31 frames): shell social inicial.
3. `nv-07` — Explorar, lugares, favoritos y sello (10 frames).
4. `nv-08` — Planes y creación asistida (13 frames).
5. `nv-09` — Reserva, pago, cupones y mis planes (17 frames).
6. `nv-10` — El día del plan (5 frames).
7. `nv-11` — Recuerdos y reseñas (21 frames).
8. `nv-12` — Grupos, chat y vaca (14 frames).
9. `nv-13` — Perfil, ajustes y estados (10 frames).
10. `nv-14` — Negocio (26 frames).

El orden es tentativo; cada spec confirma su alcance, rutas y contratos con el backend. Las pantallas de sistema (`87_PushBloqueo`, `88_PushBanner`, `89_SinConexion`) se integran en la spec del shell o de perfil, no como feature independiente.

## 7. Criterios de cierre

- Los 158 frames están asignados a uno de los 11 módulos.
- Las variantes están agrupadas y no se confunden con destinos.
- Cada módulo tiene flujo, pantallas, estados y grupos de rutas/contratos propuestos.
- Las duplicaciones y solapamientos están identificados antes de abrir `nv-05+`.
- Las rutas reales se registran en `docs/routes.md` al crear cada spec; este documento no las declara definitivas.
