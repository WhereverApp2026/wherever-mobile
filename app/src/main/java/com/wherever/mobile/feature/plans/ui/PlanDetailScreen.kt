package com.wherever.mobile.feature.plans.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverDialog
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.places.ui.categoryLabel
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import java.time.ZoneOffset

/** 16_Plan. */
@Composable
fun PlanDetailRoute(
    onBack: () -> Unit,
    onOpenPlan: (String) -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val shareTitle = stringResource(R.string.place_share)
    val removedText = stringResource(R.string.plan_stop_removed)
    val undo = stringResource(R.string.home_undo)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlanEvent.Share -> {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.link)
                    context.startActivity(Intent.createChooser(send, shareTitle))
                }
                is PlanEvent.Open -> onOpenPlan(event.planId)
                PlanEvent.Deleted -> onBack()
            }
        }
    }
    LaunchedEffect(state.removed) {
        if (state.removed != null) {
            val result = snackbar.showSnackbar(removedText, undo, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove() else viewModel.undoExpired()
        }
    }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        val plan = state.plan
        when {
            plan != null -> PlanContent(plan, state, viewModel, onBack, Modifier.padding(padding))
            state.loading -> LoadingState()
            else -> ErrorState(state.error?.asString().orEmpty(), onRetry = viewModel::load)
        }
    }
    Dialogs(state, viewModel)
}

@Composable
private fun PlanContent(
    plan: Plan,
    state: PlanDetailUiState,
    viewModel: PlanDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier
) {
    FormScreen(modifier = modifier) {
        TopTitleRow(plan.name, onBack)
        Text(
            stringResource(
                R.string.plan_date_people,
                formatDate("${plan.date}T12:00:00Z", ZoneOffset.UTC),
                plan.people
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
            WhereverOutlinedButton(
                stringResource(R.string.place_share),
                viewModel::share,
                icon = Icons.Outlined.Share,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
                tag = "plan_share"
            )
            WhereverOutlinedButton(
                stringResource(R.string.plan_repeat),
                viewModel::repeatTomorrow,
                icon = Icons.Outlined.Refresh,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
                tag = "plan_repeat"
            )
        }
        CostSummary(plan.totalCost, plan.costPerPerson, plan.people)
        Timeline(plan.stops, plan.transfers, plan.mobility) { stop, index ->
            StopActions(stop, index, plan.stops.size, state.busy, viewModel)
        }
        WhereverOutlinedButton(
            stringResource(R.string.plan_add_stop),
            viewModel::openAddStop,
            icon = Icons.Outlined.Add,
            enabled = !state.busy && plan.stops.size < MAX_STOPS,
            modifier = Modifier.fillMaxWidth(),
            tag = "plan_add_stop"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.L)) {
            WhereverTextLink(stringResource(R.string.plan_rename), viewModel::startRename)
            WhereverTextLink(
                stringResource(R.string.plan_delete),
                viewModel::askDelete,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private const val MAX_STOPS = 6

/** US-056, US-063, US-064, US-065. */
@Composable
private fun StopActions(stop: PlanStop, index: Int, count: Int, busy: Boolean, viewModel: PlanDetailViewModel) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        WhereverIconButton(onClick = { viewModel.moveUp(stop) }, enabled = !busy && index > 0) {
            Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.plan_move_up))
        }
        WhereverIconButton(onClick = { viewModel.moveDown(stop) }, enabled = !busy && index < count - 1) {
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.plan_move_down))
        }
        WhereverTextButton(onClick = { viewModel.shiftTime(stop, forward = false) }, enabled = !busy) {
            Text(stringResource(R.string.plan_earlier))
        }
        WhereverTextButton(onClick = { viewModel.shiftTime(stop, forward = true) }, enabled = !busy) {
            Text(stringResource(R.string.plan_later))
        }
        WhereverIconButton(onClick = {
            viewModel.openAlternatives(stop)
        }, enabled = !busy, modifier = Modifier.testTag("stop_change_$index")) {
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.plan_change_stop))
        }
        WhereverIconButton(onClick = {
            viewModel.removeStop(stop)
        }, enabled = !busy, modifier = Modifier.testTag("stop_remove_$index")) {
            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.plan_remove_stop))
        }
    }
}

@Composable
private fun Dialogs(state: PlanDetailUiState, viewModel: PlanDetailViewModel) {
    state.alternatives?.let { sheet ->
        WhereverDialog(
            onDismissRequest = viewModel::closeAlternatives,
            title = { Text(stringResource(R.string.plan_change_stop)) },
            text = {
                when {
                    sheet.loading -> LoadingState()
                    sheet.items.isEmpty() -> Text(stringResource(R.string.plan_no_alternatives))
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                        items(sheet.items, key = { it.placeId }) { alt ->
                            Column(
                                modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget).clickable {
                                    viewModel.replaceWith(alt)
                                }.padding(Spacing.S)
                            ) {
                                Text(alt.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    stringResource(
                                        R.string.plan_alternative_detail,
                                        stringResource(categoryLabel(alt.category)),
                                        alt.extraMinutes
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                WhereverTextButton(onClick = viewModel::closeAlternatives) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    state.addStop?.let { sheet ->
        WhereverDialog(
            onDismissRequest = viewModel::closeAddStop,
            title = { Text(stringResource(R.string.plan_add_stop)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    LabeledTextField(
                        label = stringResource(R.string.explore_search_label),
                        value = sheet.query,
                        onValueChange = viewModel::onAddQuery,
                        placeholder = stringResource(R.string.explore_search_hint),
                        tag = "add_stop_query"
                    )
                    sheet.results.take(ADD_RESULTS).forEach { place ->
                        Text(
                            place.name,
                            modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget).clickable {
                                viewModel.addPlace(place)
                            }.padding(Spacing.S)
                        )
                    }
                }
            },
            confirmButton = {
                WhereverTextButton(onClick = viewModel::closeAddStop) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
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

private const val ADD_RESULTS = 5
