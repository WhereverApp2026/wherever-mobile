// TooManyFunctions: 39_Plan, la hoja 40_Cambiar, la pantalla 41_AgregarParada, sus filas y vistas previas.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.plans.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverDialog
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.core.ui.shiftIso
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.ui.categoryLabel
import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val LongDate = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-MX"))

/** «Sábado 18 de octubre». */
private fun planDate(date: String): String = try {
    LocalDate.parse(date).format(LongDate).replaceFirstChar { it.titlecase(Locale.forLanguageTag("es-MX")) }
} catch (_: DateTimeParseException) {
    date
}

/** 39_Plan, 40_Cambiar y 41_AgregarParada (specs/nv-24). */
@Composable
fun PlanDetailRoute(
    onBack: () -> Unit,
    onOpenPlan: (String) -> Unit,
    onArrived: (ArrivalTarget) -> Unit = {},
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.place_share)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlanEvent.Share -> {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.link)
                    context.startActivity(Intent.createChooser(send, shareTitle))
                }
                is PlanEvent.Open -> onOpenPlan(event.planId)
                PlanEvent.Deleted -> onBack()
                is PlanEvent.Directions -> {
                    val geo = "geo:${event.lat},${event.lng}?q=${event.lat},${event.lng}(${Uri.encode(event.name)})"
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, geo.toUri()))
                    } catch (_: ActivityNotFoundException) {
                        viewModel.noMapsApp()
                    }
                }
            }
        }
    }
    val actions = PlanDetailActions(
        onBack = onBack,
        onRetry = viewModel::load,
        onShare = viewModel::share,
        onRepeat = viewModel::repeatTomorrow,
        onRename = viewModel::startRename,
        onDelete = viewModel::askDelete,
        onChange = viewModel::openAlternatives,
        onRemove = viewModel::removeStop,
        onMoveUp = viewModel::moveUp,
        onMoveDown = viewModel::moveDown,
        onShift = viewModel::shiftTime,
        onCloseChange = viewModel::closeAlternatives,
        onSelectAlternative = viewModel::selectAlternative,
        onSearchAlternative = viewModel::openAlternativeSearch,
        onAlternativeQuery = viewModel::onAlternativeQuery,
        onReplace = viewModel::replaceSelected,
        onRemoveFromSheet = viewModel::removeFromSheet,
        onAddStop = viewModel::openAddStop,
        onCloseAdd = viewModel::closeAddStop,
        onAddTab = viewModel::onAddTab,
        onAddQuery = viewModel::onAddQuery,
        onAddSuggestion = viewModel::addSuggestion,
        onAddPlace = viewModel::addPlace,
        onDirections = viewModel::directions,
        onArrived = { stop ->
            val plan = state.plan
            val ordered = plan?.stops?.sortedBy { it.arrives }.orEmpty()
            val next = ordered.getOrNull(ordered.indexOfFirst { it.id == stop.id } + 1)
            val transfer = plan?.transfers?.firstOrNull { it.fromStopId == stop.id && it.toStopId == next?.id }
            onArrived(ArrivalTarget(stop.placeId, stop.name, plan?.id.orEmpty(), next?.name, transfer?.minutes))
        }
    )
    PlanDetailContent(
        state = state,
        actions = actions,
        nextArrival = state.plan?.let(viewModel::nextArrival),
        onUndo = { performed -> if (performed) viewModel.undoRemove() else viewModel.undoExpired() },
        onMessageShown = viewModel::messageShown
    )
    RenameAndDelete(state, viewModel)
}

@Composable
internal fun PlanDetailContent(
    state: PlanDetailUiState,
    actions: PlanDetailActions,
    nextArrival: String?,
    onUndo: (Boolean) -> Unit = {},
    onMessageShown: () -> Unit = {},
    /** Zona de las horas; las capturas la fijan para que no dependan del equipo. */
    zone: ZoneId = ZoneId.systemDefault()
) {
    val snackbar = remember { SnackbarHostState() }
    val removedText = stringResource(R.string.plan_stop_removed)
    val undo = stringResource(R.string.home_undo)
    LaunchedEffect(state.removed) {
        if (state.removed != null) {
            val result = snackbar.showSnackbar(removedText, undo, duration = SnackbarDuration.Short)
            onUndo(result == SnackbarResult.ActionPerformed)
        }
    }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            onMessageShown()
        }
    }
    val plan = state.plan
    val addStop = state.addStop
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        when {
            plan != null && addStop != null ->
                AddStopScreen(plan, addStop, nextArrival, actions, zone, Modifier.padding(padding))
            plan != null -> PlanBody(plan, state, actions, zone, Modifier.padding(padding))
            state.loading -> LoadingState()
            else -> ErrorState(state.error?.asString().orEmpty(), onRetry = actions.onRetry)
        }
    }
    if (plan != null) state.alternatives?.let { ChangeSheet(plan, it, actions, zone) }
}

/** 39_Plan. Sin mapa (D25): la tarjeta del mapa y «Cómo llegar» llegan con nv-30. */
@Composable
private fun PlanBody(
    plan: Plan,
    state: PlanDetailUiState,
    actions: PlanDetailActions,
    zone: ZoneId,
    modifier: Modifier
) {
    FormScreen(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("plan_title")
                )
                Text(
                    planDate(plan.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CircleIconButton(
                icon = Icons.Outlined.Share,
                description = stringResource(R.string.place_share),
                onClick = actions.onShare,
                modifier = Modifier.testTag("plan_share")
            )
        }
        PlanSummaryRow(plan.stops, plan.transfers, plan.costPerPerson)
        if (plan.stops.isEmpty()) {
            InfoBanner(stringResource(R.string.plan_empty_stops), tag = "plan_empty")
        } else {
            PlanItinerary(plan.stops, plan.transfers, plan.mobility, zone, links = { stop, _ ->
                StopLinks(stop, actions)
            }) { stop, _ ->
                StopButtons(stop, state.busy, actions)
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            WhereverOutlinedButton(
                stringResource(R.string.plan_add_stop),
                actions.onAddStop,
                icon = Icons.Outlined.Add,
                enabled = !state.busy && plan.stops.size < PLAN_MAX_STOPS,
                tag = "plan_add_stop"
            )
        }
        if (plan.stops.size >= PLAN_MAX_STOPS) {
            Text(
                stringResource(R.string.plan_max_stops, PLAN_MAX_STOPS),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            WhereverTextLink(stringResource(R.string.plan_rename), actions.onRename)
            WhereverTextLink(stringResource(R.string.plan_repeat), actions.onRepeat)
            WhereverTextLink(
                stringResource(R.string.plan_delete),
                actions.onDelete,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** «Cómo llegar» (app de mapas) y «Ya llegué» (check-in) bajo cada parada (specs/nv-30). */
@Composable
private fun StopLinks(stop: PlanStop, actions: PlanDetailActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
        WhereverTextLink(
            stringResource(R.string.plan_directions),
            { actions.onDirections(stop) },
            Modifier.testTag("stop_directions_${stop.id}")
        )
        WhereverTextLink(
            stringResource(R.string.plan_arrived),
            { actions.onArrived(stop) },
            Modifier.testTag("stop_arrived_${stop.id}")
        )
    }
}

/** Cambiar (flechas) y Quitar (X) a la derecha de cada parada. */
@Composable
private fun StopButtons(stop: PlanStop, busy: Boolean, actions: PlanDetailActions) {
    Surface(
        onClick = { actions.onChange(stop) },
        enabled = !busy,
        shape = CircleShape,
        color = RouteBlueTint,
        contentColor = RouteBlue,
        modifier = Modifier.size(Sizes.TouchTarget).testTag("stop_change_${stop.id}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_wh_swap),
                contentDescription = stringResource(R.string.plan_change_stop),
                modifier = Modifier.size(Sizes.IconM)
            )
        }
    }
    WhereverIconButton(
        onClick = { actions.onRemove(stop) },
        enabled = !busy,
        modifier = Modifier.testTag("stop_remove_${stop.id}")
    ) {
        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.plan_remove_stop))
    }
}

/** 40_Cambiar: alternativas, Buscar otro lugar, Quitar esta parada y mover la parada. */
@Composable
private fun ChangeSheet(plan: Plan, sheet: AlternativesSheet, actions: PlanDetailActions, zone: ZoneId) {
    val ordered = plan.stops.sortedBy { it.arrives }
    val index = ordered.indexOfFirst { it.id == sheet.stopId }
    val stop = ordered.getOrNull(index) ?: return
    WhereverBottomSheet(onDismissRequest = actions.onCloseChange, tag = "plan_change_sheet") {
        Text(stringResource(R.string.plan_change_title, index + 1), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(
                R.string.plan_change_now,
                stop.name,
                formatTime(stop.arrives, zone),
                stop.category.takeIf { it.isNotBlank() }?.let { stringResource(categoryLabel(it)) }.orEmpty()
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (sheet.searchOpen) {
            LabeledTextField(
                label = stringResource(R.string.plan_search_place),
                value = sheet.query,
                onValueChange = actions.onAlternativeQuery,
                placeholder = stringResource(R.string.explore_search_hint),
                leadingIcon = Icons.Outlined.Search,
                tag = "plan_change_query"
            )
        }
        when {
            sheet.loading -> ListSkeleton(rows = 2)
            sheet.items.isEmpty() -> Text(stringResource(R.string.plan_no_alternatives))
            else -> sheet.items.forEach { alt ->
                AlternativeRow(alt, selected = sheet.selected == alt.placeId) { actions.onSelectAlternative(alt) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!sheet.searchOpen) {
                WhereverTextLink(stringResource(R.string.plan_search_other), actions.onSearchAlternative)
            }
            Spacer(Modifier.weight(1f))
            WhereverTextLink(
                stringResource(R.string.plan_remove_this),
                actions.onRemoveFromSheet,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("plan_remove_this")
            )
        }
        MoveRow(stop, index, ordered.size, actions)
        WhereverPrimaryButton(
            text = stringResource(R.string.plan_change_stop),
            onClick = actions.onReplace,
            enabled = sheet.selected != null,
            modifier = Modifier.fillMaxWidth(),
            tag = "plan_replace"
        )
    }
}

/** Subir, bajar o mover la hora 15 min (US-064, US-065). */
@Composable
private fun MoveRow(stop: PlanStop, index: Int, count: Int, actions: PlanDetailActions) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        WhereverTextButton(onClick = { actions.onMoveUp(stop) }, enabled = index > 0) {
            Text(stringResource(R.string.plan_move_up))
        }
        WhereverTextButton(onClick = { actions.onMoveDown(stop) }, enabled = index < count - 1) {
            Text(stringResource(R.string.plan_move_down))
        }
        WhereverTextButton(onClick = { actions.onShift(stop, false) }) { Text(stringResource(R.string.plan_earlier)) }
        WhereverTextButton(onClick = { actions.onShift(stop, true) }) { Text(stringResource(R.string.plan_later)) }
    }
}

@Composable
private fun AlternativeRow(alt: Alternative, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.L),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) Strokes.Thin else Strokes.Hairline,
            if (selected) RouteBlue else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth().testTag("alternative_${alt.placeId}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.M)) {
            PlaceAvatar(alt.name, alt.coverUrl)
            Spacer(Modifier.width(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(alt.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(
                        R.string.plan_alternative_line,
                        priceText(0, alt.priceLevel),
                        alt.extraMinutes
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (selected) RouteBlue else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(Sizes.IconL)
            )
        }
    }
}

/** 41_AgregarParada: Sugeridas (cerca de la última parada) y Buscar. */
@Composable
private fun AddStopScreen(
    plan: Plan,
    sheet: AddStopSheet,
    nextArrival: String?,
    actions: PlanDetailActions,
    zone: ZoneId,
    modifier: Modifier
) {
    val last = plan.stops.maxByOrNull { it.arrives }
    val at = nextArrival?.let { formatTime(it, zone) }.orEmpty()
    FormScreen(modifier = modifier.testTag("plan_add_screen")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onCloseAdd)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.plan_add_stop), style = MaterialTheme.typography.headlineMedium)
                if (last != null) {
                    Text(
                        stringResource(R.string.plan_add_after, last.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        SegmentedControl(
            options = listOf(stringResource(R.string.plan_add_suggested), stringResource(R.string.plan_add_search)),
            selectedIndex = sheet.tab.ordinal,
            onSelect = { actions.onAddTab(AddStopTab.entries[it]) },
            modifier = Modifier.testTag("plan_add_tabs")
        )
        when (sheet.tab) {
            AddStopTab.SUGGESTED -> {
                InfoBanner(
                    stringResource(R.string.plan_add_open_at, at),
                    icon = ImageVector.vectorResource(R.drawable.ic_wh_sparkle)
                )
                when {
                    sheet.loadingSuggestions -> ListSkeleton(rows = 3)
                    sheet.suggestions.isEmpty() -> Text(stringResource(R.string.plan_add_no_suggestions))
                    else -> sheet.suggestions.forEach { alt ->
                        AddRow(
                            alt.name,
                            alt.coverUrl,
                            priceText(0, alt.priceLevel),
                            stringResource(R.string.plan_plus_minutes, alt.extraMinutes),
                            tag = "add_${alt.placeId}"
                        ) { actions.onAddSuggestion(alt) }
                    }
                }
            }
            AddStopTab.SEARCH -> {
                LabeledTextField(
                    label = stringResource(R.string.plan_search_place),
                    value = sheet.query,
                    onValueChange = actions.onAddQuery,
                    placeholder = stringResource(R.string.explore_search_hint),
                    leadingIcon = Icons.Outlined.Search,
                    tag = "add_stop_query"
                )
                if (sheet.searching) ListSkeleton(rows = 2)
                sheet.results.forEach { place -> SearchRow(place, actions) }
            }
        }
        Text(
            stringResource(R.string.plan_add_result, plan.stops.size + 1, endTime(nextArrival, zone)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Hora a la que terminaría el plan con la parada nueva (60 min). */
private fun endTime(nextArrival: String?, zone: ZoneId): String =
    nextArrival?.let { formatTime(shiftIso(it, NEW_STOP_MINUTES.toLong()), zone) }.orEmpty()

@Composable
private fun SearchRow(place: PlaceCard, actions: PlanDetailActions) {
    AddRow(
        place.name,
        place.coverUrl,
        listOf(priceText(place.averagePrice, place.priceLevel), stringResource(categoryLabel(place.category)))
            .joinToString(" · "),
        extra = null,
        tag = "add_${place.id}"
    ) { actions.onAddPlace(place) }
}

@Composable
private fun AddRow(name: String, photo: String?, detail: String, extra: String?, tag: String, onAdd: () -> Unit) {
    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.M)) {
            PlaceAvatar(name, photo)
            Spacer(Modifier.width(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                if (extra != null) {
                    Text(
                        extra,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = RouteBlue
                    )
                }
                Surface(
                    onClick = onAdd,
                    shape = CircleShape,
                    color = RouteBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.heightIn(min = Sizes.TouchTarget).testTag(tag)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = Spacing.L)) {
                        Text(
                            stringResource(R.string.plan_add),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = Spacing.M)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameAndDelete(state: PlanDetailUiState, viewModel: PlanDetailViewModel) {
    state.renaming?.let { name ->
        WhereverDialog(
            onDismissRequest = viewModel::cancelRename,
            title = { Text(stringResource(R.string.plan_rename)) },
            text = {
                LabeledTextField(stringResource(R.string.plan_name), name, viewModel::onRename, tag = "plan_name")
            },
            confirmButton = {
                WhereverTextButton(onClick = viewModel::confirmRename) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                WhereverTextButton(onClick = viewModel::cancelRename) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
    if (state.confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.plan_delete),
            message = stringResource(R.string.plan_delete_body),
            confirmText = stringResource(R.string.plan_delete),
            onConfirm = viewModel::delete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Preview(name = "39_Plan claro")
@Preview(name = "39_Plan oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlanDetailPreview() {
    WhereverTheme {
        PlanDetailContent(
            PlanDetailUiState(loading = false, plan = samplePlanDetail()),
            PlanDetailActions(),
            null,
            zone = PREVIEW_ZONE
        )
    }
}

@Preview(name = "Plan · Paseo familiar")
@Composable
internal fun PlanFamilyPreview() {
    WhereverTheme {
        PlanDetailContent(
            PlanDetailUiState(loading = false, plan = sampleFamilyPlan()),
            PlanDetailActions(),
            null,
            zone = PREVIEW_ZONE
        )
    }
}

@Preview(name = "40_Cambiar")
@Composable
internal fun PlanChangePreview() {
    val plan = samplePlanDetail()
    WhereverTheme {
        Column { ChangeSheetPreviewBody(plan) }
    }
}

/** La hoja de 40 en su propio contenedor: `ModalBottomSheet` no se dibuja en las capturas. */
@Composable
private fun ChangeSheetPreviewBody(plan: Plan) {
    val stop = plan.stops[1]
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier.padding(Spacing.XL)
    ) {
        Text(stringResource(R.string.plan_change_title, 2), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(
                R.string.plan_change_now,
                stop.name,
                formatTime(stop.arrives, PREVIEW_ZONE),
                stringResource(R.string.category_entertainment)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        sampleAlternatives().forEachIndexed { i, alt -> AlternativeRow(alt, selected = i == 0) {} }
        WhereverPrimaryButton(stringResource(R.string.plan_change_stop), {}, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "41_AgregarParada")
@Composable
internal fun PlanAddStopPreview() {
    val plan = samplePlanDetail()
    WhereverTheme {
        PlanDetailContent(
            PlanDetailUiState(
                loading = false,
                plan = plan,
                addStop = AddStopSheet(suggestions = sampleAlternatives(), loadingSuggestions = false)
            ),
            PlanDetailActions(),
            "2026-10-18T18:30:00Z",
            zone = PREVIEW_ZONE
        )
    }
}
