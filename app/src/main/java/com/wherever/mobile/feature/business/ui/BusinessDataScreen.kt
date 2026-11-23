package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.DESCRIPTION_MAX
import com.wherever.mobile.feature.business.domain.isValidPhone
import com.wherever.mobile.feature.business.domain.isValidRange

/** «Datos y horario» de 58_NegPerfil (specs/nv-38): descripción, teléfono y horario. */
@Composable
internal fun BusinessDataRoute(
    businessId: String,
    onBack: () -> Unit,
    viewModel: BusinessDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }
    BusinessDataContent(
        state,
        BusinessDataActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onDescription = viewModel::onDescription,
            onPhone = viewModel::onPhone,
            onSchedule = viewModel::onSchedule,
            onSave = viewModel::save
        )
    )
}

@Composable
internal fun BusinessDataContent(state: BusinessDataUiState, actions: BusinessDataActions) {
    val error = state.error
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.biz_data_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "biz_data_save"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.biz_profile_data), style = MaterialTheme.typography.headlineMedium)
        }
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            else -> {
                InfoBanner(stringResource(R.string.biz_data_pending_hint), tag = "biz_data_hint")
                LabeledTextField(
                    label = stringResource(R.string.biz_data_description),
                    value = state.description,
                    onValueChange = actions.onDescription,
                    supporting = stringResource(R.string.reply_counter, state.description.length, DESCRIPTION_MAX),
                    singleLine = false,
                    tag = "biz_data_description"
                )
                LabeledTextField(
                    label = stringResource(R.string.biz_data_phone),
                    value = state.phone,
                    onValueChange = actions.onPhone,
                    placeholder = stringResource(R.string.biz_data_phone_hint),
                    error = stringResource(R.string.biz_data_phone_invalid).takeIf { !isValidPhone(state.phone) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    tag = "biz_data_phone"
                )
                Text(stringResource(R.string.business_schedule_label), style = MaterialTheme.typography.labelLarge)
                SurfaceCard { state.schedules.forEach { ScheduleRow(it, actions.onSchedule) } }
                if (state.schedules.any { !it.closed && !it.isValidRange() }) {
                    Text(
                        stringResource(R.string.business_schedule_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.saveError?.let { ErrorBanner(it.asString()) }
            }
        }
    }
}

@Preview(name = "NegDatos claro")
@Preview(name = "NegDatos oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessDataPreview() {
    WhereverTheme { BusinessDataContent(sampleBusinessData(), BusinessDataActions()) }
}
