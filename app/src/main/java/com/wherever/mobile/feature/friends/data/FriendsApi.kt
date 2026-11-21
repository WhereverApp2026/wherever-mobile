package com.wherever.mobile.feature.friends.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class PersonDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null
)

@Serializable
data class PersonDetailDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String,
    @SerialName("nombre_usuario") val username: String? = null,
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("relacion") val relation: String = "ninguna",
    @SerialName("nivel") val level: String? = null,
    @SerialName("amigos_en_comun") val mutualFriends: Int? = null,
    @SerialName("grupos_en_comun") val mutualGroups: Int? = null,
    @SerialName("silenciado") val muted: Boolean = false
)

@Serializable
data class FriendshipDto(
    @SerialName("id") val id: String,
    @SerialName("estado") val status: String,
    @SerialName("direccion") val direction: String,
    @SerialName("persona") val person: PersonDto
)

@Serializable
data class FriendshipListDto(
    @SerialName("resultados") val results: List<FriendshipDto> = emptyList(),
    @SerialName("pendientes") val pending: Int = 0
)

@Serializable
data class PersonListDto(@SerialName("resultados") val results: List<PersonDto> = emptyList())

@Serializable
data class SuggestionDto(
    @SerialName("persona") val person: PersonDto,
    @SerialName("amigos_en_comun") val mutualFriends: Int = 0
)

@Serializable
data class SuggestionListDto(@SerialName("resultados") val results: List<SuggestionDto> = emptyList())

@Serializable
data class FriendRequestDto(@SerialName("usuario_id") val userId: String)

@Serializable
data class RespondDto(@SerialName("accion") val action: String)

@Serializable
data class MuteDto(@SerialName("silenciado") val muted: Boolean)

@Serializable
data class MuteResultDto(@SerialName("id") val id: String, @SerialName("silenciado") val muted: Boolean)

/** wherever-backend `docs/endpoints.md`: personas (nv-35, nv-64, nv-70) y amistades (nv-36, nv-53). */
interface FriendsApi {
    @GET("amistades")
    suspend fun list(@Query("estado") status: String, @Query("q") query: String?): FriendshipListDto

    @GET("amistades/sugerencias")
    suspend fun suggestions(): SuggestionListDto

    @GET("personas/buscar")
    suspend fun search(@Query("q") query: String): PersonListDto

    @GET("personas/codigo/{codigo}")
    suspend fun byCode(@Path("codigo") code: String): PersonDetailDto

    @GET("personas/{id}")
    suspend fun person(@Path("id") id: String): PersonDetailDto

    @POST("amistades")
    suspend fun request(@Body body: FriendRequestDto): FriendshipDto

    @PATCH("amistades/{id}")
    suspend fun respond(@Path("id") id: String, @Body body: RespondDto): FriendshipDto

    @DELETE("amistades/{id}")
    suspend fun remove(@Path("id") id: String)

    @POST("amistades/{id}/bloquear")
    suspend fun block(@Path("id") id: String)

    @PUT("amistades/{id}/silencio")
    suspend fun setMuted(@Path("id") id: String, @Body body: MuteDto): MuteResultDto
}
