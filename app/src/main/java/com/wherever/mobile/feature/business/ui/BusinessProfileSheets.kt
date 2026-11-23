package com.wherever.mobile.feature.business.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.Danger
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.feature.business.domain.PAUSE_REASON_MAX

/** Pausar el negocio: hasta 90 días y un motivo opcional (`POST pausa`). */
@Composable
internal fun PauseSheet(state: BusinessProfileUiState, actions: BusinessProfileActions) {
    WhereverBottomSheet(onDismissRequest = actions.onClosePause, tag = "biz_pause_sheet") {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.L)
        ) {
            SheetTitle(R.string.biz_pause_title, stringResource(R.string.biz_pause_body))
            ChipGroup {
                PauseLength.entries.forEach { length ->
                    SelectChip(
                        stringResource(length.label()),
                        state.pauseLength == length,
                        { actions.onPauseLength(length) },
                        tag = "biz_pause_${length.name.lowercase()}"
                    )
                }
            }
            LabeledTextField(
                label = stringResource(R.string.biz_pause_reason),
                value = state.pauseReason,
                onValueChange = actions.onPauseReason,
                placeholder = stringResource(R.string.biz_pause_reason_hint),
                supporting = stringResource(R.string.reply_counter, state.pauseReason.length, PAUSE_REASON_MAX),
                tag = "biz_pause_reason"
            )
            WhereverPrimaryButton(
                text = stringResource(R.string.biz_pause_confirm),
                onClick = actions.onTogglePause,
                loading = state.working,
                modifier = Modifier.fillMaxWidth(),
                tag = "biz_pause_confirm"
            )
        }
    }
}

private fun PauseLength.label(): Int = when (this) {
    PauseLength.WEEK -> R.string.biz_pause_week
    PauseLength.TWO_WEEKS -> R.string.biz_pause_two_weeks
    PauseLength.MONTH -> R.string.biz_pause_month
    PauseLength.OPEN -> R.string.biz_pause_open
}

/** Eliminar mi cuenta de negocio: escribir el nombre exacto (`DELETE /negocios/{id}`). */
@Composable
internal fun DeleteBusinessSheet(state: BusinessProfileUiState, name: String, actions: BusinessProfileActions) {
    WhereverBottomSheet(onDismissRequest = actions.onCloseDelete, tag = "biz_delete_sheet") {
        Column(modifier = Modifier.padding(horizontal = Spacing.XL, vertical = Spacing.L)) {
            DeleteBusinessForm(state, name, actions)
        }
    }
}

@Composable
internal fun DeleteBusinessForm(state: BusinessProfileUiState, name: String, actions: BusinessProfileActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
        SheetTitle(R.string.biz_delete_title, stringResource(R.string.biz_delete_body, name))
        LabeledTextField(
            label = stringResource(R.string.biz_delete_label),
            value = state.deleteText,
            onValueChange = actions.onDeleteText,
            placeholder = name,
            tag = "biz_delete_name"
        )
        state.deleteError?.let { ErrorBanner(it.asString()) }
        WhereverOutlinedButton(
            text = stringResource(R.string.biz_delete_confirm),
            onClick = actions.onDelete,
            enabled = state.canDelete,
            contentColor = Danger,
            borderColor = Danger,
            modifier = Modifier.fillMaxWidth(),
            tag = "biz_delete_confirm"
        )
    }
}
