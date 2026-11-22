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

@Serializable
data object MyDataRoute

@Serializable
data object DeleteAccountRoute

@Serializable
data object DeletionPendingRoute

/** `query` y `category` los lee ExploreViewModel de SavedStateHandle por su nombre. */
@Serializable
data class ExploreRoute(val query: String = "", val category: String = "")

/** `id` y `origin` (busqueda, inicio, mapa, plan_ia o cupon) los lee PlaceDetailViewModel. */
@Serializable
data class PlaceRoute(val id: String, val origin: String = "")

@Serializable
data object FavoritesRoute

/** 19_Buscar (nv-19). */
@Serializable
data class SearchRoute(val query: String = "")

/** `phrase` la lee CreatePlanViewModel de SavedStateHandle por su nombre. */
@Serializable
data class CreatePlanRoute(val phrase: String = "")

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

@Serializable
data object AddFriendRoute

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

/** Modo negocio: Panel, Reservas, Estadísticas y Check-in con su barra interna (specs/nv-35). */
@Serializable
data object BusinessHomeRoute
