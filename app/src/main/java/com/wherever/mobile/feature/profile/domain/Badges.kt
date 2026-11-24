package com.wherever.mobile.feature.profile.domain

import com.wherever.mobile.core.common.Result

/**
 * Una insignia de `GET /usuarios/yo/insignias` (83_Insignias). El nombre y la descripción vienen de la API;
 * `current`/`goal` es el avance hacia ganarla.
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val earned: Boolean,
    val current: Int = 0,
    val goal: Int = 0
) {
    /** De 0 a 1; una insignia ganada siempre va completa. */
    val fraction: Float get() = if (earned || goal <= 0) 1f else (current.toFloat() / goal).coerceIn(0f, 1f)
}

/** Insignias ganadas y por desbloquear, en el orden del catálogo de la API. */
/** Nivel que calcula el backend (nv-79): número, nombre, puntos y el siguiente con su meta. */
data class ApiLevel(
    val number: Int,
    val name: String,
    val points: Int,
    val nextName: String?,
    val nextPoints: Int?,
    val nextNumber: Int? = null
) {
    fun toProgress(): LevelProgress = LevelProgress(
        current = LevelStep(name, 0),
        next = if (nextName != null && nextPoints != null) LevelStep(nextName, nextPoints) else null,
        points = points
    )
}

interface BadgesRepository {
    suspend fun badges(): Result<List<Badge>>

    /** El nivel de la última lectura de `badges()`, o `null` si la API no lo trajo. */
    fun lastLevel(): ApiLevel? = null
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
