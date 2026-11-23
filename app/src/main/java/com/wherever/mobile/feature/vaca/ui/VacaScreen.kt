package com.wherever.mobile.feature.vaca.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.vaca.domain.Debt
import com.wherever.mobile.feature.vaca.domain.DebtStatus
import com.wherever.mobile.feature.vaca.domain.Expense
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.PersonBalance
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.feature.vaca.domain.VacaSummary

/** 28_Vaca y `Vaca · *` (specs/nv-26). Se recalcula al volver de 45_AgregarGasto. */
@Composable
fun VacaRoute(onBack: () -> Unit, onAddExpense: (String, String) -> Unit, viewModel: VacaViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    VacaContent(
        state,
        VacaActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onMode = viewModel::onMode,
            onPay = viewModel::markPaid,
            onAdd = { onAddExpense(viewModel.groupId, state.mode.apiValue) },
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun VacaContent(state: VacaUiState, actions: VacaActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val summary = state.summary
    val error = state.error
    FormScreen(
        footer = {
            WhereverSnackbarHost(snackbar)
            WhereverPrimaryButton(
                text = stringResource(R.string.vaca_add_expense),
                onClick = actions.onAdd,
                icon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth(),
                tag = "vaca_add"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.vaca_title), style = MaterialTheme.typography.headlineMedium)
                state.title?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        InfoBanner(stringResource(R.string.vaca_notice), icon = Icons.Outlined.CheckCircle, tag = "vaca_notice")
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null && summary == null -> ErrorState(
                error.asString(),
                actions.onRetry,
                Modifier.heightIn(max = Sizes.HeroImage)
            )
            summary != null -> VacaBody(summary, state, actions)
        }
    }
}

@Composable
private fun VacaBody(summary: VacaSummary, state: VacaUiState, actions: VacaActions) {
    TotalsCard(summary.totalCents, state.perPersonCents)
    SegmentedControl(
        options = listOf(stringResource(R.string.vaca_equal), stringResource(R.string.vaca_custom)),
        selectedIndex = state.mode.ordinal,
        onSelect = { actions.onMode(SplitMode.entries[it]) },
        modifier = Modifier.testTag("vaca_mode")
    )
    PaidCard(summary, state)
    if (summary.expenses.isNotEmpty()) DebtsCard(summary, state, actions)
}

@Composable
private fun TotalsCard(total: Long, perPerson: Long) {
    SurfaceCard(modifier = Modifier.testTag("vaca_totals")) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = Spacing.M)) {
            Total(total.asPesos(), stringResource(R.string.vaca_total), null, Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            Total(perPerson.asPesos(), stringResource(R.string.vaca_per_person), RouteBlue, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Total(value: String, label: String, color: androidx.compose.ui.graphics.Color?, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** «Quién pagó»: cada gasto con quien lo pagó y en qué. */
@Composable
private fun PaidCard(summary: VacaSummary, state: VacaUiState) {
    SurfaceCard(modifier = Modifier.testTag("vaca_paid")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(
                stringResource(R.string.vaca_who_paid),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (summary.expenses.isEmpty()) {
                Text(
                    stringResource(R.string.vaca_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("vaca_empty")
                )
            }
            summary.expenses.forEach { expense -> ExpenseRow(expense, summary.people, state) }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, people: List<PersonBalance>, state: VacaUiState) {
    val index = people.indexOfFirst { it.userId == expense.paidBy }.coerceAtLeast(0)
    val name = if (expense.paidBy == state.myId) stringResource(R.string.vaca_you) else state.nameOf(expense.paidBy)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = Sizes.TouchTarget)) {
        MemberAvatar(name, avatarColor(index), Sizes.IconL)
        Spacer(Modifier.width(Spacing.M))
        Text(
            stringResource(
                R.string.vaca_expense_line,
                name,
                expense.description ?: stringResource(conceptLabel(expense.concept))
            ),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(expense.amountCents.asPesos(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/** «Para quedar a mano»: deudas desde quien mira la pantalla. */
@Composable
private fun DebtsCard(summary: VacaSummary, state: VacaUiState, actions: VacaActions) {
    SurfaceCard(modifier = Modifier.testTag("vaca_debts")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(
                stringResource(R.string.vaca_settle),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (summary.debts.isEmpty()) {
                Text(stringResource(R.string.vaca_all_even), style = MaterialTheme.typography.bodyMedium, color = Mint)
            }
            summary.debts.forEach { debt -> DebtRow(debt, summary.people, state, actions) }
        }
    }
}

@Composable
private fun DebtRow(debt: Debt, people: List<PersonBalance>, state: VacaUiState, actions: VacaActions) {
    val amount = debt.amountCents.asPesos()
    val paid = debt.status == DebtStatus.PAID
    // Del lado de quien mira: «Mariana · Te debe $190», «Jorge · Le debes $70» o «Ana le debe $70 a Luis».
    val (who, line) = when (state.myId) {
        debt.creditorId -> state.nameOf(debt.debtorId) to
            stringResource(if (paid) R.string.vaca_paid_you else R.string.vaca_owes_you, amount)
        debt.debtorId -> state.nameOf(debt.creditorId) to
            stringResource(if (paid) R.string.vaca_you_paid else R.string.vaca_you_owe, amount)
        else -> state.nameOf(debt.debtorId) to
            stringResource(R.string.vaca_owes_other, amount, state.nameOf(debt.creditorId))
    }
    val index = people.indexOfFirst {
        it.userId == (
            if (state.myId ==
                debt.debtorId
            ) {
                debt.creditorId
            } else {
                debt.debtorId
            }
            )
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("debt_${debt.id}")) {
        MemberAvatar(who, avatarColor(index.coerceAtLeast(0)), Sizes.IconXL)
        Spacer(Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(who, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            paid -> StatusPill(stringResource(R.string.vaca_paid), container = MintTint, contentColor = Ink)
            state.canPay(debt) -> WhereverOutlinedButton(
                text = stringResource(R.string.vaca_mark_paid),
                onClick = { actions.onPay(debt) },
                enabled = debt.id !in state.paying,
                tag = "pay_${debt.id}"
            )
        }
    }
}

internal fun conceptLabel(concept: ExpenseConcept): Int = when (concept) {
    ExpenseConcept.FOOD -> R.string.vaca_concept_food
    ExpenseConcept.DRINKS -> R.string.vaca_concept_drinks
    ExpenseConcept.TRANSPORT -> R.string.vaca_concept_transport
    ExpenseConcept.TICKET -> R.string.vaca_concept_ticket
    ExpenseConcept.OTHER -> R.string.vaca_concept_other
}

@Preview(name = "28_Vaca claro")
@Preview(name = "28_Vaca oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VacaPreview() {
    WhereverTheme { VacaContent(sampleVacaState(), VacaActions()) }
}

@Preview(name = "Vaca · Mariana pagó")
@Composable
internal fun VacaOnePaidPreview() {
    WhereverTheme { VacaContent(sampleVacaState(paid = setOf("d1")), VacaActions()) }
}

@Preview(name = "Vaca · todos pagaron")
@Composable
internal fun VacaAllPaidPreview() {
    WhereverTheme { VacaContent(sampleVacaState(paid = setOf("d1", "d2", "d3")), VacaActions()) }
}

@Preview(name = "Vaca vacía")
@Composable
internal fun VacaEmptyPreview() {
    WhereverTheme {
        VacaContent(
            VacaUiState(
                loading = false,
                title = "Viernes de tacos",
                myId = "u1",
                summary = VacaSummary(0, emptyList(), emptyList(), emptyList())
            ),
            VacaActions()
        )
    }
}
