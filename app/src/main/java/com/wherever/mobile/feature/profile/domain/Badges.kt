package com.wherever.mobile.feature.profile.domain

import com.wherever.mobile.core.common.Result

/** Una insignia del catálogo (83_Insignias). `id` es la clave estable que la API devolverá. */
data class Badge(val id: String, val earned: Boolean)

/**
 * Insignias ganadas y por desbloquear.
 *
 * La API v1 no tiene endpoint de insignias (anotado en `docs/revision-jorge.md`); mientras tanto la
 * implementación es local (`LocalBadgesRepository`). Cuando exista, solo cambia el binding de Hilt.
 */
interface BadgesRepository {
    suspend fun badges(): Result<List<Badge>>
}

/** Peldaño de la escalera de niveles. */
data class LevelStep(val name: String, val minPoints: Int)

/** Avance hacia el siguiente nivel; `next` es null en el último peldaño. */
data class LevelProgress(val current: LevelStep, val next: LevelStep?, val points: Int) {
    val pointsToNext: Int get() = next?.let { (it.minPoints - points).coerceAtLeast(0) } ?: 0

    /** De 0 a 1 entre el peldaño actual y el siguiente. */
    val fraction: Float
        get() {
            val target = next ?: return 1f
            val span = target.minPoints - current.minPoints
            return ((points - current.minPoints).toFloat() / span).coerceIn(0f, 1f)
        }

    companion object {
        /**
         * Escalera provisional: la API solo publica el nivel inicial ("Explorador", 50 puntos al registrarse).
         * Umbrales pendientes de backend (ver `docs/revision-jorge.md`).
         */
        val LADDER = listOf(
            LevelStep("Explorador", 0),
            LevelStep("Aventurero", 250),
            LevelStep("Trotamundos", 750),
            LevelStep("Leyenda", 2000)
        )

        fun of(points: Int, levelName: String): LevelProgress {
            val byPoints = LADDER.last { points >= it.minPoints }
            // Si la API ya dice un nivel conocido, manda la API.
            val current = LADDER.firstOrNull { it.name.equals(levelName, ignoreCase = true) } ?: byPoints
            val next = LADDER.getOrNull(LADDER.indexOf(current) + 1)
            return LevelProgress(current, next, points)
        }
    }
}
