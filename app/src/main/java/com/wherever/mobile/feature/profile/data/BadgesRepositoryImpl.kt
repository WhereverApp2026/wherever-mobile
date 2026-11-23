package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.BadgesRepository
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class BadgeProgressDto(@SerialName("actual") val current: Int = 0, @SerialName("meta") val goal: Int = 0)

@Serializable
data class BadgeDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("descripcion") val description: String = "",
    @SerialName("ganada") val earned: Boolean = false,
    @SerialName("progreso") val progress: BadgeProgressDto? = null
)

@Serializable
data class BadgesDto(@SerialName("insignias") val badges: List<BadgeDto> = emptyList())

/** wherever-backend `docs/endpoints.md`: `GET /api/v1/usuarios/yo/insignias`. */
interface BadgesApi {
    @GET("usuarios/yo/insignias")
    suspend fun badges(): BadgesDto
}

fun BadgeDto.toDomain() = Badge(
    id = id,
    name = name.ifBlank { id },
    description = description,
    earned = earned,
    current = progress?.current ?: 0,
    goal = progress?.goal ?: 0
)

class BadgesRepositoryImpl @Inject constructor(private val api: BadgesApi) : BadgesRepository {
    override suspend fun badges(): Result<List<Badge>> = safeApiCall { api.badges().badges.map { it.toDomain() } }
}
