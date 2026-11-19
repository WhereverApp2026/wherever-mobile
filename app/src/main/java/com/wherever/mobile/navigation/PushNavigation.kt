package com.wherever.mobile.navigation

import com.wherever.mobile.core.notifications.PushDestination

/**
 * Ruta de cada push (specs/nv-46 §3). Es el único lugar que hay que tocar cuando existan
 * Notificaciones (nv-18), Perfil de amigo, Plan de amigo y Votar (nv-17, nv-25); mientras tanto
 * todos abren Inicio, que es donde vivirán esas entradas.
 */
fun PushDestination.route(): Any = when (this) {
    is PushDestination.FriendRequest -> HomeRoute // → PerfilAmigo(userId) / Notificaciones
    is PushDestination.FriendAccepted -> HomeRoute // → PerfilAmigo(userId)
    is PushDestination.PlanInvitation -> HomeRoute // → PlanAmigo(groupId, planId)
    is PushDestination.VoteOpened -> HomeRoute // → Votar(groupId, voteId)
    is PushDestination.VoteClosed -> HomeRoute // → Votar(groupId, voteId), resultado
    PushDestination.Notifications -> HomeRoute // → Notificaciones
}
