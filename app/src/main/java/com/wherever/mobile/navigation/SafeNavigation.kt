package com.wherever.mobile.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

/**
 * Solo la pantalla que está al frente y quieta puede navegar. Durante una transición la de origen
 * deja de estar RESUMED, así que un doble toque no abre la misma pantalla dos veces ni regresa dos.
 */
fun canNavigateFrom(state: Lifecycle.State): Boolean = state == Lifecycle.State.RESUMED

internal fun NavBackStackEntry.isActive(): Boolean = canNavigateFrom(lifecycle.currentState)

internal fun NavHostController.navigateFrom(
    from: NavBackStackEntry,
    route: Any,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (from.isActive()) navigate(route, builder)
}

/** Regresa desde [from]; devuelve `false` si no había adónde regresar o si ya se estaba regresando. */
internal fun NavHostController.popFrom(from: NavBackStackEntry): Boolean = from.isActive() && popBackStack()

/**
 * Un resultado de la API (sesión iniciada, cuenta borrada…) puede llegar con la app en segundo plano,
 * cuando la pantalla ya no está RESUMED. Basta con que siga arriba de la pila; si ya navegó, se ignora.
 */
fun canDeliverResult(fromEntryId: String, currentEntryId: String?): Boolean = fromEntryId == currentEntryId

internal fun NavHostController.navigateOnResult(
    from: NavBackStackEntry,
    route: Any,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (canDeliverResult(from.id, currentBackStackEntry?.id)) navigate(route, builder)
}

internal fun NavHostController.popOnResult(from: NavBackStackEntry): Boolean =
    canDeliverResult(from.id, currentBackStackEntry?.id) && popBackStack()
