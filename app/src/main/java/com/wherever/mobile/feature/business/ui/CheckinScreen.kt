package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InlineProgress
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.ReservationStatus

/** 50_NegCheckin (specs/nv-35): código escrito; Reprogramar se cambia por Contactar (no existe en la API). */
@Composable
fun CheckinRoute(businessId: String, code: String?, onBack: () -> Unit, viewModel: CheckinViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId, code) }
    CheckinContent(
        state = state,
        actions = CheckinActions(
            onBack = onBack,
            onCode = viewModel::onCode,
            onSearch = viewModel::search,
            onConfirm = viewModel::confirm,
            onOpenContact = viewModel::openContact,
            onCloseContact = viewModel::closeContact,
            onContactText = viewModel::onContactText,
            onSendContact = viewModel::sendContact,
            onMessageShown = viewModel::consumeMessage
        )
    )
}

internal data class CheckinActions(
    val onBack: () -> Unit = {},
    val onCode: (String) -> Unit = {},
    val onSearch: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onOpenContact: () -> Unit = {},
    val onCloseContact: () -> Unit = {},
    val onContactText: (String) -> Unit = {},
    val onSendContact: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)

@Composable
internal fun CheckinContent(state: CheckinUiState, actions: CheckinActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.business_checkin_confirm),
                onClick = actions.onConfirm,
                enabled = state.canConfirm,
                loading = state.confirming,
                icon = Icons.Outlined.Check,
                modifier = Modifier.fillMaxWidth(),
                tag = "checkin_confirm"
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.business_action_contact),
                onClick = actions.onOpenContact,
                enabled = state.canContact,
                modifier = Modifier.fillMaxWidth(),
                tag = "checkin_contact"
            )
            WhereverSnackbarHost(snackbar)
        }
    ) {
        TopTitleRow(stringResource(R.string.business_checkin_title), actions.onBack)
        LabeledTextField(
            label = stringResource(R.string.business_checkin_code),
            value = state.code,
            onValueChange = actions.onCode,
            placeholder = stringResource(R.string.business_checkin_placeholder),
            leadingIcon = Icons.Outlined.Search,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { actions.onSearch() }),
            trailingContent = if (state.searching) ({ InlineProgress() }) else null,
            tag = "checkin_code"
        )
        state.reservation?.let { ClientCard(state) }
        if (state.notFound) {
            InfoBanner(stringResource(R.string.business_checkin_not_found), tag = "checkin_not_found")
        }
        state.error?.let { ErrorBanner(it.asString()) }
        InfoBanner(stringResource(R.string.business_checkin_notice), icon = Icons.Outlined.CheckCircle)
    }
    if (state.contactOpen) {
        WhereverBottomSheet(onDismissRequest = actions.onCloseContact, tag = "checkin_contact_sheet") {
            Text(
                text = state.reservation?.client.orEmpty(),
                style = MaterialTheme.typography.headlineSmall
            )
            ContactForm(
                state.contactText,
                state.canSendContact,
                state.sending,
                actions.onContactText,
                actions.onSendContact
            )
        }
    }
}

@Composable
private fun ClientCard(state: CheckinUiState) {
    val reservation = state.reservation ?: return
    SurfaceCard(modifier = Modifier.testTag("checkin_client")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)
        ) {
            InitialsAvatar(reservation.client, Sizes.AvatarM)
            Spacer(Modifier.width(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reservation.client,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.business_checkin_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PaymentPill(state)
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.XS),
            color = MaterialTheme.colorScheme.outline
        )
        DetailRow(stringResource(R.string.business_detail_date), reservation.dateTime.toLocalDate().shortText())
        DetailRow(stringResource(R.string.business_detail_time), reservation.dateTime.hourText())
        DetailRow(
            stringResource(R.string.business_detail_people),
            pluralStringResource(R.plurals.business_people_count, reservation.people, reservation.people)
        )
        if (reservation.deposit > 0) {
            val charged = state.charge
            DetailRow(
                stringResource(
                    if (charged != null) R.string.business_detail_deposit_paid else R.string.business_detail_deposit_due
                ),
                (charged?.deposit ?: reservation.deposit).asMoney(),
                valueColor = Mint
            )
        }
        reservation.note?.let { DetailRow(stringResource(R.string.business_detail_note), it) }
    }
}

@Composable
private fun PaymentPill(state: CheckinUiState) {
    val reservation = state.reservation ?: return
    when {
        state.arrived || reservation.status == ReservationStatus.ARRIVED -> ReservationStatusPill(
            ReservationStatus.ARRIVED
        )
        state.charge != null -> StatusPill(
            stringResource(R.string.business_paid),
            container = MintTint,
            contentColor = Ink,
            modifier = Modifier.testTag("checkin_paid")
        )
        reservation.deposit > 0 -> StatusPill(
            stringResource(R.string.business_payment_pending),
            container = GoldTint,
            contentColor = Ink
        )
        else -> ReservationStatusPill(reservation.status)
    }
}

@Preview(name = "Check-in claro")
@Preview(name = "Check-in oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CheckinPreview() {
    WhereverTheme { CheckinContent(sampleCheckinState(), CheckinActions()) }
}

@Preview(name = "Check-in vacío")
@Composable
internal fun CheckinEmptyPreview() {
    WhereverTheme { CheckinContent(CheckinUiState(), CheckinActions()) }
}
