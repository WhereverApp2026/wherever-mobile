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
@Serializable
data class TastesRoute(val fromSettings: Boolean = false)

@Serializable
data object HomeRoute

@Serializable
data object ProfileRoute

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

/** `phrase` la lee CreatePlanViewModel de SavedStateHandle por su nombre. */
@Serializable
data class CreatePlanRoute(val phrase: String = "")

/** `id` lo lee PlanDetailViewModel. */
@Serializable
data class PlanRoute(val id: String)

@Serializable
data object MyPlansRoute
