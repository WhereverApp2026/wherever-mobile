// TooManyFunctions: lista, navegación por día, hoja de acciones, contactar, cerrar horario y vistas previas de 49.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverIcons
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.BlockReason
import com.wherever.mobile.feature.business.domain.CONTACT_MAX
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.ScheduleBlock

/** 49_NegReservas (specs/nv-35). [onCheckin] abre 50_NegCheckin, con el código si viene de una reserva. */
@Composable
fun BusinessReservationsRoute(
    businessId: String,
    onCheckin: (String?) -> Unit,
    viewModel: BusinessReservationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    BusinessReservationsContent(
        state = state,
        actions = ReservationsActions(
            onTab = viewModel::selectTab,
            onNextDay = viewModel::nextDay,
            onPreviousDay = viewModel::previousDay,
            onRetry = viewModel::load,
            onOpen = viewModel::open,
            onDismiss = viewModel::dismiss,
            onConfirm = viewModel::confirm,
            onArrived = viewModel::markArrived,
            onNoShow = viewModel::markNoShow,
            onCheckin = onCheckin,
            onOpenContact = viewModel::openContact,
            onContactText = viewModel::onContactText,
            onSendContact = viewModel::sendContact,
            onOpenBlock = viewModel::openBlock,
            onCloseBlock = viewModel::closeBlock,
            onBlockForm = viewModel::onBlockForm,
            onSaveBlock = viewModel::saveBlock,
            onUnblock = viewModel::unblock,
            onMessageShown = viewModel::consumeMessage
        )
    )
}

@Composable
internal fun BusinessReservationsContent(state: BusinessReservationsUiState, actions: ReservationsActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(ScreenPadding)
            .testTag("business_reservations")
    ) {
        ReservationsHeader(state, actions)
        SegmentedControl(
            options = listOf(
                stringResource(R.string.business_tab_today),
                stringResource(R.string.business_tab_upcoming),
                stringResource(R.string.business_tab_past)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(ReservationsTab.entries[it]) },
            modifier = Modifier.testTag("business_reservations_tabs")
        )
        if (state.tab != ReservationsTab.TODAY) DayNavigator(state, actions)
        state.blocks.filter { it.from.toLocalDate() == state.date }.forEach { BlockRow(it, actions) }
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            state.items.isEmpty() -> EmptyDay(state, actions)
            else -> state.items.forEach { ReservationCard(it, { actions.onOpen(it) }) }
        }
        WhereverSnackbarHost(snackbar)
    }
    state.selected?.let { ReservationSheet(it, state, actions) }
    state.blockForm?.let { BlockSheet(it, state, actions) }
}

@Composable
private fun ReservationsHeader(state: BusinessReservationsUiState, actions: ReservationsActions) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(
                text = stringResource(R.string.business_reservations_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            state.date?.let { date ->
                Text(
                    text = if (state.loading || state.error != null) {
                        date.longText()
                    } else {
                        pluralStringResource(
                            R.plurals.business_reservations_count,
                            state.items.size,
                            date.longText(),
                            state.items.size
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        CircleIconButton(
            icon = Icons.Outlined.Lock,
            description = stringResource(R.string.business_block_open),
            onClick = actions.onOpenBlock,
            modifier = Modifier.testTag("business_block_open")
        )
        CircleIconButton(
            icon = WhereverIcons.QrScan,
            description = stringResource(R.string.business_checkin_title),
            onClick = { actions.onCheckin(null) },
            modifier = Modifier.padding(start = Spacing.S).testTag("business_checkin_open")
        )
    }
}

@Composable
private fun DayNavigator(state: BusinessReservationsUiState, actions: ReservationsActions) {
    val upcoming = state.tab == ReservationsTab.UPCOMING
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        WhereverIconButton(onClick = if (upcoming) actions.onPreviousDay else actions.onNextDay) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.business_day_previous)
            )
        }
        Text(
            text = state.date?.longText().orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).testTag("business_day")
        )
        WhereverIconButton(onClick = if (upcoming) actions.onNextDay else actions.onPreviousDay) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = stringResource(R.string.business_day_next)
            )
        }
    }
}

@Composable
private fun BlockRow(block: ScheduleBlock, actions: ReservationsActions) {
    SurfaceCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.L)
        ) {
            Text(
                text = stringResource(R.string.business_block_row, block.from.hourText(), block.to.hourText()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            WhereverTextLink(
                stringResource(R.string.business_block_reopen),
                { actions.onUnblock(block) },
                Modifier.testTag("business_block_reopen")
            )
        }
    }
}

@Composable
private fun EmptyDay(state: BusinessReservationsUiState, actions: ReservationsActions) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XXL).testTag("business_reservations_empty")
    ) {
        Text(
            text = stringResource(R.string.business_reservations_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (state.tab == ReservationsTab.PAST) {
            WhereverOutlinedButton(stringResource(R.string.business_day_previous), actions.onNextDay)
        } else {
            WhereverOutlinedButton(stringResource(R.string.business_block_open), actions.onOpenBlock)
        }
    }
}

/** Acciones según el estado: Confirmar (pendiente), Llegó / No llegó (confirmada) y Contactar. */
@Composable
private fun ReservationSheet(
    reservation: Reservation,
    state: BusinessReservationsUiState,
    actions: ReservationsActions
) {
    WhereverBottomSheet(onDismissRequest = actions.onDismiss, tag = "business_reservation_sheet") {
        Text(text = reservation.client, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(
                R.string.business_sheet_detail,
                reservation.dateTime.hourText(),
                reservation.detailText()
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ReservationStatusPill(reservation.status)
        if (state.contactOpen) {
            ContactForm(
                state.contactText,
                state.canSendContact,
                state.saving,
                actions.onContactText,
                actions.onSendContact
            )
            return@WhereverBottomSheet
        }
        val today = reservation.dateTime.toLocalDate() == state.today
        when (reservation.status) {
            ReservationStatus.PENDING -> WhereverPrimaryButton(
                text = stringResource(R.string.business_action_confirm),
                onClick = actions.onConfirm,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "business_action_confirm"
            )
            ReservationStatus.CONFIRMED -> {
                WhereverPrimaryButton(
                    text = stringResource(R.string.business_action_arrived),
                    onClick = actions.onArrived,
                    loading = state.saving,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "business_action_arrived"
                )
                WhereverOutlinedButton(
                    text = stringResource(R.string.business_action_no_show),
                    onClick = actions.onNoShow,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "business_action_no_show"
                )
                if (today && reservation.code.isNotBlank()) {
                    WhereverOutlinedButton(
                        text = stringResource(R.string.business_action_checkin),
                        onClick = {
                            actions.onDismiss()
                            actions.onCheckin(reservation.code)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> Unit
        }
        if (reservation.status in CONTACTABLE) {
            WhereverTextLink(
                stringResource(R.string.business_action_contact),
                actions.onOpenContact,
                Modifier.fillMaxWidth().testTag("business_action_contact")
            )
        }
    }
}

/** La API deja contactar confirmadas y las que llegaron o no hace menos de 24 h (si no, 409). */
private val CONTACTABLE = setOf(ReservationStatus.CONFIRMED, ReservationStatus.ARRIVED, ReservationStatus.NO_SHOW)

/** Mensaje al cliente (1 a 500 caracteres). Se usa aquí y en 50_NegCheckin. */
@Composable
internal fun ContactForm(
    text: String,
    canSend: Boolean,
    sending: Boolean,
    onText: (String) -> Unit,
    onSend: () -> Unit
) {
    LabeledTextField(
        label = stringResource(R.string.business_contact_label),
        value = text,
        onValueChange = onText,
        placeholder = stringResource(R.string.business_contact_placeholder),
        supporting = stringResource(R.string.business_counter, text.length, CONTACT_MAX),
        singleLine = false,
        tag = "business_contact_text"
    )
    WhereverPrimaryButton(
        text = stringResource(R.string.business_contact_send),
        onClick = onSend,
        enabled = canSend,
        loading = sending,
        modifier = Modifier.fillMaxWidth(),
        tag = "business_contact_send"
    )
}

@Composable
private fun BlockSheet(form: BlockForm, state: BusinessReservationsUiState, actions: ReservationsActions) {
    WhereverBottomSheet(onDismissRequest = actions.onCloseBlock, tag = "business_block_sheet") {
        Text(text = stringResource(R.string.business_block_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = state.date?.longText().orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            LabeledTextField(
                label = stringResource(R.string.business_block_from),
                value = form.from,
                onValueChange = { actions.onBlockForm(form.copy(from = it.take(TIME_LENGTH))) },
                placeholder = stringResource(R.string.business_time_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                tag = "business_block_from"
            )
            LabeledTextField(
                label = stringResource(R.string.business_block_to),
                value = form.to,
                onValueChange = { actions.onBlockForm(form.copy(to = it.take(TIME_LENGTH))) },
                placeholder = stringResource(R.string.business_time_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                tag = "business_block_to"
            )
        }
        Text(text = stringResource(R.string.business_block_reason), style = MaterialTheme.typography.labelLarge)
        ChipGroup {
            BlockReason.entries.forEach { reason ->
                SelectChip(
                    text = stringResource(reason.label()),
                    selected = form.reason == reason,
                    onClick = { actions.onBlockForm(form.copy(reason = if (form.reason == reason) null else reason)) }
                )
            }
        }
        WhereverPrimaryButton(
            text = stringResource(R.string.business_block_save),
            onClick = actions.onSaveBlock,
            enabled = form.valid,
            loading = state.saving,
            modifier = Modifier.fillMaxWidth(),
            tag = "business_block_save"
        )
    }
}

/** HH:MM. */
private const val TIME_LENGTH = 5

private fun BlockReason.label(): Int = when (this) {
    BlockReason.PRIVATE_EVENT -> R.string.business_block_private
    BlockReason.FULL -> R.string.business_block_full
    BlockReason.OTHER -> R.string.business_block_other
}

@Preview(name = "Reservas claro")
@Preview(name = "Reservas oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessReservationsPreview() {
    WhereverTheme { BusinessReservationsContent(sampleReservationsState(), ReservationsActions()) }
}

@Preview(name = "Reservas vacío")
@Composable
internal fun BusinessReservationsEmptyPreview() {
    WhereverTheme {
        BusinessReservationsContent(sampleReservationsState().copy(items = emptyList()), ReservationsActions())
    }
}
