package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.CLABE_LENGTH
import com.wherever.mobile.feature.business.domain.NAME_MAX
import com.wherever.mobile.feature.business.domain.isValidClabe
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BankAccountUiState(
    val holder: String = "",
    val clabe: String = "",
    /** Cuenta guardada: solo los últimos 4 que manda la API. */
    val currentLastFour: String? = null,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val clabeComplete: Boolean get() = clabe.length == CLABE_LENGTH

    val clabeValid: Boolean get() = isValidClabe(clabe)

    val canSave: Boolean get() = !saving && holder.isNotBlank() && clabeValid
}

/** 89_NegBanco (specs/nv-38): titular y CLABE de 18 dígitos con dígito verificador 3-7-1. */
@HiltViewModel
class BankAccountViewModel @Inject constructor(private val repository: BusinessProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(BankAccountUiState())
    val state: StateFlow<BankAccountUiState> = mutableState.asStateFlow()

    private val savedChannel = Channel<String>(Channel.BUFFERED)

    /** Últimos 4 dígitos de la cuenta guardada. */
    val saved = savedChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String, lastFour: String?) {
        if (businessId == id) return
        businessId = id
        mutableState.update { it.copy(currentLastFour = lastFour) }
    }

    fun onHolder(text: String) = mutableState.update { it.copy(holder = text.take(NAME_MAX), error = null) }

    fun onClabe(text: String) =
        mutableState.update { it.copy(clabe = text.filter { c -> c.isDigit() }.take(CLABE_LENGTH), error = null) }

    /** 422 `clabe_invalida`: el motivo de la API, sin borrar lo escrito. La CLABE no se guarda en el teléfono. */
    fun save() {
        val id = businessId ?: return
        val current = state.value
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.saveDepositAccount(id, current.clabe, current.holder)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false, clabe = "", currentLastFour = result.data.lastFour) }
                    savedChannel.send(result.data.lastFour)
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }
}
