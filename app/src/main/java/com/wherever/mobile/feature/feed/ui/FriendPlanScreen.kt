package com.wherever.mobile.feature.feed.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.ui.CostSummary
import com.wherever.mobile.feature.plans.ui.Timeline

data class FriendPlanNavigation(val onBack: () -> Unit, val onMyCopy: (String) -> Unit)

data class FriendPlanActions(
    val onRetry: () -> Unit,
    val onAskJoin: () -> Unit,
    val onJoin: () -> Unit,
    val onDismissJoin: () -> Unit,
    val onCopy: () -> Unit
)

/** 29_PlanAmigo y la hoja 14_MeApunto. */
@Composable
fun FriendPlanRoute(navigation: FriendPlanNavigation, viewModel: FriendPlanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(viewModel) { viewModel.copied.collect(navigation.onMyCopy) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            FriendPlanContent(
                state,
                navigation.onBack,
                FriendPlanActions(
                    viewModel::load,
                    viewModel::askJoin,
                    viewModel::join,
                    viewModel::dismissJoin,
                    viewModel::copy
                )
            )
        }
    }
}

@Composable
internal fun FriendPlanContent(state: FriendPlanUiState, onBack: () -> Unit, actions: FriendPlanActions) {
    val plan = state.plan
    when {
        state.loading && plan == null -> LoadingState()
        plan == null -> FormScreen {
            TopTitleRow(stringResource(R.string.feed_friend_plan_title), onBack)
            ErrorState(
                message = (state.error ?: UiText.Res(R.string.error_unknown)).asString(),
                onRetry = actions.onRetry,
                modifier = Modifier.testTag("friend_plan_error")
            )
        }
        else -> {
            FormScreen(
                footer = {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M), modifier = Modifier.fillMaxWidth()) {
                        WhereverPrimaryButton(
                            text = stringResource(if (state.joined) R.string.feed_joined_short else R.string.feed_join),
                            onClick = actions.onAskJoin,
                            enabled = !state.joined,
                            icon = Icons.Outlined.Add,
                            modifier = Modifier.weight(1f),
                            tag = "friend_plan_join"
                        )
                        WhereverOutlinedButton(
                            text = stringResource(R.string.feed_make_mine),
                            onClick = actions.onCopy,
                            enabled = !state.copying,
                            icon = Icons.Outlined.Edit,
                            modifier = Modifier.weight(1f),
                            tag = "friend_plan_copy"
                        )
                    }
                }
            ) {
                TopTitleRow(plan.name, onBack)
                Text(
                    stringResource(R.string.feed_plan_date, plan.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("friend_plan_summary")
                )
                Timeline(plan.stops, plan.transfers, plan.mobility)
                CostSummary(plan.totalCost, plan.costPerPerson, plan.people)
            }
            if (state.confirmJoin) JoinSheet(plan, state.joining, actions)
        }
    }
}

/** 14_MeApunto: la API apunta al plan completo (no por paradas), así que la hoja confirma el plan entero. */
@Composable
private fun JoinSheet(plan: Plan, joining: Boolean, actions: FriendPlanActions) {
    WhereverBottomSheet(onDismissRequest = actions.onDismissJoin, tag = "join_sheet") {
        Text(
            stringResource(R.string.feed_join_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        SurfaceCard {
            Text(
                plan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(Spacing.L)
            )
            Text(
                pluralStringResource(R.plurals.feed_join_stops, plan.stops.size, plan.stops.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.L).padding(bottom = Spacing.L)
            )
        }
        InfoBanner(stringResource(R.string.feed_join_info))
        WhereverPrimaryButton(
            text = stringResource(R.string.feed_join_confirm),
            onClick = actions.onJoin,
            loading = joining,
            tag = "join_confirm"
        )
        WhereverTextLink(
            stringResource(R.string.feed_not_now),
            onClick = actions.onDismissJoin,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FriendPlanPreview() {
    WhereverTheme {
        FriendPlanContent(
            FriendPlanUiState(loading = false, plan = PREVIEW_PLAN),
            onBack = {},
            actions = FriendPlanActions({}, {}, {}, {}, {})
        )
    }
}

private val PREVIEW_PLAN = Plan(
    id = "pl1",
    name = "Sábado de cafés",
    date = "2026-11-21",
    people = 4,
    budget = 500,
    mobility = Mobility.WALK,
    stops = listOf(
        previewStop("s1", "Café Raíz", "cafeteria", "2026-11-21T15:00:00Z"),
        previewStop("s2", "Jardín del Cerro", "entretenimiento", "2026-11-21T17:00:00Z"),
        previewStop("s3", "Fonda Doña Meche", "restaurante", "2026-11-21T20:00:00Z")
    ),
    transfers = emptyList(),
    totalCost = PREVIEW_TOTAL,
    costPerPerson = PREVIEW_PER_PERSON,
    isPublic = true,
    version = 1
)

private const val PREVIEW_TOTAL = 1680
private const val PREVIEW_PER_PERSON = 420
private const val PREVIEW_MINUTES = 90

private fun previewStop(id: String, name: String, category: String, arrives: String) = PlanStop(
    id = id,
    placeId = "p_$id",
    name = name,
    category = category,
    arrives = arrives,
    minutes = PREVIEW_MINUTES,
    price = 0
)
