package com.wherever.mobile.feature.feed.data

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.feed.domain.Comment
import com.wherever.mobile.feature.feed.domain.CommentAuthor
import com.wherever.mobile.feature.feed.domain.CommentState
import com.wherever.mobile.feature.feed.domain.MomentSocialRepository
import com.wherever.mobile.feature.feed.domain.ReactionPerson
import com.wherever.mobile.feature.feed.domain.ReactionTab
import com.wherever.mobile.feature.feed.domain.Reactions
import com.wherever.mobile.feature.feed.domain.StoryMoment
import com.wherever.mobile.feature.feed.domain.StoryPerson
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CommentAuthorDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class CommentDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: CommentAuthorDto? = null,
    @SerialName("padre_id") val parentId: String? = null,
    @SerialName("texto") val text: String = "",
    @SerialName("estado") val state: String = "publicado",
    @SerialName("propio") val own: Boolean = false,
    @SerialName("puede_borrar") val canDelete: Boolean = false,
    @SerialName("creado_en") val createdAt: String = "",
    @SerialName("respuestas") val replies: List<CommentDto> = emptyList()
)

@Serializable
data class CommentPageDto(
    @SerialName("items") val items: List<CommentDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class NewCommentDto(@SerialName("texto") val text: String, @SerialName("padre_id") val parentId: String? = null)

@Serializable
data class ReactionTotalsDto(
    @SerialName("todos") val all: Int = 0,
    @SerialName("me_gusta") val likes: Int = 0,
    @SerialName("van") val going: Int = 0
)

@Serializable
data class ReactionPersonDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("relacion") val relation: String = "ninguna",
    @SerialName("amigos_en_comun") val mutualFriends: Int = 0,
    @SerialName("me_gusta") val liked: Boolean = false,
    @SerialName("va") val going: Boolean = false
)

@Serializable
data class ReactionsDto(
    @SerialName("totales") val totals: ReactionTotalsDto = ReactionTotalsDto(),
    @SerialName("personas") val people: List<ReactionPersonDto> = emptyList()
)

@Serializable
data class StoryPlaceDto(@SerialName("id") val id: String = "", @SerialName("nombre") val name: String = "")

@Serializable
data class StoryMomentDto(
    @SerialName("id") val id: String,
    @SerialName("imagen_url") val imageUrl: String? = null,
    @SerialName("negocio") val place: StoryPlaceDto? = null,
    @SerialName("verificado") val verified: Boolean = false,
    @SerialName("creado_en") val createdAt: String = ""
)

@Serializable
data class StoryAuthorDto(
    @SerialName("id") val id: String,
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class StoryPersonDto(
    @SerialName("autor") val author: StoryAuthorDto,
    @SerialName("momentos") val moments: List<StoryMomentDto> = emptyList()
)

@Serializable
data class StoriesDto(@SerialName("personas") val people: List<StoryPersonDto> = emptyList())

/** wherever-backend `docs/endpoints.md`: me gusta, comentarios y reacciones (nv-83) e historias (nv-84). */
interface MomentSocialApi {
    @PUT("momentos/{id}/me-gusta")
    suspend fun like(@Path("id") id: String)

    @DELETE("momentos/{id}/me-gusta")
    suspend fun unlike(@Path("id") id: String)

    @GET("momentos/{id}/reacciones")
    suspend fun reactions(@Path("id") id: String, @Query("pestana") tab: String): ReactionsDto

    @GET("momentos/{id}/comentarios")
    suspend fun comments(
        @Path("id") id: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int = COMMENTS_PAGE
    ): CommentPageDto

    @POST("momentos/{id}/comentarios")
    suspend fun addComment(@Path("id") id: String, @Body body: NewCommentDto): CommentDto

    @DELETE("comentarios/{id}")
    suspend fun deleteComment(@Path("id") id: String)

    @GET("momentos/historias")
    suspend fun stories(): StoriesDto

    companion object {
        const val COMMENTS_PAGE = 20
    }
}

class MomentSocialRepositoryImpl @Inject constructor(private val api: MomentSocialApi) : MomentSocialRepository {
    override suspend fun setLiked(momentId: String, liked: Boolean): Result<Unit> = safeApiCall {
        if (liked) api.like(momentId) else api.unlike(momentId)
    }

    override suspend fun reactions(momentId: String, tab: ReactionTab): Result<Reactions> = safeApiCall {
        val dto = api.reactions(momentId, tab.apiValue)
        Reactions(
            all = dto.totals.all,
            likes = dto.totals.likes,
            going = dto.totals.going,
            people = dto.people.map {
                ReactionPerson(
                    id = it.id,
                    name = it.name,
                    username = it.username,
                    photoUrl = it.photoUrl?.takeIf(String::isNotBlank),
                    relation = it.relation,
                    mutualFriends = it.mutualFriends,
                    liked = it.liked,
                    going = it.going
                )
            }
        )
    }

    override suspend fun comments(momentId: String, cursor: String?): Result<PageResult<Comment>> = safeApiCall {
        val page = api.comments(momentId, cursor)
        PageResult(page.items.map { it.toDomain() }, page.nextCursor)
    }

    override suspend fun addComment(momentId: String, text: String, parentId: String?): Result<Comment> =
        safeApiCall { api.addComment(momentId, NewCommentDto(text.trim(), parentId)).toDomain() }

    override suspend fun deleteComment(commentId: String): Result<Unit> = safeApiCall { api.deleteComment(commentId) }

    override suspend fun stories(): Result<List<StoryPerson>> = safeApiCall {
        api.stories().people.filter { it.moments.isNotEmpty() }.map { person ->
            StoryPerson(
                authorId = person.author.id,
                username = person.author.username,
                photoUrl = person.author.photoUrl?.takeIf(String::isNotBlank),
                moments = person.moments.map {
                    StoryMoment(
                        id = it.id,
                        imageUrl = it.imageUrl?.takeIf(String::isNotBlank),
                        placeName = it.place?.name?.takeIf(String::isNotBlank),
                        verified = it.verified,
                        createdAt = it.createdAt
                    )
                }
            )
        }
    }
}

internal fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    author = author?.let {
        CommentAuthor(it.id, it.name, it.username, it.photoUrl?.takeIf(String::isNotBlank))
    },
    parentId = parentId,
    text = text,
    state = CommentState.from(state),
    own = own,
    canDelete = canDelete,
    createdAt = createdAt,
    replies = replies.map { it.toDomain() }
)
