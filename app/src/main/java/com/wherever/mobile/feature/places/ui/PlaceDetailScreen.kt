package com.wherever.mobile.feature.places.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.places.domain.DaySchedule
import com.wherever.mobile.feature.places.domain.PlaceDetail
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.feature.places.domain.Review

internal const val MAX_RATING = 5f
private const val STAR = "★"
private val STAR_FILTERS = (MAX_RATING.toInt() downTo 1).toList()

/** 20_FichaNegocio. */
@Composable
fun PlaceDetailRoute(onBack: () -> Unit, viewModel: PlaceDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        val place = state.place
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            when {
                place != null -> PlaceContent(place, state, viewModel, onBack)
                state.loading -> LoadingState()
                else -> ErrorState(state.error?.asString().orEmpty(), onRetry = viewModel::load)
            }
            if (place == null) {
                BackCircleButton(onBack, Modifier.safeDrawingPadding().padding(12.dp))
            }
        }
    }
    state.report?.let { form ->
        ReportDialog(
            form = form,
            onReason = viewModel::onReportReason,
            onDetail = viewModel::onReportDetail,
            onSend = viewModel::sendReport,
            onDismiss = viewModel::closeReport
        )
    }
}

@Composable
private fun PlaceContent(
    place: PlaceDetail,
    state: PlaceDetailUiState,
    viewModel: PlaceDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().safeDrawingPadding().testTag("place_detail")
    ) {
        item {
            Box {
                Photos(place.photos)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BackCircleButton(onBack)
                    CircleIconButton(
                        icon = if (state.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        description = stringResource(
                            if (state.favorite) R.string.place_favorite_remove else R.string.place_favorite_add
                        ),
                        onClick = viewModel::toggleFavorite,
                        modifier = Modifier.testTag("place_favorite")
                    )
                }
            }
        }
        item { Header(place) }
        item { Actions(place, context) }
        if (place.description.isNotBlank()) item { Section(R.string.place_about) { Text(place.description) } }
        item { Section(R.string.place_schedule) { Schedule(place.schedules, state.today) } }
        if (place.reviews > 0) item { Section(R.string.place_aspects) { Aspects(place.aspects) } }
        state.traffic?.let { traffic -> item { Section(R.string.place_traffic) { TrafficBars(traffic) } } }
        if (state.menu.isNotEmpty()) item { Section(R.string.place_menu) { Menu(state.menu) } }
        if (place.services.isNotEmpty() || place.accessibility.isNotEmpty() || place.dietOptions.isNotEmpty()) {
            item { Section(R.string.place_services) { Features(place) } }
        }
        item { ReviewsHeader(state, viewModel) }
        if (state.reviews.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.place_reviews_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = ScreenPadding)
                )
            }
        }
        items(state.reviews, key = { it.id }) { review ->
            ReviewCard(review, review.id in state.usefulMarked) { viewModel.markUseful(review.id) }
        }
        if (state.reviewsCursor != null) {
            item {
                WhereverOutlinedButton(
                    text = stringResource(R.string.place_reviews_more),
                    onClick = viewModel::loadMoreReviews,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding)
                )
            }
        }
        item {
            WhereverTextLink(
                stringResource(R.string.place_report),
                viewModel::openReport,
                modifier = Modifier.padding(horizontal = ScreenPadding)
            )
        }
    }
}

@Composable
private fun Photos(photos: List<String>) {
    if (photos.size <= 1) {
        PlacePhoto(photos.firstOrNull(), height = 240.dp)
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photos) { url -> PlacePhoto(url, height = 240.dp, modifier = Modifier.width(300.dp)) }
        }
    }
}

@Composable
private fun Header(place: PlaceDetail) {
    Column(modifier = Modifier.padding(horizontal = ScreenPadding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(place.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        PlaceMetaLine(place.category, place.priceLevel, null, place.openNow)
        RatingRow(place.rating, place.reviews)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (place.verified) StatusPill(stringResource(R.string.place_verified), container = MintTint)
            if (place.excellenceSeal) StatusPill(stringResource(R.string.place_seal))
        }
        if (place.averagePrice > 0) {
            Text(
                stringResource(R.string.place_average_price, place.averagePrice),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (place.address.isNotBlank()) {
            Text(
                place.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** US-070 ruta y llamada; US-072 compartir. */
@Composable
private fun Actions(place: PlaceDetail, context: Context) {
    val shareText = stringResource(R.string.place_share_text, place.name, place.address)
    val shareTitle = stringResource(R.string.place_share)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = ScreenPadding)
    ) {
        WhereverOutlinedButton(
            text = stringResource(R.string.place_route),
            icon = Icons.Filled.Place,
            onClick = {
                val point = "${place.location.lat},${place.location.lng}"
                val uri = "geo:$point?q=$point"
                context.startSafely(Intent(Intent.ACTION_VIEW, uri.toUri()))
            },
            modifier = Modifier.weight(1f),
            tag = "place_route"
        )
        place.phone?.let { phone ->
            WhereverOutlinedButton(
                text = stringResource(R.string.place_call),
                icon = Icons.Filled.Call,
                onClick = { context.startSafely(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())) },
                modifier = Modifier.weight(1f),
                tag = "place_call"
            )
        }
        CircleIconButton(Icons.Filled.Share, shareTitle, onClick = {
            val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
            context.startSafely(Intent.createChooser(send, shareTitle))
        })
    }
}

private fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Sin app de mapas o de teléfono: no hay nada que abrir.
    }
}

@Composable
private fun Section(title: Int, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = ScreenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        content()
    }
}

private val DAY_NAMES = listOf(
    R.string.day_sunday,
    R.string.day_monday,
    R.string.day_tuesday,
    R.string.day_wednesday,
    R.string.day_thursday,
    R.string.day_friday,
    R.string.day_saturday
)

@Composable
private fun Schedule(schedules: List<DaySchedule>, today: Int) {
    if (schedules.isEmpty()) {
        Text(stringResource(R.string.place_schedule_unknown), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        schedules.sortedBy { (it.day - today + DAY_NAMES.size) % DAY_NAMES.size }.forEach { day ->
            val weight = if (day.day == today) FontWeight.Bold else FontWeight.Normal
            Row {
                Text(
                    stringResource(DAY_NAMES[day.day.coerceIn(0, DAY_NAMES.lastIndex)]),
                    fontWeight = weight,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (day.closed) stringResource(R.string.place_closed_day) else "${day.opens} – ${day.closes}",
                    fontWeight = weight
                )
            }
        }
    }
}

@Composable
private fun ReviewsHeader(state: PlaceDetailUiState, viewModel: PlaceDetailViewModel) {
    Column(modifier = Modifier.padding(horizontal = ScreenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.place_reviews),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                SelectChip(
                    stringResource(R.string.place_reviews_recent),
                    state.reviewQuery.recent,
                    { viewModel.onRecentOrder(!state.reviewQuery.recent) },
                    tag = "reviews_recent"
                )
            }
            items(STAR_FILTERS) { stars ->
                SelectChip(
                    stringResource(R.string.place_reviews_stars, stars),
                    state.reviewQuery.stars == stars,
                    { viewModel.onStarsFilter(stars) },
                    tag = "reviews_stars_$stars"
                )
            }
        }
    }
}

/** US-111: toda reseña pública viene de una visita verificada (backend nv-62). */
@Composable
private fun ReviewCard(review: Review, markedUseful: Boolean, onUseful: () -> Unit) {
    SurfaceCard(modifier = Modifier.padding(horizontal = ScreenPadding).testTag("review_${review.id}")) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    review.author.ifBlank { stringResource(R.string.place_review_anonymous) },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(stringResource(R.string.place_review_visited), container = MintTint)
            }
            val starsDescription = stringResource(R.string.place_review_stars_a11y, review.rating)
            Text(
                STAR.repeat(review.rating.coerceIn(0, MAX_RATING.toInt())),
                color = Gold,
                modifier = Modifier.semantics { contentDescription = starsDescription }
            )
            if (review.comment.isNotBlank()) Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            if (review.chips.isNotEmpty()) {
                Text(review.chips.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Mint)
            }
            review.businessReply?.let { reply ->
                BusinessReply(reply)
            }
            TextButton(onClick = onUseful, enabled = !markedUseful) {
                Icon(Icons.Filled.ThumbUp, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.place_review_useful, review.useful + if (markedUseful) 1 else 0))
            }
        }
    }
}

@Composable
private fun BusinessReply(reply: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Text(stringResource(R.string.place_review_reply), style = MaterialTheme.typography.labelMedium)
        Text(reply, style = MaterialTheme.typography.bodySmall)
    }
}

private val REPORT_REASONS = listOf(
    ReportReason.SCHEDULE to R.string.report_schedule,
    ReportReason.PRICE to R.string.report_price,
    ReportReason.CLOSED to R.string.report_closed,
    ReportReason.LOCATION to R.string.report_location,
    ReportReason.OTHER to R.string.report_other
)

/** US-073: reportar información incorrecta. */
@Composable
private fun ReportDialog(
    form: ReportForm,
    onReason: (ReportReason) -> Unit,
    onDetail: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.place_report)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                REPORT_REASONS.forEach { (reason, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = form.reason == reason, role = Role.RadioButton) { onReason(reason) }
                            .testTag("report_${reason.apiValue}")
                    ) {
                        RadioButton(selected = form.reason == reason, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(label))
                    }
                }
                LabeledTextField(
                    label = stringResource(R.string.report_detail),
                    value = form.detail,
                    onValueChange = onDetail,
                    singleLine = false,
                    supporting = stringResource(R.string.common_char_count, form.detail.length, REPORT_DETAIL_MAX),
                    tag = "report_detail"
                )
                form.error?.let { ErrorBanner(it.asString()) }
            }
        },
        confirmButton = {
            TextButton(onClick = onSend, enabled = form.canSend, modifier = Modifier.testTag("report_send")) {
                Text(stringResource(R.string.report_send))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}
