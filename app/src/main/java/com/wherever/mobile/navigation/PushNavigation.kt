package com.wherever.mobile.navigation

import com.wherever.mobile.core.notifications.PushDestination

/**
 * Ruta de cada push y de cada aviso de la lista (specs/nv-46 §3 y nv-52). Es el único lugar que hay
 * que tocar cuando exista Votar (nv-25); mientras tanto las votaciones abren Avisos.
 */
fun PushDestination.route(): Any = when (this) {
    is PushDestination.FriendRequest -> PersonRoute(userId)
    is PushDestination.FriendAccepted -> PersonRoute(userId)
    is PushDestination.PlanInvitation -> planId?.let { PlanRoute(it) } ?: NotificationsRoute
    is PushDestination.VoteOpened -> NotificationsRoute // → Votar(groupId, voteId)
    is PushDestination.VoteClosed -> NotificationsRoute // → Votar(groupId, voteId), resultado
    PushDestination.Notifications -> NotificationsRoute
}
