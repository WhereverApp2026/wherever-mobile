package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.LEGAL_NAME_MAX
import com.wherever.mobile.feature.business.domain.POSTAL_CODE_LENGTH
import com.wherever.mobile.feature.business.domain.RFC_PERSON_LENGTH
import com.wherever.mobile.feature.business.domain.TaxData
import com.wherever.mobile.feature.business.domain.isValidRfc
import com.wherever.mobile.feature.business.domain.isValidTaxData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Régimen fiscal del catálogo del SAT que usan los negocios de comida y entretenimiento (D-38f). */
val TAX_REGIMES = listOf("601", "603", "605", "606", "612", "621", "625", "626")

/** Uso del CFDI de las comisiones: gastos en general por omisión (D-38f). */
val CFDI_USES = listOf("G03", "G01", "S01")

data class TaxDataUiState(
    val data: TaxData =
        TaxData(rfc = "", legalName = "", regime = TAX_REGIMES.first(), cfdiUse = CFDI_USES.first(), postalCode = ""),
    val complete: Boolean? = null,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val rfcError: Boolean get() = data.rfc.length >= RFC_PERSON_LENGTH - 1 && !isValidRfc(data.rfc)

    val canSave: Boolean get() = !saving && isValidTaxData(data)
}

/** 90_NegFiscal (specs/nv-38): RFC, razón social, régimen, uso CFDI y C.P. con `PUT datos-fiscales`. */
@HiltViewModel
class TaxDataViewModel @Inject constructor(private val repository: BusinessProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(TaxDataUiState())
    val state: StateFlow<TaxDataUiState> = mutableState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val saved = savedChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String, complete: Boolean?) {
        if (businessId == id) return
        businessId = id
        mutableState.update { it.copy(complete = complete) }
    }

    fun onRfc(text: String) = edit {
        it.copy(rfc = text.uppercase().filter { c -> c.isLetterOrDigit() || c == '&' }.take(RFC_PERSON_LENGTH))
    }

    fun onLegalName(text: String) = edit { it.copy(legalName = text.take(LEGAL_NAME_MAX)) }

    fun onRegime(code: String) = edit { it.copy(regime = code) }

    fun onCfdiUse(code: String) = edit { it.copy(cfdiUse = code) }

    fun onPostalCode(text: String) = edit {
        it.copy(postalCode = text.filter { c -> c.isDigit() }.take(POSTAL_CODE_LENGTH))
    }

    /** 422 `rfc_invalido` se explica; nada de lo escrito se guarda en el teléfono. */
    fun save() {
        val id = businessId ?: return
        val current = state.value
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.saveTaxData(id, current.data)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false, complete = true) }
                    savedChannel.send(Unit)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(saving = false, error = result.error.taxMessage())
                }
            }
        }
    }

    private fun edit(change: (TaxData) -> TaxData) = mutableState.update {
        it.copy(data = change(it.data), error = null)
    }
}

private fun AppError.taxMessage(): UiText {
    val invalidRfc = this is AppError.Validation && fields.any { it.field == "rfc" }
    return if (invalidRfc) UiText.Res(R.string.tax_rfc_invalid) else toUiText()
}
