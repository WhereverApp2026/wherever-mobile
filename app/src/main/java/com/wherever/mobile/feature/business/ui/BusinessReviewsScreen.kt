package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.business.domain.ReviewSummary
import com.wherever.mobile.feature.business.domain.SealCriterion
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.ui.StarsRow
import com.wherever.mobile.feature.reviews.ui.label
import java.time.ZoneId

/** Los aspectos van de 0 a 10 en el resumen. */
private const val ASPECT_MAX = 10f

/** Aspectos que muestra la maqueta, en su orden. */
private val SHOWN_ASPECTS = listOf(Aspect.TASTE, Aspect.SERVICE, Aspect.AMBIENCE)

/** 51_NegResenas (specs/nv-36). Se vuelve a pedir al regresar de responder. */
@Composable
fun BusinessReviewsRoute(
    businessId: String,
    message: Int?,
    onMessageShown: () -> Unit,
    onReply: (PlaceReview, Int?) -> Unit,
    onMoments: () -> Unit,
    viewModel: BusinessReviewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(businessId) {
        viewModel.start(businessId)
        onPauseOrDispose { }
    }
    BusinessReviewsContent(
        state,
        BusinessReviewsActions(
            onPeriod = viewModel::onPeriod,
            onRetry = viewModel::load,
            onReply = { onReply(it, state.replyProgress) },
            onMoments = onMoments,
            onMessageShown = onMessageShown
        ),
        message = message
    )
}

@Composable
internal fun BusinessReviewsContent(
    state: BusinessReviewsUiState,
    actions: BusinessReviewsActions,
    zone: ZoneId = ZoneId.systemDefault(),
    @StringRes message: Int? = null
) {
    val snackbar = remember { SnackbarHostState() }
    val text = message?.let { stringResource(it) }
    LaunchedEffect(text) {
        if (text != null) {
            snackbar.showSnackbar(text)
            actions.onMessageShown()
        }
    }
    val error = state.error
    FormScreen(footer = { WhereverSnackbarHost(snackbar) }) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(stringResource(R.string.biz_reviews_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(R.string.biz_reviews_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            else -> {
                SealCard(state)
                SegmentedControl(
                    options = listOf(
                        stringResource(R.string.biz_reviews_month),
                        stringResource(R.string.biz_reviews_quarter)
                    ),
                    selectedIndex = state.period.ordinal,
                    onSelect = { actions.onPeriod(ReviewPeriod.entries[it]) },
                    modifier = Modifier.testTag("biz_reviews_period")
                )
                state.summary?.takeIf { it.total > 0 }?.let { SummaryCard(it) }
                if (state.reviews.isEmpty()) {
                    Text(
                        stringResource(R.string.biz_reviews_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("biz_reviews_empty")
                    )
                }
                state.reviews.forEach { review -> ReviewCard(review, zone) { actions.onReply(review) } }
                MomentsRow(state.newMoments, actions.onMoments)
            }
        }
    }
}

/** Sello de Excelencia con sus criterios (`avance_sello` del panel). Sin datos del Sello no se dibuja. */
@Composable
private fun SealCard(state: BusinessReviewsUiState) {
    val progress = state.sealProgress ?: return
    if (progress.criteria.isEmpty()) return
    Surface(
        shape = WhereverShapes.Card,
        color = GoldTint,
        contentColor = Ink,
        modifier = Modifier.fillMaxWidth().testTag("biz_reviews_seal")
    ) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Sizes.AvatarS).background(Gold, CircleShape)
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_wh_shield_check),
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(Sizes.IconM)
                    )
                }
                Spacer(Modifier.width(Spacing.M))
                Column {
                    Text(
                        stringResource(R.string.business_seal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(
                            if (state.hasSeal) R.string.biz_reviews_seal_has else R.string.biz_reviews_seal_path
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            progress.criteria.forEach { CriterionRow(it) }
        }
    }
}

@Composable
private fun CriterionRow(criterion: SealCriterion) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (criterion.met) Mint else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Sizes.IconM)
        )
        Spacer(Modifier.width(Spacing.S))
        Text(criterion.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        criterion.current?.let {
            Text(compactNumber(it), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Promedio, distribución por estrellas y aspectos del periodo. */
@Composable
private fun SummaryCard(summary: ReviewSummary) {
    SurfaceCard(modifier = Modifier.testTag("biz_reviews_summary")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                summary.average?.let {
                    Text(
                        compactNumber(it),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(Spacing.S))
                }
                Text(
                    pluralStringResource(R.plurals.reviews_place_count, summary.total, summary.total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (summary.distribution.isNotEmpty()) Distribution(summary.distribution, summary.total)
            if (summary.aspects.isNotEmpty()) Aspects(summary.aspects)
        }
    }
}

/** Una barra por estrella, de 5 a 1. */
@Composable
private fun Distribution(distribution: Map<Int, Int>, total: Int) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
        modifier = Modifier.testTag("biz_reviews_distribution")
    ) {
        for (stars in MAX_STARS downTo 1) {
            val count = distribution[stars] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stars.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(Sizes.IconM)
                )
                WhereverProgressBar(
                    progress = { if (total > 0) count.toFloat() / total else 0f },
                    color = Gold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(Sizes.IconXXL)
                )
            }
        }
    }
}

/** Sabor, Atención y Ambiente del periodo (0 a 10). */
@Composable
private fun Aspects(aspects: Map<Aspect, Double>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M), modifier = Modifier.testTag("biz_reviews_aspects")) {
        SHOWN_ASPECTS.forEach { aspect ->
            val value = aspects[aspect] ?: return@forEach
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Row {
                    Text(
                        stringResource(aspect.label()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        compactNumber(value),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                WhereverProgressBar(progress = {
                    (value / ASPECT_MAX).toFloat()
                }, color = RouteBlue, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ReviewCard(review: PlaceReview, zone: ZoneId, onReply: () -> Unit) {
    SurfaceCard(modifier = Modifier.clickable(onClick = onReply).testTag("biz_review_${review.id}")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarsRow(review.rating, Modifier.weight(1f))
                StatusPill(
                    stringResource(
                        if (review.businessReply !=
                            null
                        ) {
                            R.string.biz_reviews_answered
                        } else {
                            R.string.biz_reviews_to_answer
                        }
                    ),
                    container = if (review.businessReply != null) MintTint else GoldTint,
                    contentColor = Ink
                )
            }
            if (review.comment.isNotBlank()) {
                Text(
                    review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                stringResource(R.string.biz_reviews_author, review.author, formatDate(review.createdAt, zone)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            review.businessReply?.let {
                Text(
                    stringResource(R.string.biz_reviews_your_reply, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MomentsRow(newMoments: Int?, onMoments: () -> Unit) {
    SurfaceCard(modifier = Modifier.clickable(onClick = onMoments).testTag("biz_reviews_moments")) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.L)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.moments_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.biz_reviews_moments_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (newMoments != null && newMoments > 0) {
                StatusPill(
                    pluralStringResource(R.plurals.moments_new_count, newMoments, newMoments),
                    container = Sunset,
                    contentColor = Ink
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** «4.8» o «126». */
internal fun compactNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else String.format(MexicanSpanish, "%.1f", value)

@Preview(name = "51_NegResenas claro")
@Preview(name = "51_NegResenas oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessReviewsPreview() {
    WhereverTheme {
        BusinessReviewsContent(sampleBusinessReviews(), BusinessReviewsActions(), ZoneId.of("America/Mexico_City"))
    }
}
