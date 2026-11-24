package com.wherever.mobile.feature.vaca.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.SPLIT_MIN
import com.wherever.mobile.feature.vaca.domain.SplitMode

/** 45_AgregarGasto (specs/nv-26). */
@Composable
fun AddExpenseRoute(onBack: () -> Unit, onSaved: () -> Unit, viewModel: AddExpenseViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }
    AddExpenseContent(
        state,
        AddExpenseActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onDescription = viewModel::onDescription,
            onConcept = viewModel::onConcept,
            onAmount = viewModel::onAmount,
            onPaidBy = viewModel::onPaidBy,
            onToggle = viewModel::toggleMember,
            onCustom = viewModel::onCustom,
            onFillEqually = viewModel::fillEqually,
            onSave = viewModel::save
        )
    )
}

@Composable
internal fun AddExpenseContent(state: AddExpenseUiState, actions: AddExpenseActions) {
    val loadError = state.loadError
    when {
        state.loading -> LoadingState()
        loadError != null -> ErrorState(loadError.asString(), actions.onRetry)
        else -> AddExpenseForm(state, actions)
    }
}

@Composable
private fun AddExpenseForm(state: AddExpenseUiState, actions: AddExpenseActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.vaca_save_expense),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "expense_save"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.vaca_add_expense_title), style = MaterialTheme.typography.headlineMedium)
                state.title?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        LabeledTextField(
            label = stringResource(R.string.vaca_what),
            value = state.description,
            onValueChange = actions.onDescription,
            placeholder = stringResource(R.string.vaca_what_hint),
            leadingIcon = Icons.Outlined.ShoppingCart,
            tag = "expense_description"
        )
        ChipGroup {
            ExpenseConcept.entries.forEach { concept ->
                SelectChip(stringResource(conceptLabel(concept)), state.concept == concept, {
                    actions.onConcept(concept)
                })
            }
        }
        AmountCard(state.amount, actions.onAmount)
        Text(
            stringResource(R.string.vaca_who_paid_q),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            state.members.forEachIndexed { index, member ->
                PayerAvatar(member, index, selected = state.paidBy == member.userId) { actions.onPaidBy(member.userId) }
            }
        }
        SplitCard(state, actions)
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

/** Monto grande al centro, en pesos («$480»); se guarda en centavos enteros. */
@Composable
private fun AmountCard(amount: String, onAmount: (String) -> Unit) {
    val label = stringResource(R.string.vaca_amount)
    SurfaceCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(Spacing.L)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.vaca_currency),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = amount,
                    onValueChange = onAmount,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    ),
                    cursorBrush = SolidColor(RouteBlue),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = Sizes.IconXL).semantics {
                        contentDescription =
                            label
                    }
                        .testTag("expense_amount")
                )
            }
        }
    }
}

@Composable
private fun PayerAvatar(member: ExpenseMember, index: Int, selected: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.vaca_paid_by, member.name)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        border = if (selected) BorderStroke(Strokes.Thin, RouteBlue) else null,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.size(Sizes.AvatarM).semantics { contentDescription = description }
            .testTag("payer_${member.userId}")
    ) {
        Box(contentAlignment = Alignment.Center) { MemberAvatar(member.name, avatarColor(index), Sizes.AvatarS) }
    }
}

/** «Dividir entre»: en partes iguales muestra «$120 c/u»; personalizado pide el monto de cada quien. */
@Composable
private fun SplitCard(state: AddExpenseUiState, actions: AddExpenseActions) {
    SurfaceCard(modifier = Modifier.testTag("expense_split")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.vaca_split_between),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val header = when (state.mode) {
                    SplitMode.EQUAL -> state.eachCents?.let { stringResource(R.string.vaca_each, it.asPesos()) }
                    SplitMode.CUSTOM -> state.customRemaining?.let { rest ->
                        if (rest == 0L) null else stringResource(R.string.vaca_remaining, rest.asPesos())
                    }
                }
                header?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (state.mode == SplitMode.CUSTOM) {
                WhereverTextLink(stringResource(R.string.vaca_fill_equally), actions.onFillEqually)
            }
            state.members.forEachIndexed { index, member ->
                MemberRow(state, member, index, actions)
            }
            // La API divide entre 2 y 12 confirmados (nv-58): con uno solo, se explica por qué no se guarda.
            if (state.members.size < SPLIT_MIN) {
                Text(
                    stringResource(R.string.vaca_need_two),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("expense_need_two")
                )
            }
        }
    }
}

@Composable
private fun MemberRow(state: AddExpenseUiState, member: ExpenseMember, index: Int, actions: AddExpenseActions) {
    val selected = member.userId in state.splitBetween
    val name = if (member.userId == state.myId) stringResource(R.string.vaca_you) else member.name
    Surface(
        onClick = { actions.onToggle(member.userId) },
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget)
            .semantics { role = Role.Checkbox }
            .testTag("split_${member.userId}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MemberAvatar(name, avatarColor(index), Sizes.IconL)
            Spacer(Modifier.width(Spacing.M))
            Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (state.mode == SplitMode.CUSTOM && selected) {
                CustomAmountField(
                    value = state.custom[member.userId].orEmpty(),
                    description = stringResource(R.string.vaca_amount_of, name),
                    onValue = { actions.onCustom(member.userId, it) },
                    tag = "custom_${member.userId}"
                )
                Spacer(Modifier.width(Spacing.S))
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

/** Monto de una persona en «Personalizado»: campo compacto con «$». */
@Composable
private fun CustomAmountField(value: String, description: String, onValue: (String) -> Unit, tag: String) {
    Surface(
        shape = RoundedCornerShape(Radius.Field),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
        modifier = Modifier.width(Sizes.CardImage).heightIn(min = Sizes.TouchTarget)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Spacing.M)) {
            Text(stringResource(R.string.vaca_currency), style = MaterialTheme.typography.bodyLarge)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(RouteBlue),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.M)
                    .semantics { contentDescription = description }.testTag(tag)
            )
        }
    }
}

@Preview(name = "45_AgregarGasto claro")
@Preview(name = "45_AgregarGasto oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AddExpensePreview() {
    WhereverTheme { AddExpenseContent(sampleExpenseState(), AddExpenseActions()) }
}

@Preview(name = "45_AgregarGasto personalizado")
@Composable
internal fun AddExpenseCustomPreview() {
    WhereverTheme {
        AddExpenseContent(
            sampleExpenseState().copy(
                mode = SplitMode.CUSTOM,
                custom = mapOf("u1" to "200", "u2" to "100", "u3" to "100")
            ),
            AddExpenseActions()
        )
    }
}
