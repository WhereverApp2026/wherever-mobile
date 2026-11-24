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
| `PlanRoute(id)` | Plan: 39_Plan con resumen y paradas, hoja 40_Cambiar y 41_AgregarParada | ninguno | release | nv-48, nv-24 |
| `ManualPlanRoute` | CrearPlan: plan a mano (desde 17_AjustaPlan y la hoja 15_Crear) | ninguno | release | nv-24 |
| `ArrivalRoute(placeId, placeName, planId, nextName, nextMinutes)` | «Ya llegué»: check-in por ubicación o código del mostrador y 26_CheckinOk | ubicación aproximada o cámara, al tocar | release | nv-30 |
| `NotificationsRoute` | Avisos: lista, filtros, marcar leídas y borrar | ninguno | release | nv-52 |
| `ReserveRoute(placeId, placeName)` | Reservar: día, personas, turno, nota y resumen | ninguno | release | nv-54 |
| `ReservationRoute(id)` | Reserva: código, detalle, calendario, cambiar hora y cancelar | ninguno | release | nv-54 |
| `MyReservationsRoute` | Mis reservas: próximas y pasadas | ninguno | release | nv-54 |
| `FriendsRoute` | Amigos: lista, solicitudes recibidas y enviadas | ninguno | release | nv-53 |
| `MyGroupsRoute` | Mis grupos: grupos por actividad, aceptar o rechazar la invitación | ninguno | release | nv-25 |
| `GroupChatRoute(id)` | Chat del grupo con plan fijado, envío optimista y lectura sin red | ninguno | release | nv-25 |
| `MeetingPointRoute(id)` | 47_PuntoMedio: compartir mi ubicación aproximada, puntos medios sugeridos en el mapa, fijar el punto y cuánto tarda cada quien | ubicación aproximada (en contexto, opcional) | release | nv-25 |
| `ExploreMapRoute` | 32_ExpMapa: lugares en el mapa (osmdroid), Buscar en esta zona, filtros, Ver detalle y A mi plan | ubicación aproximada (opcional, solo para centrar) | release | nv-21 |
| `StoryRoute(authorId)` | Visor de historias de amigos (24 h), con avance automático y «Ver publicación» | ninguno | release | nv-17 |
| `GroupRoute(id)` | 46_Grupo: integrantes, plan, votaciones y La vaca; el organizador invita, renombra y saca | ninguno | release | nv-25 |
| `VoteRoute(id)` | 48_Votar: votaciones del grupo, un voto por persona y cierre del organizador | ninguno | release | nv-25 |
| `CreateGroupRoute(planId, groupId)` | 43_CrearGrupo: grupo del plan con hasta 11 amigos, o invitar a más (`groupId`) | ninguno | release | nv-25 |
| `InvitePlanRoute(personId, personName)` | InvitarPlan: invitar a un amigo a uno de mis próximos planes | ninguno | release | nv-25 |
| `VacaRoute(id)` | La Vaca virtual del grupo: total, quién pagó y quién debe a quién (28_Vaca y variantes) | ninguno | release | nv-26 |
| `AddExpenseRoute(id, mode)` | Agregar gasto en partes iguales o personalizado (45_AgregarGasto) | ninguno | release | nv-26 |
| `AddFriendRoute` | Agregar amigos: buscar, código, sugerencias y mi usuario | ninguno | release | nv-53 |
| `MyCodeRoute(scan)` | Mi código: QR para que me agreguen y Escanear (cámara del lector ZXing) | cámara, solo al escanear y en contexto | release | nv-20 |
| `PersonRoute(id)` | Perfil de otra persona con su acción | ninguno | release | nv-53 |
| `MyPlansRoute` | Mis planes (próximos, guardados y pasados) | ninguno | release | nv-48 |
| `ProfileRoute` | Perfil | ninguno | release | nv-14 |
| `EditProfileRoute` | 84_DatosPers: nombre, correo, ciudad, foto y eliminar la cuenta | ninguno | release | nv-14 |
| `ProfileEditorRoute` | 82_EditarPerfil: nombre, usuario, bio, gustos y privacidad social | ninguno | release | nv-14 |
| `SettingsRoute` | Ajustes | ninguno | release | nv-13 |
| `BusinessOnboardingRoute` | Alta del negocio: registro, ubicación, operación, verificación y solicitud enviada (43 a 47) | ubicación aproximada al tocar «Estoy en mi negocio» | release | nv-34 |
| `BusinessHomeRoute` | Modo negocio: entra con `POST /usuarios/yo/modo` y abre el Panel del negocio activo (barra interna Panel, Reservas, Cupones, Reseñas y Negocio) | ninguno | release | nv-35 |
| `BusinessPanelDestination` (interna) | 48_NegPanel | ninguno | release | nv-35 |
| `BusinessStatsDestination` (interna) | 56_NegStats y reporte mensual PDF (Descargas, sin permisos) | ninguno | release | nv-35 |
| `BusinessReservationsDestination` (interna) | 49_NegReservas: Hoy, Próximas, Pasadas, acciones y cerrar horarios | ninguno | release | nv-35 |
| `BusinessCheckinDestination(code)` (interna) | 50_NegCheckin por código escrito (QR en D-35a) | ninguno | release | nv-35 |
| `BusinessCouponsDestination` (interna) | 53_NegCupones: Activos, Programados y Terminados con interruptor | ninguno | release | nv-37 |
| `BusinessCouponFormDestination` (interna) | 54_NegCrear: nuevo cupón con vista previa y título sugerido por IA | ninguno | release | nv-37 |
| `BusinessCapacityDestination` (interna) | 55_NegHoras: ocupación por hora, cupo y duración del turno | ninguno | release | nv-37 |
| `BusinessReviewsDestination` (interna) | 51_NegResenas: Sello, resumen por mes o trimestre y reseñas | ninguno | release | nv-36 |
| `BusinessReplyDestination(reviewId, rating, comment, author, createdAt, currentReply, progress)` (interna) | 52_NegResp: respuesta rápida, sugerencia con IA y publicar | ninguno | release | nv-36 |
| `BusinessMomentsDestination` (interna) | NegMomentos: Nuevos y Destacados con Agradecer y Destacar | ninguno | release | nv-36 |
| `BusinessProfileDestination` (interna) | 58_NegPerfil: datos, fotos, cobros, recibir reservaciones, pausa, volver a explorar, cerrar sesión y eliminar | ninguno | release | nv-38 |
| `BusinessDataDestination` (interna) | Datos y horario: descripción, teléfono y horario (`PATCH` con `If-Match`) | ninguno | release | nv-38 |
| `BusinessGalleryDestination` (interna) | 87_NegGaleria y hoja 88_NegAgregarFoto (cámara con `TakePicture` o Photo Picker) | ninguno | release | nv-38 |
| `BusinessPaymentsDestination` (interna) | 57_NegCobros: saldo, depósitos y conectar cobros (navegador) | ninguno | release | nv-38 |
| `BusinessBankDestination(lastFour)` (interna) | 89_NegBanco: titular y CLABE | ninguno | release | nv-38 |
| `BusinessTaxDestination(complete)` (interna) | 90_NegFiscal: RFC, razón social, régimen, uso CFDI y C.P. | ninguno | release | nv-38 |
| `SessionsRoute` | Sesiones abiertas | ninguno | release | nv-13 |
| `PrivacyRoute` | Privacidad | ninguno | release | nv-15 |
| `ChangePasswordRoute` | Cambiar contraseña | ninguno | release | nv-51 |
| `ChangeEmailRoute` | Cambiar correo | ninguno | release | nv-51 |
| `FindFriendsRoute` | 07_EncuentraAmigos: amigos entre mis contactos (`POST /personas/contactos`) | ninguno | release | nv-20 |
| `HelpRoute` | Ayuda: preguntas frecuentes, contacto y reportar un problema | ninguno | release | nv-33 |
| `MyDataRoute` | Mis datos: descargar copia y borrar historial | ninguno | release | nv-51 |
| `DeleteAccountRoute` | Eliminar cuenta | ninguno | release | nv-15 |
| `DeletionPendingRoute` | Eliminación pendiente | ninguno | release | nv-15 |
| `debug/conexion` | Estado de conexión con la API (`/health`, `/ready`). Sin implementar | ninguno | solo debug | nv-01 |

Convención: rutas tipadas (`@Serializable`, en `navigation/Routes.kt`), sin parámetros sensibles (nunca tokens). `email` en `CheckEmailRoute` es un dato personal: vive solo en la pila de navegación y se muestra enmascarado.
