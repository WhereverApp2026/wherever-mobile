package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.CLABE_LENGTH

/** 89_NegBanco (specs/nv-38). «Depósito de prueba» y «Cuenta principal» están diferidos: no se dibujan (D-38a). */
@Composable
internal fun BankAccountRoute(
    businessId: String,
    lastFour: String?,
    onBack: () -> Unit,
    viewModel: BankAccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId, lastFour) }
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }
    BankAccountContent(
        state,
        BankAccountActions(
            onBack = onBack,
            onHolder = viewModel::onHolder,
            onClabe = viewModel::onClabe,
            onSave = viewModel::save
        )
    )
}

@Composable
internal fun BankAccountContent(state: BankAccountUiState, actions: BankAccountActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.bank_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "bank_save"
            )
        }
    ) {
        Header(R.string.bank_title, R.string.bank_subtitle, actions.onBack)
        state.currentLastFour?.let { InfoBanner(stringResource(R.string.bank_current, it), tag = "bank_current") }
        LabeledTextField(
            label = stringResource(R.string.bank_holder),
            value = state.holder,
            onValueChange = actions.onHolder,
            placeholder = stringResource(R.string.bank_holder_hint),
            leadingIcon = Icons.Outlined.Person,
            tag = "bank_holder"
        )
        LabeledTextField(
            label = stringResource(R.string.bank_clabe),
            value = state.clabe,
            onValueChange = actions.onClabe,
            leadingIcon = Icons.Outlined.Lock,
            supporting = stringResource(R.string.bank_clabe_hint, CLABE_LENGTH),
            error = stringResource(R.string.bank_clabe_invalid).takeIf { state.clabeComplete && !state.clabeValid },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingContent = if (state.clabeValid) {
                {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.bank_clabe_ok),
                        tint = Mint
                    )
                }
            } else {
                null
            },
            tag = "bank_clabe"
        )
        state.error?.let { ErrorBanner(it.asString()) }
        InfoBanner(stringResource(R.string.bank_privacy), tag = "bank_privacy")
    }
}

/** 90_NegFiscal (specs/nv-38). Correo, constancia y factura mensual están diferidos: no se dibujan (D-38a). */
@Composable
internal fun TaxDataRoute(
    businessId: String,
    complete: Boolean?,
    onBack: () -> Unit,
    viewModel: TaxDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId, complete) }
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }
    TaxDataContent(
        state,
        TaxDataActions(
            onBack = onBack,
            onRfc = viewModel::onRfc,
            onLegalName = viewModel::onLegalName,
            onRegime = viewModel::onRegime,
            onCfdiUse = viewModel::onCfdiUse,
            onPostalCode = viewModel::onPostalCode,
            onSave = viewModel::save
        )
    )
}

@Composable
internal fun TaxDataContent(state: TaxDataUiState, actions: TaxDataActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.tax_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "tax_save"
            )
        }
    ) {
        Header(R.string.tax_title, R.string.tax_subtitle, actions.onBack)
        if (state.complete == true) InfoBanner(stringResource(R.string.tax_complete), tag = "tax_complete")
        LabeledTextField(
            label = stringResource(R.string.tax_legal_name),
            value = state.data.legalName,
            onValueChange = actions.onLegalName,
            tag = "tax_legal_name"
        )
        Row {
            LabeledTextField(
                label = stringResource(R.string.tax_rfc),
                value = state.data.rfc,
                onValueChange = actions.onRfc,
                error = stringResource(R.string.tax_rfc_invalid).takeIf { state.rfcError },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f),
                tag = "tax_rfc"
            )
            Spacer(Modifier.width(Spacing.M))
            LabeledTextField(
                label = stringResource(R.string.tax_postal_code),
                value = state.data.postalCode,
                onValueChange = actions.onPostalCode,
                leadingIcon = Icons.Outlined.Place,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                tag = "tax_postal_code"
            )
        }
        Text(stringResource(R.string.tax_regime), style = MaterialTheme.typography.labelLarge)
        ChipGroup {
            TAX_REGIMES.forEach { code ->
                SelectChip(stringResource(regimeLabel(code)), state.data.regime == code, {
                    actions.onRegime(code)
                }, tag = "tax_regime_$code")
            }
        }
        Text(stringResource(R.string.tax_cfdi_use), style = MaterialTheme.typography.labelLarge)
        ChipGroup {
            CFDI_USES.forEach { code ->
                SelectChip(stringResource(cfdiLabel(code)), state.data.cfdiUse == code, {
                    actions.onCfdiUse(code)
                }, tag = "tax_cfdi_$code")
            }
        }
        state.error?.let { ErrorBanner(it.asString()) }
        InfoBanner(stringResource(R.string.tax_privacy), tag = "tax_privacy")
    }
}

@Composable
private fun Header(@StringRes title: Int, @StringRes subtitle: Int, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BackCircleButton(onClick = onBack)
        Spacer(Modifier.width(Spacing.M))
        Column {
            Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun regimeLabel(code: String): Int = when (code) {
    "601" -> R.string.tax_regime_601
    "603" -> R.string.tax_regime_603
    "605" -> R.string.tax_regime_605
    "606" -> R.string.tax_regime_606
    "612" -> R.string.tax_regime_612
    "621" -> R.string.tax_regime_621
    "625" -> R.string.tax_regime_625
    else -> R.string.tax_regime_626
}

private fun cfdiLabel(code: String): Int = when (code) {
    "G01" -> R.string.tax_cfdi_g01
    "S01" -> R.string.tax_cfdi_s01
    else -> R.string.tax_cfdi_g03
}

@Preview(name = "89_NegBanco claro")
@Preview(name = "89_NegBanco oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BankAccountPreview() {
    WhereverTheme { BankAccountContent(sampleBankAccount(), BankAccountActions()) }
}

@Preview(name = "90_NegFiscal claro")
@Preview(name = "90_NegFiscal oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun TaxDataPreview() {
    WhereverTheme { TaxDataContent(sampleTaxData(), TaxDataActions()) }
}
