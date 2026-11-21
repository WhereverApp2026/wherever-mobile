package com.wherever.mobile.feature.friends.domain

import com.wherever.mobile.core.common.Result

/** Lo único que la API muestra de otra persona: nunca correo ni teléfono (backend nv-35). */
data class Person(val id: String, val name: String, val username: String?, val photoUrl: String?)

enum class Relation(val apiName: String) {
    FRIEND("amigo"),
    REQUEST_SENT("solicitud_enviada"),
    REQUEST_RECEIVED("solicitud_recibida"),
    NONE("ninguna");

    companion object {
        fun from(apiName: String): Relation = entries.firstOrNull { it.apiName == apiName } ?: NONE
    }
}

/** Perfil de otra persona. Los datos de amigo solo llegan con [Relation.FRIEND]. */
data class PersonDetail(
    val person: Person,
    val relation: Relation,
    val level: String? = null,
    val mutualFriends: Int? = null,
    val mutualGroups: Int? = null,
    /** Mi marca de silencio (nv-70): no veo sus avisos; la otra persona no se entera. */
    val muted: Boolean = false
)

/** Bandejas de `GET /amistades?estado=`. */
enum class FriendshipList(val apiName: String) { FRIENDS("amigos"), RECEIVED("recibidas"), SENT("enviadas") }

data class Friendship(val id: String, val person: Person, val received: Boolean, val accepted: Boolean)

data class FriendshipPage(val items: List<Friendship>, val pending: Int)

data class FriendSuggestion(val person: Person, val mutualFriends: Int)

/** Amigos y personas (wherever-backend nv-35, nv-36, nv-53, nv-64 y nv-70). */
interface FriendsRepository {
    suspend fun list(which: FriendshipList, query: String = ""): Result<FriendshipPage>

    suspend fun suggestions(): Result<List<FriendSuggestion>>

    /** Nombre de usuario (prefijo) o teléfono. */
    suspend fun search(query: String): Result<List<Person>>

    suspend fun byCode(code: String): Result<PersonDetail>

    suspend fun person(id: String): Result<PersonDetail>

    suspend fun request(userId: String): Result<Friendship>

    suspend fun respond(friendshipId: String, accept: Boolean): Result<Unit>

    /** Elimina al amigo o cancela una solicitud enviada o recibida. */
    suspend fun remove(friendshipId: String): Result<Unit>

    suspend fun block(friendshipId: String): Result<Unit>

    /** `PUT /amistades/{id}/silencio`; devuelve cómo quedó la marca. */
    suspend fun setMuted(friendshipId: String, muted: Boolean): Result<Boolean>
}
