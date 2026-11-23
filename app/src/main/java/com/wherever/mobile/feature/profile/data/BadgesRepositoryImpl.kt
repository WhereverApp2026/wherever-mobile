package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.profile.domain.ApiLevel
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
data class NextLevelDto(
    @SerialName("numero") val number: Int = 0,
    @SerialName("nombre") val name: String = "",
    @SerialName("puntos") val points: Int = 0
)

@Serializable
data class LevelDto(
    @SerialName("numero") val number: Int = 0,
    @SerialName("nombre") val name: String = "",
    @SerialName("puntos") val points: Int = 0,
    @SerialName("siguiente") val next: NextLevelDto? = null
)

@Serializable
data class BadgesDto(
    @SerialName("insignias") val badges: List<BadgeDto> = emptyList(),
    /** nv-79: nivel conciliado por el backend; reemplaza la escalera provisional de la app. */
    @SerialName("nivel") val level: LevelDto? = null
)

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
    private var level: ApiLevel? = null

    override suspend fun badges(): Result<List<Badge>> = safeApiCall {
        val dto = api.badges()
        level = dto.level?.takeIf { it.name.isNotBlank() }?.let {
            ApiLevel(it.number, it.name, it.points, it.next?.name?.takeIf(String::isNotBlank), it.next?.points)
        }
        dto.badges.map { it.toDomain() }
    }

    override fun lastLevel(): ApiLevel? = level
}
