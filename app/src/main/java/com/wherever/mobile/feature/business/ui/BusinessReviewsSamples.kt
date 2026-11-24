@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.BusinessMoment
import com.wherever.mobile.feature.business.domain.ReviewSummary
import com.wherever.mobile.feature.business.domain.SealCriterion
import com.wherever.mobile.feature.business.domain.SealProgress
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.PlaceReview

/** Datos de muestra de 51_NegResenas, 52_NegResp y NegMomentos: solo vistas previas, capturas y pruebas. */
internal fun sampleBusinessReviews() = BusinessReviewsUiState(
    loading = false,
    summary = ReviewSummary(
        total = 126,
        average = 4.8,
        aspects = mapOf(Aspect.TASTE to 9.4, Aspect.SERVICE to 8.9, Aspect.AMBIENCE to 9.1),
        previousAverage = 4.6,
        distribution = mapOf(5 to 98, 4 to 20, 3 to 5, 2 to 2, 1 to 1)
    ),
    hasSeal = true,
    sealProgress = SealProgress(
        eligible = true,
        criteria = listOf(
            SealCriterion("Calificación promedio", 4.8, 4.5, met = true),
            SealCriterion("Reseñas verificadas", 126.0, 50.0, met = true),
            SealCriterion("Reseñas respondidas (%)", 92.0, 80.0, met = true)
        )
    ),
    reviews = sampleReviews(),
    newMoments = 3
)

internal fun sampleReviews() = listOf(
    PlaceReview(
        id = "r1",
        author = "Sofía R.",
        authorPhotoUrl = null,
        rating = 5,
        comment = "Los tacos al pastor, increíbles. La atención fue rapidísima y el lugar muy agradable.",
        photos = emptyList(),
        useful = 4,
        businessReply = null,
        createdAt = "2026-11-20T19:30:00Z"
    ),
    PlaceReview(
        id = "r2",
        author = "Andrés M.",
        authorPhotoUrl = null,
        rating = 4,
        comment = "Muy rico, solo tardaron un poco en traer la cuenta.",
        photos = emptyList(),
        useful = 1,
        businessReply = "¡Gracias por venir! Ya lo estamos mejorando.",
        createdAt = "2026-11-18T02:10:00Z"
    )
)

internal fun sampleReplyTarget() = ReplyTarget(
    reviewId = "r1",
    rating = 5,
    comment = sampleReviews().first().comment,
    author = "Sofía R.",
    createdAt = "2026-11-20T19:30:00Z",
    currentReply = null,
    progress = 92
)

internal const val SAMPLE_REPLY_TEXT = "¡Gracias por venir, Sofía! Nos alegra que te gustaran los tacos."

internal fun sampleMoments() = listOf(
    BusinessMoment(
        "m1",
        "Sofía R.",
        null,
        null,
        "Cena de cumpleaños",
        "2026-11-20T19:30:00Z",
        thanked = false,
        featured = false
    ),
    BusinessMoment("m2", "Andrés M.", null, null, null, "2026-11-18T02:10:00Z", thanked = true, featured = true),
    BusinessMoment("m3", "Lucía P.", null, null, "Brunch", "2026-11-15T16:00:00Z", thanked = false, featured = false)
)
