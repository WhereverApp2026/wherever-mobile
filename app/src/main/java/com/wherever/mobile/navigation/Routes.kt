package com.wherever.mobile.navigation

import kotlinx.serialization.Serializable

// Rutas tipadas. Cada una tiene su fila en docs/routes.md (constitución §4).

@Serializable
data object WelcomeRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object RecoverRoute

/** `mode` y `email` los lee CheckEmailViewModel de SavedStateHandle por su nombre. */
@Serializable
data class CheckEmailRoute(val mode: String, val email: String)

/** `fromSettings` lo lee TastesViewModel de SavedStateHandle por su nombre. */
/** El token queda en `PendingEmailLink` (memoria), nunca en la ruta (nv-08). */
@Serializable
data object NewPasswordRoute

@Serializable
data object VerifyEmailRoute

@Serializable
data class TastesRoute(val fromSettings: Boolean = false)

@Serializable
data object PermissionsRoute

@Serializable
data object HomeRoute

@Serializable
data object ProfileRoute

@Serializable
data object BadgesRoute

@Serializable
data object EditProfileRoute

/** 82_EditarPerfil: nombre, usuario, bio, gustos y privacidad social (specs/nv-14). */
@Serializable
data object ProfileEditorRoute

@Serializable
data object SettingsRoute

@Serializable
data object SessionsRoute

@Serializable
data object PrivacyRoute

@Serializable
data object ChangePasswordRoute

@Serializable
data object ChangeEmailRoute

/** 86_Ayuda (specs/nv-33). */
@Serializable
data object HelpRoute

@Serializable
data object MyDataRoute

@Serializable
data object DeleteAccountRoute

@Serializable
data object DeletionPendingRoute

/** `query` y `category` los lee ExploreViewModel de SavedStateHandle por su nombre. */
@Serializable
data class ExploreRoute(val query: String = "", val category: String = "", val seal: Boolean = false)

/** `id` y `origin` (busqueda, inicio, mapa, plan_ia o cupon) los lee PlaceDetailViewModel. */
@Serializable
data class PlaceRoute(val id: String, val origin: String = "")

/** 34_Sello: qué es el Sello de Excelencia y los negocios con Sello cerca (specs/nv-21). */
@Serializable
data object SealRoute

@Serializable
data object FavoritesRoute

/** 19_Buscar (nv-19). */
@Serializable
data class SearchRoute(val query: String = "")

/** `phrase` la lee CreatePlanViewModel de SavedStateHandle por su nombre. */
@Serializable
data class CreatePlanRoute(val phrase: String = "")

/** Llegada a una parada y 26_CheckinOk (specs/nv-30); ArrivalViewModel lee los campos por su nombre. */
@Serializable
data class ArrivalRoute(
    val placeId: String,
    val placeName: String = "",
    val planId: String = "",
    val nextName: String = "",
    val nextMinutes: Int = 0
)

/** CrearPlan: plan a mano (specs/nv-24). */
@Serializable
data object ManualPlanRoute

/** `id` lo lee PlanDetailViewModel. */
@Serializable
data class PlanRoute(val id: String)

@Serializable
data object MyPlansRoute

@Serializable
data object NotificationsRoute

@Serializable
data object FriendsRoute

/** `placeId` y `placeName` los lee ReserveViewModel de SavedStateHandle por su nombre. */
@Serializable
data class ReserveRoute(val placeId: String, val placeName: String = "")

/** `id` lo lee ReservationViewModel. */
@Serializable
data class ReservationRoute(val id: String)

@Serializable
data object MyReservationsRoute

@Serializable
data object MyGroupsRoute

/** 45_GrupoChat; `id` lo lee GroupChatViewModel de SavedStateHandle por su nombre (specs/nv-25). */
@Serializable
data class GroupChatRoute(val id: String)

/** 32_ExpMapa: lugares en el mapa (specs/nv-21, D25 osmdroid). */
@Serializable
data object ExploreMapRoute

/** Visor de historias; StoryViewerViewModel lee `authorId` (backend nv-84, specs/nv-17). */
@Serializable
data class StoryRoute(val authorId: String)

/** 46_Grupo; GroupDetailViewModel lee `id` (specs/nv-25). */
@Serializable
data class GroupRoute(val id: String)

/** 48_Votar del grupo; VoteViewModel lee `id` (specs/nv-25). */
@Serializable
data class VoteRoute(val id: String)

/** 43_CrearGrupo: con `groupId` invita a más amigos a ese grupo (specs/nv-25). */
@Serializable
data class CreateGroupRoute(val planId: String, val groupId: String = "")

/** InvitarPlan: invitar a un amigo a uno de mis planes (specs/nv-25). */
@Serializable
data class InvitePlanRoute(val personId: String, val personName: String = "")

/** 28_Vaca del grupo; VacaViewModel lee `id` (specs/nv-26). */
@Serializable
data class VacaRoute(val id: String)

/** 45_AgregarGasto; AddExpenseViewModel lee `id` y `mode` (`igual` o `personalizada`) (specs/nv-26). */
@Serializable
data class AddExpenseRoute(val id: String, val mode: String = "igual")

@Serializable
data object AddFriendRoute

/** 07_EncuentraAmigos: amigos entre mis contactos (`POST /personas/contactos`, specs/nv-20). */
@Serializable
data object FindFriendsRoute

/** 25_MiCodigo; `scan` lo lee MyCodeViewModel para abrir en la pestaña Escanear (specs/nv-20). */
@Serializable
data class MyCodeRoute(val scan: Boolean = false)

/** `id` lo lee PersonViewModel de SavedStateHandle por su nombre. */
@Serializable
data class PersonRoute(val id: String)

/** 17_PostDetalle: un momento del feed de amigos (specs/nv-17). */
@Serializable
data class MomentRoute(val id: String)

/** 29_PlanAmigo: plan público de un amigo, con Me apunto y Hacer mi versión (specs/nv-17). */
@Serializable
data class FriendPlanRoute(val id: String)

/** Alta del negocio: 43_NegRegistro a 47_NegListo (specs/nv-34). 42_NegCuenta se salta con sesión. */
@Serializable
data object BusinessOnboardingRoute

/** 77_ResPend (specs/nv-32). */
@Serializable
data object PendingReviewsRoute

/** 79_MisRes (specs/nv-32). */
@Serializable
data object MyReviewsRoute

/**
 * 68_PostVisita → 78_ResCom → 69_Publicado (specs/nv-32). Crear: `checkinId`; editar: `reviewId`.
 * ReviewFormViewModel lee los campos de SavedStateHandle por su nombre.
 */
@Serializable
data class ReviewFormRoute(
    val checkinId: String = "",
    val placeId: String = "",
    val placeName: String = "",
    val day: String = "",
    val reviewId: String = ""
)

/** 80_ResLugar (specs/nv-32). PlaceReviewsViewModel lee `placeId` y `placeName`. */
@Serializable
data class PlaceReviewsRoute(val placeId: String, val placeName: String = "")

/** 73_Album, 74_RecPlan y 76_RecVacio (specs/nv-31). */
@Serializable
data object AlbumRoute

/** 71_Camara (specs/nv-31). Desde el check-in (nv-30) llegará con el lugar y el plan. */
@Serializable
data class CameraRoute(val placeId: String = "", val placeName: String = "", val planId: String = "")

/** 72_Postal (specs/nv-31). PostcardEditorViewModel lee los campos de SavedStateHandle por su nombre. */
@Serializable
data class PostcardEditorRoute(
    val source: String,
    val placeId: String = "",
    val placeName: String = "",
    val planId: String = "",
    val date: String = ""
)

/** 75_PostalVer (specs/nv-31). PostcardDetailViewModel lee `id`. */
@Serializable
data class PostcardRoute(val id: String)

/** 70_CrearPub (specs/nv-31). CreateMomentViewModel lee `postcardId`. */
@Serializable
data class CreateMomentRoute(val postcardId: String = "")

/** Modo negocio: Panel, Reservas, Estadísticas y Check-in con su barra interna (specs/nv-35). */
@Serializable
data object BusinessHomeRoute
