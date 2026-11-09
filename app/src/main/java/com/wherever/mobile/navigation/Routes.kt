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
data object DeleteAccountRoute

@Serializable
data object DeletionPendingRoute
