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
            return destinationFor(type, PushIds(data)) ?: Notifications
        }

        private fun destinationFor(type: String, ids: PushIds): PushDestination? = when (type) {
            "solicitud_amistad" -> ids.user?.let { FriendRequest(it) }
            "amistad_aceptada" -> ids.user?.let { FriendAccepted(it) }
            "invitacion_plan" -> planInvitation(ids)
            "votacion_abierta" -> ids.vote?.let { VoteOpened(ids.group, it) }
            "votacion_cerrada" -> ids.vote?.let { VoteClosed(ids.group, it) }
            else -> null
        }

        /** Basta con uno de los dos ids para saber qué plan abrir. */
        private fun planInvitation(ids: PushIds): PushDestination? =
            PlanInvitation(ids.group, ids.plan).takeIf { ids.group != null || ids.plan != null }

        /** Ids ya validados; uno vacío, largo o con caracteres raros queda en `null`. */
        private class PushIds(data: Map<String, String?>) {
            val user = valid(data[KEY_USER_ID])
            val group = valid(data[KEY_GROUP_ID])
            val plan = valid(data[KEY_PLAN_ID])
            val vote = valid(data[KEY_VOTE_ID])

            private fun valid(raw: String?): String? {
                val id = raw?.trim() ?: return null
                return id.takeIf { it.length <= MAX_ID_LENGTH && ID_PATTERN.matches(it) }
            }
        }
    }
}
