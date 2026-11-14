package com.wherever.mobile.core.notifications

/**
 * A dónde lleva tocar un push. El título y el texto del push son genéricos a propósito;
 * el destino sale solo de `data.tipo` y de los ids (specs/nv-46 §3).
 */
sealed interface PushDestination {
    data class FriendRequest(val userId: String) : PushDestination

    data class FriendAccepted(val userId: String) : PushDestination

    data class PlanInvitation(val groupId: String?, val planId: String?) : PushDestination

    data class VoteOpened(val groupId: String?, val voteId: String) : PushDestination

    data class VoteClosed(val groupId: String?, val voteId: String) : PushDestination

    /** Tipo desconocido o ids que faltan: abre la lista de avisos (nv-18 §2, "destino inválido"). */
    data object Notifications : PushDestination

    companion object {
        const val KEY_TYPE = "tipo"
        const val KEY_USER_ID = "usuario_id"
        const val KEY_GROUP_ID = "grupo_id"
        const val KEY_PLAN_ID = "plan_id"
        const val KEY_VOTE_ID = "votacion_id"

        /** Claves que se copian del push al `Intent` que abre la app. */
        val KEYS = listOf(KEY_TYPE, KEY_USER_ID, KEY_GROUP_ID, KEY_PLAN_ID, KEY_VOTE_ID)

        private const val MAX_ID_LENGTH = 64
        private val ID_PATTERN = Regex("^[A-Za-z0-9_-]+$")

        /**
         * `null` si no hay `tipo` (la app se abrió normal). Los ids se validan porque cualquier app
         * puede abrir la actividad launcher con extras: un id raro se descarta y se va a [Notifications].
         */
        fun from(data: Map<String, String?>): PushDestination? {
            val type = data[KEY_TYPE]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            fun id(key: String) = data[key]?.trim()?.takeIf { it.length <= MAX_ID_LENGTH && ID_PATTERN.matches(it) }
            val userId = id(KEY_USER_ID)
            val groupId = id(KEY_GROUP_ID)
            val planId = id(KEY_PLAN_ID)
            val voteId = id(KEY_VOTE_ID)
            return when (type) {
                "solicitud_amistad" -> userId?.let { FriendRequest(it) }
                "amistad_aceptada" -> userId?.let { FriendAccepted(it) }
                "invitacion_plan" -> if (groupId != null || planId != null) PlanInvitation(groupId, planId) else null
                "votacion_abierta" -> voteId?.let { VoteOpened(groupId, it) }
                "votacion_cerrada" -> voteId?.let { VoteClosed(groupId, it) }
                else -> null
            } ?: Notifications
        }
    }
}
