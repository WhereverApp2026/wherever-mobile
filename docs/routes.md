# Rutas de navegación — vivo (nv-01)

> Fuente de contratos de navegación. Las specs solo enlazan, no duplican. Constitución §4 y §6.
> Las filas de pantallas del producto se agregan con su spec (`nv-04+`).

| Ruta | Pantalla | Permisos | Visible en | Spec |
|---|---|---|---|---|
| `WelcomeRoute` | Bienvenida | ninguno | release | nv-06 |
| `LoginRoute` | Iniciar sesión | ninguno | release | nv-07 |
| `RegisterRoute` | Registro | ninguno | release | nv-09 |
| `RecoverRoute` | Recuperar contraseña | ninguno | release | nv-08 |
| `CheckEmailRoute(mode, email)` | Revisa tu correo | ninguno | release | nv-08 |
| `TastesRoute(fromSettings)` | Gustos | ninguno | release | nv-10 |
| `PermissionsRoute` | Permisos (paso 3 de 3) | ninguno | release | nv-11 |
| `HomeRoute` | Inicio con recomendaciones, clima y eventos | ubicación aproximada (opcional, en contexto) | release | nv-47 |
| `ExploreRoute(query, category)` | Explorar: búsqueda, filtros y recientes | ubicación aproximada (opcional) | release | nv-47 |
| `PlaceRoute(id, origin)` | Ficha del lugar | ninguno | release | nv-47 |
| `MomentRoute(id)` | Publicación de un amigo (17_PostDetalle) | ninguno | release | nv-17 |
| `FriendPlanRoute(id)` | Plan de un amigo con Me apunto y Hacer mi versión (29_PlanAmigo) | ninguno | release | nv-17 |
| `PendingReviewsRoute` | Por reseñar (77_ResPend) | ninguno | release | nv-32 |
| `MyReviewsRoute` | Mis reseñas, eliminar con Deshacer (79_MisRes) | ninguno | release | nv-32 |
| `ReviewFormRoute(checkinId, placeId, placeName, day, reviewId)` | Reseñar o editar: 68_PostVisita, 78_ResCom y 69_Publicado | ninguno (fotos con el selector del sistema) | release | nv-32 |
| `PlaceReviewsRoute(placeId, placeName)` | Reseñas del lugar con filtros, Útil y Reportar (80_ResLugar) | ninguno | release | nv-32 |
| `AlbumRoute` | Recuerdos: Todas, Por plan y vacío (73_Album, 74_RecPlan, 76_RecVacio) | ninguno | release | nv-31 |
| `CameraRoute(placeId, placeName, planId)` | Tomar una postal (71_Camara), cámara del sistema o galería | cámara (en contexto, al tocar el disparador; sin ella, galería) | release | nv-31 |
| `PostcardEditorRoute(source, placeId, placeName, planId, date)` | Tu postal: estilo, texto, guardar y compartir (72_Postal) | ninguno | release | nv-31 |
| `PostcardRoute(id)` | Postal con lugar y plan, compartir, descargar y eliminar (75_PostalVer) | ninguno | release | nv-31 |
| `CreateMomentRoute(postcardId)` | Nuevo momento: Foto, Plan (pronto) y Reseña (70_CrearPub) | ninguno | release | nv-31 |
| `NewPasswordRoute(token)` | Nueva contraseña desde el enlace del correo | ninguno | release | nv-08 |
| `VerifyEmailRoute(token)` | Verificar correo desde el enlace | ninguno | release | nv-08 |
| `SearchRoute(query)` | Buscar: lugares, personas y planes; recientes, ideas y sin resultados | ninguno | release | nv-19 |
| `FavoritesRoute` | Mis favoritos | ninguno | release | nv-47 |
| `BadgesRoute` | Insignias: nivel, puntos, ganadas y por desbloquear | ninguno | release | nv-14 |
| `CreatePlanRoute(phrase)` | Crear plan con IA: IAPalabra, 17_AjustaPlan y Generando 1/4 a 4/4; luego el plan propuesto (Guardar u Otra idea) | ubicación aproximada (obligatoria para generar) | release | nv-48, nv-23 |
| `PlanRoute(id)` | Plan: línea de tiempo y edición | ninguno | release | nv-48 |
| `NotificationsRoute` | Avisos: lista, filtros, marcar leídas y borrar | ninguno | release | nv-52 |
| `ReserveRoute(placeId, placeName)` | Reservar: día, personas, turno, nota y resumen | ninguno | release | nv-54 |
| `ReservationRoute(id)` | Reserva: código, detalle, calendario, cambiar hora y cancelar | ninguno | release | nv-54 |
| `MyReservationsRoute` | Mis reservas: próximas y pasadas | ninguno | release | nv-54 |
| `FriendsRoute` | Amigos: lista, solicitudes recibidas y enviadas | ninguno | release | nv-53 |
| `MyGroupsRoute` | Mis grupos: grupos por actividad, aceptar o rechazar la invitación | ninguno | release | nv-25 |
| `GroupChatRoute(id)` | Chat del grupo con plan fijado, envío optimista y lectura sin red | ninguno | release | nv-25 |
| `AddFriendRoute` | Agregar amigos: buscar, código, sugerencias y mi usuario | ninguno | release | nv-53 |
| `MyCodeRoute(scan)` | Mi código: QR para que me agreguen y Escanear (cámara del lector ZXing) | cámara, solo al escanear y en contexto | release | nv-20 |
| `PersonRoute(id)` | Perfil de otra persona con su acción | ninguno | release | nv-53 |
| `MyPlansRoute` | Mis planes (próximos, guardados y pasados) | ninguno | release | nv-48 |
| `ProfileRoute` | Perfil | ninguno | release | nv-14 |
| `EditProfileRoute` | Editar perfil | ninguno | release | nv-14 |
| `SettingsRoute` | Ajustes | ninguno | release | nv-13 |
| `SessionsRoute` | Sesiones abiertas | ninguno | release | nv-13 |
| `PrivacyRoute` | Privacidad | ninguno | release | nv-15 |
| `ChangePasswordRoute` | Cambiar contraseña | ninguno | release | nv-51 |
| `ChangeEmailRoute` | Cambiar correo | ninguno | release | nv-51 |
| `HelpRoute` | Ayuda: preguntas frecuentes, contacto y reportar un problema | ninguno | release | nv-33 |
| `MyDataRoute` | Mis datos: descargar copia y borrar historial | ninguno | release | nv-51 |
| `DeleteAccountRoute` | Eliminar cuenta | ninguno | release | nv-15 |
| `DeletionPendingRoute` | Eliminación pendiente | ninguno | release | nv-15 |
| `debug/conexion` | Estado de conexión con la API (`/health`, `/ready`). Sin implementar | ninguno | solo debug | nv-01 |

Convención: rutas tipadas (`@Serializable`, en `navigation/Routes.kt`), sin parámetros sensibles (nunca tokens). `email` en `CheckEmailRoute` es un dato personal: vive solo en la pila de navegación y se muestra enmascarado.
