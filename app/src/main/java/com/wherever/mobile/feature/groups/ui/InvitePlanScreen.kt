package com.wherever.mobile.feature.groups.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.plans.domain.PlanSummary
import java.time.ZoneOffset

/** InvitarPlan (specs/nv-25). */
@Composable
fun InvitePlanRoute(
    onBack: () -> Unit,
    onCreatePlan: () -> Unit,
    onGroup: (String) -> Unit,
    onShared: () -> Unit = {},
    viewModel: InvitePlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.sharedInChat) { if (state.sharedInChat) onShared() }
    val chatText = stringResource(R.string.direct_chat_invite_text)
    InvitePlanContent(
        state,
        InvitePlanActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onSelect = viewModel::select,
            onSend = { viewModel.send(chatText) },
            onCreatePlan = onCreatePlan,
            onGroup = onGroup
        )
    )
}

@Composable
internal fun InvitePlanContent(state: InvitePlanUiState, actions: InvitePlanActions) {
    FormScreen(
        footer = {
            val group = state.sentToGroup
            if (group != null) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.invite_plan_open_group),
                    onClick = { actions.onGroup(group) },
                    tag = "invite_plan_group"
                )
            } else {
                WhereverPrimaryButton(
                    text = stringResource(R.string.invite_plan_send),
                    onClick = actions.onSend,
                    enabled = state.canSend,
                    loading = state.sending,
                    tag = "invite_plan_send"
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.invite_plan_title), actions.onBack)
        TitleBlock(stringResource(R.string.invite_plan_headline, state.personName))
        if (state.sentToGroup != null) {
            InfoBanner(
                stringResource(R.string.invite_plan_sent, state.personName),
                icon = Icons.Outlined.Check,
                tag = "invite_plan_sent"
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
        when {
            state.loading -> LoadingState()
            state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
            state.plans.isEmpty() -> Text(
                stringResource(R.string.invite_plan_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("invite_plan_empty")
            )
            else -> state.plans.forEach { plan ->
                PlanChoice(plan, plan.id == state.selected, state.sentToGroup == null, actions.onSelect)
            }
        }
        WhereverOutlinedButton(
            text = stringResource(R.string.invite_plan_create),
            onClick = actions.onCreatePlan,
            tag = "invite_plan_create"
        )
    }
}

@Composable
private fun PlanChoice(plan: PlanSummary, selected: Boolean, enabled: Boolean, onSelect: (String) -> Unit) {
    SurfaceCard(
        modifier = Modifier
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton) { onSelect(plan.id) }
            .testTag("invite_plan_${plan.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.M)
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(
                        R.string.groups_chat_plan_line,
                        formatDate("${plan.date}T12:00:00Z", ZoneOffset.UTC),
                        pluralStringResource(R.plurals.groups_stops, plan.stops, plan.stops),
                        plan.costPerPerson
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun InvitePlanPreview() {
    WhereverTheme {
        InvitePlanContent(
            InvitePlanUiState(
                personName = "Mariana",
                loading = false,
                plans = listOf(
                    PlanSummary("p1", "Domingo en Coyoacán", "2026-11-29", stops = 3, costPerPerson = 350),
                    PlanSummary("p2", "Cena de viernes", "2026-11-27", stops = 2, costPerPerson = 500)
                ),
                selected = "p1"
            ),
            InvitePlanActions()
        )
    }
}
