package com.wherever.mobile.feature.memories.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result

/** Límites de la API (specs/nv-31). */
const val POSTCARD_TEXT_MAX = 80
const val MOMENT_TEXT_MAX = 280
const val MAX_TAGGED = 10

/** Estilos que acepta `POST /postales`. La maqueta dice Clásica, Atardecer y Minimal; manda la API (D-31a). */
enum class PostcardStyle(val apiKey: String) {
    CLASSIC("clasica"),
    POLAROID("polaroid"),
    PASSPORT("pasaporte");

    companion object {
        fun fromApi(key: String): PostcardStyle = entries.firstOrNull { it.apiKey == key } ?: CLASSIC
    }
}

enum class Visibility(val apiKey: String) {
    PRIVATE("privada"),
    FRIENDS("amigos"),
    PUBLIC("publica");

    companion object {
        fun fromApi(key: String): Visibility = entries.firstOrNull { it.apiKey == key } ?: PRIVATE
    }
}

enum class PostcardState(val apiKey: String) {
    IN_MODERATION("en_moderacion"),
    READY("lista"),
    REJECTED("rechazada");

    companion object {
        fun fromApi(key: String): PostcardState = entries.firstOrNull { it.apiKey == key } ?: IN_MODERATION
    }
}

/** Postal propia (`GET /postales`). `imageUrl` solo existe con la imagen publicada. */
data class Postcard(
    val id: String,
    val imageUrl: String?,
    val style: PostcardStyle,
    val text: String,
    val visibility: Visibility,
    val state: PostcardState,
    val planId: String?,
    val placeId: String?,
    val version: Long,
    val createdAt: String
)

data class PlaceInfo(val id: String, val name: String, val category: String, val address: String)

data class PlanInfo(val id: String, val title: String, val date: String, val stops: Int)

/** Lo que muestra 75_PostalVer: la postal con su lugar y su plan, si se pudieron leer. */
data class PostcardView(val postcard: Postcard, val place: PlaceInfo?, val plan: PlanInfo?)

data class MemoryStop(val name: String, val time: String, val photoUrl: String?)

/** Recuerdo de un plan (`GET /recuerdos`). */
data class Memory(
    val id: String,
    val planId: String,
    val title: String,
    val date: String,
    val stops: List<MemoryStop>,
    val postcardIds: List<String>
)

/** Lo que pide `POST /postales`. */
data class NewPostcard(
    val imageId: String,
    val style: PostcardStyle,
    val text: String,
    val visibility: Visibility,
    val placeId: String? = null,
    val planId: String? = null
)

/** Lo que pide `POST /momentos`: postal, texto, visibilidad y amigos etiquetados. */
data class MomentDraft(val postcardId: String, val text: String, val visibility: Visibility, val tagged: List<String>)

/** Recuerdos y postales (specs/nv-31). La implementación vive en `data`. */
interface MemoriesRepository {
    suspend fun postcards(cursor: String? = null): Result<PageResult<Postcard>>

    suspend fun memories(cursor: String? = null): Result<PageResult<Memory>>

    /** `GET /postales/{id}`; 404 = ajena o ya no existe (`AppError.NotFound`). */
    suspend fun postcard(id: String): Result<PostcardView>

    /** `POST /postales` responde 202 con `Location`; devuelve el id de la postal nueva. */
    suspend fun createPostcard(postcard: NewPostcard): Result<String>

    /** `DELETE /postales/{id}` con `If-Match`. Borra también su momento. */
    suspend fun deletePostcard(postcard: Postcard): Result<Unit>

    /** `DELETE /imagenes/{id}`: la foto subida que no llegó a ser postal. */
    suspend fun deleteImage(imageId: String): Result<Unit>

    suspend fun createMoment(draft: MomentDraft): Result<Unit>
}

/** Dibuja la postal (marco, texto y sello) sobre la foto y deja un JPEG sin metadatos listo para subir. */
interface PostcardRenderer {
    /** Devuelve la ruta (`file://`) de la imagen dibujada. */
    suspend fun render(source: String, style: PostcardStyle, title: String, subtitle: String): Result<String>
}

/** Guarda la imagen publicada de una postal en la galería (Descargar de 75_PostalVer). */
interface PostcardGallery {
    /** En Android 8 y 9 pediría permiso de almacenamiento: ahí responde `Forbidden` y se ofrece Compartir. */
    suspend fun save(imageUrl: String): Result<Unit>
}
