package com.wherever.mobile.feature.plans.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.ToggleRow
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.places.ui.categoryLabel
import java.time.LocalDate

/** CrearPlan (specs/nv-24): plan a mano. Entra desde «Crear plan a mano» de 17_AjustaPlan y de la hoja 15_Crear. */
@Composable
fun ManualPlanRoute(onBack: () -> Unit, onCreated: (String) -> Unit, viewModel: ManualPlanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.created.collect(onCreated) }
    ManualPlanContent(
        state = state,
        today = viewModel.today(),
        actions = ManualPlanActions(
            onBack = onBack,
            onName = viewModel::onName,
            onCompany = viewModel::onCompany,
            onDate = viewModel::onDate,
            onTime = viewModel::onTime,
            onPrice = viewModel::onPrice,
            onReorder = viewModel::toggleReorder,
            onMoveUp = viewModel::moveUp,
            onMoveDown = viewModel::moveDown,
            onRemove = viewModel::removeStop,
            onOpenSearch = viewModel::openSearch,
            onCloseSearch = viewModel::closeSearch,
            onSearch = viewModel::onSearch,
            onPick = viewModel::addPlace,
            onPublic = viewModel::onPublic,
            onCreate = viewModel::create
        ),
        times = viewModel.arrivals(state).map { iso -> isoToLocalTime(iso) }
    )
}

/** Hora local de una llegada ISO (en la zona del teléfono). */
private fun isoToLocalTime(iso: String): String =
    java.time.Instant.parse(iso).atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(TimeFormat)

@Composable
internal fun ManualPlanContent(
    state: ManualPlanUiState,
    today: LocalDate,
    actions: ManualPlanActions,
    times: List<String>
) {
    var pickDate by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_manual_create),
                onClick = actions.onCreate,
                enabled = state.canCreate,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "manual_create"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.plan_manual_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.plan_manual_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LabeledTextField(
            label = stringResource(R.string.plan_manual_name),
            value = state.name,
            onValueChange = actions.onName,
            placeholder = stringResource(R.string.plan_manual_name_hint),
            trailingContent = {
                Text(
                    stringResource(R.string.common_char_count, state.name.length, PLAN_NAME_MAX),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            tag = "manual_name"
        )
        SectionLabel(R.string.plan_who)
        ChipGroup {
            Company.entries.forEach { company ->
                SelectChip(stringResource(companyLabel(company)), state.company == company, {
                    actions.onCompany(company)
                })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            PickerField(
                label = stringResource(R.string.plan_day),
                value = state.date?.format(DayFormat)?.replace(".", "")?.replaceFirstChar { it.uppercase() }.orEmpty(),
                icon = Icons.Outlined.DateRange,
                onClick = { pickDate = true },
                modifier = Modifier.weight(1f)
            )
            PickerField(
                label = stringResource(R.string.plan_start_at),
                value = state.time.format(TimeFormat),
                icon = null,
                onClick = { pickTime = true },
                modifier = Modifier.weight(1f)
            )
        }
        if (state.invalidDateTime) ErrorBanner(stringResource(R.string.plan_manual_date_invalid))
        SectionLabel(R.string.plan_budget)
        SegmentedControl(
            options = PriceLevel.entries.map { stringResource(priceLabel(it)) },
            selectedIndex = state.price?.ordinal ?: -1,
            onSelect = { actions.onPrice(PriceLevel.entries[it]) }
        )
        StopsSection(state, actions, times)
        SurfaceCard {
            ToggleRow(
                title = stringResource(R.string.plan_manual_public),
                subtitle = stringResource(R.string.plan_manual_public_hint),
                checked = state.isPublic,
                onCheckedChange = actions.onPublic,
                tag = "manual_public"
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
    if (pickDate) {
        PlanDatePicker(today, state.date ?: today, { pickDate = false }) {
            pickDate = false
            actions.onDate(it)
        }
    }
    if (pickTime) {
        PlanTimePicker(state.time, { pickTime = false }) {
            pickTime = false
            actions.onTime(it)
        }
    }
    state.search?.let { SearchSheet(it, actions) }
}

@Composable
private fun StopsSection(state: ManualPlanUiState, actions: ManualPlanActions, times: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.plan_stops_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (state.stops.size > 1) {
            WhereverTextLink(
                stringResource(
                    if (state.reordering) R.string.plan_manual_reorder_done else R.string.plan_manual_reorder
                ),
                actions.onReorder,
                Modifier.testTag("manual_reorder")
            )
        }
    }
    state.stops.forEachIndexed { index, place ->
        SurfaceCard(modifier = Modifier.testTag("manual_stop_$index")) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.M)) {
                PlaceAvatar(place.name, place.photoUrl)
                Spacer(Modifier.width(Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.plan_manual_stop, times.getOrElse(index) { "" }, place.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val category = place.category.takeIf { it.isNotBlank() }?.let { stringResource(categoryLabel(it)) }
                    Text(
                        listOfNotNull(category, durationText(MANUAL_STOP_MINUTES)).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.reordering) {
                    WhereverIconButton(onClick = { actions.onMoveUp(index) }, enabled = index > 0) {
                        Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.plan_move_up))
                    }
                    WhereverIconButton(onClick = {
                        actions.onMoveDown(index)
                    }, enabled = index < state.stops.lastIndex) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.plan_move_down)
                        )
                    }
                } else {
                    WhereverIconButton(onClick = {
                        actions.onRemove(index)
                    }, modifier = Modifier.testTag("manual_remove_$index")) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.plan_remove_stop))
                    }
                }
            }
        }
    }
    if (state.canAddStop) {
        Surface(
            onClick = actions.onOpenSearch,
            shape = RoundedCornerShape(Radius.L),
            color = RouteBlueTint,
            contentColor = RouteBlue,
            border = BorderStroke(Strokes.Hairline, RouteBlue),
            modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.ButtonHeight).testTag("manual_add_stop")
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(Spacing.S))
                Text(
                    stringResource(R.string.plan_add_stop),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    if (state.stops.size < AI_MIN_STOPS) {
        Text(
            stringResource(R.string.plan_manual_min_stops, AI_MIN_STOPS, AI_MAX_STOPS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Buscar un lugar para la parada (`GET /busqueda`). */
@Composable
private fun SearchSheet(search: PlaceSearch, actions: ManualPlanActions) {
    WhereverBottomSheet(onDismissRequest = actions.onCloseSearch, tag = "manual_search_sheet") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WhereverIconButton(onClick = actions.onCloseSearch) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.common_back)
                )
            }
            Text(stringResource(R.string.plan_add_stop), style = MaterialTheme.typography.headlineSmall)
        }
        LabeledTextField(
            label = stringResource(R.string.plan_search_place),
            value = search.query,
            onValueChange = actions.onSearch,
            placeholder = stringResource(R.string.explore_search_hint),
            leadingIcon = Icons.Outlined.Search,
            tag = "manual_search"
        )
        if (search.searching) ListSkeleton(rows = 2)
        search.results.forEachIndexed { index, place ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Surface(onClick = {
                actions.onPick(place)
            }, modifier = Modifier.fillMaxWidth().testTag("manual_pick_${place.id}")) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Spacing.S)) {
                    PlaceAvatar(place.name, place.coverUrl)
                    Spacer(Modifier.width(Spacing.M))
                    Column {
                        Text(place.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(categoryLabel(place.category)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Box(Modifier.heightIn(min = Spacing.XXL))
    }
}

@Preview(name = "CrearPlan claro")
@Preview(name = "CrearPlan oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Suppress("MagicNumber") // Precios de ejemplo de la maqueta.
@Composable
internal fun ManualPlanPreview() {
    WhereverTheme {
        ManualPlanContent(
            state = ManualPlanUiState(
                name = "Sábado de cafés",
                company = Company.FRIENDS,
                date = PREVIEW_DAY,
                price = PriceLevel.MEDIUM,
                stops = listOf(
                    ManualPlace("n1", "Café Raíz", "cafeteria", null, 180),
                    ManualPlace("n2", "Jardín del Cerro", "entretenimiento", null, 0)
                ),
                isPublic = true
            ),
            today = PREVIEW_DAY.minusDays(1),
            actions = ManualPlanActions(),
            times = listOf("09:00", "10:15")
        )
    }
}

@Suppress("MagicNumber") // Fecha de la maqueta.
private val PREVIEW_DAY: LocalDate = LocalDate.of(2026, 10, 18)
