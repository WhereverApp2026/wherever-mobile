package com.wherever.mobile.feature.profile.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.BadgesRepository
import javax.inject.Inject

/**
 * Catálogo local de insignias mientras la API no tenga `GET /usuarios/yo/insignias`.
 * Solo "bienvenida" está ganada: la API da 50 puntos al registrarse (backend nv-05).
 */
class LocalBadgesRepository @Inject constructor() : BadgesRepository {
    override suspend fun badges(): Result<List<Badge>> = Result.Success(CATALOG.map { Badge(it, it == WELCOME) })

    companion object {
        const val WELCOME = "bienvenida"
        val CATALOG = listOf(
            WELCOME,
            "primer_plan",
            "primera_reserva",
            "primera_resena",
            "cinco_amigos",
            "racha_cuatro",
            "seis_giros",
            "primer_recuerdo"
        )
    }
}
